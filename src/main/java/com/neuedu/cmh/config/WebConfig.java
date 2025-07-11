package com.neuedu.cmh.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源的访问路径
        registry.addResourceHandler("/uploads/**") // 匹配 /uploads/ 开头的路径
                .addResourceLocations("file:uploads/"); // 实际的文件存储路径
    }
}