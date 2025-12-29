package org.example.army.militarymap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.army.militarymap.DTO.DeptGisDTO;
import org.example.army.militarycommon.Entity.Dept;
import org.example.army.militarycommon.Entity.deptRelation;
import org.example.army.militarycommon.mapper.DeptMapper;
import org.example.army.militarymap.service.IGisService;
import org.example.army.militarycommon.mapper.DeptRelationMapper;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GisServiceImpl implements IGisService {

    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private DeptRelationMapper deptRelationMapper; // 注入关系表 Mapper

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeptRegion(Long deptId, DeptGisDTO dto) {
        // 1. 创建一个只包含需要更新字段的实体对象
        Dept dept = new Dept();
        dept.setDeptId(deptId); // 设置主键，作为更新条件

        // 2. 设置几何字段 (MyBatis-Plus 会自动检测非空字段进行更新)
        if (dto.getRegionShape() != null) {
            dto.getRegionShape().setSRID(4326);

            dept.setRegionShape(dto.getRegionShape());
            dept.setShapeType(dto.getRegionShape().getGeometryType());
        }

        // 3. 处理中心点逻辑
        if (dto.getRegionCenter() != null) {
            dept.setRegionCenter(dto.getRegionCenter());
        } else if (dto.getRegionShape() != null) {
            // 如果前端没传中心点，后端自动计算几何质心
            Point centroid = dto.getRegionShape().getCentroid();
            dept.setRegionCenter(centroid);
        }

        // 4. 执行更新 (关键点：使用 updateById)
        // updateById 会读取 Dept 类上的 @TableField(typeHandler=...) 配置
        // 从而正确调用 MySqlGeometryTypeHandler 将 Geometry 转为 byte[]
        deptMapper.updateById(dept);
    }


    @Override
    public DeptGisDTO getDeptGisData(Long deptId) {
        Dept dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new RuntimeException("部门不存在");
        }
        // 使用第一步中定义的 DTO 静态方法进行转换
        return DeptGisDTO.fromEntity(dept);
    }

    @Override
    public List<DeptGisDTO> getSubordinateSituation(Long parentId) {
        // 1. 查询关系表，找到所有直属下级的 ID
        LambdaQueryWrapper<deptRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(deptRelation::getParentId, parentId);

        List<deptRelation> relations = deptRelationMapper.selectList(relationWrapper);

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 提取 ID 列表
        List<Long> childIds = relations.stream()
                .map(deptRelation::getChildId)
                .collect(Collectors.toList());

        // 3. 根据 ID 列表批量查询部门详情 (MyBatis Plus 提供的 selectBatchIds)
        List<Dept> childDepts = deptMapper.selectBatchIds(childIds);

        // 4. 将 Entity 转换为 DTO 返回
        return childDepts.stream()
                .map(DeptGisDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeptGisDTO> getSituationByLayer(Integer deptType) {
        LambdaQueryWrapper<Dept> queryWrapper = new LambdaQueryWrapper<>();

        // 1. 筛选特定级别
        queryWrapper.eq(Dept::getDeptType, deptType);

        // 2. 修正逻辑：( 有形状 OR 有中心点 )
        // 使用 .and(wrapper -> ...) 来加括号
        queryWrapper.and(wrapper ->
                wrapper.isNotNull(Dept::getRegionShape)
                        .or()
                        .isNotNull(Dept::getRegionCenter)
        );

        List<Dept> depts = deptMapper.selectList(queryWrapper);

        return depts.stream()
                .map(DeptGisDTO::fromEntity)
                .collect(Collectors.toList());
    }

}

