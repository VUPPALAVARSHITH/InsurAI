package com.insurai.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 👇 UPDATED PATH
    private static final String UPLOAD_DIR = "C:/Users/varsh/OneDrive/Documents/InsurAI_Uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + UPLOAD_DIR)
                .setCachePeriod(0)
                .resourceChain(true);
    }
}