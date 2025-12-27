package edu.controller;

import edu.DTO.MapSituationDTO;
import edu.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private MapService mapService;

    /**
     * 获取态势感知 GIS 数据
     * GET /api/map/situation?deptId=100&deptType=3
     */
    @GetMapping("/situation")
    public Map<String, Object> getSituation(
            @RequestParam Long deptId,      // 当前登录用户的部门ID
            @RequestParam Integer deptType  // 想查看的层级
    ) {
        List<MapSituationDTO> list = mapService.getSituationData(deptId, deptType);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "查询成功");
        result.put("data", list);

        return result;
    }
}

