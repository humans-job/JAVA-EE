package org.example.army.militarycommon.Entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_notice_record")
public class noticeRecord  {
    @TableField("notice_id") // 假设id是复合主键的一部分
    private Long noticeId;// 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    @TableField("notice_id") // 假设id是复合主键的一部分
    private Long userId;

    private int is_read;
    private LocalDateTime readTime;
}
