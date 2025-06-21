package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get("image-service/uploads/").toAbsolutePath().toUri().toString();

        registry
                .addResourceHandler("/uploads/**") // URL truy cập
                .addResourceLocations(uploadPath); // Thư mục thực tế
    }
}
