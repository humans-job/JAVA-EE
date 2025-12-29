// NoticePublishReq.java
package org.example.army.militarynotice.dto;

import lombok.Data;

import java.util.List;

@Data
public class NoticePublishReq {
    private String title;

    private String content;

    /** 1=通知公告, 2=教育学习, 3=团场内部通知 */
    private Integer noticeType;
}
