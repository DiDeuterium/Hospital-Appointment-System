package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.request.DepartmentRequest;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.ScheduleMapper;
import com.hospital.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper,
                                  ScheduleMapper scheduleMapper,
                                  DoctorMapper doctorMapper) {
        this.departmentMapper = departmentMapper;
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
    }

    @Override
    public List<Department> list(String keyword) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Department::getDeptName, keyword);
        }
        wrapper.eq(Department::getStatus, 1);
        wrapper.orderByAsc(Department::getDeptId);
        return departmentMapper.selectList(wrapper);
    }

    @Override
    public List<Department> listAll(String keyword) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Department::getDeptName, keyword);
        }
        wrapper.orderByAsc(Department::getDeptId);
        return departmentMapper.selectList(wrapper);
    }

    @Override
    public Department getById(Integer deptId) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(404, "科室不存在");
        }
        return dept;
    }

    @Override
    public void add(DepartmentRequest request) {
        Department dept = new Department();
        dept.setDeptName(request.getDeptName());
        dept.setLocation(request.getLocation());
        dept.setDescription(request.getDescription());
        dept.setStatus(1);
        departmentMapper.insert(dept);
    }

    @Override
    public void update(Integer deptId, DepartmentRequest request) {
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
    public void updateStatus(Integer deptId, Integer status) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(404, "科室不存在");
        }

        // 停用前检查是否有未来的有效排班
        if (status == 0) {
            List<Doctor> doctors = doctorMapper.selectList(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getDeptId, deptId));
            if (!doctors.isEmpty()) {
                List<Integer> docIds = doctors.stream().map(Doctor::getDocId).toList();
                LambdaQueryWrapper<Schedule> schWrapper = new LambdaQueryWrapper<>();
                schWrapper.in(Schedule::getDocId, docIds)
                         .ge(Schedule::getWorkDate, LocalDate.now())
                         .eq(Schedule::getStatus, 1);
                long futureCount = scheduleMapper.selectCount(schWrapper);
                if (futureCount > 0) {
                    throw new BusinessException(409, "该科室下存在未来有效排班，无法停用");
                }
            }
        }

        dept.setStatus(status);
        departmentMapper.updateById(dept);
    }

    @Override
    public void delete(Integer deptId) {
        Department dept = departmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(404, "科室不存在");
        }
        departmentMapper.deleteById(deptId);
    }
}
