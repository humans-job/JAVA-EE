// NoticeServiceImpl.java
package edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dto.NoticeMyListReq;
import edu.dto.NoticePublishReq;
import edu.service.NoticeService;
import edu.util.SecurityUtil;
import edu.vo.NoticeFeedbackVO;
import edu.vo.NoticeMyListItem;
import edu.vo.NoticeRecordItem;
import lombok.RequiredArgsConstructor;
import org.example.army.militarycommon.Entity.Dept;
import org.example.army.militarycommon.Entity.deptNotices;
import org.example.army.militarycommon.Entity.deptRelation;
import org.example.army.militarycommon.Entity.noticeRecord;
import org.example.army.militarycommon.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final DeptNoticesMapper deptNoticesMapper;
    private final NoticeRecordMapper noticeRecordMapper;
    private final DeptRelationMapper deptRelationMapper;
    private final DeptMapper deptMapper;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(NoticePublishReq req) {
        Long senderDeptId = securityUtil.getDeptId();

        deptNotices notice = new deptNotices();
        notice.setTitle(req.getTitle());
        notice.setContent(req.getContent());
        notice.setType(req.getNoticeType());
        notice.setSenderId(senderDeptId);
        notice.setSendTime(LocalDateTime.now());
        notice.setStatus(0);

        deptNoticesMapper.insert(notice);

        // 1) 计算接收
        Set<Long> receiverDeptIds = new HashSet<>();
        LambdaQueryWrapper<deptRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(deptRelation::getParentId, senderDeptId);
        List<deptRelation> allDeptBelongs = deptRelationMapper.selectList(queryWrapper);
        List<Long> allDeptIds = new ArrayList<>();
        for(deptRelation b : allDeptBelongs){
            allDeptIds.add(b.getChildId());
        }
        receiverDeptIds.addAll(allDeptIds);

        if (receiverDeptIds.isEmpty()) {
            // 也可以改成抛异常：必须选择接收范围
            return -1L;
        }

        // 2) 批量插入 biz_notice_record
        List<noticeRecord> records = receiverDeptIds.stream().map(uid -> {
            noticeRecord r = new noticeRecord();
            r.setNoticeId(notice.getNoticeId());
            r.setUserId(uid);
            r.setIsRead(0);
            r.setReadTime(null);
            return r;
        }).toList();

        // MyBatis-Plus BaseMapper 没有批量 insert，这里用循环（量大可改成自定义 batch）
        for (noticeRecord r : records) {
            noticeRecordMapper.insert(r);
        }

        return notice.getNoticeId();
    }

    @Override
    public IPage<NoticeMyListItem> myList(NoticeMyListReq req) {
        Long deptId = securityUtil.getDeptId();

        LambdaQueryWrapper<noticeRecord> rw = new LambdaQueryWrapper<noticeRecord>()
                .eq(noticeRecord::getUserId, deptId);

        // readStatus：在 record 层过滤
        if (req.getReadStatus() != null) {
            rw.eq(noticeRecord::getIsRead, req.getReadStatus());
        }

        // noticeType：前置到 record 查询里
        if (req.getNoticeType() != null) {
            // 不写 join / XML，用子查询方式筛 noticeId
            rw.inSql(noticeRecord::getNoticeId,
                    "select notice_id from biz_notice where type = " + req.getNoticeType());
        }

        Page<noticeRecord> page = new Page<>(req.getPageNum(), req.getPageSize());
        IPage<noticeRecord> recordPage = noticeRecordMapper.selectPage(page, rw);

        List<Long> noticeIds = recordPage.getRecords().stream()
                .map(noticeRecord::getNoticeId)
                .distinct()
                .toList();

        List<deptNotices> notices = noticeIds.isEmpty()
                ? List.of()
                : deptNoticesMapper.selectList(new LambdaQueryWrapper<deptNotices>()
                .in(deptNotices::getNoticeId, noticeIds));

        Map<Long, deptNotices> noticeMap = notices.stream()
                .collect(Collectors.toMap(deptNotices::getNoticeId, n -> n, (a, b) -> a));

        List<NoticeMyListItem> items = new ArrayList<>(recordPage.getRecords().size());
        for (noticeRecord r : recordPage.getRecords()) {
            deptNotices n = noticeMap.get(r.getNoticeId());
            if (n == null) {
                continue;
            }
            NoticeMyListItem item = new NoticeMyListItem();
            item.setNoticeId(n.getNoticeId());
            item.setTitle(n.getTitle());
            item.setNoticeType(n.getType());
            item.setStatus(n.getStatus());
            item.setCreateTime(n.getSendTime());
            item.setSenderDeptId(n.getSenderId());
            item.setIsRead(r.getIsRead());
            item.setReadTime(r.getReadTime());
            items.add(item);
        }

        Page<NoticeMyListItem> out = new Page<>(req.getPageNum(), req.getPageSize());
        out.setTotal(recordPage.getTotal());  // total 也与 noticeType/readStatus 对齐
        out.setRecords(items);               // records 数量不会再因为 noticeType 过滤变少
        return out;
    }


    @Override
    public void markRead(Long noticeId) {
        Long deptId = securityUtil.getDeptId();

        noticeRecord record = noticeRecordMapper.selectOne(new LambdaQueryWrapper<noticeRecord>()
                .eq(noticeRecord::getNoticeId, noticeId)
                .eq(noticeRecord::getUserId, deptId)
        );
        if (record == null) return;

        if (record.getIsRead() != null && record.getIsRead() == 1) return;

        record.setIsRead(1);
        record.setReadTime(LocalDateTime.now());
        int updated = noticeRecordMapper.update(record, new LambdaQueryWrapper<noticeRecord>()
                .eq(noticeRecord::getNoticeId, noticeId)
                .eq(noticeRecord::getUserId, deptId)
        );

        if (updated > 0) {
            complete(noticeId);
        }
    }

    @Override
    public NoticeFeedbackVO feedback(Long noticeId, Integer readStatus, long pageNum, long pageSize) {
        // 统计
        long total = noticeRecordMapper.selectCount(new LambdaQueryWrapper<noticeRecord>()
                .eq(noticeRecord::getNoticeId, noticeId)
        );
        long readCount = noticeRecordMapper.selectCount(new LambdaQueryWrapper<noticeRecord>()
                .eq(noticeRecord::getNoticeId, noticeId)
                .eq(noticeRecord::getIsRead, 1)
        );
        long unreadCount = total - readCount;

        // 列表（分页）
        LambdaQueryWrapper<noticeRecord> rw = new LambdaQueryWrapper<noticeRecord>()
                .eq(noticeRecord::getNoticeId, noticeId)
                .eq(readStatus != null, noticeRecord::getIsRead, readStatus);

        Page<noticeRecord> page = new Page<>(pageNum, pageSize);
        IPage<noticeRecord> recordPage = noticeRecordMapper.selectPage(page, rw);

        List<Long> deptIds = recordPage.getRecords().stream().map(noticeRecord::getUserId).toList();
        Map<Long, Dept> deptMap = deptIds.isEmpty() ? Map.of() :
                deptMapper.selectList(new LambdaQueryWrapper<Dept>().in(Dept::getDeptId, deptIds))
                        .stream().collect(Collectors.toMap(Dept::getDeptId, u -> u));

        List<NoticeRecordItem> deptList = new ArrayList<>();
        for (noticeRecord r : recordPage.getRecords()) {
            Dept u = deptMap.get(r.getUserId());
            NoticeRecordItem item = new NoticeRecordItem();
            item.setUserId(r.getUserId());
            item.setIsRead(r.getIsRead());
            item.setReadTime(r.getReadTime());
            if (u != null) {
                item.setUsername(u.getDeptName());
                item.setDeptId(u.getDeptId());
            }
            deptList.add(item);
        }

        NoticeFeedbackVO vo = new NoticeFeedbackVO();
        vo.setNoticeId(noticeId);
        vo.setTotal(total);
        vo.setReadCount(readCount);
        vo.setUnreadCount(unreadCount);
        vo.setDeptList(deptList);
        return vo;
    }


    @Override
    public void complete(Long noticeId) {
        // 1) 先查有没有未读（is_read = 0 或 null 都当未读）
        Long unreadCnt = noticeRecordMapper.selectCount(new LambdaQueryWrapper<noticeRecord>()
                .eq(noticeRecord::getNoticeId, noticeId)
                .and(w -> w.isNull(noticeRecord::getIsRead)
                        .or()
                        .eq(noticeRecord::getIsRead, 0))
        );

        // 有未读 -> 不允许完成
        if (unreadCnt != null && unreadCnt > 0) {
            return;
        }

        // 2) 没有未读 -> 设置状态为完成
        deptNotices n = deptNoticesMapper.selectById(noticeId);
        if (n == null) return;

        if (n.getStatus() != null && n.getStatus() == 1) return; // 已完成就不重复更新

        n.setStatus(1);
        deptNoticesMapper.updateById(n);
    }

}
