package com.hospital.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DoctorScheduleVO {
    private Integer scheduleId;
    private LocalDate workDate;
    private String shift;
    private Integer totalQuota;
    private Integer restQuota;
    private Integer appointedCount;
}
