// NoticeController.java
package org.example.army.militarynotice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.example.army.militaryauthenticate.common.ApiResp;
import org.example.army.militarynotice.dto.NoticeMyListReq;
import org.example.army.militarynotice.dto.NoticePublishReq;
import org.example.army.militarynotice.service.NoticeService;
import org.example.army.militarynotice.vo.NoticeFeedbackVO;
import org.example.army.militarynotice.vo.NoticeMyListItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    public ApiResp<Long> publish(@Valid @RequestBody NoticePublishReq req) {
        return ApiResp.ok(noticeService.publish(req));
    }

    @GetMapping("/my")
    public ApiResp<IPage<NoticeMyListItem>> myList(NoticeMyListReq req) {
        try {
            return ApiResp.ok(noticeService.myList(req));
        } catch (Exception e) {
            return ApiResp.fail(e.getMessage());
        }
    }

    @PutMapping("/{noticeId}/read")
    public ApiResp<Void> read(@PathVariable Long noticeId) {
        noticeService.markRead(noticeId);
        return ApiResp.ok(null);
    }

    @GetMapping("/{noticeId}/records")
    public ApiResp<NoticeFeedbackVO> feedback(
            @PathVariable Long noticeId,
            @RequestParam(required = false) Integer readStatus,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResp.ok(noticeService.feedback(noticeId, readStatus, pageNum, pageSize));
    }

    @GetMapping("/sent")
    public ApiResp<IPage<NoticeMyListItem>> sentList(NoticeMyListReq req) {
        return ApiResp.ok(noticeService.sentList(req));
    }


}
