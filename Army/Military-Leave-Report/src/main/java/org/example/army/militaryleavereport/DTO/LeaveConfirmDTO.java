package org.example.army.militaryleavereport.DTO;
import lombok.Data;

@Data
public class LeaveConfirmDTO {
    private Long leaveId;
    private Long confirmBy; // 确认人/部门ID (对应 reportBcakConfirmDept)
}

