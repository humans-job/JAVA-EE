package org.example.army.militaryworkreport.DTO;

import lombok.Data;

@Data
public class ReportApproveDTO {
    private Long reportId;
    private Integer status;    // 1=通过, 2=驳回
    private Long approveDeptId;// 审批人ID (或者审批部门ID，根据业务需求)
}
