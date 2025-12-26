package edu.vo.militia;

import lombok.Data;

import java.util.List;

@Data
public class MilitiaImportResp {
    private int total;
    private int success;
    private int fail;
    private List<MilitiaImportFailItem> failList;
}
