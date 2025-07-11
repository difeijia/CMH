package com.neuedu.cmh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class StableDiffusionService {

    @Value("${stable-diffusion.api-url}")
    private String apiUrl;

    @Value("${stable-diffusion.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public StableDiffusionService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String generateImage(String prompt) {
        // 1. 严格验证prompt
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }

        try {
            // 2. 构建符合Stability AI最新API要求的请求体
            Map<String, Object> requestBody = new LinkedHashMap<>();

            List<Map<String, Object>> textPrompts = new ArrayList<>();
            Map<String, Object> textPrompt = new LinkedHashMap<>();
            textPrompt.put("text", prompt.trim());
            textPrompt.put("weight", 1.0);
            textPrompts.add(textPrompt);

            requestBody.put("text_prompts", textPrompts);
            requestBody.put("cfg_scale", 7);
            requestBody.put("height", 1024);
            requestBody.put("width", 1024);
            requestBody.put("samples", 1);
            requestBody.put("steps", 30);
            requestBody.put("style_preset", "digital-art");

            // 3. 打印请求日志（调试用）
            try {
                System.out.println("Sending to Stability AI: " + objectMapper.writeValueAsString(requestBody));
            } catch (JsonProcessingException e) {
                System.err.println("Failed to log request: " + e.getMessage());
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Accept", "application/json");

            // 4. 使用RestTemplate发送请求
            try {
                ResponseEntity<Map> response = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.POST,
                        new HttpEntity<>(requestBody, headers),
                        Map.class
                );

                // 5. 处理响应
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    List<Map<String, Object>> artifacts = (List<Map<String, Object>>) response.getBody().get("artifacts");
                    if (artifacts != null && !artifacts.isEmpty()) {
                        String base64Image = (String) artifacts.get(0).get("base64");
                        if (base64Image != null) {
                            // 保存图片到服务器
                            String fileName = "sd-image-" + UUID.randomUUID() + ".png";
                            Path uploadDir = Paths.get("uploads/ai-images");
                            Files.createDirectories(uploadDir);
                            Path filePath = uploadDir.resolve(fileName);

                            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                            Files.write(filePath, imageBytes);

                            return "/uploads/ai-images/" + fileName;
                        }
                    }
                }
                throw new RuntimeException("API response format unexpected");
            } catch (HttpClientErrorException e) {
                throw new RuntimeException("API request failed: " + e.getResponseBodyAsString(), e);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

}