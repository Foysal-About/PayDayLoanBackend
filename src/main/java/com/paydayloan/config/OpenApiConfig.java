package com.paydayloan.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PayDay Loan Backend API")
                        .version("1.0.0")
                        .description("Enterprise-Grade PayDay Loan Application API")
                        .contact(new Contact()
                                .name("PayDay Loan Team")
                                .email("support@paydayloan.com")));
    }
}
