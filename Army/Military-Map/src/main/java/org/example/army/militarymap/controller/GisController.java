package org.example.army.militarymap.controller;

import org.example.army.militarymap.DTO.DeptGisDTO;
import org.example.army.militarymap.service.IGisService;
import org.example.army.militaryauthenticate.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gis")
public class GisController {

    @Autowired
    private IGisService gisService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 更新本级管辖区域
     * deptId 从 Token/Redis 中解析，防止越权修改他人区域
     */
    @PutMapping("/region")
    public String updateRegion(@RequestBody DeptGisDTO dto) {
        // 从 Redis 获取当前登录用户的 DeptId
        Long deptId = securityUtil.getDeptId();
        if (deptId == null) {
            return "更新失败：无法获取当前用户的部门信息";
        }

        // 简单参数校验 (不再依赖前端传入的 deptId)
        if (dto.getRegionShape() == null) {
            return "参数错误：需要区域形状";
        }

        // 使用从 SecurityUtil 获取的 deptId 进行更新
        gisService.updateDeptRegion(deptId, dto);
        return "更新成功";
    }

    /**
     * 获取当前部门的区域数据（用于地图回显）
     * 修改说明：
     * 1. URL 从 "/region/{deptId}" 修改为 "/region" (不再接收路径参数)
     * 2. deptId 改为由 SecurityUtil 自动获取
     */
    @GetMapping("/region")
    public DeptGisDTO getRegion() {
        // 自动抓取当前登录用户的部门ID
        Long deptId = securityUtil.getDeptId();
        return gisService.getDeptGisData(deptId);
    }

    @GetMapping("/situation/subordinates")
    public List<DeptGisDTO> getSubordinates() {

        Long parentId = securityUtil.getDeptId();
            if (parentId == null) {
                throw new RuntimeException("无法获取当前用户的部门信息");
            }
        return gisService.getSubordinateSituation(parentId);
    }

    /**
     * 兵团宏观视角：分层加载全疆地图
     * URL示例: /api/gis/situation/layer?type=30
     */
    @GetMapping("/situation/layer")
    public List<DeptGisDTO> getLayerData(@RequestParam Integer type) {
        // 真实场景下，此处应校验当前用户是否为“兵团级(10)”用户
        // 只有兵团级用户才有权限查看全疆数据

        return gisService.getSituationByLayer(type);
    }
}


