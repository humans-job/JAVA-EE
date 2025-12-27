package org.example.army.militarycommon.Entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_dept_belong")
public class deptRelation {
    @TableField("child_id") // 假设id是复合主键的一部分
    private Long childId;// 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    @TableField("parent_id") // 假设id是复合主键的一部分
    private Long parentId;
}

