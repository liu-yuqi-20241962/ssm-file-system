package com.example.ssm.config;

import com.example.ssm.interceptor.LoginInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类。
 * 按任务书要求：使用 WebMvcConfigurer#addInterceptors 注册拦截器（不使用 XML）。
 * 放行路径包含登录页、登录接口以及 CSS/JS/图片等静态资源。
 *
 * 注意：本项目为纯 XML 装配，Spring 6.1 起 <mvc:annotation-driven/> 不再注册
 * DelegatingWebMvcConfiguration，须在 spring-mvc.xml 中显式声明该委托类，
 * 本类的 addInterceptors 才会被调用。
 */
@Component
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                // 放行：登录页与登录接口（/login 的 GET/POST）、静态资源、错误页
                .excludePathPatterns("/login", "/static/**", "/error");
    }
}
