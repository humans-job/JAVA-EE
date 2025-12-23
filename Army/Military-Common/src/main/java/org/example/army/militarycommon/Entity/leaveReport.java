package org.example.army.militarycommon.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_leave")
public class leaveReport {
    @TableId(value = "leave_id", type = IdType.AUTO) // 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long leaveId;
    private Long userId;
    private Long deptId;
    private String leaveReason;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime applyTime;
    private int status;
    private Long approveDept;
    private Long approveOpinion;
    private LocalDateTime reportBcakTime;
    private String reportBcakLocation;
    private String reportBcakConfirmDept;
}
