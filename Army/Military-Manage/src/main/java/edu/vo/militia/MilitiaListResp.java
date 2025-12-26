package edu.vo.militia;

import lombok.Data;
import org.example.army.militarycommon.Entity.militiaInfo;

import java.util.List;

@Data
public class MilitiaListResp {
    private List<militiaInfo> list;
    private long total;
}
