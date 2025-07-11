package com.neuedu.cmh.controller;


import com.neuedu.cmh.entity.Admin;
import com.neuedu.cmh.dto.AdminDTO;
import com.neuedu.cmh.mapper.AdminMapper;
import com.neuedu.cmh.util.EmailUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Redis键前缀和过期时间
    private static final String REDIS_CODE_PREFIX = "email_code:";
    private static final int CODE_EXPIRE_MINUTES = 5;


//@PostMapping("/login")
//public Admin login(@RequestBody Admin admin) {
//    Map<String, String> result = adminMapper.login(admin.getAdmin_name(), admin.getAdmin_pwd());
//    if (result != null && result.containsKey("admin_name") && result.containsKey("admin_pwd")) {
//        Admin loggedAdmin = new Admin();
//        loggedAdmin.setAdmin_name(result.get("admin_name"));
//        loggedAdmin.setAdmin_pwd(result.get("admin_pwd"));
//        return loggedAdmin;
//    } else {
//        return null; // 返回null表示登录失败
//    }
//}
    @PostMapping("/login")
    public Admin login(@RequestBody Admin admin) {
        // 添加空指针检查
        if (admin == null || admin.getAdmin_name() == null || admin.getAdmin_pwd() == null) {
            return null; // 直接返回null表示参数不合法
        }

        Map<String, String> result = adminMapper.login(admin.getAdmin_name(), admin.getAdmin_pwd());
        if (result != null && result.containsKey("admin_name") && result.containsKey("admin_pwd")) {
            Admin loggedAdmin = new Admin();
            loggedAdmin.setAdmin_name(result.get("admin_name"));
            loggedAdmin.setAdmin_pwd(result.get("admin_pwd"));
            return loggedAdmin;
        } else {
            return null;
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
        String email = request.get("admin_email");
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

    String captcha1 = "";
    @GetMapping("/captcha")
    public void getCaptcha(HttpSession session, HttpServletResponse response) throws IOException {
        // 1. 验证码配置参数
        int width = 80; // 图片宽度
        int height = 30; // 图片高度
        int length = 4; // 验证码长度
        String chars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // 可选：避免混淆字符（如0/O,1/I）

        // 2. 创建图片缓冲区
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 3. 绘制背景（添加干扰元素）
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        // 4. 绘制干扰线（增强安全性）
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(random.nextInt(220), random.nextInt(220), random.nextInt(220)));
            g.drawLine(random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height));
        }

        // 5. 生成随机验证码
        StringBuilder captcha = new StringBuilder();
        Font font = new Font("Arial", Font.BOLD, 20); // 字体大小
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);

        // 计算文字总宽度
        int totalWidth = 0;
        for (int i = 0; i < length; i++) {
            String ch = String.valueOf(chars.charAt(random.nextInt(chars.length())));
            captcha.append(ch);
            totalWidth += metrics.charWidth(ch.charAt(0));
        }

        // 计算水平起始位置
        int startX = (width - totalWidth) / 2;

        // 计算垂直居中位置
        int centerY = (height - metrics.getHeight()) / 2 + metrics.getAscent();

        // 绘制验证码
        for (int i = 0; i < length; i++) {
            String ch = captcha.toString().substring(i, i + 1);
            int x = startX + metrics.charWidth(ch.charAt(0)) * i;
            int y = centerY;

            g.setColor(new Color(50 + random.nextInt(150), 50 + random.nextInt(150), 50 + random.nextInt(150)));
            g.drawString(ch, x, y);
        }

        // 6. 存入Session（建议设置过期时间）
        String captchaStr = captcha.toString();
        session.setAttribute("captcha", captchaStr);
        session.setMaxInactiveInterval(120); // 2分钟过期

        // 7. 调试日志（生产环境应移除）
        System.out.println("[Captcha] Generated: " + captchaStr + " (SessionID: " + session.getId() + ")");
        System.out.println("captchaStr:"+captchaStr);
        captcha1= captchaStr.toString();
        System.out.println("captcha1:"+captcha1);
        // 8. 输出图片
        g.dispose();
        response.setHeader("Cache-Control", "no-store"); // 禁止缓存
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        ImageIO.write(image, "jpeg", response.getOutputStream());
    }

    @RequestMapping("/verify")
    public Map<String, Object> verifyCaptcha(@RequestBody Map<String, String> request, HttpSession session) {
        String captcha = captcha1.toString();

        String sessionCaptcha = request.get("captcha"); // 用户输入

        System.out.println("sessionCaptcha:" + sessionCaptcha + "---------" + "captcha:" + captcha);

        Map<String, Object> response = new HashMap<>();

        if (sessionCaptcha != null && sessionCaptcha.equalsIgnoreCase(captcha)) {
            response.put("success", true);
            response.put("message", "验证码正确");
        } else {
            response.put("success", false);
            response.put("message", "图形验证码错误");
        }
        System.out.println(response);
        return response;
    }


    //返回int版本
    @PostMapping("/register")
    public int registerAdmin(
            @RequestBody Admin admin,
            @RequestHeader("X-Session-ID") String sessionId) {
        System.out.println("admin_name:"+admin.getAdmin_name());
        try {
            System.out.println("2 Session ID: " + sessionId);
            // 1. 从Redis获取验证码
            String redisKey = REDIS_CODE_PREFIX + sessionId;
            String savedCode = redisTemplate.opsForValue().get(redisKey);
            System.out.println("邮箱验证码savedCode:"+savedCode);
            System.out.println("填写验证码email_code:"+admin.getEmail_code());
            // 2. 验证码校验
            if (savedCode == null) {
                return -1; // -1表示验证码过期
            }
            if (!savedCode.equals(admin.getEmail_code())) {
                return -2; // -2表示验证码错误
            }

            // 3. 处理角色逻辑
            if ("admin".equals(admin.getRole())) {
                admin.setAdmin_id(adminMapper.findNextAdmin_id());
            }

            // 4. 插入用户数据并返回受影响行数
            int affectedRows = adminMapper.insertAdmin(admin.getAdmin_id(), admin.getAdmin_name(), admin.getAdmin_pwd(),admin.getAdmin_email());
            System.out.println("admin_id:"+admin.getAdmin_id());
            System.out.println("admin_name:"+admin.getAdmin_name());
            if (affectedRows > 0) {
                redisTemplate.delete(redisKey);
            }
            return affectedRows; // 成功时返回1（插入1条数据）

        } catch (Exception e) {
            e.printStackTrace();
            return -3; // -3表示系统错误
        }
    }

    @GetMapping("/getAllAdmin_ids")
    public List<Integer> getAllAdmin_ids() {
        return adminMapper.getAllAdmin_ids();
    }

}
