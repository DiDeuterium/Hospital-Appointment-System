package com.hospital.controller;

import com.hospital.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 前端启动时校验 token 是否仍在 LoginService.tokenStore 中。
    // TokenInterceptor 已经做了校验；若 token 失效会直接返回 401，根本走不到这里。
    @GetMapping("/me")
    public Result<Map<String, String>> me(HttpServletRequest request) {
        Map<String, String> info = new HashMap<>();
        info.put("role", (String) request.getAttribute("role"));
        info.put("userId", (String) request.getAttribute("userId"));
        info.put("name", (String) request.getAttribute("userName"));
        return Result.ok(info);
    }
}
