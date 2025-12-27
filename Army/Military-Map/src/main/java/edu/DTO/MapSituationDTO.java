package edu.DTO;

import lombok.Data;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class MapSituationDTO implements Serializable {
    private Long deptId;
    private String deptName;
    private Integer deptType; // 1=兵团, 2=师...

    // 中心点 (用于放置标注/Label)
    private Map<String, Double> center;

    // 边界点集合 (用于绘制多边形覆盖物)
    private List<Map<String, Double>> boundary;

    // 静态工具方法：将 Entity 转为 DTO
    public static MapSituationDTO fromEntity(Long deptId, String name, Integer type, Point centerPoint, Geometry regionShape) {
        MapSituationDTO dto = new MapSituationDTO();
        dto.setDeptId(deptId);
        dto.setDeptName(name);
        dto.setDeptType(type);

        // 1. 处理中心点 (Point -> {lng, lat})
        if (centerPoint != null) {
            Map<String, Double> c = new HashMap<>();
            c.put("lng", centerPoint.getX());
            c.put("lat", centerPoint.getY());
            dto.setCenter(c);
        }

        // 2. 处理区域形状 (Geometry -> List<{lng, lat}>)
        // 百度地图通常需要一个闭合的经纬度数组
        if (regionShape != null) {
            List<Map<String, Double>> points = new ArrayList<>();
            for (Coordinate coord : regionShape.getCoordinates()) {
                Map<String, Double> p = new HashMap<>();
                p.put("lng", coord.x);
                p.put("lat", coord.y);
                points.add(p);
            }
            dto.setBoundary(points);
        }

        return dto;
    }
}

