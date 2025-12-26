package edu.dto.militia;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MilitiaSubmitAuditReq {
    @NotEmpty
    private List<Long> ids;
}
