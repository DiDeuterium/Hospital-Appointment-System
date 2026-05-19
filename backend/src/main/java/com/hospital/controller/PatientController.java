package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.dto.request.PasswordChangeRequest;
import com.hospital.dto.request.PatientLoginRequest;
import com.hospital.dto.request.PatientRegisterRequest;
import com.hospital.dto.request.PatientUpdateRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.dto.response.PatientProfileVO;
import com.hospital.exception.BusinessException;
import com.hospital.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping("/me")
    public Result<PatientProfileVO> getMe(HttpServletRequest request) {
        return Result.ok(patientService.getProfile(currentPatientId(request)));
    }

    @PutMapping("/me")
    public Result<PatientProfileVO> updateMe(HttpServletRequest request,
                                              @Valid @RequestBody PatientUpdateRequest body) {
        return Result.ok("资料更新成功",
                patientService.updateProfile(currentPatientId(request), body));
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(HttpServletRequest request,
                                        @Valid @RequestBody PasswordChangeRequest body) {
        patientService.changePassword(currentPatientId(request), body);
        return Result.ok("密码修改成功", null);
    }

    private Integer currentPatientId(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"PATIENT".equals(role)) {
            throw new BusinessException(403, "仅患者可访问个人中心");
        }
        try {
            return Integer.parseInt((String) request.getAttribute("userId"));
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "登录信息异常，请重新登录");
        }
    }
}
