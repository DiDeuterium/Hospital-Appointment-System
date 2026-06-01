package com.hospital.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AppointmentVO {
    private Integer apptId;
    private Integer patientId;
    private String patientName;
    private String patientGender;
    private String patientPhone;
    private Integer scheduleId;
    private Integer queueNumber;
    private Integer status;
    private String cancelReason;
    private Integer docId;
    private String docName;
    private String title;
    private Integer deptId;
    private String deptName;
    private LocalDate workDate;
    private String shift;
    private BigDecimal fee;
    private Integer paymentId;
    private BigDecimal amount;
    private Integer payStatus;
    private String payMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
