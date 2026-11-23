package edu.handong.week12.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookOpenAPI() {
        return new OpenAPI().info(new Info().title("Week12 Book REST API").description("DTO, Validation, 전역 예외 처리, Pagination/Sorting을 적용한 도서 관리 API").version("v1.0.0"));
    }
}
