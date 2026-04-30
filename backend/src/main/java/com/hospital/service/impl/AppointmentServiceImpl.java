package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentVO;
import com.hospital.entity.Appointment;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.*;
import com.hospital.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final PatientMapper patientMapper;

    public AppointmentServiceImpl(AppointmentMapper appointmentMapper,
                                   ScheduleMapper scheduleMapper,
                                   DoctorMapper doctorMapper,
                                   DepartmentMapper departmentMapper,
                                   PatientMapper patientMapper) {
        this.appointmentMapper = appointmentMapper;
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
        this.departmentMapper = departmentMapper;
        this.patientMapper = patientMapper;
    }

    @Override
    @Transactional
    public AppointmentVO makeAppointment(AppointmentRequest request) {
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在");
        }
        if (schedule.getRestQuota() <= 0) {
            throw new BusinessException(409, "该排班号源已约满");
        }

        // Check duplicate appointment
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, request.getPatientId())
                .eq(Appointment::getScheduleId, request.getScheduleId())
                .eq(Appointment::getStatus, 1);
        if (appointmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "您已预约过该排班，请勿重复挂号");
        }

        // Insert appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setScheduleId(request.getScheduleId());
        appointment.setStatus(1);
        appointmentMapper.insert(appointment);

        // Decrement rest quota
        schedule.setRestQuota(schedule.getRestQuota() - 1);
        scheduleMapper.updateById(schedule);

        return buildVO(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Integer apptId) {
        Appointment appointment = appointmentMapper.selectById(apptId);
        if (appointment == null) {
            throw new BusinessException(404, "预约记录不存在");
        }
        if (appointment.getStatus() != 1) {
            throw new BusinessException(409, "该预约无法取消（已取消或已就诊）");
        }

        // Update status to cancelled
        appointment.setStatus(2);
        appointmentMapper.updateById(appointment);

        // Increment rest quota
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null) {
            schedule.setRestQuota(schedule.getRestQuota() + 1);
            scheduleMapper.updateById(schedule);
        }
    }

    @Override
    public List<AppointmentVO> listByPatient(Integer patientId, Integer status) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, patientId);
        if (status != null) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getCreateTime);
        List<Appointment> appointments = appointmentMapper.selectList(wrapper);

        return appointments.stream().map(this::buildVO).collect(Collectors.toList());
    }

    @Override
    public List<AppointmentVO> listBySchedule(Integer scheduleId) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getScheduleId, scheduleId)
                .eq(Appointment::getStatus, 1);
        wrapper.orderByAsc(Appointment::getCreateTime);
        List<Appointment> appointments = appointmentMapper.selectList(wrapper);

        return appointments.stream().map(this::buildVO).collect(Collectors.toList());
    }

    private AppointmentVO buildVO(Appointment appointment) {
        AppointmentVO vo = new AppointmentVO();
        vo.setApptId(appointment.getApptId());
        vo.setScheduleId(appointment.getScheduleId());
        vo.setStatus(appointment.getStatus());
        vo.setCreateTime(appointment.getCreateTime());

        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null) {
            vo.setWorkDate(schedule.getWorkDate());
            vo.setShift(schedule.getShift());
            Doctor doctor = doctorMapper.selectById(schedule.getDocId());
            if (doctor != null) {
                vo.setDocName(doctor.getDocName());
                Department dept = departmentMapper.selectById(doctor.getDeptId());
                if (dept != null) {
                    vo.setDeptName(dept.getDeptName());
                }
            }
        }
        return vo;
    }
}
