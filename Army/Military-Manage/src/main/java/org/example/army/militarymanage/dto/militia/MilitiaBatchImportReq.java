package org.example.army.militarymanage.dto.militia;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MilitiaBatchImportReq {
    /** 前端把 Excel 解析成 JSON 后传入 */
    @NotEmpty
    private List<MilitiaImportItem> data;
}
