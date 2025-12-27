package org.example.army.militaryworkreport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.army.militarycommon.Entity.deptRelation;
import org.example.army.militarycommon.Entity.workReport;
import org.example.army.militarycommon.mapper.DeptRelationMapper;
import org.example.army.militaryworkreport.DTO.ReportApproveDTO;
import org.example.army.militaryworkreport.DTO.ReportQueryDTO;
import org.example.army.militaryworkreport.DTO.ReportSubmitDTO;
import org.example.army.militarycommon.mapper.WorkReportMapper;

import org.example.army.militaryworkreport.service.WorkReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class WorkReportServiceImpl extends ServiceImpl<WorkReportMapper, workReport> implements WorkReportService {

    @Autowired
    private DeptRelationMapper deptRelationMapper;

    @Override
    public void submitReport(ReportSubmitDTO dto) {
        // 1. 将 DTO 转换为实体对象
        workReport report = new workReport();
        report.setDeptId(dto.getDeptId());
        report.setTitle(dto.getTitle());
        report.setContent(dto.getContent());
        report.setFilePath(dto.getFilePath());
        report.setReportType(dto.getReportType());
        report.setReportMonth(dto.getReportMonth());

        // 2. 设置初始状态
        report.setStatus(0); // 0 = 待审批
        report.setCreateTime(LocalDateTime.now());
        report.setApproveTime(LocalDateTime.now());// 设置创建时间
        if (dto.getDeptId() != null) {
            // 构造查询条件：SELECT * FROM sys_dept_belong WHERE child_id = {当前提交部门ID}
            LambdaQueryWrapper<deptRelation> relationWrapper = new LambdaQueryWrapper<>();
            relationWrapper.eq(deptRelation::getChildId, dto.getDeptId());

            deptRelation relation = deptRelationMapper.selectOne(relationWrapper);

            if (relation != null) {
                // 找到了上级单位，设置为审批单位
                report.setApproveDeptId(relation.getParentId());
            } else {
                // 如果没找到上级（可能是最高级单位，或者数据未维护）
                // 默认设为 0，防止空指针，表示无上级或由超管处理
                report.setApproveDeptId(0L);
            }
        }
        // 3. 保存到数据库 (调用 MyBatis-Plus 提供的 save 方法)
        this.save(report);
    }

    @Override
    public void approveReport(ReportApproveDTO dto) {
        // 1. 先查询报表是否存在
        workReport report = this.getById(dto.getReportId());
        if (report == null) {
            throw new RuntimeException("报表不存在，无法审批");
        }

        // 2. 更新状态和审批信息
        report.setStatus(dto.getStatus());
        report.setApproveDeptId(dto.getApproveDeptId()); // 这里假设 approveBy 存入 approveDeptId
        report.setApproveTime(LocalDateTime.now());

        // 3. 更新数据库
        this.updateById(report);
    }

    @Override
    public Page<workReport> queryReports(ReportQueryDTO dto) {
        // 1. 构建分页对象
        Page<workReport> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // --- 严格权限控制逻辑 ---

        // 条件1: 必须传入部门ID (否则不知道查谁的待审批)
        if (dto.getDeptId() == null) {
            return page; // 直接返回空列表
        }

        // 条件2: 如果前端明确指定了要查 status != 0 (例如查已归档)，则根据需求直接驳回/返回空
        if (dto.getStatus() != null && dto.getStatus() != 0) {
            return page; // 需求规定：其余不能查看，直接返回空
        }

        // -----------------------

        // 2. 构建查询条件
        LambdaQueryWrapper<workReport> wrapper = new LambdaQueryWrapper<>();

        // 3. 强制固定的核心条件
        // 必须是待审批 (status = 0)
        wrapper.eq(workReport::getStatus, 0);
        // 必须是待我审批 (approve_dept_id = 当前部门)
        wrapper.eq(workReport::getApproveDeptId, dto.getDeptId());

        // 4. 其他辅助筛选条件 (类型、月份) - 只有满足核心条件后，这些筛选才有意义
        wrapper.eq(dto.getReportType() != null, workReport::getReportType, dto.getReportType())
                .eq(StringUtils.hasText(dto.getReportMonth()), workReport::getReportMonth, dto.getReportMonth());

        // 按时间倒序
        wrapper.orderByDesc(workReport::getCreateTime);

        // 5. 执行查询
        return this.page(page, wrapper);
    }
}
