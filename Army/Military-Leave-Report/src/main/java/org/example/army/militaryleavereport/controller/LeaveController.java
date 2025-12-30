package org.example.army.militaryleavereport.controller;

import org.example.army.militaryleavereport.DTO.*;
import org.example.army.militaryleavereport.service.impl.LeaveServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/work/leave")
public class LeaveController {

    @Autowired
    private LeaveServiceImpl leaveService;

    private Map<String, Object> result(int code, String msg, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        if (data != null) map.put("data", data);
        return map;
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody LeaveSubmitDTO dto) {
        leaveService.submitLeave(dto);
        return result(200, "申请提交成功", null);
    }

    /**
     * 重新提交请假（驳回后修改）
     */
    @PutMapping("/resubmit")
    public Map<String, Object> resubmit(@RequestBody LeaveResubmitDTO dto) {
        leaveService.resubmitLeave(dto);
        return result(200, "重新提交成功", null);
    }

    @PostMapping("/approve")
    public Map<String, Object> approve(@RequestBody LeaveApproveDTO dto) {
        leaveService.approveLeave(dto);
        return result(200, "审批完成", null);
    }

    /**
     * 民兵销假打卡
     * 状态: 1(待销假) → 3(已销假待确认)
     */
    @PutMapping("/report_back")
    public Map<String, Object> reportBack(@RequestBody LeaveReportBackDTO dto) {
        leaveService.reportBack(dto);
        return result(200, "销假打卡成功，等待管理员确认归档", null);
    }

    /**
     * 管理员确认归档
     * 状态: 3(已销假待确认) → 4(已归档)
     */
    @PostMapping("/confirm")
    public Map<String, Object> confirm(@RequestBody LeaveConfirmDTO dto) {
        leaveService.confirmReportBack(dto);
        return result(200, "归档成功", null);
    }

    @GetMapping("/list")
    public Map<String, Object> list(LeaveQueryDTO dto) {
        Map<String, Object> data = leaveService.getLeaveStats(dto);
        return result(200, "查询成功", data);
    }
}


