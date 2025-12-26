package edu.dto.militia;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MilitiaAuditReq {
    @NotNull
    private Long id;

    /** 2=通过, 3=驳回 */
    @NotNull
    private Integer auditStatus;

    /** 驳回必填 */
    private String auditFeedback;
}
