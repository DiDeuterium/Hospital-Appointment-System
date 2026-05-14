package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.dto.request.*;
import com.hospital.dto.response.LoginResponse;
import com.hospital.dto.response.ScheduleVO;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final LoginService loginService;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;
    private final ScheduleService scheduleService;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    public AdminController(LoginService loginService,
                           DepartmentService departmentService,
                           DoctorService doctorService,
                           ScheduleService scheduleService) {
        this.loginService = loginService;
        this.departmentService = departmentService;
        this.doctorService = doctorService;
        this.scheduleService = scheduleService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        if (!adminUsername.equals(request.getUsername()) || !adminPassword.equals(request.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        String token = loginService.createToken("ADMIN", "admin", "管理员");
        return Result.ok("登录成功", new LoginResponse(token, "admin", "管理员"));
    }

    // ---- Departments ----
    @GetMapping("/departments")
    public Result<List<Department>> listDepartments(@RequestParam(required = false) String keyword) {
        return Result.ok(departmentService.list(keyword));
    }

    @PostMapping("/departments")
    public Result<Void> addDepartment(@Valid @RequestBody DepartmentRequest request) {
        departmentService.add(request);
        return Result.ok("新增科室成功", null);
    }

    @PutMapping("/departments/{deptId}")
    public Result<Void> updateDepartment(@PathVariable String deptId,
                                          @Valid @RequestBody DepartmentRequest request) {
        departmentService.update(deptId, request);
        return Result.ok("修改科室成功", null);
    }

    @DeleteMapping("/departments/{deptId}")
    public Result<Void> deleteDepartment(@PathVariable String deptId) {
        departmentService.delete(deptId);
        return Result.ok("删除科室成功", null);
    }

    // ---- Doctors ----
    @GetMapping("/doctors")
    public Result<List<Doctor>> listDoctors(@RequestParam(required = false) String deptId) {
        return Result.ok(doctorService.list(deptId));
    }

    @PostMapping("/doctors")
    public Result<Void> addDoctor(@Valid @RequestBody DoctorRequest request) {
        doctorService.add(request);
        return Result.ok("新增医生成功", null);
    }

    @PutMapping("/doctors/{docId}")
    public Result<Void> updateDoctor(@PathVariable String docId,
                                      @Valid @RequestBody DoctorRequest request) {
        doctorService.update(docId, request);
        return Result.ok("修改医生成功", null);
    }

    @DeleteMapping("/doctors/{docId}")
    public Result<Void> deleteDoctor(@PathVariable String docId) {
        doctorService.delete(docId);
        return Result.ok("删除医生成功", null);
    }

    // ---- Schedules ----
    @GetMapping("/schedules")
    public Result<List<ScheduleVO>> listSchedules(
            @RequestParam(required = false) String docId,
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
}
