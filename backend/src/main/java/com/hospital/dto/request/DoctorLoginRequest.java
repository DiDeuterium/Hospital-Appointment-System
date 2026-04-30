package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorLoginRequest {
    @NotBlank(message = "医生工号不能为空")
    private String docId;

    @NotBlank(message = "密码不能为空")
    private String password;
}
