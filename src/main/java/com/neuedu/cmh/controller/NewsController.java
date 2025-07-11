package com.neuedu.cmh.controller;

import com.neuedu.cmh.entity.News;
import com.neuedu.cmh.mapper.NewsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {

    @Autowired
    private NewsMapper newsMapper;

    /**
     * 获取资讯列表（所有租户可见）
     */
    @GetMapping("/list")
    public Map<String, Object> getNewsList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 计算偏移量
            int offset = (page - 1) * size;

            // 修改Mapper调用，添加分页参数
            List<News> newsList = newsMapper.getAllNewsList(title, summary, author, offset, size);
            int total = newsMapper.countNews(title, summary, author);

            result.put("code", 200);
            result.put("data", newsList);
            result.put("total", total);
            result.put("msg", "查询成功");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 新增资讯（自动关联当前租户）
     */
    @PostMapping("/add")
    public Map<String, Object> addNews(
            @RequestBody News news,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 验证必填字段
            if (news.getNews_title() == null || news.getNews_title().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "新闻标题不能为空");
                return result;
            }
            if (news.getNews_content() == null || news.getNews_content().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "新闻内容不能为空");
                return result;
            }

            // 设置新闻ID和租户ID
            news.setNews_id("N" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            news.setTenant_id(tenantId);

            int rows = newsMapper.addNews(news);
            if (rows > 0) {
                result.put("code", 200);
                result.put("msg", "添加成功");
            } else {
                result.put("code", 500);
                result.put("msg", "添加失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "添加异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 修改资讯（验证租户权限）
     */
    @PostMapping("/update")
    public Map<String, Object> updateNews(
            @RequestBody News news,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 验证必填字段
            if (news.getNews_title() == null || news.getNews_title().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "新闻标题不能为空");
                return result;
            }

            // 检查资讯是否存在
            News existingNews = newsMapper.getNewsById(news.getNews_id());
            if (existingNews == null) {
                result.put("code", 404);
                result.put("msg", "资讯不存在");
                return result;
            }

            // 验证租户权限
            if (!existingNews.getTenant_id().equals(tenantId)) {
                result.put("code", 403);
                result.put("msg", "无权修改其他租户的资讯");
                return result;
            }

            // 更新操作
            int rows = newsMapper.updateNews(news);
            if (rows > 0) {
                result.put("code", 200);
                result.put("msg", "更新成功");
                result.put("data", newsMapper.getNewsById(news.getNews_id())); // 返回更新后的数据
            } else {
                result.put("code", 500);
                result.put("msg", "更新失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "更新异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除资讯（验证租户权限）
     */
    @DeleteMapping("/delete/{newsId}")
    public Map<String, Object> deleteNews(
            @PathVariable String newsId,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        Map<String, Object> result = new HashMap<>();
        try {
            // 检查资讯是否存在
            News existingNews = newsMapper.getNewsById(newsId);
            if (existingNews == null) {
                result.put("code", 404);
                result.put("msg", "资讯不存在");
                return result;
            }

            // 验证租户权限
            if (!existingNews.getTenant_id().equals(tenantId)) {
                result.put("code", 403);
                result.put("msg", "无权删除其他租户的资讯");
                return result;
            }

            int rows = newsMapper.deleteNews(newsId);
            if (rows > 0) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 500);
                result.put("msg", "删除失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "删除异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取资讯详情（所有租户可见）
     */
    @GetMapping("/detail/{newsId}")
    public Map<String, Object> getNewsDetail(@PathVariable String newsId) {
        Map<String, Object> result = new HashMap<>();
        try {
            News news = newsMapper.getNewsById(newsId);
            if (news != null) {
                result.put("code", 200);
                result.put("data", news);
                result.put("msg", "查询成功");
            } else {
                result.put("code", 404);
                result.put("msg", "资讯不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "查询异常: " + e.getMessage());
        }
        return result;
    }
    // 添加文件上传路径配置


    /**
     * 图片上传接口
     */
// 在NewsController类中添加以下代码

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 图片上传接口
     */
    @PostMapping("/upload")
    public Map<String, Object> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        Map<String, Object> result = new HashMap<>();

        try {
            // 验证文件是否为空
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "上传文件不能为空");
                return result;
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                result.put("code", 400);
                result.put("msg", "只支持JPG/PNG格式图片");
                return result;
            }

            // 验证文件大小 (2MB限制)
            if (file.getSize() > 2 * 1024 * 1024) {
                result.put("code", 400);
                result.put("msg", "图片大小不能超过2MB");
                return result;
            }

            // 创建上传目录(如果不存在)
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            File dest = new File(uploadDir + File.separator + newFilename);
            file.transferTo(dest);

            // 返回相对路径 (前端会拼接完整URL)
            String relativePath = "/" + newFilename;

            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("data", relativePath);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "上传失败: " + e.getMessage());
        }

        return result;
    }
// 在NewsController.java中添加以下方法

    /**
     * 小程序获取资讯列表接口
     */
    @GetMapping("/applet_search_page")
    public Map<String, Object> appletSearchPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * size;
            List<News> newsList = newsMapper.getAllNewsList(null, null, null, offset, size);
            int total = newsMapper.countNews(null, null, null);

            // 转换为前端需要的格式
            List<Map<String, Object>> formattedList = newsList.stream().map(news -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", news.getNews_id());
                item.put("title", news.getNews_title());
                item.put("introduction", news.getNews_summary());
                item.put("image", news.getNews_image_path());
                return item;
            }).collect(Collectors.toList());

            // 修改返回结构，增加content层级
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("content", formattedList);
            contentMap.put("total", total);

            result.put("code", 200);
            result.put("message", "success");
            result.put("data", new HashMap<String, Object>() {{
                put("newsList", contentMap); // 确保结构与前端匹配
            }});
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取数据失败: " + e.getMessage());
        }
        return result;
    }
    /**
     * 小程序获取资讯详情接口
     * 专门为小程序设计的详情接口，返回结构更匹配前端需求
     */
    @GetMapping("/applet_detail/{newsId}")
    public Map<String, Object> getAppletNewsDetail(@PathVariable String newsId) {
        Map<String, Object> result = new HashMap<>();
        try {
            News news = newsMapper.getNewsById(newsId);
            if (news != null) {
                // 构建小程序专用的数据结构
                Map<String, Object> detailData = new HashMap<>();
                detailData.put("id", news.getNews_id());
                detailData.put("title", news.getNews_title());
                detailData.put("content", news.getNews_content());
                detailData.put("image", news.getNews_image_path());
                detailData.put("author", news.getAuthor());
                detailData.put("createTime", news.getCreate_time());
                detailData.put("summary", news.getNews_summary());

                result.put("code", 200);
                result.put("message", "success");
                result.put("data", detailData);
            } else {
                result.put("code", 404);
                result.put("message", "资讯不存在");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取详情失败: " + e.getMessage());
        }
        return result;
    }
    /**
     * AI生成简介接口
     */
    @PostMapping("/generate-summary")
    public Map<String, Object> generateSummaryWithAI(
            @RequestBody Map<String, String> requestData,
            @RequestHeader("X-Tenant-ID") String tenantId) {

        Map<String, Object> result = new HashMap<>();
        try {
            String title = requestData.get("title");
            String content = requestData.get("content");

            if ((title == null || title.isEmpty()) && (content == null || content.isEmpty())) {
                result.put("code", 400);
                result.put("msg", "标题和内容不能同时为空");
                return result;
            }

            // 调用DeepSeek API
            String prompt = "请根据以下内容生成一个简洁的新闻简介(50-100字):\n";
            if (title != null && !title.isEmpty()) {
                prompt += "标题: " + title + "\n";
            }
            if (content != null && !content.isEmpty()) {
                // 截取前500字作为内容，避免太长
                String shortContent = content.length() > 500 ? content.substring(0, 500) + "..." : content;
                prompt += "内容: " + shortContent;
            }

            // 调用DeepSeek API
            String apiKey = "sk-e4282e53e1454600a5dfb3e502301f1d";
            String apiUrl = "https://api.deepseek.com/v1/chat/completions";

            // 创建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "deepseek-chat");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 150);

            // 发送请求
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            // 处理响应
            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> firstChoice = choices.get(0);
                        Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                        String summary = message.get("content").trim();

                        result.put("code", 200);
                        result.put("msg", "生成成功");
                        result.put("data", Map.of("summary", summary));
                        return result;
                    }
                }
            }

            result.put("code", 500);
            result.put("msg", "AI生成简介失败");
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "AI生成简介异常: " + e.getMessage());
        }
        return result;
    }
}