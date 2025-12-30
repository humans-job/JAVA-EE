package org.example.army.militaryleavereport.DTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaveResubmitDTO {
    private Long leaveId;       // 必须传 ID 才知道改哪条
    private String reason;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")// 新的理由
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}

