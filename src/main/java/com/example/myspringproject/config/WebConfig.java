package com.example.myspringproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v2/**") // Разрешить CORS для всех путей внутри /api/v2
                .allowedOrigins("http://localhost:3000") // URL вашего фронтенда
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Разрешенные HTTP методы
                .allowedHeaders("*") // Разрешить все заголовки
                .allowCredentials(false); // Установите в true, если нужны куки или авторизация
        // (но для простого случая false достаточно)
    }
}