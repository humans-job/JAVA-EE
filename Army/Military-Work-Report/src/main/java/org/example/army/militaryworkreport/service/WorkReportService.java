package org.example.army.militaryworkreport.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.army.militarycommon.Entity.workReport;
import org.example.army.militaryworkreport.DTO.ReportApproveDTO;
import org.example.army.militaryworkreport.DTO.ReportQueryDTO;
import org.example.army.militaryworkreport.DTO.ReportSubmitDTO;

/**
 * 工作报表业务接口
 */
public interface WorkReportService extends IService<workReport> {

    /**
     * 提交报表
     * @param dto 提交参数
     */
    void submitReport(ReportSubmitDTO dto);

    /**
     * 审批报表
     * @param dto 审批参数
     */
    void approveReport(ReportApproveDTO dto);

    /**
     * 分页查询报表列表
     * @param dto 查询条件
     * @return 分页结果
     */
    Page<workReport> queryReports(ReportQueryDTO dto);
}
