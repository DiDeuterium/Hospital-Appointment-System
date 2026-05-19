package com.hospital.dto.response;

import lombok.Data;

@Data
public class PatientProfileVO {
    private Integer patientId;
    private String idCard;
    private String realName;
    private String gender;
    private String phone;
}
