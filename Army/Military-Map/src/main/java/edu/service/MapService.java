package edu.service;

import edu.DTO.MapSituationDTO;
import java.util.List;

public interface MapService {
    /**
     * 查询态势感知数据
     * @param currentDeptId 当前登录用户的部门ID
     * @param targetType    目标显示的层级 (1=兵团, 2=师, 3=团...)
     * @return GIS数据列表
     */
    List<MapSituationDTO> getSituationData(Long currentDeptId, Integer targetType);
}

