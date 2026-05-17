package com.hospital.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentVO {
    private Integer apptId;
    private Integer scheduleId;
    private Integer patientId;
    private String realName;
    private String phone;
    private String deptName;
    private String docName;
    private LocalDate workDate;
    private String shift;
    private Integer status;
    private LocalDateTime createTime;
}
