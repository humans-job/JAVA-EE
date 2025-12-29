package org.example.army.militarymanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.army.militarymanage.dto.militia.*;
import org.example.army.militaryauthenticate.util.Auth0JwtUtil;
import org.example.army.militaryauthenticate.util.MD5Util;
import org.example.army.militaryauthenticate.util.PkiServiceUtil;
import org.example.army.militaryauthenticate.util.SecurityUtil;
import org.example.army.militarymanage.util.FormatValidateUtil;
import org.example.army.militarymanage.service.MilitiaArchiveService;
import org.example.army.militarymanage.vo.militia.MilitiaImportFailItem;
import org.example.army.militarymanage.vo.militia.MilitiaImportResp;
import org.example.army.militarymanage.vo.militia.MilitiaListResp;
import lombok.RequiredArgsConstructor;
import org.example.army.militarycommon.Entity.Dept;
import org.example.army.militarycommon.Entity.User;
import org.example.army.militarycommon.Entity.deptRelation;
import org.example.army.militarycommon.Entity.militiaInfo;
import org.example.army.militarycommon.mapper.DeptMapper;
import org.example.army.militarycommon.mapper.DeptRelationMapper;
import org.example.army.militarycommon.mapper.MilitiaInfoMapper;
import org.example.army.militarycommon.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilitiaArchiveServiceImpl implements MilitiaArchiveService {

    private final MilitiaInfoMapper militiaInfoMapper;
    private final DeptMapper deptMapper;
    private final DeptRelationMapper deptRelationMapper;
    private final UserMapper userMapper;
    private final PkiServiceUtil pkiServiceUtil;
    private final SecurityUtil securityUtil;
    private final Auth0JwtUtil auth0JwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MilitiaImportResp batchImport(MilitiaBatchImportReq req) {
        Long currentDeptId = securityUtil.getDeptId();

        // 团账号：只能往自己团里导入
        Dept currentDept = deptMapper.selectById(currentDeptId);
        if (currentDept != null && Integer.valueOf(3).equals(currentDept.getDeptType())) {
            if (!Objects.equals(currentDeptId, securityUtil.getDeptId())) {
                throw new IllegalArgumentException("无权限：只能导入本团数据");
            }
        }

        List<MilitiaImportItem> rows = req.getData();
        int total = rows == null ? 0 : rows.size();
        List<MilitiaImportFailItem> failList = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            MilitiaImportResp resp = new MilitiaImportResp();
            resp.setTotal(0);
            resp.setSuccess(0);
            resp.setFail(0);
            resp.setFailList(List.of());
            return resp;
        }

        // 1) 基础格式校验
        List<MilitiaImportItem> valid = new ArrayList<>();
        for (MilitiaImportItem r : rows) {
            String idCard = r.getIdCard();
            String phone = r.getPhone();

            if (!FormatValidateUtil.isValidIdCard(idCard)) {
                failList.add(new MilitiaImportFailItem(idCard, "身份证格式错误"));
                continue;
            }
            if (!FormatValidateUtil.isValidPhone(phone)) {
                failList.add(new MilitiaImportFailItem(idCard, "手机号格式错误"));
                continue;
            }
            valid.add(r);
        }

        // 2) 去重（按本团 create_dept + id_card）
        Set<String> idCards = valid.stream()
                .map(MilitiaImportItem::getIdCard)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        Set<String> existed = new HashSet<>();
        if (!idCards.isEmpty()) {
            List<militiaInfo> existRows = militiaInfoMapper.selectList(new LambdaQueryWrapper<militiaInfo>()
                    .eq(militiaInfo::getCreateDept, securityUtil.getDeptId())
                    .in(militiaInfo::getIdCard, idCards));
            existed = existRows.stream().map(militiaInfo::getIdCard).collect(Collectors.toSet());
        }

        int success = 0;
        for (MilitiaImportItem r : valid) {
            String idCard = r.getIdCard().trim();
            if (existed.contains(idCard)) {
                failList.add(new MilitiaImportFailItem(idCard, "身份证已存在"));
                continue;
            }

            militiaInfo mi = new militiaInfo();
            mi.setDeptId(securityUtil.getDeptId());
            mi.setCreateDept(securityUtil.getDeptId());
            mi.setName(r.getName() == null ? null : r.getName().trim());
            mi.setIdCard(idCard);
            mi.setPhone(r.getPhone() == null ? null : r.getPhone().trim());
            mi.setAddress(r.getAddress());
            mi.setPoliticStatus(r.getPoliticStatus());
            mi.setJoinTime(r.getJoinTime());
            mi.setAuditStatus(0);
            mi.setCreateTime(LocalDateTime.now());

            militiaInfoMapper.insert(mi);
            success++;
        }

        MilitiaImportResp resp = new MilitiaImportResp();
        resp.setTotal(total);
        resp.setSuccess(success);
        resp.setFail(total - success);
        resp.setFailList(failList);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitToDivision(MilitiaSubmitAuditReq req) {
        Long currentDeptId = securityUtil.getDeptId();
        Dept currentDept = deptMapper.selectById(currentDeptId);
        if (currentDept != null && !Integer.valueOf(3).equals(currentDept.getDeptType())) {
            // 只允许团提交
            throw new IllegalArgumentException("仅团机关可提交师部审核");
        }

        Long divisionDeptId = findDivisionDeptId(currentDeptId);
        if (divisionDeptId == null) {
            throw new IllegalArgumentException("未找到上级师部，请检查 sys_dept / sys_dept_belong 配置");
        }

        for (Long id : req.getIds()) {
            militiaInfo mi = militiaInfoMapper.selectById(id);
            if (mi == null) {
                throw new IllegalArgumentException("记录不存在：" + id);
            }
            if (!Objects.equals(mi.getDeptId(), currentDeptId)) {
                throw new IllegalArgumentException("无权限：只能提交本团数据");
            }

            Integer st = mi.getAuditStatus();
            if (st != null && st == 2) {
                throw new IllegalArgumentException("已归档不可重复提交：" + id);
            }
            // 草稿(0) / 驳回(3) 可提交
            mi.setAuditStatus(1);
            mi.setAuditDept(divisionDeptId);
            mi.setAuditFeedback(null);
            militiaInfoMapper.updateById(mi);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void divisionAudit(MilitiaAuditReq req) {
        Long currentDeptId = securityUtil.getDeptId();
        Dept currentDept = deptMapper.selectById(currentDeptId);
        if (currentDept != null && !Integer.valueOf(2).equals(currentDept.getDeptType())) {
            throw new IllegalArgumentException("仅师机关可审核");
        }

        militiaInfo mi = militiaInfoMapper.selectById(req.getId());
        if (mi == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        if (!Objects.equals(mi.getAuditDept(), currentDeptId)) {
            throw new IllegalArgumentException("无权限：只能审核分配到本师的数据");
        }
        if (mi.getAuditStatus() == null || mi.getAuditStatus() != 1) {
            throw new IllegalArgumentException("当前状态不可审核（需待师部审核）");
        }

        if (req.getAuditStatus() == 2) {
            User user = new User();
            user.setDeptId(mi.getDeptId());
            user.setUsername(mi.getIdCard());
            user.setUserType(1);
            user.setPassword(MD5Util.md5WithSalt("12345678"));
            userMapper.insert(user);
            pkiServiceUtil.issueCertForUser(user.getUserId());
            Long userId = user.getUserId();
            mi.setUserId(userId);
            mi.setAuditStatus(2);
            mi.setAuditFeedback(null);
        } else if (req.getAuditStatus() == 3) {
            if (req.getAuditFeedback() == null || req.getAuditFeedback().isBlank()) {
                throw new IllegalArgumentException("驳回必须填写 auditFeedback");
            }
            mi.setAuditStatus(3);
            mi.setAuditFeedback(req.getAuditFeedback());
        } else {
            throw new IllegalArgumentException("auditStatus 仅支持 2=通过, 3=驳回");
        }

        militiaInfoMapper.updateById(mi);
    }

    @Override
    public MilitiaListResp list(Long deptId, Integer auditStatus, String idCardLike, long pageNum, long pageSize) {
        Long currentDeptId = securityUtil.getDeptId();
        Dept currentDept = deptMapper.selectById(currentDeptId);
        Integer currentType = currentDept == null ? null : currentDept.getDeptType();

        LambdaQueryWrapper<militiaInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(auditStatus != null, militiaInfo::getAuditStatus, auditStatus);
        if (idCardLike != null && !idCardLike.isBlank()) {
            qw.like(militiaInfo::getIdCard, idCardLike.trim());
        }

        // 权限过滤
        if (Integer.valueOf(3).equals(currentType)) {
            // 团：只能看本团
            qw.eq(militiaInfo::getDeptId, currentDeptId);
        } else if (Integer.valueOf(2).equals(currentType)) {
            // 师：可看所有下属团
            List<Long> childDeptIds = deptRelationMapper.selectList(new LambdaQueryWrapper<deptRelation>()
                            .eq(deptRelation::getParentId, currentDeptId))
                    .stream().map(deptRelation::getChildId).toList();

            if (deptId != null) {
                if (!childDeptIds.contains(deptId)) {
                    // 返回空
                    MilitiaListResp r = new MilitiaListResp();
                    r.setList(List.of());
                    r.setTotal(0);
                    return r;
                }
                qw.eq(militiaInfo::getDeptId, deptId);
            } else {
                if (childDeptIds.isEmpty()) {
                    qw.eq(militiaInfo::getDeptId, -1L);
                } else {
                    qw.in(militiaInfo::getDeptId, childDeptIds);
                }
            }
        } else {
            // 其它角色：默认按传参过滤
            if (deptId != null) {
                qw.eq(militiaInfo::getCreateDept, deptId);
            }
        }

        Page<militiaInfo> page = new Page<>(pageNum, pageSize);
        IPage<militiaInfo> p = militiaInfoMapper.selectPage(page, qw.orderByDesc(militiaInfo::getCreateTime));

        MilitiaListResp resp = new MilitiaListResp();
        resp.setList(p.getRecords());
        resp.setTotal(p.getTotal());
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long update(MilitiaUpdateReq update) {
        if (update == null || update.getId() == null) {
            throw new IllegalArgumentException("id不能为空");
        }

        Long currentDeptId = securityUtil.getDeptId();
        Dept currentDept = deptMapper.selectById(currentDeptId);
        Integer currentType = currentDept == null ? null : currentDept.getDeptType();

        militiaInfo mi = militiaInfoMapper.selectById(update.getId());
        if (mi == null) {
            throw new IllegalArgumentException("记录不存在");
        }

        assertCanOperate(currentType, currentDeptId, mi);

        // 字段校验
        if (update.getIdCard() != null && !update.getIdCard().isBlank()) {
            if (!FormatValidateUtil.isValidIdCard(update.getIdCard())) {
                throw new IllegalArgumentException("身份证格式错误");
            }
            mi.setIdCard(update.getIdCard().trim());
        }
        if (update.getPhone() != null && !update.getPhone().isBlank()) {
            if (!FormatValidateUtil.isValidPhone(update.getPhone())) {
                throw new IllegalArgumentException("手机号格式错误");
            }
            mi.setPhone(update.getPhone().trim());
        }
        if (update.getName() != null) {
            mi.setName(update.getName());
        }
        if (update.getAddress() != null) {
            mi.setAddress(update.getAddress());
        }
        if (update.getPoliticStatus() != null) {
            mi.setPoliticStatus(update.getPoliticStatus());
        }
        if (update.getJoinTime() != null) {
            mi.setJoinTime(update.getJoinTime());
        }

        // 驳回后编辑：回到草稿，允许重新提交
        if (mi.getAuditStatus() != null && mi.getAuditStatus() == 3) {
            mi.setAuditStatus(0);
            mi.setAuditFeedback(null);
            mi.setAuditDept(null);
        }

        militiaInfoMapper.updateById(mi);
        return mi.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id不能为空");
        }

        Long currentDeptId = securityUtil.getDeptId();
        Dept currentDept = deptMapper.selectById(currentDeptId);
        Integer currentType = currentDept == null ? null : currentDept.getDeptType();

        militiaInfo mi = militiaInfoMapper.selectById(id);
        if (mi == null) return;

        assertCanOperate(currentType, currentDeptId, mi);

        // 团默认不允许删除已归档（防止误删）；师可删
        if (Integer.valueOf(3).equals(currentType) && mi.getAuditStatus() != null && mi.getAuditStatus() == 2) {
            throw new IllegalArgumentException("已归档档案不允许删除");
        }

        militiaInfoMapper.deleteById(id);
    }

    private void assertCanOperate(Integer currentType, Long currentDeptId, militiaInfo target) {
        if (Integer.valueOf(3).equals(currentType)) {
            if (!Objects.equals(target.getDeptId(), currentDeptId)) {
                throw new IllegalArgumentException("无权限：只能操作本团数据");
            }
            return;
        }
        if (Integer.valueOf(2).equals(currentType)) {
            List<Long> childDeptIds = deptRelationMapper.selectList(new LambdaQueryWrapper<deptRelation>()
                            .eq(deptRelation::getParentId, currentDeptId))
                    .stream().map(deptRelation::getChildId).toList();
            if (!childDeptIds.contains(target.getDeptId())) {
                throw new IllegalArgumentException("无权限：只能操作下属团数据");
            }
        }
    }

    /**
     * 从当前团沿 parentId 向上找 dept_type=2 的师部。
     */
    private Long findDivisionDeptId(Long regimentDeptId) {
        Long cursor = regimentDeptId;
        for (int i = 0; i < 10 && cursor != null && cursor > 0; i++) {
            Dept d = deptMapper.selectById(cursor);
            if (d == null) return null;
            if (Integer.valueOf(2).equals(d.getDeptType())) {
                return d.getDeptId();
            }

            // 优先使用 sys_dept.parent_id
            if (d.getParentId() != null && d.getParentId() > 0) {
                cursor = d.getParentId();
                continue;
            }

            // 兜底：使用 sys_dept_belong
            deptRelation rel = deptRelationMapper.selectOne(new LambdaQueryWrapper<deptRelation>()
                    .eq(deptRelation::getChildId, cursor));
            if (rel == null) return null;
            cursor = rel.getParentId();
        }
        return null;
    }
}
