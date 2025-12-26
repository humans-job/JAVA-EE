package org.example.army.militaryworkreport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.army.militarycommon.Entity.workReport;
import org.example.army.militaryworkreport.DTO.ReportApproveDTO;
import org.example.army.militaryworkreport.DTO.ReportQueryDTO;
import org.example.army.militaryworkreport.DTO.ReportSubmitDTO;
import org.example.army.militarycommon.mapper.WorkReportMapper;
import org.example.army.militaryworkreport.service.WorkReportService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class WorkReportServiceImpl extends ServiceImpl<WorkReportMapper, workReport> implements WorkReportService {

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
        report.setCreateTime(LocalDateTime.now()); // 设置创建时间

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
        report.setApproveDeptId(dto.getApproveBy()); // 这里假设 approveBy 存入 approveDeptId
        report.setApproveTime(LocalDateTime.now());

        // 3. 更新数据库
        this.updateById(report);
    }

    @Override
    public Page<workReport> queryReports(ReportQueryDTO dto) {
        // 1. 构建分页对象
        Page<workReport> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. 构建查询条件构造器
        LambdaQueryWrapper<workReport> wrapper = new LambdaQueryWrapper<>();

        // 动态拼接条件：如果参数不为空，则拼接到 SQL 中
        wrapper.eq(dto.getReportType() != null, workReport::getReportType, dto.getReportType())
                .eq(StringUtils.hasText(dto.getReportMonth()), workReport::getReportMonth, dto.getReportMonth())
                .eq(dto.getStatus() != null, workReport::getStatus, dto.getStatus());

        // 部门查询逻辑
        if (dto.getDeptId() != null) {
            // 简单逻辑：查询特定部门的报表
            wrapper.eq(workReport::getDeptId, dto.getDeptId());

            // 【进阶扩展思路】如果需要“查询某师及下属所有连队”，可以用 inSql 实现：
            // wrapper.and(w -> w.eq(workReport::getDeptId, dto.getDeptId())
            //    .or()
            //    .inSql(workReport::getDeptId, "SELECT id FROM sys_dept WHERE ancestors LIKE '%," + dto.getDeptId() + ",%'"));
        }

        // 按创建时间倒序排列（最新的在前面）
        wrapper.orderByDesc(workReport::getCreateTime);

        // 3. 执行查询
        return this.page(page, wrapper);
    }
}
