package edu.vo.militia;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MilitiaImportFailItem {
    private String idCard;
    private String reason;
}
