package com.hospital.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScheduleVO {
    private Integer scheduleId;
    private Integer docId;
    private String docName;
    private String title;
    private Integer deptId;
    private String deptName;
    private LocalDate workDate;
    private String shift;
    private Integer totalQuota;
    private Integer restQuota;
    private BigDecimal fee;
    private String avatarUrl;
    private String specialty;
}
