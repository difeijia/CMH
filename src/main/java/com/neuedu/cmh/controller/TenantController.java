package com.neuedu.cmh.controller;


import com.neuedu.cmh.entity.Tenant;
import com.neuedu.cmh.mapper.TenantMapper;
import com.neuedu.cmh.service.AIContentService;
import com.neuedu.cmh.service.WenxinContentService;
import com.neuedu.cmh.util.EmailUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@RequestMapping("/tenant")
public class TenantController {
    @Autowired
    private TenantMapper tenantMapper;
    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AIContentService aiContentService;
    @Autowired
    private WenxinContentService wenxinContentService;

    // Redis键前缀和过期时间
    private static final String REDIS_CODE_PREFIX = "tenant_emailCode:";
    private static final int CODE_EXPIRE_MINUTES = 5;


    @RequestMapping("/login")
    public Tenant login(@RequestBody Tenant tenant) {
        System.out.println("tenant_name:"+tenant.getTenant_name());
        System.out.println("tenant_pwd:"+tenant.getTenant_pwd());
        Map<String, String> result = tenantMapper.login(tenant.getTenant_name(), tenant.getTenant_pwd());
        if (result != null && result.containsKey("tenant_name") && result.containsKey("tenant_pwd")) {
            Tenant loggedTenant = new Tenant();
            loggedTenant.setTenant_name(result.get("tenant_name"));
            loggedTenant.setTenant_pwd(result.get("tenant_pwd"));
            loggedTenant.setTenant_id(result.get("tenant_id"));
            return loggedTenant;
        } else {
            return null; // 返回null表示登录失败
        }
    }


    @PostMapping("/register")
    public Tenant registerTenant(
            @RequestBody Tenant tenant,
            @RequestHeader("X-Session-ID") String sessionId) {

        try {
            System.out.println("2 Session ID: " + sessionId);
            // 1. 从Redis获取验证码
            String redisKey = REDIS_CODE_PREFIX + sessionId;
            String savedCode = redisTemplate.opsForValue().get(redisKey);

            // 2. 验证码校验
            if (savedCode == null) {
                throw new IllegalArgumentException("验证码过期");
            }
            if (!savedCode.equals(tenant.getTenant_emailCode())) {
                throw new IllegalArgumentException("验证码错误");
            }

            // 3. 处理角色逻辑
            if ("tenant".equals(tenant.getRole())) {
                tenant.setTenant_id(tenantMapper.findNextTenant_id());
            }
            // 获取下一个admin_id
            String nextAdminId = tenantMapper.findNextAdmin_id();
            tenant.setAdmin_id(nextAdminId);

            // 4. 插入用户数据
            int affectedRows = tenantMapper.insertTenant(
                    tenant.getTenant_id(),
                    tenant.getTenant_name(),
                    tenant.getTenant_pwd(),
                    tenant.getTenant_email(),
                    tenant.getAdmin_id(),
                    tenant.getContact_phonenum()
            );

            System.out.println("tenant_id:" + tenant.getTenant_id());
            System.out.println("tenant_name:" + tenant.getTenant_name());
            System.out.println("admin_id:" + tenant.getAdmin_id());
            System.out.println("contact_phonenum:" + tenant.getContact_phonenum());

            if (affectedRows > 0) {
                // 如果插入成功，删除Redis中的验证码
                redisTemplate.delete(redisKey);
                System.out.println("tenant_id2:"+tenant.getTenant_id());
                // 返回插入的对象
                return tenant;
            } else {
                // 如果插入失败，返回null或抛出异常
                throw new RuntimeException("用户注册失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("系统错误", e);
        }
    }
    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendEmailCode")
    public Map<String, Object> sendEmailCode(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionIdFromHeader) {
        System.out.println("1 Session ID: " + sessionIdFromHeader);
        // 1. 校验邮箱
        String email = request.get("tenant_email");
        if (email == null || email.isEmpty()) {
            return Map.of("success", false, "message", "邮箱不能为空");
        }

        // 2. 生成或复用SessionID
        String sessionId = sessionIdFromHeader != null ? sessionIdFromHeader : UUID.randomUUID().toString();

        // 3. 发送验证码（模拟生成6位数字）
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        boolean sendSuccess = emailUtil.sendVerificationCode(email, code);

        if (sendSuccess) {
            // 4. 存储到Redis（有效期5分钟）
            String redisKey = REDIS_CODE_PREFIX + sessionId;
            redisTemplate.opsForValue().set(redisKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            return Map.of(
                    "success", true,
                    "message", "验证码已发送",
                    "sessionId", sessionId  // 返回给前端
            );
        } else {
            return Map.of("success", false, "message", "验证码发送失败");
        }
    }


    @GetMapping("/getAllTenant_ids")
    public List<Integer> getAllTenant_ids() {
        return tenantMapper.getAllTenant_ids();
    }

    @PostMapping("/getTenantInfo")
    public Map<String, Object> getTenantInfo(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        System.out.println("id:" + id);
        Map<String, Object> tenantInfo = tenantMapper.getTenantInfoById(id);
        if (tenantInfo == null || tenantInfo.isEmpty()) {
            tenantInfo = new HashMap<>();
            tenantInfo.put("tenant_name", null);
            tenantInfo.put("tenant_logo", null);
            tenantInfo.put("tenant_contact", null);
            tenantInfo.put("contact_phonenum", null);
            tenantInfo.put("tenant_comment", null);
            tenantInfo.put("tenant_pwd", null);
            tenantInfo.put("admin_id", null);
            tenantInfo.put("tenant_email", null);
            tenantInfo.put("content", null);
        }
        // 打印 tenant_name 的值
        System.out.println("Tenant Name: " + tenantInfo.get("tenant_name"));
        System.out.println("CONTENT: " + tenantInfo.get("content"));
        System.out.println("tenant_comment: " + tenantInfo.get("tenant_comment"));
        return tenantInfo;
    }


@PostMapping("/upload")
public String uploadFile(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
        throw new IllegalArgumentException("文件为空");
    }

    try {
        String targetDir = "uploads";
        Path targetPath = Paths.get(targetDir);
        Files.createDirectories(targetPath);

        String fileHash = DigestUtils.md5DigestAsHex(file.getBytes());
        String originalFilename = file.getOriginalFilename();

        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String hashedFilename = fileHash + fileExtension;

        Path filePath = targetPath.resolve(hashedFilename);

        if (!Files.exists(filePath)) {
            // 明确分开两个可能抛出IOException的操作
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath); // 这是我们要模拟失败的点
            }
            System.out.println("新文件已保存: " + filePath.toAbsolutePath());
        } else {
            System.out.println("文件已存在，直接返回: " + filePath.toAbsolutePath());
        }

        return hashedFilename;
    } catch (IOException e) {
        // 确保这里抛出的是RuntimeException
        throw new RuntimeException("文件上传失败", e); // 修改点：确保抛出异常
    }
}

