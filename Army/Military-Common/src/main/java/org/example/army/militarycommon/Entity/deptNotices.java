package org.example.army.militarycommon.Entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_notice")
public class deptNotices  {
    @TableId(value = "notice_id", type = IdType.AUTO) // 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long noticeId;
    private String title;
    private String content;
    private Integer type;
    private Long senderId;
    private LocalDateTime sendTime;
    private Integer status;
}
