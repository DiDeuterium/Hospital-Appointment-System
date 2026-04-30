package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {
    @NotBlank(message = "科室编号不能为空")
    private String deptId;

    @NotBlank(message = "科室名称不能为空")
    private String deptName;

    private String location;
    private String description;
}
