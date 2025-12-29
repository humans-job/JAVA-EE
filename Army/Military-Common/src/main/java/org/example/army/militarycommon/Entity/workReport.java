package org.example.army.militarycommon.Entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_work_report")
public class workReport {
    @TableId(value = "report_id", type = IdType.AUTO) // 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long reportId;
    private Long deptId;
    private String title;
    private String content;
    private String filePath;
    private Integer reportType;
    private String reportMonth;
    private Integer status;
    private Long approveDeptId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
