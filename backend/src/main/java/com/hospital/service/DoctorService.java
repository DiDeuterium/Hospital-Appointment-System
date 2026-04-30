package com.hospital.service;

import com.hospital.dto.request.DoctorRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.entity.Doctor;

import java.util.List;

public interface DoctorService {
    LoginResponse login(String docId, String password);
    List<Doctor> list(String deptId);
    Doctor getById(String docId);
    void add(DoctorRequest request);
    void update(String docId, DoctorRequest request);
    void delete(String docId);
}
