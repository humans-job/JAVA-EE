// NoticeFeedbackVO.java
package edu.vo;

import lombok.Data;

import java.util.List;

@Data
public class NoticeFeedbackVO {
    private Long noticeId;
    private long total;
    private long readCount;
    private long unreadCount;
    private List<NoticeRecordItem> deptList;
}
