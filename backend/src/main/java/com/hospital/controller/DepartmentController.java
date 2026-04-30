package com.hospital.controller;

import com.hospital.dto.Result;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.service.DepartmentService;
import com.hospital.service.DoctorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DoctorService doctorService;

    public DepartmentController(DepartmentService departmentService, DoctorService doctorService) {
        this.departmentService = departmentService;
        this.doctorService = doctorService;
    }

    @GetMapping
    public Result<List<Department>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(departmentService.list(keyword));
    }

    @GetMapping("/{deptId}")
    public Result<Department> getById(@PathVariable String deptId) {
        return Result.ok(departmentService.getById(deptId));
    }

    @GetMapping("/{deptId}/doctors")
    public Result<List<Doctor>> getDoctors(@PathVariable String deptId) {
        return Result.ok(doctorService.list(deptId));
    }
}
