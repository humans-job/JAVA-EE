package org.example.army.militaryleavereport.DTO;
import lombok.Data;

@Data
public class LeaveQueryDTO {
    private Long deptId;
    private String month;       // 格式 "2025-12"
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}

