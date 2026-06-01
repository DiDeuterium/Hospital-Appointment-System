package com.hospital.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleRequest {
    @NotNull(message = "医生工号不能为空")
    private Integer docId;

    @NotNull(message = "出诊日期不能为空")
    @FutureOrPresent(message = "出诊日期不能早于今天")
    private LocalDate workDate;

    @NotBlank(message = "时段不能为空")
    @Pattern(regexp = "上午|下午|夜诊", message = "时段必须为上午、下午或夜诊")
    private String shift;

    @Min(value = 1, message = "总号源数至少为1")
    private Integer totalQuota;

    @DecimalMin(value = "0.00", message = "挂号费不能为负数")
    private BigDecimal fee;
}
