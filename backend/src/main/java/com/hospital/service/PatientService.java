package com.hospital.service;

import com.hospital.dto.request.PasswordChangeRequest;
import com.hospital.dto.request.PatientRegisterRequest;
import com.hospital.dto.request.PatientUpdateRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.dto.response.PatientProfileVO;
import com.hospital.entity.Patient;

public interface PatientService {
    LoginResponse register(PatientRegisterRequest request);
    LoginResponse login(String idCard, String password);
    Patient getById(Integer patientId);

    PatientProfileVO getProfile(Integer patientId);
    PatientProfileVO updateProfile(Integer patientId, PatientUpdateRequest request);
    void changePassword(Integer patientId, PasswordChangeRequest request);
}
