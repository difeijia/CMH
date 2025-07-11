package com.neuedu.cmh.service;

import com.neuedu.cmh.entity.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class MinimaxService {
    private static final String DEFAULT_ERROR = "生成失败，请稍后重试";

    @Value("${minimax.api.key}")
    private String apiKey;

    @Value("${minimax.api.url}")
    private String apiUrl;

    @Value("${minimax.group.id}")
    private String groupId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MinimaxService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateCourseContent(String prompt) {
        // 1. 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 2. 构建符合Minimax要求的请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "abab5.5-chat");
        requestBody.put("stream", false);  // 明确关闭流式响应
        requestBody.put("temperature", 0.7);

        // 3. 构建符合Minimax要求的messages结构
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "sender_type", "USER",
                "text", prompt
        ));
        requestBody.put("messages", messages);

        // 4. 添加必填参数
        Map<String, Object> botSetting = new HashMap<>();
        botSetting.put("bot_name", "CourseBot");
        botSetting.put("content", "你是一个专业的课程简介生成助手");
        requestBody.put("bot_setting", List.of(botSetting));
        requestBody.put("reply_constraints", Map.of(
                "sender_type", "BOT",
                "sender_name", "CourseBot"
        ));

        try {
            // 5. 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            // 6. 解析响应
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<>() {}
                );

                // 7. 检查错误响应
                if (body.containsKey("base_resp")) {
                    Map<String, Object> baseResp = (Map<String, Object>) body.get("base_resp");
                    if (Integer.parseInt(baseResp.get("status_code").toString()) != 0) {
                        return DEFAULT_ERROR + ": " + baseResp.get("status_msg");
                    }
                }

                // 8. 提取回复内容
                return (String) Optional.ofNullable(body.get("reply"))
                        .orElseGet(() -> extractFromChoices(body));
            }
            return DEFAULT_ERROR + ": HTTP " + response.getStatusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return DEFAULT_ERROR + ": " + e.getMessage();
        }
    }

    private String extractFromChoices(Map<String, Object> json) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) json.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                return (String) message.get("text");
            }
            return DEFAULT_ERROR + "(无有效回复)";
        } catch (Exception e) {
            return DEFAULT_ERROR + "(解析异常)";
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