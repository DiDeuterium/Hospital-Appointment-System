package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.dto.request.PatientLoginRequest;
import com.hospital.dto.request.PatientRegisterRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody PatientRegisterRequest request) {
        return Result.ok("注册成功", patientService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody PatientLoginRequest request) {
        return Result.ok("登录成功", patientService.login(request.getIdCard(), request.getPassword()));
    }
}
