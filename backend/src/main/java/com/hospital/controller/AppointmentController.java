package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentVO;
import com.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public Result<AppointmentVO> make(@Valid @RequestBody AppointmentRequest request) {
        return Result.ok("挂号成功", appointmentService.makeAppointment(request));
    }

    @PutMapping("/{apptId}/cancel")
    public Result<Void> cancel(@PathVariable Integer apptId,
                               @RequestBody(required = false) Map<String, String> body) {
        String cancelReason = null;
        if (body != null && body.containsKey("cancelReason")) {
            cancelReason = body.get("cancelReason");
        }
        appointmentService.cancelAppointment(apptId, cancelReason);
        return Result.ok("取消成功", null);
    }

    @PutMapping("/{apptId}/finish")
    public Result<Void> finish(@PathVariable Integer apptId) {
        appointmentService.finishAppointment(apptId);
        return Result.ok("已完成就诊", null);
    }

    @PostMapping("/{apptId}/pay")
    public Result<Void> pay(@PathVariable Integer apptId) {
        appointmentService.payAppointment(apptId);
        return Result.ok("支付成功", null);
    }

    @GetMapping("/patients/{patientId}")
    public Result<List<AppointmentVO>> listByPatient(
            @PathVariable Integer patientId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(appointmentService.listByPatient(patientId, status));
    }
}
