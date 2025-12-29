package org.example.army.militaryleavereport.DTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaveSubmitDTO {
    private String reason;      // 请假理由 (对应 leaveReason)
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}