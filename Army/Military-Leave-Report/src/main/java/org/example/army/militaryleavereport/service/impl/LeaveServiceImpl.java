package org.example.army.militaryleavereport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.army.militaryauthenticate.util.SecurityUtil;
import org.example.army.militarycommon.Entity.leaveReport;
import org.example.army.militarycommon.mapper.DeptRelationMapper;
import org.example.army.militarycommon.mapper.LeaveReportMapper;
import org.example.army.militaryleavereport.DTO.*;
import org.example.army.militaryleavereport.service.LeaveService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请销假服务实现类
 */
@Service
public class LeaveServiceImpl extends ServiceImpl<LeaveReportMapper, leaveReport> implements LeaveService {

    @Autowired
    private LeaveReportMapper deptRelationMapper;

    @Autowired
    private SecurityUtil securityUtil; // 2. 注入工具类
    /**
     * 提交请假申请
     * 状态: null → 0(待审批)
     */
    @Override
    @Transactional
    public void submitLeave(LeaveSubmitDTO dto) {
        Long userId = securityUtil.getUserId();
        Long deptId = securityUtil.getDeptId();

        leaveReport leave = new leaveReport();
        leave.setUserId(userId);
        leave.setDeptId(deptId);
        leave.setLeaveReason(dto.getReason());
        leave.setStartTime(dto.getStartTime());
        leave.setEndTime(dto.getEndTime());
        leave.setApplyTime(LocalDateTime.now());
        leave.setStatus(STATUS_PENDING); // 初始状态：待审批

        this.save(leave);
    }

    /**
     * 重新提交请假申请（驳回后修改）
     * 状态: 2(已驳回) → 0(待审批)
     */
    @Override
    @Transactional
    public void resubmitLeave(LeaveResubmitDTO dto) {
        Long userId = securityUtil.getUserId();

        leaveReport leave = this.getById(dto.getLeaveId());
        if (leave == null) {
            throw new RuntimeException("请假记录不存在");
        }
        if (leave.getStatus() != STATUS_REJECTED) {
            throw new RuntimeException("只有被驳回的假条才能重新提交");
        }
        if (!leave.getUserId().equals(userId)) {
            throw new RuntimeException("只能修改自己的假条");
        }

        LambdaUpdateWrapper<leaveReport> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(leaveReport::getLeaveId, dto.getLeaveId())
                .set(leaveReport::getLeaveReason, dto.getReason())
                .set(leaveReport::getStartTime, dto.getStartTime())
                .set(leaveReport::getEndTime, dto.getEndTime())
                .set(leaveReport::getApplyTime, LocalDateTime.now())
                .set(leaveReport::getStatus, STATUS_PENDING)  // 重新变为待审批
                .set(leaveReport::getApproveOpinion, null);

        this.update(wrapper);
    }

    /**
     * 审批请假
     * 状态: 0(待审批) → 1(待销假) 或 2(已驳回)
     */
    @Override
    @Transactional
    public void approveLeave(LeaveApproveDTO dto) {
        Long deptId = securityUtil.getDeptId();

        leaveReport leave = this.getById(dto.getLeaveId());
        if (leave == null) {
            throw new RuntimeException("请假记录不存在");
        }
        if (leave.getStatus() != STATUS_PENDING) {
            throw new RuntimeException("当前状态不允许审批，只有待审批的假条才能审批");
        }

        // 验证状态值合法性
        if (dto.getStatus() != STATUS_APPROVED && dto.getStatus() != STATUS_REJECTED) {
            throw new RuntimeException("审批状态值非法，只能是1(通过)或2(驳回)");
        }

        LambdaUpdateWrapper<leaveReport> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(leaveReport::getLeaveId, dto.getLeaveId())
                .set(leaveReport::getStatus, dto.getStatus()) // 1=通过(待销假), 2=驳回
                .set(leaveReport::getApproveDept, deptId)
                .set(leaveReport::getApproveOpinion, dto.getApproveOpinion());

        this.update(wrapper);
    }

    /**
     * 民兵销假打卡
     * 状态: 1(待销假) → 3(已销假待确认)
     */
    @Override
    @Transactional
    public void reportBack(LeaveReportBackDTO dto) {
        Long userId = securityUtil.getUserId();
        leaveReport leave = this.getById(dto.getLeaveId());
        long approveId = leave.getApproveDept();
        if (leave == null) {
            throw new RuntimeException("请假记录不存在");
        }
        // 只有状态为 1(审批通过/待销假) 才能销假
        if (leave.getStatus() != STATUS_APPROVED) {
            throw new RuntimeException("当前状态不允许销假，只有审批通过(待销假)的假条才能销假打卡");
        }
        // 校验是否是本人的假条
        if (!leave.getUserId().equals(userId)) {
            throw new RuntimeException("只能对自己的假条进行销假");
        }

        LambdaUpdateWrapper<leaveReport> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(leaveReport::getLeaveId, dto.getLeaveId())
                .set(leaveReport::getStatus, STATUS_REPORTED)  // 关键：设置为 3(已销假待确认)
                .set(leaveReport::getReportBackTime, LocalDateTime.now())
                .set(leaveReport::getReportBackLocation, dto.getReportBackLocation());

        this.update(wrapper);
    }

