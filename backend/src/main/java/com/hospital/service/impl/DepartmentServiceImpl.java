package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.request.DepartmentRequest;
import com.hospital.entity.Department;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<Department> list(String keyword) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Department::getDeptName, keyword);
        }
        wrapper.orderByAsc(Department::getDeptId);
        return departmentMapper.selectList(wrapper);
    }

    @Override
    public Department getById(String deptId) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(404, "科室不存在");
        }
        return dept;
    }

    @Override
    public void add(DepartmentRequest request) {
        Department exist = departmentMapper.selectById(request.getDeptId());
        if (exist != null) {
            throw new BusinessException(409, "科室编号已存在");
        }
        Department dept = new Department();
        dept.setDeptId(request.getDeptId());
        dept.setDeptName(request.getDeptName());
        dept.setLocation(request.getLocation());
        dept.setDescription(request.getDescription());
        departmentMapper.insert(dept);
    }

    @Override
    public void update(String deptId, DepartmentRequest request) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(404, "科室不存在");
        }
        dept.setDeptName(request.getDeptName());
        dept.setLocation(request.getLocation());
        dept.setDescription(request.getDescription());
        departmentMapper.updateById(dept);
    }

    @Override
    public void delete(String deptId) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(404, "科室不存在");
        }
        departmentMapper.deleteById(deptId);
    }
}
