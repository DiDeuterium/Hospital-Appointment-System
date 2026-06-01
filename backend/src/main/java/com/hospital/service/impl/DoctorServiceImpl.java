package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.request.DoctorRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.entity.Doctor;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.DoctorMapper;
import com.hospital.service.DoctorService;
import com.hospital.service.LoginService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorMapper doctorMapper;
    private final LoginService loginService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DoctorServiceImpl(DoctorMapper doctorMapper, LoginService loginService) {
        this.doctorMapper = doctorMapper;
        this.loginService = loginService;
    }

    @Override
    public LoginResponse login(Integer docId, String password) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(401, "工号或密码错误");
        }
        if (doctor.getStatus() != null && doctor.getStatus() == 0) {
            throw new BusinessException(403, "账号已停用，请联系管理员");
        }
        if (!encoder.matches(password, doctor.getPassword())) {
            throw new BusinessException(401, "工号或密码错误");
        }
        String token = loginService.createToken("DOCTOR", String.valueOf(doctor.getDocId()), doctor.getDocName());
        return new LoginResponse(token, String.valueOf(doctor.getDocId()), doctor.getDocName());
    }

    @Override
    public List<Doctor> list(Integer deptId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) {
            wrapper.eq(Doctor::getDeptId, deptId);
        }
        wrapper.eq(Doctor::getStatus, 1);
        wrapper.orderByAsc(Doctor::getDocId);
        return doctorMapper.selectList(wrapper);
    }

    @Override
    public List<Doctor> listAll(Integer deptId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null) {
            wrapper.eq(Doctor::getDeptId, deptId);
        }
        wrapper.orderByAsc(Doctor::getDocId);
        return doctorMapper.selectList(wrapper);
    }

    @Override
    public Doctor getById(Integer docId) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        return doctor;
    }

    @Override
    public void add(DoctorRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException(400, "密码不能为空");
        }
        Doctor doctor = new Doctor();
        doctor.setDocName(request.getDocName());
        doctor.setGender(request.getGender());
        doctor.setTitle(request.getTitle());
        doctor.setDeptId(request.getDeptId());
        doctor.setPassword(encoder.encode(request.getPassword()));
        doctor.setAvatarUrl(request.getAvatarUrl());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setStatus(1);
        doctorMapper.insert(doctor);
    }

    @Override
    public void update(Integer docId, DoctorRequest request) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        doctor.setDocName(request.getDocName());
        doctor.setGender(request.getGender());
        doctor.setTitle(request.getTitle());
        doctor.setDeptId(request.getDeptId());
        doctor.setAvatarUrl(request.getAvatarUrl());
        doctor.setSpecialty(request.getSpecialty());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            doctor.setPassword(encoder.encode(request.getPassword()));
        }
        doctorMapper.updateById(doctor);
    }

    @Override
    public void updateStatus(Integer docId, Integer status) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        doctor.setStatus(status);
        doctorMapper.updateById(doctor);
    }

    @Override
    public void delete(Integer docId) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        doctorMapper.deleteById(docId);
    }
}
