package org.example.army.militarycommon.Entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("biz_militia_info")
public class militiaInfo  {
    @TableId(value = "id", type = IdType.AUTO) // 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long id;
    private Long userId;
    private Long deptId;
    private String idCard;
    private String phone;
    private String address;
    private String politicStstus;
    private Date joinTime;
    private int auditStatus;
    private String auditFeedback;
    private Long auditDept;
    private Long createDept;
    private Date createTime;
}
