package org.example.army.militaryleavereport.DTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaveSubmitDTO {
    private Long userId;        // 申请人民兵ID
    private Long deptId;        // 所属部门ID (可选，建议后端通过userId自动查)
    private String reason;      // 请假理由 (对应 leaveReason)
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}