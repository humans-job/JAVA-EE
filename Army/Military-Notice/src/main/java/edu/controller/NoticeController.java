// NoticeController.java
package edu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import edu.dto.NoticeMyListReq;
import edu.dto.NoticePublishReq;
import edu.service.NoticeService;
import edu.vo.NoticeFeedbackVO;
import edu.vo.NoticeMyListItem;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ApiResp.ok(noticeService.myList(req));
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

    @PutMapping("/{noticeId}/complete")
    public ApiResp<Void> complete(@PathVariable Long noticeId) {
        noticeService.complete(noticeId);
        return ApiResp.ok(null);
    }

    @Data
    public static class ApiResp<T> {
        private int code;
        private String msg;
        private T data;

        public static <T> ApiResp<T> ok(T data) {
            ApiResp<T> r = new ApiResp<>();
            r.code = 200;
            r.msg = "success";
            r.data = data;
            return r;
        }

        public static ApiResp<Boolean> fail(String msg) {
            ApiResp<Boolean> r = new ApiResp<>();
            r.code = 200;
            r.msg = msg;
            r.data = false;
            return r;
        }
    }
}
