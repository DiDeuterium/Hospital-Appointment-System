package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DoctorRequest {
    @NotBlank(message = "医生工号不能为空")
    private String docId;

    @NotBlank(message = "姓名不能为空")
    private String docName;

    @Pattern(regexp = "[MF]", message = "性别必须为 M 或 F")
    private String gender;

    private String title;

    @NotBlank(message = "所属科室不能为空")
    private String deptId;

    @NotBlank(message = "密码不能为空")
    private String password;
}
