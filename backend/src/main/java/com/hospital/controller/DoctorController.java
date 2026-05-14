package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.dto.request.DoctorLoginRequest;
import com.hospital.dto.response.AppointmentVO;
import com.hospital.dto.response.DoctorScheduleVO;
import com.hospital.dto.response.LoginResponse;
import com.hospital.service.AppointmentService;
import com.hospital.service.DoctorService;
import com.hospital.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final ScheduleService scheduleService;
    private final AppointmentService appointmentService;

    public DoctorController(DoctorService doctorService,
                            ScheduleService scheduleService,
                            AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.scheduleService = scheduleService;
        this.appointmentService = appointmentService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody DoctorLoginRequest request) {
        return Result.ok("登录成功", doctorService.login(request.getDocId(), request.getPassword()));
    }

    @GetMapping("/{docId}/schedules")
    public Result<List<DoctorScheduleVO>> getSchedules(
            @PathVariable String docId,
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
                    return vo;
                }).toList();
        return Result.ok(vos);
    }

    @GetMapping("/schedules/{scheduleId}/patients")
    public Result<List<AppointmentVO>> getSchedulePatients(@PathVariable Integer scheduleId) {
        return Result.ok(appointmentService.listBySchedule(scheduleId));
    }
}
