package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.request.PasswordChangeRequest;
import com.hospital.dto.request.PatientRegisterRequest;
import com.hospital.dto.request.PatientUpdateRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.dto.response.PatientProfileVO;
import com.hospital.entity.Patient;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.PatientMapper;
import com.hospital.service.LoginService;
import com.hospital.service.PatientService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;
    private final LoginService loginService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public PatientServiceImpl(PatientMapper patientMapper, LoginService loginService) {
        this.patientMapper = patientMapper;
        this.loginService = loginService;
    }

    @Override
    public LoginResponse register(PatientRegisterRequest request) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getIdCard, request.getIdCard());
        if (patientMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "该身份证号已注册");
        }
        Patient patient = new Patient();
        patient.setIdCard(request.getIdCard());
        patient.setRealName(request.getRealName());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setPassword(encoder.encode(request.getPassword()));
        patientMapper.insert(patient);

        String token = loginService.createToken("PATIENT",
                patient.getPatientId().toString(), patient.getRealName());
        return new LoginResponse(token, patient.getPatientId(), patient.getRealName());
    }

    @Override
    public LoginResponse login(String idCard, String password) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getIdCard, idCard);
        Patient patient = patientMapper.selectOne(wrapper);
        if (patient == null) {
            throw new BusinessException(401, "身份证号或密码错误");
        }
        if (!encoder.matches(password, patient.getPassword())) {
            throw new BusinessException(401, "身份证号或密码错误");
        }
        String token = loginService.createToken("PATIENT",
                patient.getPatientId().toString(), patient.getRealName());
        return new LoginResponse(token, patient.getPatientId(), patient.getRealName());
    }

    @Override
    public Patient getById(Integer patientId) {
        Patient patient = patientMapper.selectById(patientId);
        if (patient == null) {
            throw new BusinessException(404, "患者不存在");
        }
        return patient;
    }

    @Override
    public PatientProfileVO getProfile(Integer patientId) {
        return toVO(getById(patientId));
    }

    @Override
    public PatientProfileVO updateProfile(Integer patientId, PatientUpdateRequest request) {
        Patient patient = getById(patientId);

        // 手机号唯一性校验（排除自己）
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getPhone, request.getPhone())
               .ne(Patient::getPatientId, patientId);
        if (patientMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "该手机号已被使用");
        }

        patient.setPhone(request.getPhone());
        patientMapper.updateById(patient);
        return toVO(patient);
    }

    @Override
    public void changePassword(Integer patientId, PasswordChangeRequest request) {
        Patient patient = getById(patientId);
        if (!encoder.matches(request.getOldPassword(), patient.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        if (encoder.matches(request.getNewPassword(), patient.getPassword())) {
            throw new BusinessException(400, "新密码不能与原密码相同");
        }
        patient.setPassword(encoder.encode(request.getNewPassword()));
        patientMapper.updateById(patient);
    }

    private PatientProfileVO toVO(Patient patient) {
        PatientProfileVO vo = new PatientProfileVO();
        vo.setPatientId(patient.getPatientId());
        vo.setIdCard(patient.getIdCard());
        vo.setRealName(patient.getRealName());
        vo.setGender(patient.getGender());
        vo.setPhone(patient.getPhone());
        return vo;
    }
}
