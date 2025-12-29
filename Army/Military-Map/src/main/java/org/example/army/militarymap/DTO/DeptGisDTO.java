package org.example.army.militarymap.DTO;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.example.army.militarycommon.Entity.Dept;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.n52.jackson.datatype.jts.GeometryDeserializer;

@Data
public class DeptGisDTO {
    private Long deptId;
    private String deptName;
    private Integer deptType;

    // 这里的 Geometry 对象会被 JacksonConfig 中的 JtsModule 自动转为 GeoJSON
    @JsonDeserialize(using = GeometryDeserializer.class)
    private Geometry regionShape;

    @JsonDeserialize(using = GeometryDeserializer.class)
    private Point regionCenter;

    /**
     * 将 Entity 转换为 DTO
     */
    public static DeptGisDTO fromEntity(Dept dept) {
        if (dept == null) return null;
        DeptGisDTO dto = new DeptGisDTO();
        dto.setDeptId(dept.getDeptId());
        dto.setDeptName(dept.getDeptName());
        dto.setDeptType(dept.getDeptType());
        dto.setRegionShape(dept.getRegionShape());
        dto.setRegionCenter(dept.getRegionCenter());
        return dto;
    }
}
