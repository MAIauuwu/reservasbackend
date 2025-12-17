package com.example.reservabackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow requests from any origin by default; for production set explicit origins
        registry.addMapping("/**").allowedOrigins("*").allowedMethods("GET", "POST", "OPTIONS").allowedHeaders("*");
    }
}
