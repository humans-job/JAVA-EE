package org.example.army.militaryleavereport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl; // 导入 ServiceImpl
import org.example.army.militarycommon.Entity.deptRelation;
import org.example.army.militarycommon.Entity.leaveReport;
import org.example.army.militarycommon.Entity.militiaInfo;
import org.example.army.militarycommon.mapper.DeptRelationMapper;
import org.example.army.militarycommon.mapper.LeaveReportMapper;
import org.example.army.militarycommon.mapper.MilitiaInfoMapper;
import org.example.army.militaryleavereport.DTO.*;
import org.example.army.militaryleavereport.service.impl.LeaveServiceImpl;
import org.junit.jupiter.api.BeforeEach; // 导入 BeforeEach
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field; // 导入反射 Field
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceUnitTest {

    @InjectMocks
    private LeaveServiceImpl leaveService;

    @Mock
    private LeaveReportMapper leaveReportMapper;

    @Mock
    private MilitiaInfoMapper militiaInfoMapper;

    @Mock
    private DeptRelationMapper deptRelationMapper;

    /**
     * 关键修复：手动注入 baseMapper
     * 解决 com.baomidou.mybatisplus.core.exceptions.MybatisPlusException: baseMapper can not be null
     */
    @BeforeEach
    void setUp() throws Exception {
        // 利用反射获取 ServiceImpl 的 baseMapper 字段
        // leaveService 是 ServiceImpl 的子类对象
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        // 将我们 Mock 出的 leaveReportMapper 设置给 leaveService 父类的 baseMapper
        baseMapperField.set(leaveService, leaveReportMapper);
    }

    @Test
    @DisplayName("测试提交请假 - 正常流程自动匹配审批单位")
    void testSubmitLeave_Success() {
        // 准备数据
        LeaveSubmitDTO dto = new LeaveSubmitDTO();
        dto.setUserId(1001L);
        dto.setReason("家里有事");
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        dto.setEndTime(LocalDateTime.now().plusDays(3));

        // Mock 1: 查找用户所属部门
        militiaInfo user = new militiaInfo();
        user.setUserId(1001L);
        user.setDeptId(200L); // 假设属于某连队
        when(militiaInfoMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        // Mock 2: 查找上级部门
        deptRelation relation = new deptRelation();
        relation.setChildId(200L);
        relation.setParentId(100L); // 假设上级是营部
        when(deptRelationMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(relation);

        // Mock 3: IService.save() 实际上调用的是 baseMapper.insert()
        when(leaveReportMapper.insert(any(leaveReport.class))).thenReturn(1);

        // 执行
        leaveService.submitLeave(dto);

        // 验证
        ArgumentCaptor<leaveReport> captor = ArgumentCaptor.forClass(leaveReport.class);
        verify(leaveReportMapper).insert(captor.capture());
        leaveReport savedLeave = captor.getValue();

        assertEquals(1001L, savedLeave.getUserId());
        assertEquals(200L, savedLeave.getDeptId());
        assertEquals(100L, savedLeave.getApproveDept()); // 验证自动找到了上级
        assertEquals(0, savedLeave.getStatus()); // 验证初始状态为0
    }

    @Test
    @DisplayName("测试民兵销假 - 只有状态为1(待销假)时才能销假")
    void testReportBack_Success() {
        // 准备数据
        LeaveReportBackDTO dto = new LeaveReportBackDTO();
        dto.setLeaveId(55L);
        dto.setReportBackLocation("营区大门定位点");

        // Mock: 数据库中存在状态为1的记录
        leaveReport existingLeave = new leaveReport();
        existingLeave.setLeaveId(55L);
        existingLeave.setStatus(1);
        when(leaveReportMapper.selectById(55L)).thenReturn(existingLeave);

        when(leaveReportMapper.updateById(any(leaveReport.class))).thenReturn(1);

        // 执行
        leaveService.reportBack(dto);

        // 验证
        verify(leaveReportMapper).updateById(existingLeave);
        assertNotNull(existingLeave.getReportBackTime()); // 验证时间被填充 (注意你的实体类拼写是 Bcak)
        assertEquals("营区大门定位点", existingLeave.getReportBackLocation());
        assertEquals(1, existingLeave.getStatus()); // 销假动作本身不改状态，等管理员确认
    }

    @Test
    @DisplayName("测试民兵销假 - 状态不正确时抛出异常")
    void testReportBack_Fail_WrongStatus() {
        LeaveReportBackDTO dto = new LeaveReportBackDTO();
        dto.setLeaveId(55L);

        // Mock: 数据库中记录状态为0 (还没审批通过)
        leaveReport existingLeave = new leaveReport();
        existingLeave.setLeaveId(55L);
        existingLeave.setStatus(0);
        when(leaveReportMapper.selectById(55L)).thenReturn(existingLeave);

        // 执行 & 断言
        assertThrows(RuntimeException.class, () -> leaveService.reportBack(dto));
    }

    @Test
    @DisplayName("测试管理员确认销假 - 最终归档")
    void testConfirmReportBack() {
        // 假设的DTO，因为附件没提供这个类，如果报错请根据实际类修改
        LeaveConfirmDTO dto = new LeaveConfirmDTO();
        dto.setLeaveId(55L);
        dto.setConfirmBy(999L);

        leaveReport existingLeave = new leaveReport();
        existingLeave.setLeaveId(55L);
        existingLeave.setStatus(1);

        when(leaveReportMapper.selectById(55L)).thenReturn(existingLeave);
        when(leaveReportMapper.updateById(any(leaveReport.class))).thenReturn(1);

        leaveService.confirmReportBack(dto);

        assertEquals(3, existingLeave.getStatus()); // 验证状态变为3 (已归档)
        assertEquals(999L, existingLeave.getReportBackConfirmDept());
    }
}


