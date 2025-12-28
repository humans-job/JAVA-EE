// NoticeService.java
package edu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import edu.dto.NoticeMyListReq;
import edu.dto.NoticePublishReq;
import edu.vo.NoticeFeedbackVO;
import edu.vo.NoticeMyListItem;
import org.example.army.militarycommon.Entity.deptNotices;

import java.util.List;

public interface NoticeService {
    Long publish(NoticePublishReq req);

    IPage<NoticeMyListItem> myList(NoticeMyListReq req);

    void markRead(Long noticeId);

    NoticeFeedbackVO feedback(Long noticeId, Integer readStatus, long pageNum, long pageSize);


    void complete(Long noticeId);

    IPage<deptNotices> sentList(NoticeMyListReq req);
}
