package org.example.army.militarymanage.dto.militia;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.example.army.militarymanage.util.CustomLocalDateTimeDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MilitiaUpdateReq  {// 明确指定主键列名，如果是雪花算法用 ASSIGN_ID
    private Long id;
    private Long userId;
    private Long deptId;
    private String name;
    private String idCard;
    private String phone;
    private String address;
    private String politicStatus;

    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;
    private Integer auditStatus;
    private String auditFeedback;
    private Long auditDept;
    private Long createDept;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
