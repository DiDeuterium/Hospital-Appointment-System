package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    public List<ScheduleVO> list(Integer deptId, Integer docId, LocalDate workDate, String shift) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();

        if (docId != null) {
            wrapper.eq(Schedule::getDocId, docId);
        }

        if (deptId != null) {
            List<Doctor> doctors = doctorMapper.selectList(
                    new LambdaQueryWrapper<Doctor>().eq(Doctor::getDeptId, deptId));
            if (doctors.isEmpty()) return new ArrayList<>();
            List<Integer> docIds = doctors.stream().map(Doctor::getDocId).toList();
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

        Map<Integer, Doctor> doctorMap = schedules.stream()
                .map(Schedule::getDocId)
                .distinct()
                .map(doctorMapper::selectById)
                .collect(Collectors.toMap(Doctor::getDocId, d -> d));

        Map<Integer, Department> deptMap = doctorMap.values().stream()
                .map(Doctor::getDeptId)
                .distinct()
                .map(departmentMapper::selectById)
                .collect(Collectors.toMap(Department::getDeptId, d -> d));

        return buildVOList(schedules, doctorMap, deptMap);
    }

    @Override
    public List<ScheduleVO> listAvailable(Integer deptId, LocalDate workDate, String shift) {
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Schedule::getStatus, 1)
               .gt(Schedule::getRestQuota, 0);

        if (deptId != null) {
            List<Doctor> doctors = doctorMapper.selectList(
                    new LambdaQueryWrapper<Doctor>()
                            .eq(Doctor::getDeptId, deptId)
                            .eq(Doctor::getStatus, 1));
            if (doctors.isEmpty()) return new ArrayList<>();
            List<Integer> docIds = doctors.stream().map(Doctor::getDocId).toList();
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

        Map<Integer, Doctor> doctorMap = schedules.stream()
                .map(Schedule::getDocId)
                .distinct()
                .map(doctorMapper::selectById)
                .filter(d -> d != null && d.getStatus() != null && d.getStatus() == 1)
                .collect(Collectors.toMap(Doctor::getDocId, d -> d));

        Map<Integer, Department> deptMap = doctorMap.values().stream()
                .map(Doctor::getDeptId)
                .distinct()
                .map(departmentMapper::selectById)
                .filter(d -> d != null && d.getStatus() != null && d.getStatus() == 1)
                .collect(Collectors.toMap(Department::getDeptId, d -> d));

        return buildVOList(schedules, doctorMap, deptMap);
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
        schedule.setFee(request.getFee() != null ? request.getFee() : java.math.BigDecimal.ZERO);
        schedule.setStatus(1);
        scheduleMapper.insert(schedule);
    }

    @Override
    public void update(Integer scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在");
        }
        int diff = request.getTotalQuota() - schedule.getTotalQuota();
        int newRestQuota = schedule.getRestQuota() + diff;
        if (newRestQuota < 0 || newRestQuota > request.getTotalQuota()) {
            throw new BusinessException(400, "号源调整后剩余号源不符合约束");
        }

        var updateWrapper = new LambdaUpdateWrapper<Schedule>()
                .eq(Schedule::getScheduleId, scheduleId)
                .set(Schedule::getWorkDate, request.getWorkDate())
                .set(Schedule::getShift, request.getShift())
                .set(Schedule::getTotalQuota, request.getTotalQuota())
                .set(Schedule::getRestQuota, newRestQuota);
        if (request.getFee() != null) {
            updateWrapper.set(Schedule::getFee, request.getFee());
        }
        scheduleMapper.update(null, updateWrapper);
    }

    @Override
    public void delete(Integer scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在");
        }
        scheduleMapper.deleteById(scheduleId);
    }

    private List<ScheduleVO> buildVOList(List<Schedule> schedules,
                                         Map<Integer, Doctor> doctorMap,
                                         Map<Integer, Department> deptMap) {
        return schedules.stream().map(s -> {
            ScheduleVO vo = new ScheduleVO();
            vo.setScheduleId(s.getScheduleId());
            vo.setDocId(s.getDocId());
            vo.setWorkDate(s.getWorkDate());
            vo.setShift(s.getShift());
            vo.setTotalQuota(s.getTotalQuota());
            vo.setRestQuota(s.getRestQuota());
            vo.setFee(s.getFee());

            Doctor doc = doctorMap.get(s.getDocId());
            if (doc != null) {
                vo.setDocName(doc.getDocName());
                vo.setTitle(doc.getTitle());
                vo.setDeptId(doc.getDeptId());
                vo.setAvatarUrl(doc.getAvatarUrl());
                vo.setSpecialty(doc.getSpecialty());
                Department dept = deptMap.get(doc.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
            return vo;
        }).toList();
    }
}
