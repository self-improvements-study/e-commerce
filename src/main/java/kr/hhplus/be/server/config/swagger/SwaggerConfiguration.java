package kr.hhplus.be.server.config.swagger;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SwaggerConfiguration {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce API")
                        .version("1.0")
                        .description("E-Commerce 서비스의 API 명세서")
                        .contact(new Contact()
                                .name("개발팀")
                                .email("support@company.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("GitHub Repository")
                        .url("https://github.com/self-improvements-study/e-commerce"));
    }

}
