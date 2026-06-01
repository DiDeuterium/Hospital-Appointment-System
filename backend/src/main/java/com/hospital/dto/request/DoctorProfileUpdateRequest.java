package com.hospital.dto.request;

import lombok.Data;

@Data
public class DoctorProfileUpdateRequest {
    private String avatarUrl;
    private String specialty;
}
