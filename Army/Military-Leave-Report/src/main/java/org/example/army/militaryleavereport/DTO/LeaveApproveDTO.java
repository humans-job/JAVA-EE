package org.example.army.militaryleavereport.DTO;
import lombok.Data;

@Data
public class LeaveApproveDTO {
    private Long leaveId;
    private Integer status;     // 1 = 通过 / 2 = 驳回
    private Long approveDeptId;     // 审批人/部门ID

    // 注意：你的实体类中 approveOpinion 是 Long 类型，建议改为 String。
    // 这里暂时定义为 String，Service 层处理时需要注意类型转换或修改实体类
    private String approveOpinion;
    }

