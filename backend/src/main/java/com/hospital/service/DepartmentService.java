package com.hospital.service;

import com.hospital.dto.request.DepartmentRequest;
import com.hospital.entity.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> list(String keyword);
    Department getById(String deptId);
    void add(DepartmentRequest request);
    void update(String deptId, DepartmentRequest request);
    void delete(String deptId);
}
