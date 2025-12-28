package org.example.army.militarycommon.Entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.example.army.militarycommon.handler.MysqlGeometryTypeHandler;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import lombok.Data;

@Data
@TableName(value = "sys_dept", autoResultMap = true)
public class Dept {
    @TableId(value = "dept_id", type = IdType.AUTO) // 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long deptId;
    private String deptName;
    private Long parentId;
    private Integer deptType;
    private String shapeType;
    @TableField(value = "region_shape", typeHandler = MysqlGeometryTypeHandler.class)
    private Geometry regionShape;

    @TableField(value = "region_center", typeHandler = MysqlGeometryTypeHandler.class)
    private Point regionCenter;
}
