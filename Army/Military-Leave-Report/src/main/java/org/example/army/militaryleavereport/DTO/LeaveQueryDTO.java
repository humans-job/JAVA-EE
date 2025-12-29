package org.example.army.militaryleavereport.DTO;
import lombok.Data;

@Data
public class LeaveQueryDTO {
    private String month;       // 格式 "2025-12"
    private Integer status;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /**
     * 查询类型：
     * 1 (或 null) = 查下属 (管理员用)
     * 2 = 查自己 (民兵用)
     */
    private Integer queryType;
    // 是否只看待销假确认的 (用于管理员界面过滤已打卡但未归档的人)
    private Boolean onlyWaitConfirm;
}

