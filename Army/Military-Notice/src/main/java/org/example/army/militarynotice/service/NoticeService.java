// NoticeService.java
package org.example.army.militarynotice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.army.militarynotice.dto.NoticeMyListReq;
import org.example.army.militarynotice.dto.NoticePublishReq;
import org.example.army.militarynotice.vo.NoticeFeedbackVO;
import org.example.army.militarynotice.vo.NoticeMyListItem;

public interface NoticeService {
    Long publish(NoticePublishReq req);

    IPage<NoticeMyListItem> myList(NoticeMyListReq req);

    void markRead(Long noticeId);

    NoticeFeedbackVO feedback(Long noticeId, Integer readStatus, long pageNum, long pageSize);


    void complete(Long noticeId);

    IPage<NoticeMyListItem> sentList(NoticeMyListReq req);
}
