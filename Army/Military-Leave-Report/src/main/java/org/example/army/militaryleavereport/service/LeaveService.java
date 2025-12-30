package org.example.army.militaryleavereport.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.army.militarycommon.Entity.leaveReport;
import org.example.army.militaryleavereport.DTO.*;

import java.util.Map;

/**
 * 请销假服务接口
 *
 * 状态流转说明：
 * ┌─────────────┐     审批通过      ┌─────────────┐     民兵销假      ┌─────────────┐     管理员归档     ┌─────────────┐
 * │  0-待审批   │ ───────────────► │  1-待销假   │ ───────────────► │  3-待确认   │ ────────────────► │  4-已归档   │
 * └─────────────┘                   └─────────────┘                   └─────────────┘                    └─────────────┘
 *        │
 *        │ 审批驳回
 *        ▼
 * ┌─────────────┐     重新提交
 * │  2-已驳回   │ ─────────────────► 回到 0-待审批
 * └─────────────┘
 *
 * 状态码定义：
 * 0 = 待审批
 * 1 = 审批通过(待销假)
 * 2 = 已驳回
 * 3 = 已销假(待确认) - 民兵已打卡，等待管理员归档
 * 4 = 已归档 - 管理员已确认归档
 */
public interface LeaveService extends IService<leaveReport> {

    // ==================== 状态常量 ====================

    /** 待审批 */
    int STATUS_PENDING = 0;

    /** 审批通过(待销假) */
    int STATUS_APPROVED = 1;

    /** 已驳回 */
    int STATUS_REJECTED = 2;

    /** 已销假(待确认) - 民兵已打卡，等待管理员归档 */
    int STATUS_REPORTED = 3;

    /** 已归档 - 管理员已确认 */
    int STATUS_ARCHIVED = 4;

    // ==================== 服务方法 ====================

    /**
     * 1. 提交请假申请
     * 状态: null → 0(待审批)
     *
     * @param dto 请假申请信息
     */
    void submitLeave(LeaveSubmitDTO dto);

    /**
     * 2. 重新提交请假申请（驳回后修改）
     * 状态: 2(已驳回) → 0(待审批)
     *
     * @param dto 修改后的请假信息
     */
    void resubmitLeave(LeaveResubmitDTO dto);

    /**
     * 3. 审批请假
     * 状态: 0(待审批) → 1(待销假) 或 2(已驳回)
     *
     * @param dto 审批信息，包含审批结果和意见
     */
    void approveLeave(LeaveApproveDTO dto);

    /**
     * 4. 民兵销假打卡
     * 状态: 1(待销假) → 3(已销假待确认)
     *
     * 民兵归队后调用此接口进行销假打卡，
     * 打卡后状态变为"待确认"，等待管理员归档。
     *
     * @param dto 销假信息，包含销假位置
     */
    void reportBack(LeaveReportBackDTO dto);

    /**
     * 5. 管理员确认归档
     * 状态: 3(已销假待确认) → 4(已归档)
     *
     * 管理员确认民兵已归队，将假条归档。
     * 只有状态为3的假条才能进行归档操作。
     *
     * @param dto 确认信息，包含假条ID
     */
    void confirmReportBack(LeaveConfirmDTO dto);

    /**
     * 6. 查询请销假台账（含统计）
     *
     * 返回数据包含：
     * - list: 请假记录列表
     * - total: 总记录数
     * - pendingCount: 待审批数量
     * - approvedCount: 待销假数量
     * - rejectedCount: 已驳回数量
     * - reportedCount: 待确认数量（已销假待归档）
     * - confirmedCount: 已归档数量
     *
     * @param dto 查询条件
     * @return 包含列表和统计数据的Map
     */
    Map<String, Object> getLeaveStats(LeaveQueryDTO dto);
}

