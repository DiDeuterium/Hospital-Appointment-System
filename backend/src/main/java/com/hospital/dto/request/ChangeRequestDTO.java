package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ChangeRequestDTO {
    @NotNull(message = "变更类型不能为空")
    private Integer changeType;  // 1=停诊 2=修改排班

    private LocalDate targetWorkDate;
    private String targetShift;
    private Integer targetTotalQuota;

    @NotBlank(message = "申请原因不能为空")
    private String reason;
}
