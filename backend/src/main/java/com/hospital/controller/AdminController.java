package com.hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.Result;
import com.hospital.dto.request.*;
import com.hospital.dto.response.LoginResponse;
import com.hospital.dto.response.ScheduleVO;
import com.hospital.entity.*;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.PaymentRecordMapper;
import com.hospital.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminUserService adminUserService;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;
    private final ScheduleService scheduleService;
    private final ScheduleChangeRequestService changeRequestService;
    private final SysNoticeService sysNoticeService;
    private final PaymentRecordMapper paymentRecordMapper;

    public AdminController(AdminUserService adminUserService,
                           DepartmentService departmentService,
                           DoctorService doctorService,
                           ScheduleService scheduleService,
                           ScheduleChangeRequestService changeRequestService,
                           SysNoticeService sysNoticeService,
                           PaymentRecordMapper paymentRecordMapper) {
        this.adminUserService = adminUserService;
        this.departmentService = departmentService;
        this.doctorService = doctorService;
        this.scheduleService = scheduleService;
        this.changeRequestService = changeRequestService;
        this.sysNoticeService = sysNoticeService;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return Result.ok("登录成功", adminUserService.login(request.getUsername(), request.getPassword()));
    }

    // ---- Departments ----

    @GetMapping("/departments")
    public Result<List<Department>> listDepartments(@RequestParam(required = false) String keyword) {
        return Result.ok(departmentService.listAll(keyword));
    }

    @PostMapping("/departments")
    public Result<Void> addDepartment(@Valid @RequestBody DepartmentRequest request) {
        departmentService.add(request);
        return Result.ok("新增科室成功", null);
    }

    @PutMapping("/departments/{deptId}")
    public Result<Void> updateDepartment(@PathVariable Integer deptId,
                                          @Valid @RequestBody DepartmentRequest request) {
        departmentService.update(deptId, request);
        return Result.ok("修改科室成功", null);
    }

    @PutMapping("/departments/{deptId}/status")
    public Result<Void> toggleDepartmentStatus(@PathVariable Integer deptId,
                                                @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值必须为 0 或 1");
        }
        departmentService.updateStatus(deptId, status);
        return Result.ok("状态更新成功", null);
    }

    @DeleteMapping("/departments/{deptId}")
    public Result<Void> deleteDepartment(@PathVariable Integer deptId) {
        departmentService.delete(deptId);
        return Result.ok("删除科室成功", null);
    }

    // ---- Doctors ----

    @GetMapping("/doctors")
    public Result<List<Doctor>> listDoctors(@RequestParam(required = false) Integer deptId) {
        return Result.ok(doctorService.listAll(deptId));
    }

    @PostMapping("/doctors")
    public Result<Void> addDoctor(@Valid @RequestBody DoctorRequest request) {
        doctorService.add(request);
        return Result.ok("新增医生成功", null);
    }

    @PutMapping("/doctors/{docId}")
    public Result<Void> updateDoctor(@PathVariable Integer docId,
                                      @Valid @RequestBody DoctorRequest request) {
        doctorService.update(docId, request);
        return Result.ok("修改医生成功", null);
    }

    @PutMapping("/doctors/{docId}/status")
    public Result<Void> toggleDoctorStatus(@PathVariable Integer docId,
                                            @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态值必须为 0 或 1");
        }
        doctorService.updateStatus(docId, status);
        return Result.ok("状态更新成功", null);
    }

    @DeleteMapping("/doctors/{docId}")
    public Result<Void> deleteDoctor(@PathVariable Integer docId) {
        doctorService.delete(docId);
        return Result.ok("删除医生成功", null);
    }

    // ---- Schedules ----

    @GetMapping("/schedules")
    public Result<List<ScheduleVO>> listSchedules(
            @RequestParam(required = false) Integer docId,
            @RequestParam(required = false) LocalDate workDate) {
        return Result.ok(scheduleService.list(null, docId, workDate, null));
    }

    @PostMapping("/schedules")
    public Result<Void> addSchedule(@Valid @RequestBody ScheduleRequest request) {
        scheduleService.add(request);
        return Result.ok("发布排班成功", null);
    }

    @PutMapping("/schedules/{scheduleId}")
    public Result<Void> updateSchedule(@PathVariable Integer scheduleId,
                                        @Valid @RequestBody ScheduleRequest request) {
        scheduleService.update(scheduleId, request);
        return Result.ok("修改排班成功", null);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public Result<Void> deleteSchedule(@PathVariable Integer scheduleId) {
        scheduleService.delete(scheduleId);
        return Result.ok("删除排班成功", null);
    }

    // ---- 排班变更审核 ----

    @GetMapping("/change-requests")
    public Result<List<ScheduleChangeRequest>> listChangeRequests(
            @RequestParam(required = false) Integer status) {
        return Result.ok(changeRequestService.listAll(status));
    }

    @GetMapping("/change-requests/{requestId}")
    public Result<ScheduleChangeRequest> getChangeRequest(@PathVariable Integer requestId) {
        return Result.ok(changeRequestService.getById(requestId));
    }

    @PutMapping("/change-requests/{requestId}/approve")
    public Result<Void> approveChangeRequest(HttpServletRequest request,
                                              @PathVariable Integer requestId,
                                              @Valid @RequestBody AuditRequest body) {
        if (body.getApproved() == 1) {
            changeRequestService.approve(currentAdminId(request), requestId, body.getRemark());
            return Result.ok("审核通过", null);
        } else {
            changeRequestService.reject(currentAdminId(request), requestId, body.getRemark());
            return Result.ok("已驳回", null);
        }
    }

    // ---- 支付记录 ----

    @GetMapping("/payments")
    public Result<List<PaymentRecord>> listPayments(
            @RequestParam(required = false) Integer payStatus,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        if (payStatus != null) {
            wrapper.eq(PaymentRecord::getPayStatus, payStatus);
        }
        if (startTime != null) {
            wrapper.ge(PaymentRecord::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(PaymentRecord::getCreateTime, endTime);
        }
        wrapper.orderByDesc(PaymentRecord::getCreateTime);
        return Result.ok(paymentRecordMapper.selectList(wrapper));
    }

    // ---- 系统公告管理 ----

    @GetMapping("/notices")
    public Result<List<SysNotice>> listNotices(@RequestParam(required = false) Integer status) {
        return Result.ok(sysNoticeService.listAll(status));
    }

    @PostMapping("/notices")
    public Result<Void> addNotice(HttpServletRequest request,
                                   @RequestBody Map<String, Object> body) {
        sysNoticeService.add(
                (String) body.get("title"),
                (String) body.get("content"),
                body.get("isTop") != null ? ((Number) body.get("isTop")).intValue() : null,
                body.get("status") != null ? ((Number) body.get("status")).intValue() : null,
                currentAdminId(request));
        return Result.ok("公告发布成功", null);
    }

    @PutMapping("/notices/{noticeId}")
    public Result<Void> updateNotice(@PathVariable Integer noticeId,
                                      @RequestBody Map<String, Object> body) {
        sysNoticeService.update(
                noticeId,
                (String) body.get("title"),
                (String) body.get("content"),
                body.get("isTop") != null ? ((Number) body.get("isTop")).intValue() : null,
                body.get("status") != null ? ((Number) body.get("status")).intValue() : null);
        return Result.ok("公告更新成功", null);
    }

    @PutMapping("/notices/{noticeId}/offline")
    public Result<Void> offlineNotice(@PathVariable Integer noticeId) {
        sysNoticeService.offline(noticeId);
        return Result.ok("公告已下线", null);
    }

    // ---- helper ----

    private Integer currentAdminId(HttpServletRequest request) {
        try {
            return Integer.parseInt((String) request.getAttribute("userId"));
        } catch (NumberFormatException e) {
            throw new BusinessException(401, "登录信息异常，请重新登录");
        }
    }
}
