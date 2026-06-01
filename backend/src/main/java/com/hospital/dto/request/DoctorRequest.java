package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DoctorRequest {
    @NotBlank(message = "姓名不能为空")
    private String docName;

    @Pattern(regexp = "[MF]", message = "性别必须为 M 或 F")
    private String gender;

    private String title;

    @NotNull(message = "所属科室不能为空")
    private Integer deptId;

    private String password;
    private String avatarUrl;
    private String specialty;
}
