package com.hospital.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleRequest {
    @NotBlank(message = "医生工号不能为空")
    private String docId;

    @NotNull(message = "出诊日期不能为空")
    @FutureOrPresent(message = "出诊日期不能早于今天")
    private LocalDate workDate;

    @NotBlank(message = "时段不能为空")
    private String shift;

    @Min(value = 1, message = "总号源数至少为1")
    private Integer totalQuota;
}
