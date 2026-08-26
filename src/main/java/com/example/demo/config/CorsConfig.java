package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
         registry.addMapping("/**")//Applies to all API routes
                 .allowedOrigins("http://localhost:5173") // the React app origin
                 .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")// HTTP methods permitted
                 .allowedHeaders( "*")
                 .allowCredentials(true);// permits cookies/auth headers if needed later

    }
}
