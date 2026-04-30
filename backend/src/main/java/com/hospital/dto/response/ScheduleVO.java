package com.hospital.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScheduleVO {
    private Integer scheduleId;
    private String docId;
    private String docName;
    private String title;
    private String deptId;
    private String deptName;
    private LocalDate workDate;
    private String shift;
    private Integer totalQuota;
    private Integer restQuota;
}
