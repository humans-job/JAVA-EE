package edu.service;
import edu.DTO.DeptGisDTO;

import java.util.List;

public interface IGisService {
    /**
     * 更新部门的管辖区域
     * @param deptId 部门ID
     * @param dto 前端传来的GIS数据
     */
    void updateDeptRegion(Long deptId, DeptGisDTO dto);

    /**
     * 获取部门的GIS信息
     * @param deptId 部门ID
     * @return DTO
     */
    DeptGisDTO getDeptGisData(Long deptId);
    /**
     * 查询直属下级单位的 GIS 态势
     * @param parentId 当前查看的父级单位ID
     * @return 下级单位列表（包含GIS数据）
     */
    List<DeptGisDTO> getSubordinateSituation(Long parentId);
    /**
     * 全疆态势：根据单位级别分层加载
     * @param deptType 单位级别 (如 30=团级)
     * @return 符合该级别的所有单位 GIS 信息
     */
    List<DeptGisDTO> getSituationByLayer(Integer deptType);

}

