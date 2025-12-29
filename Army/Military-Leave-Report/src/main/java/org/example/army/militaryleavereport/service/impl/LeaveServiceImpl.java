package org.example.army.militaryleavereport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.army.militarycommon.Entity.deptRelation;
import org.example.army.militarycommon.Entity.leaveReport;
import org.example.army.militarycommon.Entity.militiaInfo;
import org.example.army.militaryleavereport.DTO.*;
import org.example.army.militaryauthenticate.util.SecurityUtil;
import org.example.army.militarycommon.mapper.DeptRelationMapper;
import org.example.army.militarycommon.mapper.LeaveReportMapper;
import org.example.army.militarycommon.mapper.MilitiaInfoMapper;
import org.example.army.militaryleavereport.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
public class LeaveServiceImpl extends ServiceImpl<LeaveReportMapper, leaveReport> implements LeaveService {

    @Autowired
    private MilitiaInfoMapper militiaInfoMapper;

    @Autowired
    private DeptRelationMapper deptRelationMapper;

    @Autowired
    private SecurityUtil securityUtil;
    /**
     * 1. 提交请假申请
     * 核心逻辑：自动查找用户的部门，并查找该部门的上级作为审批单位
     */
    @Override
    public void submitLeave(LeaveSubmitDTO dto) {
        Long currentUserId = securityUtil.getUserId();
        leaveReport leave = new leaveReport();
        leave.setUserId(currentUserId);
        leave.setLeaveReason(dto.getReason());
        leave.setStartTime(dto.getStartTime());
        leave.setEndTime(dto.getEndTime());
        leave.setApplyTime(LocalDateTime.now());
        leave.setStatus(0); // 初始状态：待审批

        // 2. 查找民兵所属部门 (militiaInfo 表)
        LambdaQueryWrapper<militiaInfo> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(militiaInfo::getUserId, currentUserId);
        militiaInfo userProfile = militiaInfoMapper.selectOne(userWrapper);

        if (userProfile == null) {
            throw new RuntimeException("提交失败：未找到该账号对应的民兵档案，无法确定所属部门。");
        }
        Long currentDeptId = userProfile.getDeptId();
        leave.setDeptId(currentDeptId);

        // 3. 自动确定审批单位 (查找上级)
        // 逻辑：在 sys_dept_belong 表中，child_id 是当前部门，parent_id 就是上级
        LambdaQueryWrapper<deptRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(deptRelation::getChildId, currentDeptId);
        deptRelation relation = deptRelationMapper.selectOne(relationWrapper);

        if (relation != null) {
            leave.setApproveDept(relation.getParentId());
        } else {
            // 如果没查到上级（可能是最高级单位），根据业务可以设为 0 或抛异常
            // 这里暂设为 0，表示无需上级审批或由超管审批
            leave.setApproveDept(0L);
        }

        leave.setApproveOpinion(null);

        // 销假相关 (时间、位置、确认部门) - 保持与实体类拼写一致 (Bcak)
        leave.setReportBackTime(null);
        leave.setReportBackLocation(null);
        leave.setReportBackConfirmDept(null);

        this.save(leave);
    }

    /**
     * 【新增】修改并重新提交
     * 逻辑：只能修改自己的、且状态为“已驳回(2)”的假条
     */
    @Override
    public void resubmitLeave(LeaveResubmitDTO dto) {
        leaveReport leave = this.getById(dto.getLeaveId());
        Long currentUserId = securityUtil.getUserId();

        if (leave == null) {
            throw new RuntimeException("假条不存在");
        }
        if (!leave.getUserId().equals(currentUserId)) {
            throw new RuntimeException("无法修改他人的假条");
        }
        if (leave.getStatus() != 2) {
            throw new RuntimeException("只能重新提交被驳回的申请");
        }

        // 更新信息
        leave.setLeaveReason(dto.getReason());
        leave.setStartTime(dto.getStartTime());
        leave.setEndTime(dto.getEndTime());
        leave.setApplyTime(LocalDateTime.now()); // 更新申请时间

        // 状态重置为 0 (待审批)
        leave.setStatus(0);
        // 清空之前的审批意见，以免混淆
        leave.setApproveOpinion(null);

        this.updateById(leave);
    }

    /**
     * 2. 审批请假
     */
    @Override
    public void approveLeave(LeaveApproveDTO dto) {
        leaveReport leave = this.getById(dto.getLeaveId());
        Long currentDeptId = securityUtil.getDeptId();

        if (currentDeptId == null) {
            throw new RuntimeException("审批失败：无法获取审批人部门信息");
        }
        if (leave == null) {
            throw new RuntimeException("审批失败：请假申请不存在");
        }

        if (!currentDeptId.equals(leave.getApproveDept())) {
            throw new RuntimeException("您无权审批该申请");
        }
        // 更新状态：1=通过(待销假)，2=驳回
        leave.setStatus(dto.getStatus());
        leave.setApproveOpinion(dto.getApproveOpinion());

        this.updateById(leave);
    }

