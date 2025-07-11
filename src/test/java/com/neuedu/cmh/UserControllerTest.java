package com.neuedu.cmh;

import com.neuedu.cmh.controller.UserController;
import com.neuedu.cmh.entity.User;
import com.neuedu.cmh.mapper.UserMapper;
import com.neuedu.cmh.util.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // getAllUserIds 测试
    @Test
    public void testGetAllUserIds_Success() {
        // 准备测试数据
        List<String> expectedIds = Arrays.asList("001", "002", "003");
        when(userMapper.getAllUser_ids()).thenReturn(expectedIds);

        // 执行测试
        List<String> actualIds = userController.getAllUserIds();

        // 验证结果
        assertEquals(expectedIds, actualIds);
        verify(userMapper, times(1)).getAllUser_ids();
    }

    @Test
    public void testGetAllUserIds_Empty() {
        // 准备测试数据
        when(userMapper.getAllUser_ids()).thenReturn(Collections.emptyList());

        // 执行测试
        List<String> actualIds = userController.getAllUserIds();

        // 验证结果
        assertTrue(actualIds.isEmpty());
        verify(userMapper, times(1)).getAllUser_ids();
    }

    // getUserInfo 测试
    @Test
    public void testGetUserInfo_Success() {
        // 准备测试数据
        String userId = "001";
        Map<String, Object> expectedInfo = new HashMap<>();
        expectedInfo.put("user_id", userId);
        expectedInfo.put("user_name", "张三");

        when(userMapper.getUserInfoById(userId)).thenReturn(expectedInfo);

        // 执行测试
        Map<String, Object> request = new HashMap<>();
        request.put("id", userId);
        Map<String, Object> actualInfo = userController.getUserInfo(request);

        // 验证结果
        assertEquals(expectedInfo, actualInfo);
        verify(userMapper, times(1)).getUserInfoById(userId);
    }

    @Test
    public void testGetUserInfo_NotFound() {
        // 准备测试数据
        String userId = "999";
        when(userMapper.getUserInfoById(userId)).thenReturn(null);

        // 执行测试
        Map<String, Object> request = new HashMap<>();
        request.put("id", userId);
        Map<String, Object> actualInfo = userController.getUserInfo(request);

        // 验证结果
        assertNotNull(actualInfo);
        assertNull(actualInfo.get("user_id"));
        verify(userMapper, times(1)).getUserInfoById(userId);
    }

    @Test
    public void testGetUserInfo_EmptyId() {
        // 准备测试数据
        String userId = "";
        when(userMapper.getUserInfoById(userId)).thenReturn(null);

        // 执行测试
        Map<String, Object> request = new HashMap<>();
        request.put("id", userId);
        Map<String, Object> actualInfo = userController.getUserInfo(request);

        // 验证结果
        assertNotNull(actualInfo);
        assertNull(actualInfo.get("user_id"));
        verify(userMapper, times(1)).getUserInfoById(userId);
    }

    @Test
    public void testGetUserInfo_NullId() {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();
        request.put("id", null);

        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            userController.getUserInfo(request);
        });
    }

    @Test
    public void testGetUserInfo_MissingId() {
        // 准备测试数据
        Map<String, Object> request = new HashMap<>();

        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            userController.getUserInfo(request);
        }, "当请求体中缺少 'id' 时，应抛出 IllegalArgumentException");
    }

    @Test
    public void testGetUserInfo_EmptyMap() {
        // 准备测试数据
        String userId = "002";
        when(userMapper.getUserInfoById(userId)).thenReturn(new HashMap<>());

        // 执行测试
        Map<String, Object> request = new HashMap<>();
        request.put("id", userId);
        Map<String, Object> actualInfo = userController.getUserInfo(request);

        // 验证结果
        assertNotNull(actualInfo);
        assertNull(actualInfo.get("user_id"));
        assertNull(actualInfo.get("user_name"));
        assertNull(actualInfo.get("user_nickname"));
        assertNull(actualInfo.get("dept_id"));
        assertNull(actualInfo.get("tenant_id"));
        assertNull(actualInfo.get("phonenum"));
        assertNull(actualInfo.get("mailbox"));
        assertNull(actualInfo.get("user_gender"));
        assertNull(actualInfo.get("user_job_id"));
        assertNull(actualInfo.get("createdate"));
        assertNull(actualInfo.get("user_comment"));
        assertNull(actualInfo.get("user_state"));
    }


    // updateUser 测试
    @Test
    public void testUpdateUser_Success() {
        // 准备测试数据
        User user = new User();
        user.setUser_id("001");
        user.setUser_nickname("新昵称");
        user.setDept_id("D001");
        user.setTenant_id("T001");
        user.setPhonenum("13800138000");
        user.setMailbox("test@example.com");
        user.setUser_gender("M");
        user.setUser_job_id("J001");
        user.setUser_comment("备注");
        user.setUser_role("admin");
        user.setUser_state("active");

        when(userMapper.updateUser(
                eq("001"), eq("新昵称"), eq("D001"), eq("T001"),
                eq("13800138000"), eq("test@example.com"), eq("M"),
                eq("J001"), eq("备注"), eq("admin"), eq("active")))
                .thenReturn(1);

        // 执行测试
        ResponseEntity<Map<String, String>> response = userController.updateUser(user);

        // 验证结果
        assertEquals("修改成功", response.getBody().get("status"));
        verify(userMapper, times(1)).updateUser(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testUpdateUser_Failure() {
        // 准备测试数据
        User user = new User();
        user.setUser_id("001");
        when(userMapper.updateUser(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        // 执行测试
        ResponseEntity<Map<String, String>> response = userController.updateUser(user);

        // 验证结果
        assertEquals("修改失败", response.getBody().get("status"));
    }

    @Test
    public void testUpdateUser_NullUser() {
        // 执行测试并验证异常
        assertThrows(NullPointerException.class, () -> {
            userController.updateUser(null);
        });
    }

    // addUser 测试
    @Test
    public void testAddUser_Success() {
        // 准备测试数据
        User user = new User();
        user.setUser_name("李四");
        user.setUser_nickname("昵称");
        user.setDept_id("D001");
        user.setTenant_id("T001");
        user.setPhonenum("13800138000");
        user.setMailbox("test@example.com");
        user.setUser_gender("F");
        user.setUser_job_id("J001");
        user.setUser_comment("备注");
        user.setUser_pwd("password");
        user.setUser_role("user");
        user.setUser_state("active");

        when(userMapper.findNextUser_id()).thenReturn("002");
        when(userMapper.insertUser(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        // 执行测试
        ResponseEntity<Map<String, String>> response = userController.addUser(user);

        // 验证结果
        assertEquals("添加成功", response.getBody().get("status"));
        assertEquals("002", response.getBody().get("user_id"));
        verify(userMapper, times(1)).findNextUser_id();
        verify(userMapper, times(1)).insertUser(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testAddUser_Failure() {
        // 准备测试数据
        User user = new User();
        user.setUser_name("李四");
        when(userMapper.findNextUser_id()).thenReturn("002");
        when(userMapper.insertUser(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        // 执行测试
        ResponseEntity<Map<String, String>> response = userController.addUser(user);

        // 验证结果
        assertEquals("添加失败", response.getBody().get("status"));
        assertNull(response.getBody().get("user_id"));
    }

    @Test
    public void testAddUser_NullUser() {
        // 执行测试并验证异常
        assertThrows(NullPointerException.class, () -> {
            userController.addUser(null);
        });
    }

    // searchUsers 测试
    @Test
    public void testSearchUsers_WithAllParams() {
        // 准备测试数据
        List<User> expectedUsers = Arrays.asList(new User(), new User());
        when(userMapper.searchUsers(anyMap())).thenReturn(expectedUsers);

        // 执行测试
        List<User> actualUsers = userController.searchUsers("张三", "13800138000", "active", "2023-01-01", "2023-12-31");

        // 验证结果
        assertEquals(2, actualUsers.size());
        verify(userMapper, times(1)).searchUsers(anyMap());
    }

    @Test
    public void testSearchUsers_WithSomeParams() {
        // 准备测试数据
        List<User> expectedUsers = Collections.singletonList(new User());
        when(userMapper.searchUsers(anyMap())).thenReturn(expectedUsers);

        // 执行测试
        List<User> actualUsers = userController.searchUsers(null, "13800138000", null, null, "2023-12-31");

        // 验证结果
        assertEquals(1, actualUsers.size());
        verify(userMapper, times(1)).searchUsers(anyMap());
    }

    @Test
    public void testSearchUsers_NoParams() {
        // 准备测试数据
        List<User> expectedUsers = Arrays.asList(new User(), new User(), new User());
        when(userMapper.searchUsers(anyMap())).thenReturn(expectedUsers);

        // 执行测试
        List<User> actualUsers = userController.searchUsers(null, null, null, null, null);

        // 验证结果
        assertEquals(3, actualUsers.size());
        verify(userMapper, times(1)).searchUsers(anyMap());
    }

    // getAll 测试
    @Test
    public void testGetAll_Success() {
        // 准备测试数据
        List<User> expectedUsers = Arrays.asList(new User(), new User());
        when(userMapper.getAllUsers()).thenReturn(expectedUsers);

        // 执行测试
        List<User> actualUsers = userController.getAll();

        // 验证结果
        assertEquals(2, actualUsers.size());
        verify(userMapper, times(1)).getAllUsers();
    }

    @Test
    public void testGetAll_Empty() {
        // 准备测试数据
        when(userMapper.getAllUsers()).thenReturn(Collections.emptyList());

        // 执行测试
        List<User> actualUsers = userController.getAll();

        // 验证结果
        assertTrue(actualUsers.isEmpty());
        verify(userMapper, times(1)).getAllUsers();
    }

    // deleteUser 测试
    @Test
    public void testDeleteUser_Success() throws IOException {
        // 准备测试数据
        String userId = "001";
        User user = new User();
        user.setUser_id(userId);

        when(userMapper.getUserById(userId)).thenReturn(user);
        when(userMapper.deleteUser(userId)).thenReturn(1);

        // 执行测试
        Integer result = userController.deleteUser(userId);

        // 验证结果
        assertEquals(1, result);
        verify(userMapper, times(1)).getUserById(userId);
        verify(userMapper, times(1)).deleteUser(userId);
    }

    @Test
    public void testDeleteUser_NotFound() throws IOException {
        // 准备测试数据
        String userId = "999";
        when(userMapper.getUserById(userId)).thenReturn(null);

        // 执行测试
        Integer result = userController.deleteUser(userId);

        // 验证结果
        assertEquals(0, result);
        verify(userMapper, times(1)).getUserById(userId);
        verify(userMapper, never()).deleteUser(any());
    }

    @Test
    public void testDeleteUser_EmptyId() throws IOException {
        // 执行测试并验证异常
        assertThrows(IllegalArgumentException.class, () -> {
            userController.deleteUser("");
        });
    }

    // getNextUser_id 测试
    @Test
    public void testGetNextUser_id_Success() {
        // 准备测试数据
        String expectedId = "003";
        when(userMapper.findNextUser_id()).thenReturn(expectedId);

        // 执行测试
        String actualId = userController.getNextUser_id();

        // 验证结果
        assertEquals(expectedId, actualId);
        verify(userMapper, times(1)).findNextUser_id();
    }

    @Test
    public void testGetNextUser_id_ReturnsNull() {
        // 准备测试数据
        when(userMapper.findNextUser_id()).thenReturn(null);

        // 执行测试
        String actualId = userController.getNextUser_id();

        // 验证结果
        assertNull(actualId);
        verify(userMapper, times(1)).findNextUser_id();
    }
}
