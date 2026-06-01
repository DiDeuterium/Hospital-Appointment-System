package com.hospital.service;

import com.hospital.dto.request.DoctorRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.entity.Doctor;

import java.util.List;

public interface DoctorService {
    LoginResponse login(Integer docId, String password);
    List<Doctor> list(Integer deptId);
    List<Doctor> listAll(Integer deptId);
    Doctor getById(Integer docId);
    void add(DoctorRequest request);
    void update(Integer docId, DoctorRequest request);
    void updateStatus(Integer docId, Integer status);
    void delete(Integer docId);
}
