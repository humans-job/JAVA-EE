package edu.controller;

import edu.DTO.DeptGisDTO;
import edu.service.IGisService;
// 假设你有统一的 Result 封装，如果没有请直接返回 DeptGisDTO
// import org.example.army.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gis")
public class GisController {

    @Autowired
    private IGisService gisService;

    /**
     * 更新本级管辖区域
     * 实际场景中，deptId 应该从 Token 中解析，防止越权修改他人区域
     * 这里为了测试方便，暂时作为参数传入
     */
    @PutMapping("/region")
    public String updateRegion(@RequestBody DeptGisDTO dto) {
        // 简单参数校验
        if (dto.getDeptId() == null || dto.getRegionShape() == null) {
            return "参数错误：需要部门ID和区域形状";
        }

        gisService.updateDeptRegion(dto.getDeptId(), dto);
        return "更新成功";
    }

    /**
     * 获取指定部门的区域数据（用于地图回显）
     */
    @GetMapping("/region/{deptId}")
    public DeptGisDTO getRegion(@PathVariable Long deptId) {
        return gisService.getDeptGisData(deptId);
    }

    @GetMapping("/situation/subordinates")
    public List<DeptGisDTO> getSubordinates(@RequestParam(required = false) Long parentId) {
        // 如果前端没传 parentId，通常应该从 Token 获取当前登录用户的 DeptId
        // 这里为了演示方便，假设必传或默认为 1
        if (parentId == null) {
            // TODO: 获取当前用户 DeptId
            parentId = 1L;
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

