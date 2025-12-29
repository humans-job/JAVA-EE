// NoticeMyListReq.java
package org.example.army.militarynotice.dto;

import lombok.Data;

@Data
public class NoticeMyListReq {
    private Integer noticeType;   // 可空
    private Integer readStatus;   // 0未读/1已读 可空
    private long pageNum = 1;
    private long pageSize = 10;
}
