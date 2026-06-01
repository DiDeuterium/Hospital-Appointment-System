package com.hospital.service;

import com.hospital.dto.request.DepartmentRequest;
import com.hospital.entity.Department;

import java.util.List;

public interface DepartmentService {
    List<Department> list(String keyword);
    List<Department> listAll(String keyword);
    Department getById(Integer deptId);
    void add(DepartmentRequest request);
    void update(Integer deptId, DepartmentRequest request);
    void updateStatus(Integer deptId, Integer status);
    void delete(Integer deptId);
}
