package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRequest {
    @NotNull(message = "审核结果不能为空")
    private Integer approved;  // 1=通过 0=驳回

    @NotBlank(message = "审核意见不能为空")
    private String remark;
}
