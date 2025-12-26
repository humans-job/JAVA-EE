package edu.dto.militia;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.util.CustomLocalDateTimeDeserializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class MilitiaImportItem {
    private String idCard;
    private String name;
    private String phone;
    private String address;
    private String politicStatus;

    /**
     * 前端如果传字符串，建议："yyyy-MM-dd"。
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime joinTime;
}
