package com.neuedu.cmh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


@Service
public class WenxinContentService {

    @Value("${wenxin.api.key}")
    private String apiKey;

    @Value("${wenxin.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public WenxinContentService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String generateCompanyProfile(String companyName, String existingContent) {

        // 构建更智能的提示词，考虑现有内容
        String prompt = String.format(
                "请基于以下信息为%s公司完善专业简介：\n" +
                        "---- 现有内容 ----\n" +
                        "%s\n" +
                        "要求：\n" +
                        "1. 保持现有内容的风格连贯性\n" +
                        "2. 补充300字左右的专业内容\n" +
                        "3. 重点突出技术实力和客户价值",
                companyName,
                existingContent
        );

        System.out.println("生成提示词：" + prompt);

        return webClient.post()
                .uri(apiUrl)
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "deepseek-chat",
                        "messages", new Object[]{
                                Map.of("role", "user", "content", prompt)
                        },
                        "temperature", 0.7,
                        "max_tokens", 1000
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response != null && response.containsKey("choices")) {
                        Map<String, Object> firstChoice = ((List<Map<String, Object>>) response.get("choices")).get(0);
                        Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                        return message.get("content");
                    }
                    return "未能生成公司简介，请手动填写。";
                })
                .onErrorReturn("公司简介生成服务暂时不可用，请稍后重试或手动填写。")
                .block();
    }
}