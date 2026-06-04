package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.Result;
import com.hospital.dto.request.PasswordChangeRequest;
import com.hospital.dto.request.PatientLoginRequest;
import com.hospital.dto.request.PatientRegisterRequest;
import com.hospital.dto.request.PatientUpdateRequest;
import com.hospital.dto.response.LoginResponse;
import com.hospital.dto.response.PatientProfileVO;
import com.hospital.entity.Appointment;
import com.hospital.entity.PaymentRecord;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.AppointmentMapper;
import com.hospital.mapper.PaymentRecordMapper;
import com.hospital.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final PaymentRecordMapper paymentRecordMapper;
    private final AppointmentMapper appointmentMapper;

    public PatientController(PatientService patientService,
                              PaymentRecordMapper paymentRecordMapper,
                              AppointmentMapper appointmentMapper) {
        this.patientService = patientService;
        this.paymentRecordMapper = paymentRecordMapper;
        this.appointmentMapper = appointmentMapper;
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody PatientRegisterRequest request) {
        return Result.ok("注册成功", patientService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody PatientLoginRequest request) {
        return Result.ok("登录成功", patientService.login(request.getIdCard(), request.getPassword()));
    }

    @GetMapping("/me")
    public Result<PatientProfileVO> getMe(HttpServletRequest request) {
        return Result.ok(patientService.getProfile(currentPatientId(request)));
    }

    @PutMapping("/me")
    public Result<PatientProfileVO> updateMe(HttpServletRequest request,
                                              @Valid @RequestBody PatientUpdateRequest body) {
        return Result.ok("资料更新成功",
                patientService.updateProfile(currentPatientId(request), body));
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(HttpServletRequest request,
                                        @Valid @RequestBody PasswordChangeRequest body) {
        patientService.changePassword(currentPatientId(request), body);
        return Result.ok("密码修改成功", null);
    }

    @GetMapping("/me/payments")
    public Result<List<Map<String, Object>>> myPayments(HttpServletRequest request) {
        Integer patientId = currentPatientId(request);

        LambdaQueryWrapper<Appointment> apptWrapper = new LambdaQueryWrapper<>();
        apptWrapper.eq(Appointment::getPatientId, patientId);
        List<Integer> apptIds = appointmentMapper.selectList(apptWrapper).stream()
                .map(Appointment::getApptId).toList();

        if (apptIds.isEmpty()) return Result.ok(List.of());

        LambdaQueryWrapper<PaymentRecord> prWrapper = new LambdaQueryWrapper<>();
        prWrapper.in(PaymentRecord::getApptId, apptIds)
                 .orderByDesc(PaymentRecord::getCreateTime);
        List<PaymentRecord> records = paymentRecordMapper.selectList(prWrapper);

        Map<Integer, Appointment> apptMap = new HashMap<>();
        for (Appointment a : appointmentMapper.selectBatchIds(apptIds)) {
            apptMap.put(a.getApptId(), a);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (PaymentRecord pr : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("paymentId", pr.getPaymentId());
            item.put("apptId", pr.getApptId());
            item.put("amount", pr.getAmount());
            item.put("payStatus", pr.getPayStatus());
            item.put("payMethod", pr.getPayMethod());
            item.put("payTime", pr.getPayTime());
            item.put("createTime", pr.getCreateTime());
            Appointment appt = apptMap.get(pr.getApptId());
            if (appt != null) {
                item.put("scheduleId", appt.getScheduleId());
                item.put("appointmentStatus", appt.getStatus());
            }
            result.add(item);
        }
        return Result.ok(result);
    }

    private Integer currentPatientId(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"PATIENT".equals(role)) {
            throw new BusinessException(403, "仅患者可访问个人中心");
        }
        try {
            return Integer.parseInt((String) request.getAttribute("userId"));
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "登录信息异常，请重新登录");
        }
    }
}
