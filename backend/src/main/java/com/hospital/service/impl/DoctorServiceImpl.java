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
    public LoginResponse login(String docId, String password) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(401, "工号或密码错误");
        }
        if (!encoder.matches(password, doctor.getPassword())) {
            throw new BusinessException(401, "工号或密码错误");
        }
        String token = loginService.createToken("DOCTOR", doctor.getDocId(), doctor.getDocName());
        return new LoginResponse(token, doctor.getDocId(), doctor.getDocName());
    }

    @Override
    public List<Doctor> list(String deptId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        if (deptId != null && !deptId.isBlank()) {
            wrapper.eq(Doctor::getDeptId, deptId);
        }
        wrapper.orderByAsc(Doctor::getDocId);
        return doctorMapper.selectList(wrapper);
    }

    @Override
    public Doctor getById(String docId) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        return doctor;
    }

    @Override
    public void add(DoctorRequest request) {
        Doctor exist = doctorMapper.selectById(request.getDocId());
        if (exist != null) {
            throw new BusinessException(409, "医生工号已存在");
        }
        Doctor doctor = new Doctor();
        doctor.setDocId(request.getDocId());
        doctor.setDocName(request.getDocName());
        doctor.setGender(request.getGender());
        doctor.setTitle(request.getTitle());
        doctor.setDeptId(request.getDeptId());
        doctor.setPassword(encoder.encode(request.getPassword()));
        doctorMapper.insert(doctor);
    }

    @Override
    public void update(String docId, DoctorRequest request) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        doctor.setDocName(request.getDocName());
        doctor.setGender(request.getGender());
        doctor.setTitle(request.getTitle());
        doctor.setDeptId(request.getDeptId());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            doctor.setPassword(encoder.encode(request.getPassword()));
        }
        doctorMapper.updateById(doctor);
    }

    @Override
    public void delete(String docId) {
        Doctor doctor = doctorMapper.selectById(docId);
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        doctorMapper.deleteById(docId);
    }
}
