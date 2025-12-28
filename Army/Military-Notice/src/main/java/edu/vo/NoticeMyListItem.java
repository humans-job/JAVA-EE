// NoticeMyListItem.java
package edu.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeMyListItem {
    private Long noticeId;
    private String title;
    private String content;
    private Integer noticeType;
    private Integer status;
    private LocalDateTime createTime;
    private Long senderDeptId;

    private Integer isRead;
    private LocalDateTime readTime;
}
