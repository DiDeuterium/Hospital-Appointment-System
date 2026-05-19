package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PatientUpdateRequest {
    @NotBlank(message = "姓名不能为空")
    private String realName;

    @Pattern(regexp = "[MF]", message = "性别必须为 M 或 F")
    private String gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
