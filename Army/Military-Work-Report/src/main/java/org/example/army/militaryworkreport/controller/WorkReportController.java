package org.example.army.militaryworkreport.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.army.militarycommon.Entity.workReport;
import org.example.army.militaryworkreport.DTO.ReportApproveDTO;
import org.example.army.militaryworkreport.DTO.ReportQueryDTO;
import org.example.army.militaryworkreport.DTO.ReportSubmitDTO;
import org.example.army.militaryworkreport.service.WorkReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 模块四：工作与报表管理 控制层
 */
@RestController
@RequestMapping("/api/work/report") // 定义统一的基础路由
public class WorkReportController {

    @Autowired
    private WorkReportService workReportService;

    /**
     * 辅助方法：统一返回结果格式
     * 实际项目中通常使用全局 Result<T> 类
     */
    private Map<String, Object> result(int code, String msg, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        if (data != null) {
            map.put("data", data);
        }
        return map;
    }

    /**
     * 接口1：提交报表
     * 请求方式：POST
     * 路径：/api/work/report/submit
     */
    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody ReportSubmitDTO dto) {
        // 调用 Service 层逻辑
        workReportService.submitReport(dto);
        return result(200, "提交成功", null);
    }

    /**
     * 接口2：审批报表
     * 请求方式：POST
     * 路径：/api/work/report/approve
     */
    @PostMapping("/approve")
    public Map<String, Object> approve(@RequestBody ReportApproveDTO dto) {
        // 调用 Service 层逻辑
        workReportService.approveReport(dto);
        return result(200, "审批完成", null);
    }

    /**
     * 接口3：查询报表列表
     * 请求方式：GET
     * 路径：/api/work/report/list
     * 说明：GET 请求的参数会自动映射到 ReportQueryDTO 对象中（如 ?deptId=1&status=0）
     */
    @GetMapping("/list")
    public Map<String, Object> list(ReportQueryDTO dto) {
        // 调用 Service 获取分页结果
        Page<workReport> pageResult = workReportService.queryReports(dto);

        // 封装返回数据 data: { list: [...], total: 50 }
        Map<String, Object> data = new HashMap<>();
        data.put("list", pageResult.getRecords());
        data.put("total", pageResult.getTotal());

        return result(200, "查询成功", data);
    }
    /**
     * 接口4：查看报表详情
     * 请求方式：GET
     * 路径：/api/work/report/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> detail(@PathVariable Long id) {
        // 修改点：调用自定义的业务方法，包含权限校验
        workReport report = workReportService.getReportDetail(id);
        return result(200, "查询成功", report);
    }

}

