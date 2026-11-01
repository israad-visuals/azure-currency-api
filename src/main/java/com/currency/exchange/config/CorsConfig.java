package com.currency.exchange.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/custom-currencies").allowedOrigins("*").allowedMethods("GET", "POST")
                .allowedHeaders("Content-Type");
        registry.addMapping("/api/**").allowedOrigins("*").allowedMethods("GET");
    }
}
