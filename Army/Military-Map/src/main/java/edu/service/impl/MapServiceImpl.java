package edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.army.militarycommon.Entity.Dept;
import edu.DTO.MapSituationDTO;
import org.example.army.militarycommon.mapper.DeptMapper;
import edu.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapServiceImpl implements MapService {

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public List<MapSituationDTO> getSituationData(Long currentDeptId, Integer targetType) {
        LambdaQueryWrapper<Dept> wrapper = new LambdaQueryWrapper<>();

        // 1. 筛选目标层级 (例如：只看"团"级单位)
        if (targetType != null) {
            wrapper.eq(Dept::getDeptType, targetType);
        }

        // 2. 权限范围筛选 (核心)
        // 只能看自己管辖范围内的单位。
        // SQL 逻辑：WHERE ... AND (id = currentDeptId OR ancestors LIKE '%,currentDeptId,%')
        if (currentDeptId != null) {
            wrapper.and(w -> w.eq(Dept::getDeptId, currentDeptId)
                    .or()
                    .like(Dept::getAncestors, "," + currentDeptId + ",") // 确保匹配的是完整ID段
            );
        }

        // 3. 执行查询
        List<Dept> deptList = deptMapper.selectList(wrapper);

        // 4. 转换 Entity -> DTO (处理 JTS Geometry 到 JSON 的转换)
        return deptList.stream()
                .map(dept -> MapSituationDTO.fromEntity(
                        dept.getDeptId(),
                        dept.getDeptName(),
                        dept.getDeptType(),
                        dept.getRegionCenter(),
                        dept.getRegionShape()
                ))
                .collect(Collectors.toList());
    }
}