    @PostMapping("/update")
    public ResponseEntity<Map<String, String>> updateTenant(@RequestBody Tenant tenant) {
        System.out.println("Received Tenant object: " + tenant);
        System.out.println("改前logo:"+tenant.getTenant_logo());
        int result = tenantMapper.updateTenant(
                tenant.getTenant_id(),
                tenant.getTenant_logo(),
                tenant.getTenant_name(),
                tenant.getTenant_contact(),
                tenant.getContact_phonenum(),
                tenant.getTenant_comment(),
                tenant.getAdmin_id(),
                tenant.getTenant_email(),
                tenant.getContent()
        );
        System.out.println("phone:"+tenant.getContact_phonenum());
        System.out.println("comment:"+tenant.getTenant_comment());
        System.out.println("修改content:" + tenant.getContent());
        System.out.println("修改logo:" + tenant.getTenant_logo());
        Map<String, String> response = new HashMap<>();
        response.put("status", result > 0 ? "修改成功" : "修改失败");
        return ResponseEntity.ok(response);
    }

@PostMapping("/add")
public ResponseEntity<Map<String, String>> addTenant(@RequestBody Tenant tenant) {
    System.out.println("Received Tenant object: " + tenant);
    String nextTenantId = tenantMapper.findNextTenant_id();
    tenant.setTenant_id(nextTenantId);

    // 自动生成公司简介
    if (tenant.getContent() == null || tenant.getContent().isEmpty()) {
        String generatedContent = aiContentService.generateCompanyProfile(
                tenant.getTenant_name(),tenant.getContent()
        );
        System.out.println("content:"+tenant.getContent());
        tenant.setContent(generatedContent);
    }

    int result = tenantMapper.insertTenant2(
            tenant.getTenant_id(),
            tenant.getTenant_logo(),
            tenant.getTenant_name(),
            tenant.getTenant_contact(),
            tenant.getContact_phonenum(),
            tenant.getTenant_comment(),
            tenant.getAdmin_id(),
            tenant.getTenant_email(),
            tenant.getContent()
    );

    Map<String, String> response = new HashMap<>();
    response.put("status", result > 0 ? "添加成功" : "添加失败");
    if (result > 0) {
        response.put("tenant_id", nextTenantId);
        response.put("generated_content", tenant.getContent()); // 返回生成的内容
    }
    return ResponseEntity.ok(response);
}


@PostMapping("/generateContent")
public ResponseEntity<Map<String, String>> generateContent(@RequestBody Map<String, Object> request) {
    try {
        // 获取请求参数
        String provider = (String) request.getOrDefault("provider", "deepseek");
        String companyName = (String) request.get("companyName");
        String existingContent = (String) request.getOrDefault("existingContent", "");

        // 参数校验
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new IllegalArgumentException("公司名称不能为空");
        }

        // 调用AI服务
        String generatedContent;
        switch (provider) {
            case "wenxin":
                generatedContent = wenxinContentService.generateCompanyProfile(companyName, existingContent);
                break;
            case "deepseek":
            default:
                generatedContent = aiContentService.generateCompanyProfile(companyName, existingContent);
        }

        // 构造响应
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("provider", provider);
        response.put("generated_content", generatedContent);

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(500).body(errorResponse);
    }
}

    @GetMapping("/searchTenants")
    public List<Tenant> searchTenants(@RequestParam Map<String, String> searchParams) {
        return tenantMapper.searchTenants(searchParams);
    }

    // 查询所有租户
    @GetMapping("/getAll")
    public List<Tenant> getAll() {
        return tenantMapper.getAllTenants();
    }

    @RequestMapping("/delete")
    public Integer deleteTenant(@RequestParam("tenant_id") String tenant_id) throws IOException {
        Tenant tenant = tenantMapper.getTenantById(tenant_id);
        //不删除后端的图片，只删除数据库中的
        return tenantMapper.deleteTenant(tenant_id);
    }

    @GetMapping("/getNextTenant_id")
    public String getNextTenant_id() {
        return tenantMapper.findNextTenant_id();
    }
}

