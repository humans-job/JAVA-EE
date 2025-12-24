// NoticeRecordItem.java（阅读反馈列表用）
package edu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeRecordItem {
    private Long userId;
    private String username;
    private Long deptId;
    private Integer isRead;
    private LocalDateTime readTime;
}