    /**
     * 管理员确认归档
     * 状态: 3(已销假待确认) → 4(已归档)
     */
    @Override
    @Transactional
    public void confirmReportBack(LeaveConfirmDTO dto) {
        Long deptId = securityUtil.getDeptId();

        leaveReport leave = this.getById(dto.getLeaveId());
        if (leave == null) {
            throw new RuntimeException("请假记录不存在");
        }
        // 只有状态为 3(已销假待确认) 才能归档
        if (leave.getStatus() != STATUS_REPORTED) {
            throw new RuntimeException("当前状态不允许归档，只有已销假待确认(状态3)的假条才能归档");
        }

        LambdaUpdateWrapper<leaveReport> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(leaveReport::getLeaveId, dto.getLeaveId())
                .set(leaveReport::getStatus, STATUS_ARCHIVED)  // 关键：设置为 4(已归档)
                .set(leaveReport::getReportBackConfirmDept, deptId);

        this.update(wrapper);
    }

    /**
     * 查询请假列表及统计
     */
    @Override
    public Map<String, Object> getLeaveStats(LeaveQueryDTO dto) {
        Long userId = securityUtil.getUserId();
        Long deptId = securityUtil.getDeptId();

        LambdaQueryWrapper<leaveReport> wrapper = new LambdaQueryWrapper<>();

        // 根据 queryType 决定查询范围
        if (dto.getQueryType() != null && dto.getQueryType() == 2) {
            // 民兵查自己
            wrapper.eq(leaveReport::getUserId, userId);
        } else {
            // 管理员查下属（同部门）
            wrapper.eq(leaveReport::getDeptId, deptId);
        }

        // 月份筛选
        if (dto.getMonth() != null && !dto.getMonth().isEmpty()) {
            YearMonth ym = YearMonth.parse(dto.getMonth(), DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = ym.atEndOfMonth().atTime(23, 59, 59);
            wrapper.between(leaveReport::getApplyTime, monthStart, monthEnd);
        }

        // 状态筛选
        if (dto.getStatus() != null) {
            wrapper.eq(leaveReport::getStatus, dto.getStatus());
        }

        // 只看待确认（已销假待归档）
        if (Boolean.TRUE.equals(dto.getOnlyWaitConfirm())) {
            wrapper.eq(leaveReport::getStatus, STATUS_REPORTED);
        }

        wrapper.orderByDesc(leaveReport::getApplyTime);

        // 分页查询
        Page<leaveReport> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<leaveReport> result = this.page(page, wrapper);

        // 统计各状态数量（基于相同的查询范围，但不限制状态）
        LambdaQueryWrapper<leaveReport> countWrapper = new LambdaQueryWrapper<>();
        if (dto.getQueryType() != null && dto.getQueryType() == 2) {
            countWrapper.eq(leaveReport::getUserId, userId);
        } else {
            countWrapper.eq(leaveReport::getDeptId, deptId);
        }

        // 月份筛选（统计时也应用）
        if (dto.getMonth() != null && !dto.getMonth().isEmpty()) {
            YearMonth ym = YearMonth.parse(dto.getMonth(), DateTimeFormatter.ofPattern("yyyy-MM"));
            LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = ym.atEndOfMonth().atTime(23, 59, 59);
            countWrapper.between(leaveReport::getApplyTime, monthStart, monthEnd);
        }

        List<leaveReport> allRecords = this.list(countWrapper);

        // 统计各状态数量
        long pendingCount = allRecords.stream().filter(r -> r.getStatus() == STATUS_PENDING).count();
        long approvedCount = allRecords.stream().filter(r -> r.getStatus() == STATUS_APPROVED).count();
        long rejectedCount = allRecords.stream().filter(r -> r.getStatus() == STATUS_REJECTED).count();
        long reportedCount = allRecords.stream().filter(r -> r.getStatus() == STATUS_REPORTED).count();
        long confirmedCount = allRecords.stream().filter(r -> r.getStatus() == STATUS_ARCHIVED).count();

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("pendingCount", pendingCount);      // 待审批
        data.put("approvedCount", approvedCount);    // 待销假
        data.put("rejectedCount", rejectedCount);    // 已驳回
        data.put("reportedCount", reportedCount);    // 待确认（已销假待归档）
        data.put("confirmedCount", confirmedCount);  // 已归档

        return data;
    }
}
