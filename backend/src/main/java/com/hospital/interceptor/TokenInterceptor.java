package com.hospital.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.Result;
import com.hospital.service.LoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private final LoginService loginService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenInterceptor(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);
        if (token == null) {
            writeError(response, 401, "未登录，请先登录");
            return false;
        }

        Map<String, String> userInfo = loginService.validateToken(token);
        if (userInfo == null) {
            writeError(response, 401, "登录已过期，请重新登录");
            return false;
        }

        request.setAttribute("role", userInfo.get("role"));
        request.setAttribute("userId", userInfo.get("userId"));
        request.setAttribute("userName", userInfo.get("name"));
        return true;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) {
            return null;
        }
        if (header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return header;
    }

    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code, message)));
    }
}
