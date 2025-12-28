package org.example.army.militaryleavereport;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.army.militaryleavereport.DTO.LeaveSubmitDTO;
import org.example.army.militaryleavereport.controller.LeaveController;
import org.example.army.militaryleavereport.service.impl.LeaveServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource; // SpringBoot 2.x 使用 javax.sql, 3.x 使用 jakarta.sql，请根据版本调整
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = LeaveController.class,
        excludeAutoConfiguration = {
                // 1. 排除安全认证
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                // 2. 排除数据库和MyBatis-Plus自动配置 (关键修复！)
                DataSourceAutoConfiguration.class,
                MybatisPlusAutoConfiguration.class
        }
)
// 3. 避免扫描到其他的配置类（如全局异常处理中可能引用了Dao）
@AutoConfigureMockMvc(addFilters = false)
public class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock Service层，只测Controller逻辑
    @MockBean
    private LeaveServiceImpl leaveService;

    // 4. 兜底 Mock：如果项目中还有组件强行依赖 DataSource，这个 Mock 可以防止报错
    @MockBean
    private DataSource dataSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("接口测试：提交请假申请 /api/work/leave/submit")
    void testSubmitApi() throws Exception {
        // 构造请求体
        LeaveSubmitDTO dto = new LeaveSubmitDTO();
        dto.setUserId(1L);
        dto.setReason("API测试请假");
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusDays(1));

        // Mock Service 行为 (void方法用 doNothing)
        doNothing().when(leaveService).submitLeave(any(LeaveSubmitDTO.class));

        // 发送 POST 请求
        mockMvc.perform(post("/api/work/leave/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk()) // 期望 200 OK
                .andExpect(jsonPath("$.code").value(200)) // 验证返回JSON中的 code
                .andExpect(jsonPath("$.msg").value("申请提交成功"));
    }
}




