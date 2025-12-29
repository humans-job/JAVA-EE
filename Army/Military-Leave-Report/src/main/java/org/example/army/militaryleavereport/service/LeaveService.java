package org.example.army.militaryleavereport.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.army.militarycommon.Entity.leaveReport;
import org.example.army.militaryleavereport.DTO.*;

import java.util.Map;

/**
 * 请销假服务接口
 */
public interface LeaveService extends IService<leaveReport> {

    /**
     * 1. 提交请假申请
     */
    void submitLeave(LeaveSubmitDTO dto);
    // 【新增】修改并重新提交
    void resubmitLeave(LeaveResubmitDTO dto);
    /**
     * 2. 审批请假
     */
    void approveLeave(LeaveApproveDTO dto);

    /**
     * 3. 民兵销假 (打卡)
     */
    void reportBack(LeaveReportBackDTO dto);

    /**
     * 4. 确认销假 (归档)
     */
    void confirmReportBack(LeaveConfirmDTO dto);

    /**
     * 5. 查询请销假台账 (含统计)
     */
    Map<String, Object> getLeaveStats(LeaveQueryDTO dto);
}
