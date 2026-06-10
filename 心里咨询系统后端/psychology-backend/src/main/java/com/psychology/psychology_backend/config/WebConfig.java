package com.psychology.psychology_backend.config;

import com.psychology.psychology_backend.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/wx-login",
                        "/api/user/register",
                        "/api/user/login",
                        "/api/user/online-status/*",
                        "/api/user/online-status-list",
                        "/api/admin/login",
                        "/api/counselor/login",
                        "/api/public/**",
                        "/api/counselor/list",
                        "/api/schedule/**",
                        "/api/science/**",
                        "/test/**",
                        "/api/admin/schedules/**",
                        "/api/admin/counselors/**",
                        "/api/admin/users/**",
                        "/api/admin/messages/**",
                        "/api/counselors/online-status-list",
                        "/api/counselors/recommend",
                        "/api/tests",
                        "/api/tests/*/questions",
                        "/api/tests/types"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}
