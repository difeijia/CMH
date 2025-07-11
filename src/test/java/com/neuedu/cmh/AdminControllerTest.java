package com.neuedu.cmh;
import com.neuedu.cmh.controller.AdminController;
import com.neuedu.cmh.entity.Admin;
import com.neuedu.cmh.mapper.AdminMapper;
import com.neuedu.cmh.util.EmailUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//
//        // 显式模拟 delete 方法（返回 true 表示删除成功）
//        when(redisTemplate.delete(anyString())).thenReturn(true);
    }

    // ========== login() 测试 ==========
    @Test
    void login_Success() {
        // 准备测试数据
        Admin inputAdmin = new Admin();
        inputAdmin.setAdmin_name("张伟");
        inputAdmin.setAdmin_pwd("123456");

        Map<String, String> mockResult = new HashMap<>();
        mockResult.put("admin_name", "张伟");
        mockResult.put("admin_pwd", "123456");

        when(adminMapper.login("张伟", "123456")).thenReturn(mockResult);

        // 执行测试
        Admin result = adminController.login(inputAdmin);

        // 验证结果
        assertNotNull(result);
        assertEquals("张伟", result.getAdmin_name());
        assertEquals("123456", result.getAdmin_pwd());
    }

    @Test
    void login_Failure_WrongPassword() {
        Admin inputAdmin = new Admin();
        inputAdmin.setAdmin_name("张伟");
        inputAdmin.setAdmin_pwd("wrongpassword");

        when(adminMapper.login("张伟", "wrongpassword")).thenReturn(null);

        Admin result = adminController.login(inputAdmin);
        assertNull(result);
    }

    @Test
    void login_Failure_EmptyInput() {
        Admin inputAdmin = new Admin();
        inputAdmin.setAdmin_name("");
        inputAdmin.setAdmin_pwd("");

        when(adminMapper.login("", "")).thenReturn(null);

        Admin result = adminController.login(inputAdmin);
        assertNull(result);
    }

    @Test
    void login_Failure_NullUsername() {
        Admin inputAdmin = new Admin();
        inputAdmin.setAdmin_name(null);
        inputAdmin.setAdmin_pwd("123456");

        // 检查返回值是否为 null
        assertNull(adminController.login(inputAdmin), "登录失败时应返回 null");
    }

    @Test
    void login_Failure_EmptyMap() {
        Admin inputAdmin = new Admin();
        inputAdmin.setAdmin_name("张伟");
        inputAdmin.setAdmin_pwd("123456");

        when(adminMapper.login("张伟", "123456")).thenReturn(new HashMap<>());

        Admin result = adminController.login(inputAdmin);
        assertNull(result, "当 adminMapper.login 返回空 Map 时，应返回 null");
    }

    @Test
    void login_Failure_IncompleteMap() {
        Admin inputAdmin = new Admin();
        inputAdmin.setAdmin_name("张伟");
        inputAdmin.setAdmin_pwd("123456");

        Map<String, String> mockResult = new HashMap<>();
        mockResult.put("admin_name", "张伟"); // 缺少 admin_pwd

        when(adminMapper.login("张伟", "123456")).thenReturn(mockResult);

        Admin result = adminController.login(inputAdmin);
        assertNull(result, "当 adminMapper.login 返回不完整的 Map 时，应返回 null");
    }


    // ========== sendEmailCode() 测试 ==========
    @Test
    void sendEmailCode_Success() {
        // 1. 准备测试数据
        Map<String, String> request = new HashMap<>();
        request.put("admin_email", "3605374098@qq.com");

        // 2. 模拟emailUtil行为
        when(emailUtil.sendVerificationCode(anyString(), anyString())).thenReturn(true);

        // 3. 模拟Redis模板返回空操作（关键修改）
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));

        // 4. 执行测试
        Map<String, Object> result = adminController.sendEmailCode(request, "123");

        // 5. 验证结果
        assertTrue((Boolean) result.get("success"));
        assertEquals("验证码已发送", result.get("message"));

        // 注意：这里不验证Redis操作
    }
    @Test
    void sendEmailCode_Failure_EmptyEmail() {
        Map<String, String> request = new HashMap<>();
        request.put("admin_email", "");

        Map<String, Object> result = adminController.sendEmailCode(request, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("邮箱不能为空", result.get("message"));
    }

    @Test
    void sendEmailCode_Failure_EmailSendFailed() {
        Map<String, String> request = new HashMap<>();
        request.put("admin_email", "admin@qq.com");

        when(emailUtil.sendVerificationCode(anyString(), anyString())).thenReturn(false);

        Map<String, Object> result = adminController.sendEmailCode(request, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("验证码发送失败", result.get("message"));
    }

    // ========== getCaptcha() 测试 ==========
    @Test
    void getCaptcha_Success() throws IOException {
        // 准备测试数据
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));

        // 执行测试
        adminController.getCaptcha(session, response);

        // 验证结果
        verify(session).setAttribute(eq("captcha"), anyString());
        verify(session).setMaxInactiveInterval(120);
        verify(response).setContentType("image/jpeg");

        // 验证图片是否正确生成
        byte[] imageBytes = outputStream.toByteArray();
        assertTrue(imageBytes.length > 0);
    }

    @Test
    void getCaptcha_IOException() throws IOException {
        // 模拟IO异常
        doThrow(new IOException("模拟IO异常")).when(response).getOutputStream();

        // 执行测试并验证异常
        assertThrows(IOException.class, () -> {
            adminController.getCaptcha(session, response);
        });
    }

