package org.example.army.militarycommon.Entity; // 建议包名全小写

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表实体
 * 对应数据库表：sys_user
 */
@Data
@TableName("sys_user")
public class User implements Serializable {
    @TableId(value = "user_id", type = IdType.AUTO) // 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long userId;
    private Long deptId;
    private String username;
    @JsonIgnore
    private String password;
    private Integer userType;
    private Integer status;
    private String usbKey;
    private String certSn;
    private String loginIp;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

}

