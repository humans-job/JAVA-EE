package org.example.army.militaryworkreport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.army.militaryauthenticate.util.SecurityUtil; // 1. 引入 SecurityUtil
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

import java.time.LocalDateTime;

@Service
public class WorkReportServiceImpl extends ServiceImpl<WorkReportMapper, workReport> implements WorkReportService {

    @Autowired
    private DeptRelationMapper deptRelationMapper;

    @Autowired
    private SecurityUtil securityUtil; // 2. 注入工具类

    @Override
    public void submitReport(ReportSubmitDTO dto) {
        // --- 获取当前登录用户的部门ID ---
        Long currentDeptId = securityUtil.getDeptId();
        if (currentDeptId == null) {
            throw new RuntimeException("获取部门信息失败，请重新登录");
        }

        // 1. 将 DTO 转换为实体对象
        workReport report = new workReport();
        report.setDeptId(currentDeptId);      // 修改后：从 Redis/Token 获取

        report.setTitle(dto.getTitle());
        report.setContent(dto.getContent());
        report.setFilePath(dto.getFilePath());
        report.setReportType(dto.getReportType());
        report.setReportMonth(dto.getReportMonth());

        // 2. 设置初始状态
        report.setStatus(0); // 0 = 待审批
        report.setCreateTime(LocalDateTime.now());
        report.setApproveTime(LocalDateTime.now()); // 注意：刚创建时 approveTime 通常可以为空，或者设为创建时间

        // 使用 currentDeptId 查找上级
        // 构造查询条件：SELECT * FROM sys_dept_belong WHERE child_id = {当前提交部门ID}
        LambdaQueryWrapper<deptRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(deptRelation::getChildId, currentDeptId);

        deptRelation relation = deptRelationMapper.selectOne(relationWrapper);

        if (relation != null) {
            // 找到了上级单位，设置为审批单位
            report.setApproveDeptId(relation.getParentId());
        } else {
            // 如果没找到上级（可能是最高级单位，或者数据未维护）
            // 默认设为 0，防止空指针，表示无上级或由超管处理
            report.setApproveDeptId(0L);
        }

        // 3. 保存到数据库 (调用 MyBatis-Plus 提供的 save 方法)
        this.save(report);
    }

    @Override
    public void approveReport(ReportApproveDTO dto) {
        // --- 获取当前审批人的部门ID ---
        Long currentDeptId = securityUtil.getDeptId();
        if (currentDeptId == null) {
            throw new RuntimeException("获取部门信息失败，请重新登录");
        }

        // 1. 先查询报表是否存在
        workReport report = this.getById(dto.getReportId());
        if (report == null) {
            throw new RuntimeException("报表不存在，无法审批");
        }

        if (!currentDeptId.equals(report.getApproveDeptId())) {
            throw new RuntimeException("您所在的部门无权审批此报表");
        }
        // 2. 更新状态和审批信息
        report.setStatus(dto.getStatus());
        report.setApproveDeptId(currentDeptId);
        report.setApproveTime(LocalDateTime.now());

        // 3. 更新数据库
        this.updateById(report);
    }
    @Override
    // 修改 WorkReportServiceImpl.java 的 queryReports 方法
    public Page<workReport> queryReports(ReportQueryDTO dto) {
        Page<workReport> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Long currentDeptId = securityUtil.getDeptId();
        if (currentDeptId == null) return page;

        LambdaQueryWrapper<workReport> wrapper = new LambdaQueryWrapper<>();

        // --- 逻辑分支 ---
        // 场景A：我是上级，我要看待我审批的 (前端传 status=0 用来标记这是待办箱)
        if (dto.getStatus() != null && dto.getStatus() == 0) {
            wrapper.eq(workReport::getStatus, 0);
            wrapper.eq(workReport::getApproveDeptId, currentDeptId);
        }
        // 场景B：我是下级，我要看我提交的历史记录 (前端不传 status 或传其他值)
        else {
            // 查询 deptId 是我自己的记录
            wrapper.eq(workReport::getDeptId, currentDeptId);
            // 可选：如果是历史记录，可能需要根据前端传的 status 进一步过滤
            if (dto.getStatus() != null) {
                wrapper.eq(workReport::getStatus, dto.getStatus());
            }
        }

        // 其他通用条件...
        wrapper.orderByDesc(workReport::getCreateTime);
        return this.page(page, wrapper);
    }

    // 在 WorkReportServiceImpl 类中实现该方法

    @Override
    public workReport getReportDetail(Long id) {
        // 1. 查询数据
        workReport report = this.getById(id);

        // 2. 判空
        if (report == null) {
            throw new RuntimeException("报表不存在");
        }

        // 3. 获取当前用户部门ID
        Long currentDeptId = securityUtil.getDeptId();
        if (currentDeptId == null) {
            throw new RuntimeException("登录已过期");
        }

        // 4. 权限校验 (核心安全逻辑)
        // 允许查看的条件：当前用户是“提交者” 或者 当前用户是“审批者”
        boolean isSubmitter = currentDeptId.equals(report.getDeptId());
        boolean isApprover = currentDeptId.equals(report.getApproveDeptId());

        if (!isSubmitter && !isApprover) {
            // 如果既不是提交人，也不是审批人，则抛出异常
            throw new RuntimeException("您所在的部门无权查看该报表详情");
        }

        return report;
    }

}
