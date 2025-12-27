package org.example.army.militaryleavereport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.army.militarycommon.Entity.deptRelation;
import org.example.army.militarycommon.Entity.leaveReport;
import org.example.army.militarycommon.Entity.militiaInfo;
import org.example.army.militaryleavereport.DTO.*;
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

    /**
     * 1. 提交请假申请
     * 核心逻辑：自动查找用户的部门，并查找该部门的上级作为审批单位
     */
    @Override
    public void submitLeave(LeaveSubmitDTO dto) {
        // 1. 校验与基础数据填充
        leaveReport leave = new leaveReport();
        leave.setUserId(dto.getUserId());
        leave.setLeaveReason(dto.getReason());
        leave.setStartTime(dto.getStartTime());
        leave.setEndTime(dto.getEndTime());
        leave.setApplyTime(LocalDateTime.now());
        leave.setStatus(0); // 初始状态：待审批

        // 2. 查找民兵所属部门 (militiaInfo 表)
        LambdaQueryWrapper<militiaInfo> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(militiaInfo::getUserId, dto.getUserId());
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
        leave.setReportBcakTime(null);
        leave.setReportBcakLocation(null);
        leave.setReportBcakConfirmDept(null);

        this.save(leave);
    }

    /**
     * 2. 审批请假
     */
    @Override
    public void approveLeave(LeaveApproveDTO dto) {
        leaveReport leave = this.getById(dto.getLeaveId());
        if (leave == null) {
            throw new RuntimeException("审批失败：请假申请不存在");
        }

        // 更新状态：1=通过(待销假)，2=驳回
        leave.setStatus(dto.getStatus());
        leave.setApproveOpinion(dto.getApproveOpinion());
        // 记录审批人 (对应 DTO 中的 approveBy)
        // 注意：实体类中 approveOpinion 类型是 Long，这通常存的是 ID 而不是文本意见
        // 如果需要存文本，请修改实体类 leaveReport 的 approveOpinion 为 String 类型
        // 这里暂时假设 DTO 传来的 opinion 是无效的，或者需要存入其他字段
        // leave.setApproveOpinion( ... );

        this.updateById(leave);
    }

    /**
     * 3. 民兵销假 (回队打卡)
     */
    @Override
    public void reportBack(LeaveReportBackDTO dto) {
        leaveReport leave = this.getById(dto.getLeaveId());

        // 只有状态为 1 (审批通过/待销假) 的才能销假
        if (leave == null || leave.getStatus() != 1) {
            throw new RuntimeException("操作失败：该申请未通过审批或状态不正确");
        }

        // 记录销假时间和位置
        // 注意：使用实体类中原有的拼写 (Bcak -> Back)
        leave.setReportBcakTime(LocalDateTime.now());
        leave.setReportBcakLocation(dto.getReportBackLocation());

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
        if (leave == null) {
            throw new RuntimeException("申请不存在");
        }

        // 最终归档
        leave.setStatus(3);

        // 记录确认人
        // 实体类 reportBcakConfirmDept 是 String，DTO confirmBy 是 Long，做转换
        if (dto.getConfirmBy() != null) {
            leave.setReportBcakConfirmDept(dto.getConfirmBy());
        }

        this.updateById(leave);
    }

    /**
     * 5. 查询台账 (含下级穿透查询 & 月份筛选)
     */
    @Override
    public Map<String, Object> getLeaveStats(LeaveQueryDTO dto) {
        // 1. 构建分页
        Page<leaveReport> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<leaveReport> wrapper = new LambdaQueryWrapper<>();

        // 2. 部门穿透查询 (核心修改点)
        if (dto.getDeptId() != null) {
            // 步骤 A: 构造查询符合条件的部门ID的 SQL
            // 逻辑: 选出 parent_id = dto.getDeptId() 的所有 child_id，再加上 dto.getDeptId() 自己
            // 注意：这里为了简化 SQL 拼接，我们直接查找属于这些部门的 user_id

            // 子查询 1: 找出该部门及其直属下级的所有部门 ID
            // SQL 意图: SELECT id FROM sys_dept WHERE id = {deptId} OR id IN (SELECT child_id FROM sys_dept_belong WHERE parent_id = {deptId})
            // 但为了性能和写法简便，我们直接查 militia_info

            // 最终目标 SQL:
            // SELECT user_id FROM biz_militia_info WHERE dept_id = {deptId}
            // OR dept_id IN (SELECT child_id FROM sys_dept_belong WHERE parent_id = {deptId})

            String subDeptSql = "SELECT child_id FROM sys_dept_belong WHERE parent_id = " + dto.getDeptId();

            String userIdsSql = "SELECT user_id FROM biz_militia_info WHERE dept_id = " + dto.getDeptId() +
                    " OR dept_id IN (" + subDeptSql + ")";

            // 步骤 B: 使用 inSql 限制 leaveReport 的 user_id
            wrapper.inSql(leaveReport::getUserId, userIdsSql);
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

