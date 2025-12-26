package edu.dto.militia;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.util.CustomLocalDateTimeDeserializer;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime joinTime;
    private Integer auditStatus;
    private String auditFeedback;
    private Long auditDept;
    private Long createDept;
    private LocalDateTime createTime;
}
