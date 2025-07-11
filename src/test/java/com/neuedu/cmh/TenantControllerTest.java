package com.neuedu.cmh;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.neuedu.cmh.controller.TenantController;
import com.neuedu.cmh.entity.Tenant;
import com.neuedu.cmh.mapper.TenantMapper;
import com.neuedu.cmh.service.AIContentService;
import com.neuedu.cmh.service.WenxinContentService;
import com.neuedu.cmh.util.EmailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class TenantControllerTest {

    @Mock
    private TenantMapper tenantMapper;

    @Mock
    private EmailUtil emailUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AIContentService aiContentService;

    @Mock
    private WenxinContentService wenxinContentService;

    @InjectMocks
    private TenantController tenantController;

    private Tenant testTenant;
    private Map<String, String> testLoginResult;

    @BeforeEach
    void setUp() {
        testTenant = new Tenant();
        testTenant.setTenant_id("1");
        testTenant.setTenant_name("testUser");
        testTenant.setTenant_pwd("password123");
        testTenant.setTenant_email("test@example.com");
        testTenant.setTenant_emailCode("123456");
        testTenant.setAdmin_id("admin1");
        testTenant.setContact_phonenum("12345678901");
        testTenant.setContent("Test content");

        testLoginResult = new HashMap<>();
        testLoginResult.put("tenant_name", "testUser");
        testLoginResult.put("tenant_pwd", "password123");
        testLoginResult.put("tenant_id", "1");

//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    // ========== getAllTenant_ids 方法测试 ==========
    @Test
    void getAllTenant_ids_Success() {
        // 准备模拟数据
        List<Integer> mockIds = Arrays.asList(1, 2, 3);
        when(tenantMapper.getAllTenant_ids()).thenReturn(mockIds);

        // 调用测试方法
        List<Integer> result = tenantController.getAllTenant_ids();

        // 验证
        assertEquals(3, result.size());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));
        verify(tenantMapper, times(1)).getAllTenant_ids();
    }

    @Test
    void getAllTenant_ids_EmptyList() {
        // 模拟返回空列表
        when(tenantMapper.getAllTenant_ids()).thenReturn(Collections.emptyList());

        // 调用测试方法
        List<Integer> result = tenantController.getAllTenant_ids();

        // 验证
        assertTrue(result.isEmpty());
        verify(tenantMapper, times(1)).getAllTenant_ids();
    }

    @Test
    void getAllTenant_ids_NullCheck() {
        // 模拟返回null
        when(tenantMapper.getAllTenant_ids()).thenReturn(null);

        // 调用测试方法
        List<Integer> result = tenantController.getAllTenant_ids();

        // 验证
        assertNull(result);
        verify(tenantMapper, times(1)).getAllTenant_ids();
    }
    // ========== getTenantInfo 方法测试 ==========
    @Test
    void getTenantInfo_Success() throws Exception {
        Map<String, Object> mockInfo = new HashMap<>();
        mockInfo.put("tenant_name", "testUser");
        mockInfo.put("content", "Test content");
        when(tenantMapper.getTenantInfoById("1")).thenReturn(mockInfo);

        Map<String, Object> result = tenantController.getTenantInfo(Map.of("id", "1"));

        assertEquals("testUser", result.get("tenant_name"));
        assertEquals("Test content", result.get("content"));
    }

    @Test
    void getTenantInfo_NotFound() throws Exception {
        when(tenantMapper.getTenantInfoById("999")).thenReturn(null);

        Map<String, Object> result = tenantController.getTenantInfo(Map.of("id", "999"));

        assertNull(result.get("tenant_name"));
        assertNull(result.get("content"));
    }

    @Test
    void getTenantInfo_EmptyId() throws Exception {
        Map<String, Object> result = tenantController.getTenantInfo(Map.of("id", ""));

        assertNull(result.get("tenant_name"));
        assertNull(result.get("content"));
    }

    // ========== uploadFile 方法测试 ==========
    @Test
    void uploadFile_Success() throws Exception {
        String testContent = "test file content";
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", testContent.getBytes());

        String result = tenantController.uploadFile(file);

        assertNotNull(result);
        assertTrue(result.startsWith(DigestUtils.md5DigestAsHex(testContent.getBytes())));
    }

    @Test
    void uploadFile_Failure_EmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            tenantController.uploadFile(emptyFile);
        });
    }

    @Test
    void uploadFile_Failure_NullFile() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            tenantController.uploadFile(null);
        });
    }

    @Test
    void uploadFile_NullFilename_ShouldHandle() throws Exception {
        String testContent = "content with null filename";
        // 创建没有原始文件名的MultipartFile（需要反射技巧）
        MockMultipartFile file = new MockMultipartFile(
                "file", "", "text/plain", testContent.getBytes());

        // 通过反射设置originalFilename为null
        Field field = file.getClass().getDeclaredField("originalFilename");
        field.setAccessible(true);
        field.set(file, null);

        String result = tenantController.uploadFile(file);

        // 应该只有MD5哈希没有扩展名
        String expectedHash = DigestUtils.md5DigestAsHex(testContent.getBytes());
        assertEquals(expectedHash, result);
    }

    @Test
    void uploadFile_CopyFailure_ShouldThrow() throws Exception {
        // 1. 准备测试数据
        String testContent = "test content";
        MultipartFile file = mock(MultipartFile.class);

        // 2. 配置mock行为
        when(file.isEmpty()).thenReturn(false);
        when(file.getBytes()).thenReturn(testContent.getBytes());
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(testContent.getBytes()));

        // 3. 模拟文件系统环境
        try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
            // 强制让系统认为文件不存在（关键修改点）
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            // 模拟目录创建成功
            mockedFiles.when(() -> Files.createDirectories(any(Path.class)))
                    .thenReturn(Paths.get("mock_dir"));

            // 模拟copy操作失败（这才是我们真正要测试的）
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class)))
                    .thenThrow(new IOException("Simulated copy failure"));

            // 4. 执行并验证
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> tenantController.uploadFile(file));

            // 5. 验证异常信息
            assertTrue(exception.getMessage().contains("文件上传失败"));
            assertTrue(exception.getCause() instanceof IOException);
        }
    }

    @Test
    void uploadFile_MultipleDotsInFilename_ShouldHandle() throws Exception {
        String testContent = "file with multiple.dots.txt";
        MockMultipartFile file = new MockMultipartFile(
                "file", "archive.tar.gz", "application/gzip", testContent.getBytes());

        String result = tenantController.uploadFile(file);

        // 应该只取最后一个点作为扩展名
        assertTrue(result.endsWith(".gz"));
    }
    // ========== updateTenant 方法测试 ==========
    @Test
    void updateTenant_Success() throws Exception {
        when(tenantMapper.updateTenant(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        ResponseEntity<Map<String, String>> response = tenantController.updateTenant(testTenant);

        assertEquals("修改成功", response.getBody().get("status"));
    }

    @Test
    void updateTenant_Failure() throws Exception {
        when(tenantMapper.updateTenant(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        ResponseEntity<Map<String, String>> response = tenantController.updateTenant(testTenant);

        assertEquals("修改失败", response.getBody().get("status"));
    }

    // ========== addTenant 方法测试 ==========
    @Test
    void addTenant_Success_WithGeneratedContent() throws Exception {
        when(tenantMapper.findNextTenant_id()).thenReturn("100");
        when(aiContentService.generateCompanyProfile(eq("testUser"), isNull()))
                .thenReturn("Generated content");
        when(tenantMapper.insertTenant2(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        testTenant.setContent(null);
        ResponseEntity<Map<String, String>> response = tenantController.addTenant(testTenant);

        assertEquals("添加成功", response.getBody().get("status"));
        assertEquals("100", response.getBody().get("tenant_id"));
        assertEquals("Generated content", response.getBody().get("generated_content"));
    }
    @Test
    void addTenant_Success_WithExistingContent() throws Exception {
        when(tenantMapper.findNextTenant_id()).thenReturn("100");
        when(tenantMapper.insertTenant2(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1);

        ResponseEntity<Map<String, String>> response = tenantController.addTenant(testTenant);

        assertEquals("添加成功", response.getBody().get("status"));
        assertEquals("100", response.getBody().get("tenant_id"));
        // 修改断言：检查返回的内容是否与输入一致
        assertEquals("Test content", response.getBody().get("generated_content"));
    }

    @Test
    void addTenant_Failure() throws Exception {
        when(tenantMapper.findNextTenant_id()).thenReturn("100");
        when(tenantMapper.insertTenant2(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0);

        ResponseEntity<Map<String, String>> response = tenantController.addTenant(testTenant);

        assertEquals("添加失败", response.getBody().get("status"));
    }

    // ========== generateContent 方法测试 ==========
    @Test
    void generateContent_Success_DeepSeek() throws Exception {
        when(aiContentService.generateCompanyProfile("Test Company", ""))
                .thenReturn("Generated by DeepSeek");

        Map<String, Object> request = new HashMap<>();
        request.put("provider", "deepseek");
        request.put("companyName", "Test Company");

        ResponseEntity<Map<String, String>> response = tenantController.generateContent(request);

        assertEquals("success", response.getBody().get("status"));
        assertEquals("deepseek", response.getBody().get("provider"));
        assertEquals("Generated by DeepSeek", response.getBody().get("generated_content"));
    }

    @Test
    void generateContent_Success_Wenxin() throws Exception {
        when(wenxinContentService.generateCompanyProfile("Test Company", "Existing content"))
                .thenReturn("Generated by Wenxin");

        Map<String, Object> request = new HashMap<>();
        request.put("provider", "wenxin");
        request.put("companyName", "Test Company");
        request.put("existingContent", "Existing content");

        ResponseEntity<Map<String, String>> response = tenantController.generateContent(request);

        assertEquals("success", response.getBody().get("status"));
        assertEquals("wenxin", response.getBody().get("provider"));
        assertEquals("Generated by Wenxin", response.getBody().get("generated_content"));
    }

    @Test
    void generateContent_Failure_EmptyCompanyName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("provider", "deepseek");
        request.put("companyName", "");

        ResponseEntity<Map<String, String>> response = tenantController.generateContent(request);

        assertEquals("error", response.getBody().get("status"));
        assertEquals("公司名称不能为空", response.getBody().get("message"));
    }

    // ========== searchTenants 方法测试 ==========
    @Test
    void searchTenants_Success() throws Exception {
        List<Tenant> mockList = Arrays.asList(testTenant);
        when(tenantMapper.searchTenants(anyMap())).thenReturn(mockList);

        Map<String, String> params = new HashMap<>();
        params.put("tenant_name", "test");
        List<Tenant> result = tenantController.searchTenants(params);

        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getTenant_name());
    }

    @Test
    void searchTenants_EmptyParams() throws Exception {
        List<Tenant> mockList = Arrays.asList(testTenant);
        when(tenantMapper.searchTenants(anyMap())).thenReturn(mockList);

        List<Tenant> result = tenantController.searchTenants(new HashMap<>());

        assertEquals(1, result.size());
    }

    // ========== getAll 方法测试 ==========
    @Test
    void getAll_ShouldReturnListOfTenants() {
        // 准备模拟数据
        Tenant tenant1 = new Tenant();
        tenant1.setTenant_id("1");
        tenant1.setTenant_name("Tenant 1");

        Tenant tenant2 = new Tenant();
        tenant2.setTenant_id("2");
        tenant2.setTenant_name("Tenant 2");

        List<Tenant> mockTenants = Arrays.asList(tenant1, tenant2);

        // 设置mock行为
        when(tenantMapper.getAllTenants()).thenReturn(mockTenants);

        // 调用测试方法
        List<Tenant> result = tenantController.getAll();

        // 验证
        assertEquals(2, result.size());
        assertEquals("1", result.get(0).getTenant_id());
        assertEquals("Tenant 1", result.get(0).getTenant_name());
        assertEquals("2", result.get(1).getTenant_id());
        assertEquals("Tenant 2", result.get(1).getTenant_name());

        // 验证mapper方法被调用
        verify(tenantMapper, times(1)).getAllTenants();
    }

    @Test
    void getAll_ShouldReturnEmptyListWhenNoTenants() {
        // 设置mock返回空列表
        when(tenantMapper.getAllTenants()).thenReturn(Collections.emptyList());

        // 调用测试方法
        List<Tenant> result = tenantController.getAll();

        // 验证
        assertTrue(result.isEmpty());
        verify(tenantMapper, times(1)).getAllTenants();
    }

    @Test
    void getAll_ShouldHandleNullReturnFromMapper() {
        // 设置mock返回null
        when(tenantMapper.getAllTenants()).thenReturn(null);

        // 调用测试方法
        List<Tenant> result = tenantController.getAll();

        // 验证
        assertNull(result);
        verify(tenantMapper, times(1)).getAllTenants();
    }

    @Test
    void getAll_ShouldReturnAllTenantFields() {
        // 准备包含完整字段的模拟租户
        Tenant fullTenant = new Tenant();
        fullTenant.setTenant_id("100");
        fullTenant.setTenant_name("Full Tenant");
        fullTenant.setTenant_email("full@example.com");
        fullTenant.setAdmin_id("admin100");
        fullTenant.setContact_phonenum("1234567890");
        fullTenant.setContent("Full content");

        // 设置mock行为
        when(tenantMapper.getAllTenants()).thenReturn(Collections.singletonList(fullTenant));

        // 调用测试方法
        List<Tenant> result = tenantController.getAll();

        // 验证所有字段
        assertEquals(1, result.size());
        Tenant returnedTenant = result.get(0);
        assertEquals("100", returnedTenant.getTenant_id());
        assertEquals("Full Tenant", returnedTenant.getTenant_name());
        assertEquals("full@example.com", returnedTenant.getTenant_email());
        assertEquals("admin100", returnedTenant.getAdmin_id());
        assertEquals("1234567890", returnedTenant.getContact_phonenum());
        assertEquals("Full content", returnedTenant.getContent());
    }

    // ========== deleteTenant 方法测试 ==========
    @Test
    void deleteTenant_Success() throws Exception {
        when(tenantMapper.deleteTenant("1")).thenReturn(1);

        Integer result = tenantController.deleteTenant("1");

        assertEquals(1, result);
    }

    @Test
    void deleteTenant_Failure() throws Exception {
        when(tenantMapper.deleteTenant("999")).thenReturn(0);

        Integer result = tenantController.deleteTenant("999");

        assertEquals(0, result);
    }

    // ========== getNextTenant_id 方法测试 ==========
    @Test
    void getNextTenant_id_Success() throws Exception {
        when(tenantMapper.findNextTenant_id()).thenReturn("100");

        String result = tenantController.getNextTenant_id();

        assertEquals("100", result);
    }
}