package com.hospital.service;

import com.hospital.dto.request.PatientRegisterRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.entity.Patient;

public interface PatientService {
    LoginResponse register(PatientRegisterRequest request);
    LoginResponse login(String idCard, String password);
    Patient getById(Integer patientId);
}