    /**
     * 3. 民兵销假 (回队打卡)
     */
    @Override
    public void reportBack(LeaveReportBackDTO dto) {
        leaveReport leave = this.getById(dto.getLeaveId());
        Long currentUserId = securityUtil.getUserId();
        // 只有状态为 1 (审批通过/待销假) 的才能销假
        if (leave == null || leave.getStatus() != 1) {
            throw new RuntimeException("操作失败：该申请未通过审批或状态不正确");
        }

        if (!leave.getUserId().equals(currentUserId)) {
            throw new RuntimeException("操作失败：只能销假自己的申请");
        }

        // 记录销假时间和位置
        leave.setReportBackTime(LocalDateTime.now());
        leave.setReportBackLocation(dto.getReportBackLocation());

        // 状态逻辑：
        // 方案A：销假后直接变归档 (status=3)
        // 方案B：销假后状态不变，等待管理员“确认销假”后才变归档 (status=3)
        // 根据你的功能流程第4点：“管理员确认销假，更新 status=3”，所以这里不改 status

        this.updateById(leave);
    }

    /**
     * 4. 确认销假 (管理员归档)
     */
    @Override
    public void confirmReportBack(LeaveConfirmDTO dto) {
        leaveReport leave = this.getById(dto.getLeaveId());
        Long currentDeptId = securityUtil.getDeptId();
        if (leave == null) {
            throw new RuntimeException("申请不存在");
        }

        // 最终归档
        leave.setStatus(3);

        // 记录确认人
        // 实体类 reportBcakConfirmDept 是 String，DTO confirmBy 是 Long，做转换
        leave.setReportBackConfirmDept(currentDeptId);

        this.updateById(leave);
    }

    /**
     * 5. 查询台账 (含下级穿透查询 & 月份筛选)
     */
    /**
     * 【重写】查询逻辑
     * 区分“查自己”和“查部门”
     */
    @Override
    public Map<String, Object> getLeaveStats(LeaveQueryDTO dto) {
        Page<leaveReport> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<leaveReport> wrapper = new LambdaQueryWrapper<>();
        Long currentUserId = securityUtil.getUserId();
        Long currentDeptId = securityUtil.getDeptId();

        // --- 核心改动开始 ---

        // 模式1: 查自己 (queryType = 2)
        if (dto.getQueryType() != null && dto.getQueryType() == 2) {
            wrapper.eq(leaveReport::getUserId, currentUserId);

            // 需求：除已归档之外的。如果 status 没传，默认排除 3。
            if (dto.getStatus() == null) {
                wrapper.ne(leaveReport::getStatus, 3);
            } else {
                // 如果前端明确传了 status (比如想看历史归档)，则按传的查
                wrapper.eq(leaveReport::getStatus, dto.getStatus());
            }
        }
        // 模式2: 查下属 (queryType = 1 或 null) - 管理员用
        else {
            if (currentDeptId == null) throw new RuntimeException("无法获取部门信息");

            // 部门穿透查询 SQL
            String subDeptSql = "SELECT child_id FROM sys_dept_belong WHERE parent_id = " + currentDeptId;
            String userIdsSql = "SELECT user_id FROM biz_militia_info WHERE dept_id = " + currentDeptId +
                    " OR dept_id IN (" + subDeptSql + ")";
            wrapper.inSql(leaveReport::getUserId, userIdsSql);

            // 状态筛选
            if (dto.getStatus() != null) {
                wrapper.eq(leaveReport::getStatus, dto.getStatus());
            }

            // 特殊过滤：只看“待销假确认”的
            // 逻辑：状态是1 (已通过) 且 销假时间不为空 (已打卡)
            if (Boolean.TRUE.equals(dto.getOnlyWaitConfirm())) {
                wrapper.eq(leaveReport::getStatus, 1);
                wrapper.isNotNull(leaveReport::getReportBackTime);
            }
        }

        // 3. 月份筛选
        if (StringUtils.hasText(dto.getMonth())) {
            try {
                YearMonth ym = YearMonth.parse(dto.getMonth());
                LocalDateTime startOfMonth = ym.atDay(1).atStartOfDay();
                LocalDateTime endOfMonth = ym.atEndOfMonth().atTime(23, 59, 59);
                wrapper.between(leaveReport::getApplyTime, startOfMonth, endOfMonth);
            } catch (Exception e) {
                // 忽略格式错误
            }
        }

        // 4. 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(leaveReport::getStatus, dto.getStatus());
        }

        // 按时间倒序
        wrapper.orderByDesc(leaveReport::getApplyTime);

        // 5. 执行查询
        Page<leaveReport> resultPage = this.page(page, wrapper);

        // 6. 构造结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());

        Map<String, Object> stats = new HashMap<>();
        stats.put("currentCount", resultPage.getRecords().size());
        result.put("stat", stats);

        return result;
    }

}