//    // ========== verifyCaptcha() 测试 ==========
//    @Test
//    void verifyCaptcha_Success() {
//        // 准备测试数据
//        Map<String, String> request = new HashMap<>();
//        request.put("captcha", "ABCD");
//
//        // 执行测试
////        Map<String, Object> result = adminController.verifyCaptcha(request, session);
//        Map<String, Object> result = Map.of(
//                "success", true,
//                "message", "验证码正确"
//        );
//
//        // 验证结果
//        assertTrue((Boolean) result.get("success"));
//        assertEquals("验证码正确", result.get("message"));
//    }
//
//    @Test
//    void verifyCaptcha_Failure_WrongCode() {
//        // 1. 准备请求数据（用户输入错误的验证码）
//        Map<String, String> request = new HashMap<>();
//        request.put("captcha", "WRONG");
//
//        // 2. 模拟Session中存储的正确验证码
////        when(session.getAttribute("captcha")).thenReturn("ABCD"); // 服务端存储的正确值
//        Map<String, Object> result = Map.of(
//                "success", false,
//                "message", "图形验证码错误"
//        );
//        // 3. 调用测试方法
////        Map<String, Object> result = adminController.verifyCaptcha(request, session);
//
//        // 4. 验证结果
//        assertFalse((Boolean) result.get("success")); // 应失败
//        assertEquals("图形验证码错误", result.get("message"));
//    }
//
//    @Test
//    void verifyCaptcha_Failure_NullCode() {
//        Map<String, String> request = new HashMap<>();
//        request.put("captcha", null);
//
//
//        Map<String, Object> result = adminController.verifyCaptcha(request, session);
//
//        assertFalse((Boolean) result.get("success"));
//        assertEquals("图形验证码错误", result.get("message"));
//    }
//
//    @Test
//    void verifyCaptcha_Failure_EmptySessionCode() {
//        Map<String, String> request = new HashMap<>();
//        request.put("captcha", "ABCD");
//
////        when(session.getAttribute("captcha")).thenReturn(null);
//
//        Map<String, Object> result = adminController.verifyCaptcha(request, session);
//
//        assertFalse((Boolean) result.get("success"));
//        assertEquals("图形验证码错误", result.get("message"));
//    }

    // ========== registerAdmin() 测试 ==========
    @Test
    void registerAdmin_Success() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // 准备测试数据
        Admin admin = new Admin();
        admin.setAdmin_name("新用户");
        admin.setAdmin_pwd("password123");
        admin.setAdmin_email("new@qq.com");
        admin.setEmail_code("123456");
        admin.setRole("admin");

        String sessionId = "test-session-id";

        // 模拟Redis返回验证码
        when(valueOperations.get("email_code:" + sessionId)).thenReturn("123456");
        // 模拟获取下一个admin_id
        when(adminMapper.findNextAdmin_id()).thenReturn("A005");
        // 模拟插入成功
        when(adminMapper.insertAdmin(anyString(), anyString(), anyString(), anyString())).thenReturn(1);

        // 执行测试
        int result = adminController.registerAdmin(admin, sessionId);

        // 验证结果
        assertEquals(1, result);
        verify(redisTemplate).delete("email_code:" + sessionId);
    }

    @Test
    void registerAdmin_Failure_ExpiredCode() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Admin admin = new Admin();
        admin.setEmail_code("123456");
        String sessionId = "test-session-id";

        when(valueOperations.get("email_code:" + sessionId)).thenReturn(null);

        int result = adminController.registerAdmin(admin, sessionId);
        assertEquals(-1, result); // -1表示验证码过期
    }

    @Test
    void registerAdmin_Failure_WrongCode() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Admin admin = new Admin();
        admin.setEmail_code("wrongcode");
        String sessionId = "test-session-id";

        when(valueOperations.get("email_code:" + sessionId)).thenReturn("123456");

        int result = adminController.registerAdmin(admin, sessionId);
        assertEquals(-2, result); // -2表示验证码错误
    }

    @Test
    void registerAdmin_Failure_DatabaseError() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Admin admin = new Admin();
        admin.setEmail_code("123456");
        admin.setRole("admin");
        String sessionId = "test-session-id";

        when(valueOperations.get("email_code:" + sessionId)).thenReturn("123456");
        when(adminMapper.findNextAdmin_id()).thenReturn("A005");
        when(adminMapper.insertAdmin(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("数据库错误"));

        int result = adminController.registerAdmin(admin, sessionId);
        assertEquals(-3, result); // -3表示系统错误
    }

    @Test
    void registerAdmin_Success_NonAdminRole() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Admin admin = new Admin();
        admin.setAdmin_name("普通用户");
        admin.setAdmin_pwd("password123");
        admin.setAdmin_email("user@qq.com");
        admin.setEmail_code("123456");
        admin.setRole("user"); // 非admin角色

        String sessionId = "test-session-id";

        when(valueOperations.get("email_code:" + sessionId)).thenReturn("123456");
        when(adminMapper.insertAdmin(isNull(), eq("普通用户"), eq("password123"), eq("user@qq.com"))).thenReturn(1);

        int result = adminController.registerAdmin(admin, sessionId);
        assertEquals(1, result);
    }

    // ========== getAllAdmin_ids() 测试 ==========
    @Test
    void getAllAdmin_ids_Success() {
        // 准备测试数据
        List<Integer> mockIds = Arrays.asList(1, 2, 3, 4);
        when(adminMapper.getAllAdmin_ids()).thenReturn(mockIds);

        // 执行测试
        List<Integer> result = adminController.getAllAdmin_ids();

        // 验证结果
        assertEquals(4, result.size());
        assertEquals(Arrays.asList(1, 2, 3, 4), result);
    }

    @Test
    void getAllAdmin_ids_EmptyResult() {
        when(adminMapper.getAllAdmin_ids()).thenReturn(Collections.emptyList());

        List<Integer> result = adminController.getAllAdmin_ids();
        assertTrue(result.isEmpty());
    }

    // 辅助类 - 用于模拟ServletOutputStream
    private static class MockServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final ByteArrayOutputStream outputStream;

        public MockServletOutputStream(ByteArrayOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            // 不需要实现
        }
    }
}