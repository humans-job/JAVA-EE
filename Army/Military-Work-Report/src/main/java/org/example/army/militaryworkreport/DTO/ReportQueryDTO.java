package org.example.army.militaryworkreport.DTO;

import lombok.Data;

@Data
public class ReportQueryDTO {
    private Long deptId;
    private Integer reportType;
    private String reportMonth;
    private Integer status;
    private Integer pageNum = 1;  // 默认第1页
    private Integer pageSize = 10; // 默认10条
}
