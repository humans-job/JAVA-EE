// NoticeRecordItem.java（阅读反馈列表用）
package org.example.army.militarynotice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeRecordItem {
    private Long userId;
    private String username;
    private Long deptId;
    private Integer isRead;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readTime;
}
