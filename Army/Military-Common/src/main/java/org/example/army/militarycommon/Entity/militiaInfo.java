package org.example.army.militarycommon.Entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("biz_notice")
public class militiaInfo  {
    @TableId(value = "id", type = IdType.AUTO) // 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long id;
    private Long userId;
    private Long deptId;
    private String id_card;
    private String phone;
    private String address;
    private String politic_ststus;
    private Date join_time;
    private int audit_status;
    private String audit_feedback;
    private Long audit_dept;
    private Long create_dept;
    private Date create_time;
}
