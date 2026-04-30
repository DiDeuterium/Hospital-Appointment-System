package com.hospital.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentRequest {
    @NotNull(message = "患者ID不能为空")
    private Integer patientId;

    @NotNull(message = "排班ID不能为空")
    private Integer scheduleId;
}
