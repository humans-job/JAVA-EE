package org.example.army.militarycommon.Entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import lombok.Data;

@Data
@TableName("sys_dept")
public class Dept {
    @TableId(value = "id", type = IdType.AUTO) // 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long deptId;
    private Long parentId;
    private String deptName;
    private Integer deptType;
    private String ancestors;
    private Integer sortOrder;
    private Geometry regionShape;
    private Point regionCenter;
    private Integer regionRadius;
}
