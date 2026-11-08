package com.example.ssm.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：统一登录校验。
 * 未登录（session 无 loginUser）访问受保护页面时，重定向到登录页。
 * 放行路径在 WebMvcConfig#addInterceptors 中通过 excludePathPatterns 配置（登录页、登录接口、静态资源）。
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            return true;
        }
        // 未登录：重定向到登录页
        response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
