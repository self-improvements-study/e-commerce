package kr.hhplus.be.server.config.web;

import kr.hhplus.be.server.config.interceptor.CustomInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CustomInterceptor customInterceptor;

    public WebConfig(CustomInterceptor customInterceptor) {
        this.customInterceptor = customInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 인터셉터 등록
        registry.addInterceptor(customInterceptor)
                .addPathPatterns("/api/**");  // 인터셉터를 적용할 URL 패턴을 지정
    }
}
