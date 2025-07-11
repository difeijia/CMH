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
public class DeepSeekService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public DeepSeekService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateCourseContent(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("messages", new Object[]{
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", prompt);
                }}
        });
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 2000);

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
            return "调用大模型服务失败: " + e.getMessage();
        }
    }

    public String generateMindMapPrompt(List<Course> courses) {
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

        return prompt.toString();
    }

    public String generateSearchLinksPrompt(List<Course> courses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为以下课程生成相关的学习资源链接，每个课程提供3个高质量的学习网址。\n");
        prompt.append("返回格式要求:\n");
        prompt.append("```\n");
        prompt.append("## [课程名称]\n");
        prompt.append("- [资源1标题](URL)\n");
        prompt.append("- [资源2标题](URL)\n");
        prompt.append("- [资源3标题](URL)\n");
        prompt.append("```\n");
        prompt.append("\n课程列表:\n");

        for (Course course : courses) {
            prompt.append("- ").append(course.getCourseName())
                    .append(": ").append(course.getCourseIntroduction())
                    .append("\n");
        }

        return prompt.toString();
    }

    public String generateSummaryPrompt(List<Course> courses) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请对以下课程信息进行专业总结，包括:\n");
        prompt.append("1. 课程总数统计\n");
        prompt.append("2. 按作者分类统计\n");
        prompt.append("3. 课程内容主题分析\n");
        prompt.append("4. 学习资源丰富度评估\n");
        prompt.append("\n课程数据:\n");

        for (Course course : courses) {
            prompt.append("- ").append(course.getCourseName())
                    .append(" (作者: ").append(course.getCourseAuthor())
                    .append("): ").append(course.getCourseIntroduction())
                    .append("\n");
        }

        prompt.append("\n请用专业、简洁的语言进行总结，适合教育管理者阅读。");

        return prompt.toString();
    }
    public String generateMindMapContent(List<Course> courses) {
        String prompt = generateMindMapPrompt(courses);
        return generateCourseContent(prompt);
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