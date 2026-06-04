package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.dto.request.*;
import com.hospital.dto.response.AppointmentVO;
import com.hospital.dto.response.DoctorScheduleVO;
import com.hospital.dto.response.LoginResponse;
import com.hospital.entity.Doctor;
import com.hospital.entity.ScheduleChangeRequest;
import com.hospital.exception.BusinessException;
import com.hospital.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final ScheduleService scheduleService;
    private final AppointmentService appointmentService;
    private final ScheduleChangeRequestService changeRequestService;

    public DoctorController(DoctorService doctorService,
                            ScheduleService scheduleService,
                            AppointmentService appointmentService,
                            ScheduleChangeRequestService changeRequestService) {
        this.doctorService = doctorService;
        this.scheduleService = scheduleService;
        this.appointmentService = appointmentService;
        this.changeRequestService = changeRequestService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody DoctorLoginRequest request) {
        return Result.ok("登录成功", doctorService.login(request.getDocId(), request.getPassword()));
    }

    // ---- 医生个人资料 ----

    @GetMapping("/me")
    public Result<Doctor> getProfile(HttpServletRequest request) {
        return Result.ok(doctorService.getById(currentDoctorId(request)));
    }

    @PutMapping("/me")
    public Result<Void> updateProfile(HttpServletRequest request,
                                       @Valid @RequestBody DoctorProfileUpdateRequest body) {
        Doctor doctor = doctorService.getById(currentDoctorId(request));
        if (body.getAvatarUrl() != null) doctor.setAvatarUrl(body.getAvatarUrl());
        if (body.getSpecialty() != null) doctor.setSpecialty(body.getSpecialty());
        doctorService.update(doctor.getDocId(), buildUpdateRequest(doctor));
        return Result.ok("资料更新成功", null);
    }

    @PostMapping("/me/avatar")
    public Result<Map<String, String>> uploadAvatar(HttpServletRequest request,
                                                     @RequestParam("file") MultipartFile file) throws IOException {
        Integer docId = currentDoctorId(request);
        Doctor doctor = doctorService.getById(docId);

        String fileName = "doctor-" + docId + ".png";
        byte[] bytes = file.getBytes();

        // Write to src/main/resources/static/avatars (source)
        Path srcPath = Path.of(System.getProperty("user.dir"), "src/main/resources/static/avatars", fileName);
        Files.createDirectories(srcPath.getParent());
        Files.write(srcPath, bytes);

        // Also write to target/classes/static/avatars (runtime classpath)
        Path targetPath = Path.of(System.getProperty("user.dir"), "target/classes/static/avatars", fileName);
        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, bytes);

        String avatarUrl = "/avatars/" + fileName + "?t=" + System.currentTimeMillis();
        doctor.setAvatarUrl(avatarUrl);
        doctorService.update(docId, buildUpdateRequest(doctor));

        return Result.ok("头像上传成功", Map.of("avatarUrl", avatarUrl));
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(HttpServletRequest request,
                                        @Valid @RequestBody PasswordChangeRequest body) {
        Integer docId = currentDoctorId(request);
        Doctor doctor = doctorService.getById(docId);
        // verify old password
        DoctorService ds = doctorService;
        // reuse login validation — old password check is internal
        if (!new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .matches(body.getOldPassword(), doctor.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        if (body.getNewPassword().equals(body.getOldPassword())) {
            throw new BusinessException(400, "新密码不能与原密码相同");
        }
        DoctorRequest updateReq = new DoctorRequest();
        updateReq.setDocName(doctor.getDocName());
        updateReq.setGender(doctor.getGender());
        updateReq.setTitle(doctor.getTitle());
        updateReq.setDeptId(doctor.getDeptId());
        updateReq.setAvatarUrl(doctor.getAvatarUrl());
        updateReq.setSpecialty(doctor.getSpecialty());
        updateReq.setPassword(body.getNewPassword());
        doctorService.update(docId, updateReq);
        return Result.ok("密码修改成功", null);
    }

    // ---- 排班相关 ----

    @GetMapping("/{docId}/schedules")
    public Result<List<DoctorScheduleVO>> getSchedules(
            @PathVariable Integer docId,
            @RequestParam(required = false) LocalDate workDate) {
        var schedules = scheduleService.list(null, docId, workDate, null);
        List<DoctorScheduleVO> vos = schedules.stream()
                .map(s -> {
                    DoctorScheduleVO vo = new DoctorScheduleVO();
                    vo.setScheduleId(s.getScheduleId());
                    vo.setWorkDate(s.getWorkDate());
                    vo.setShift(s.getShift());
                    vo.setTotalQuota(s.getTotalQuota());
                    vo.setRestQuota(s.getRestQuota());
                    vo.setAppointedCount(s.getTotalQuota() - s.getRestQuota());
                    vo.setStatus(s.getStatus());
                    return vo;
                }).toList();
        return Result.ok(vos);
    }

    @GetMapping("/schedules/{scheduleId}/patients")
    public Result<List<AppointmentVO>> getSchedulePatients(@PathVariable Integer scheduleId) {
        return Result.ok(appointmentService.listBySchedule(scheduleId));
    }

    // ---- 排班变更申请 ----

    @PostMapping("/schedules/{scheduleId}/change-request")
    public Result<ScheduleChangeRequest> submitChangeRequest(
            HttpServletRequest request,
            @PathVariable Integer scheduleId,
            @Valid @RequestBody ChangeRequestDTO dto) {
        return Result.ok("申请提交成功",
                changeRequestService.submitChangeRequest(currentDoctorId(request), scheduleId, dto));
    }

    @GetMapping("/change-requests")
    public Result<List<ScheduleChangeRequest>> listChangeRequests(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status) {
        return Result.ok(changeRequestService.listByDoctor(currentDoctorId(request), status));
    }

    @PutMapping("/change-requests/{requestId}/withdraw")
    public Result<Void> withdrawChangeRequest(HttpServletRequest request, @PathVariable Integer requestId) {
        changeRequestService.withdraw(currentDoctorId(request), requestId);
        return Result.ok("申请已撤回", null);
    }

    // ---- helper ----

    private Integer currentDoctorId(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"DOCTOR".equals(role)) {
            throw new BusinessException(403, "仅医生可访问");
        }
        try {
            return Integer.parseInt((String) request.getAttribute("userId"));
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "登录信息异常，请重新登录");
        }
    }

    private DoctorRequest buildUpdateRequest(Doctor doctor) {
        DoctorRequest req = new DoctorRequest();
        req.setDocName(doctor.getDocName());
        req.setGender(doctor.getGender());
        req.setTitle(doctor.getTitle());
        req.setDeptId(doctor.getDeptId());
        req.setAvatarUrl(doctor.getAvatarUrl());
        req.setSpecialty(doctor.getSpecialty());
        return req;
    }
}
