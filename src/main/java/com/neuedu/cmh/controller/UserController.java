package com.neuedu.cmh.controller;

import com.neuedu.cmh.entity.User;
import com.neuedu.cmh.entity.User;
import com.neuedu.cmh.mapper.UserMapper;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // Redis键前缀和过期时间
    private static final String REDIS_CODE_PREFIX = "user_emailCode:";
    private static final int CODE_EXPIRE_MINUTES = 5;


    
    @GetMapping("/getAllUserIds")
    public List<String> getAllUserIds() {
        return userMapper.getAllUser_ids();
    }

    @PostMapping("/getUserInfo")
    public Map<String, Object> getUserInfo(@RequestBody Map<String, Object> body) {
        if (body == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        String id = (String) body.get("id");
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        System.out.println("id:" + id);
        Map<String, Object> userInfo = userMapper.getUserInfoById(id);
        if (userInfo == null || userInfo.isEmpty()) {
            userInfo = new HashMap<>();
            userInfo.put("user_id", null);
            userInfo.put("user_name", null);
            userInfo.put("user_nickname", null);
            userInfo.put("dept_id", null);
            userInfo.put("tenant_id", null);
            userInfo.put("phonenum", null);
            userInfo.put("mailbox", null);
            userInfo.put("user_gender", null);
            userInfo.put("user_job_id", null);
            userInfo.put("createdate", null);
            userInfo.put("user_comment", null);
            userInfo.put("user_state", null);
        }
     return userInfo;
    }
    

    @PostMapping("/update")
    public ResponseEntity<Map<String, String>> updateUser(@RequestBody User user) {
        int result = userMapper.updateUser(
                user.getUser_id(),
//                user.getUser_name(),
                user.getUser_nickname(),
                user.getDept_id(),
                user.getTenant_id(),
                user.getPhonenum(),
                user.getMailbox(),
                user.getUser_gender(),
                user.getUser_job_id(),
                user.getUser_comment(),
                user.getUser_role(),
                user.getUser_state()
        );
        Map<String, String> response = new HashMap<>();
        response.put("status", result > 0 ? "修改成功" : "修改失败");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addUser(@RequestBody User user) {
        System.out.println("Received User object: " + user);
        String nextUserId = userMapper.findNextUser_id();
        user.setUser_id(nextUserId);
        System.out.println("user_id:" + nextUserId);
        user.setCreatedate(new Date());
        int result = userMapper.insertUser(
                user.getUser_id(),
                user.getUser_name(),
                user.getUser_nickname(),
                user.getDept_id(),
                user.getTenant_id(),
                user.getPhonenum(),
                user.getMailbox(),
                user.getUser_gender(),
                user.getUser_job_id(),
                user.getCreatedate(),
                user.getUser_comment(),
                user.getUser_pwd(),
                user.getUser_role(),
                user.getUser_state()

        );
        Map<String, String> response = new HashMap<>();
        response.put("status", result > 0 ? "添加成功" : "添加失败");
        if (result > 0) {
            response.put("user_id", nextUserId); // 添加 user_id 到响应中
        }
        System.out.println("response:"+response);
        return ResponseEntity.ok(response);
    }

    // 搜索用户
    @GetMapping("/searchUsers")
    public List<User> searchUsers(
            @RequestParam(value = "user_name", required = false) String user_name,
            @RequestParam(value = "phonenum", required = false) String phonenum,
            @RequestParam(value = "user_state", required = false) String user_state,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {

        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("user_name", user_name);
        searchParams.put("phonenum", phonenum);
        searchParams.put("user_state", user_state);
        searchParams.put("startDate", startDate);
        searchParams.put("endDate", endDate);

        return userMapper.searchUsers(searchParams);
    }


//    // 查询所有租户
//    @GetMapping("/getAll")
//    public List<User> getAll() {
//        return userMapper.getAllUsers();
//
//    }
    @GetMapping("/getAll")
    public List<User> getAll() {
        List<User> users = userMapper.getAllUsers();
        System.out.println("查询到的用户列表：");
        for (User user : users) {
            System.out.println(user);
        }
        return users;
    }


    @RequestMapping("/delete")
    public Integer deleteUser(@RequestParam("user_id") String user_id) throws IOException {
        if (user_id == null || user_id.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        User user = userMapper.getUserById(user_id);
        if (user == null) {
            return 0;  // 用户不存在时返回0
        }
        // 不删除后端的图片，只删除数据库中的
        return userMapper.deleteUser(user_id);
    }

    @GetMapping("/getNextUser_id")
    public String getNextUser_id() {
        return userMapper.findNextUser_id();
    }
}


