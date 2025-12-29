package org.example.army.militarymanage.controller;

import org.example.army.militarymanage.common.ApiResp;
import org.example.army.militarymanage.dto.militia.MilitiaAuditReq;
import org.example.army.militarymanage.dto.militia.MilitiaBatchImportReq;
import org.example.army.militarymanage.dto.militia.MilitiaSubmitAuditReq;
import org.example.army.militarymanage.dto.militia.MilitiaUpdateReq;
import org.example.army.militarymanage.service.MilitiaArchiveService;
import org.example.army.militarymanage.vo.militia.MilitiaImportResp;
import org.example.army.militarymanage.vo.militia.MilitiaListResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.army.militarymanage.vo.common.IdResp;
import org.springframework.web.bind.annotation.*;

/**
 * 模块二：民兵档案管理
 */
@RestController
@RequestMapping("/api/militia/archive")
@RequiredArgsConstructor
public class MilitiaArchiveController {

    private final MilitiaArchiveService militiaArchiveService;

    /**
     * 批量导入：Excel -> JSON
     */
    @PostMapping("/import")
    public ApiResp<MilitiaImportResp> batchImport(@Valid @RequestBody MilitiaBatchImportReq req) {
        try {
            MilitiaImportResp r = militiaArchiveService.batchImport(req);
            String msg = String.format("导入成功，共%d条，成功%d条", r.getTotal(), r.getSuccess());
            return ApiResp.ok(msg, r);
        } catch (Exception e) {
            return ApiResp.fail(e.getMessage() == null ? "导入失败" : e.getMessage());
        }
    }

    /**
     * 团机关提交师部审核
     */
    @PostMapping("/submit")
    public ApiResp<Void> submit(@Valid @RequestBody MilitiaSubmitAuditReq req) {
        try {
            militiaArchiveService.submitToDivision(req);
            return ApiResp.ok("提交成功，待师部审核", null);
        } catch (Exception e) {
            return ApiResp.fail(e.getMessage() == null ? "提交失败" : e.getMessage());
        }
    }

    /**
     * 师部审核：2=通过, 3=驳回
     */
    @PostMapping("/audit")
    public ApiResp<Void> audit(@Valid @RequestBody MilitiaAuditReq req) {
        try {
            militiaArchiveService.divisionAudit(req);
            return ApiResp.ok("审核完成", null);
        } catch (Exception e) {
            return ApiResp.fail(e.getMessage() == null ? "审核失败" : e.getMessage());
        }
    }

    /**
     * 查询档案列表
     */
    @GetMapping("/list")
    public ApiResp<MilitiaListResp> list(
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) String idCard,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        try {
            return ApiResp.ok("查询成功", militiaArchiveService.list(deptId, auditStatus, idCard, pageNum, pageSize));
        } catch (Exception e) {
            return ApiResp.fail(e.getMessage() == null ? "查询失败" : e.getMessage());
        }
    }

    /**
     * 编辑档案
     */
    @PutMapping("/update")
    public ApiResp<IdResp> update(@RequestBody MilitiaUpdateReq req) {
        try {
            Long id = militiaArchiveService.update(req);
            return ApiResp.ok("编辑成功", new IdResp(id));
        } catch (Exception e) {
            return ApiResp.fail(e.getMessage() == null ? "编辑失败" : e.getMessage());
        }
    }

    /**
     * 删除档案
     */
    @DeleteMapping("/delete/{id}")
    public ApiResp<Void> delete(@PathVariable Long id) {
        try {
            militiaArchiveService.delete(id);
            return ApiResp.ok("删除成功", null);
        } catch (Exception e) {
            return ApiResp.fail(e.getMessage() == null ? "删除失败" : e.getMessage());
        }
    }
}
