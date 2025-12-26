package edu.dto.militia;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MilitiaBatchImportReq {
    /** 前端把 Excel 解析成 JSON 后传入 */
    @NotEmpty
    private List<MilitiaImportItem> data;
}
