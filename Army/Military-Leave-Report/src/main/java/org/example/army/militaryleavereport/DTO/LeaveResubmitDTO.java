package org.example.army.militaryleavereport.DTO;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaveResubmitDTO {
    private Long leaveId;       // 必须传 ID 才知道改哪条
    private String reason;      // 新的理由
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

