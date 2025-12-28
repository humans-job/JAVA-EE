package edu;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.DTO.DeptGisDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // 开启虚拟MVC调用
@Transactional // 自动回滚，保证测试数据不污染数据库
public class GisModuleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // JTS工厂，用于构造测试数据
    private final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * 测试场景1：获取单个单位的 GIS 信息
     * URL: GET /api/gis/region/{deptId}
     * 数据准备: ID=200 (第一师) 在初始化脚本中已存在
     */
    @Test
    @DisplayName("接口测试：获取指定单位GIS数据")
    public void testGetRegion() throws Exception {
        mockMvc.perform(get("/api/gis/region/200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()) // 打印响应日志
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deptId", is(200)))
                .andExpect(jsonPath("$.deptName", is("第一师")))
                // 验证 GeoJSON 结构是否正确返回
                .andExpect(jsonPath("$.regionShape.type", is("Polygon")))
                .andExpect(jsonPath("$.regionCenter.type", is("Point")));
    }

    /**
     * 测试场景2：战术视角 - 查询下级单位
     * URL: GET /api/gis/situation/subordinates?parentId=200
     * 预期: 第一师(200) 下面应该有两个团 (301, 302)
     */
    @Test
    @DisplayName("接口测试：战术视角(直属下级)")
    public void testGetSubordinates() throws Exception {
        mockMvc.perform(get("/api/gis/situation/subordinates")
                        .param("parentId", "200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // 应该是一个长度为2的数组
                .andExpect(jsonPath("$[0].regionShape").exists()); // 确保包含地图数据
    }

    /**
     * 测试场景3：战略视角 - 分层加载全疆团级单位
     * URL: GET /api/gis/situation/layer?type=30
     * 预期: 数据库里有两个类型为30的团
     */
    @Test
    @DisplayName("接口测试：战略视角(全疆分层)")
    public void testGetLayerData() throws Exception {
        mockMvc.perform(get("/api/gis/situation/layer")
                        .param("type", "30") // 30=团级
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // 验证数量
                .andExpect(jsonPath("$[0].deptType", is(30)));
    }

    /**
     * 测试场景4：数据采集 - 更新区域形状
     * URL: PUT /api/gis/region
     * 描述: 模拟前端绘制一个多边形并保存
     */
    @Test
    @DisplayName("接口测试：绘制并更新区域")
    public void testUpdateRegion() throws Exception {
        // 1. 构造前端传来的 DTO (GeoJSON)
        Long targetDeptId = 401L; // 特战营

        // 构造一个矩形 (前端传来的通常不带 SRID，或者 SRID=0，这是正常的)
        Coordinate[] coords = new Coordinate[] {
                new Coordinate(86.0, 42.0),
                new Coordinate(87.0, 42.0),
                new Coordinate(87.0, 43.0),
                new Coordinate(86.0, 43.0),
                new Coordinate(86.0, 42.0)
        };
        Polygon polygon = geometryFactory.createPolygon(coords);
        Point center = geometryFactory.createPoint(new Coordinate(86.5, 42.5));

        DeptGisDTO dto = new DeptGisDTO();
        dto.setDeptId(targetDeptId);
        dto.setRegionShape(polygon);
        dto.setRegionCenter(center);

        // 将对象转为 JSON 字符串
        String jsonContent = objectMapper.writeValueAsString(dto);

        // 2. 发送 PUT 请求
        mockMvc.perform(put("/api/gis/region")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk()); // 期望返回 200

        // 3. 再次查询验证是否保存成功 (二次校验)
        mockMvc.perform(get("/api/gis/region/" + targetDeptId))
                .andExpect(status().isOk())
                // 验证存进去的坐标 (验证 Service 层是否自动加上了 SRID 4326 并正确入库)
                .andExpect(jsonPath("$.regionShape.coordinates[0][0][0]", is(86.0)));
    }
}

