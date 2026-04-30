package com.hospital.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginService {
    // token -> { "role": "PATIENT|DOCTOR|ADMIN", "userId": "..." }
    private final Map<String, Map<String, String>> tokenStore = new ConcurrentHashMap<>();

    public String createToken(String role, String userId, String name) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, Map.of("role", role, "userId", userId, "name", name));
        return token;
    }

    public Map<String, String> validateToken(String token) {
        return tokenStore.get(token);
    }

    public void removeToken(String token) {
        tokenStore.remove(token);
    }
}
