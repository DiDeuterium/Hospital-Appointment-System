package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.request.ScheduleRequest;
import com.hospital.dto.response.ScheduleVO;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.DepartmentMapper;
import com.hospital.mapper.DoctorMapper;
import com.hospital.mapper.ScheduleMapper;
import com.hospital.service.ScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;

    public ScheduleServiceImpl(ScheduleMapper scheduleMapper,
                               DoctorMapper doctorMapper,
                               DepartmentMapper departmentMapper) {
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public List<ScheduleVO> list(String deptId, LocalDate workDate, String shift) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();

        // Build doctor IDs for the given department
        if (deptId != null && !deptId.isBlank()) {
            List<Doctor> doctors = doctorMapper.selectList(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getDeptId, deptId));
            if (doctors.isEmpty()) return new ArrayList<>();
            List<String> docIds = doctors.stream().map(Doctor::getDocId).toList();
            wrapper.in(Schedule::getDocId, docIds);
        }

        if (workDate != null) {
            wrapper.eq(Schedule::getWorkDate, workDate);
        } else {
            wrapper.ge(Schedule::getWorkDate, LocalDate.now());
        }

        if (shift != null && !shift.isBlank()) {
            wrapper.eq(Schedule::getShift, shift);
        }
        wrapper.orderByAsc(Schedule::getWorkDate, Schedule::getShift);

        List<Schedule> schedules = scheduleMapper.selectList(wrapper);

        // Collect all doctor IDs and dept IDs for batch lookup
        Map<String, Doctor> doctorMap = schedules.stream()
                .map(Schedule::getDocId)
                .distinct()
                .map(doctorMapper::selectById)
                .collect(Collectors.toMap(Doctor::getDocId, d -> d));

        Map<String, Department> deptMap = doctorMap.values().stream()
                .map(Doctor::getDeptId)
                .distinct()
                .map(departmentMapper::selectById)
                .collect(Collectors.toMap(Department::getDeptId, d -> d));

        return schedules.stream().map(s -> {
            ScheduleVO vo = new ScheduleVO();
            vo.setScheduleId(s.getScheduleId());
            vo.setDocId(s.getDocId());
            vo.setWorkDate(s.getWorkDate());
            vo.setShift(s.getShift());
            vo.setTotalQuota(s.getTotalQuota());
            vo.setRestQuota(s.getRestQuota());

            Doctor doc = doctorMap.get(s.getDocId());
            if (doc != null) {
                vo.setDocName(doc.getDocName());
                vo.setTitle(doc.getTitle());
                vo.setDeptId(doc.getDeptId());
                Department dept = deptMap.get(doc.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
            return vo;
        }).toList();
    }

    @Override
    public Schedule getById(Integer scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在");
        }
        return schedule;
    }

    @Override
    public void add(ScheduleRequest request) {
        Doctor doctor = doctorMapper.selectById(request.getDocId());
        if (doctor == null) {
            throw new BusinessException(404, "医生不存在");
        }
        // Check for duplicate schedule (same doc, date, shift)
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getDocId, request.getDocId())
                .eq(Schedule::getWorkDate, request.getWorkDate())
                .eq(Schedule::getShift, request.getShift());
        if (scheduleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "该医生在" + request.getWorkDate() + request.getShift() + "已有排班");
        }

        Schedule schedule = new Schedule();
        schedule.setDocId(request.getDocId());
        schedule.setWorkDate(request.getWorkDate());
        schedule.setShift(request.getShift());
        schedule.setTotalQuota(request.getTotalQuota());
        schedule.setRestQuota(request.getTotalQuota());
        scheduleMapper.insert(schedule);
    }

    @Override
    public void update(Integer scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在");
        }
        schedule.setWorkDate(request.getWorkDate());
        schedule.setShift(request.getShift());
        // Allow updating total quota, adjust rest quota accordingly
        int diff = request.getTotalQuota() - schedule.getTotalQuota();
        schedule.setTotalQuota(request.getTotalQuota());
        schedule.setRestQuota(schedule.getRestQuota() + diff);
        scheduleMapper.updateById(schedule);
    }

    @Override
    public void delete(Integer scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在");
        }
        scheduleMapper.deleteById(scheduleId);
        // FK RESTRICT will prevent deletion if appointments exist
    }
}
