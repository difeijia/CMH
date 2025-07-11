package com.neuedu.cmh.service;

import com.neuedu.cmh.entity.Course;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MoonshotService {

    @Value("${moonshot.api.key}")
    private String apiKey;

    @Value("${moonshot.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public MoonshotService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateCourseContent(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "moonshot-v1-8k"); // 修改为正确的模型名称
        requestBody.put("messages", new Object[]{
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
        });
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                Map<String, Object> choice = choices.get(0);
                Map<String, String> message = (Map<String, String>) choice.get("message");
                return message.get("content");
            }
            return "无法生成内容，请重试";
        } catch (Exception e) {
            e.printStackTrace();
            return "调用Moonshot API失败: " + e.getMessage();
        }
    }
    public String generateMindMapContent(List<Course> courses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据以下课程信息生成一个Markdown格式的思维导图大纲。\n");
        prompt.append("课程列表:\n");

        for (Course course : courses) {
            prompt.append("- ").append(course.getCourseName())
                    .append(" (ID: ").append(course.getCourseId())
                    .append(", 作者: ").append(course.getCourseAuthor())
                    .append(")\n");
        }

        prompt.append("\n请按照以下格式返回思维导图:\n");
        prompt.append("```markdown\n");
        prompt.append("# 课程管理系统思维导图\n");
        prompt.append("## 课程分类\n");
        prompt.append("- 技术类课程\n");
        prompt.append("  - [课程名称1]\n");
        prompt.append("  - [课程名称2]\n");
        prompt.append("## 作者分布\n");
        prompt.append("- [作者1]\n");
        prompt.append("  - [课程名称1]\n");
        prompt.append("```\n");

        return generateCourseContent(prompt.toString());
    }
    public byte[] generateImage(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-image");
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1); // 生成1张图片
        requestBody.put("size", "1024x1024"); // 图片尺寸

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                    apiUrl + "/images/generations",
                    request,
                    byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            throw new RuntimeException("API调用失败: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("生成图片失败: " + e.getMessage());
        }
    }
}