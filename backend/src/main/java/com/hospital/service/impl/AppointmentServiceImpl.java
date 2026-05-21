package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentVO;
import com.hospital.entity.Appointment;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Patient;
import com.hospital.entity.Schedule;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.*;
import com.hospital.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        // Check if schedule date has passed
        LocalDate today = LocalDate.now();
        if (schedule.getWorkDate().isBefore(today)) {
            throw new BusinessException(409, "该排班日期已过，无法预约");
        }
        if (schedule.getWorkDate().equals(today)) {
            LocalTime now = LocalTime.now();
            boolean shiftPassed = switch (schedule.getShift()) {
                case "上午" -> now.isAfter(LocalTime.of(12, 0));
                case "下午" -> now.isAfter(LocalTime.of(17, 0));
                case "夜诊" -> now.isAfter(LocalTime.of(21, 0));
                default -> false;
            };
            if (shiftPassed) {
                throw new BusinessException(409, "该时段已过，无法预约");
            }
        }

        // Check duplicate appointment
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, request.getPatientId())
                .eq(Appointment::getScheduleId, request.getScheduleId())
                .eq(Appointment::getStatus, Appointment.STATUS_BOOKED);
        if (appointmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(409, "您已预约过该排班，请勿重复挂号");
        }

        // Insert appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setScheduleId(request.getScheduleId());
        appointment.setStatus(Appointment.STATUS_BOOKED);
        appointmentMapper.insert(appointment);

        // Decrement rest quota
        scheduleMapper.updateRestQuota(schedule.getScheduleId(), schedule.getRestQuota() - 1);

        return buildVO(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Integer apptId) {
        Appointment appointment = appointmentMapper.selectById(apptId);
        if (appointment == null) {
            throw new BusinessException(404, "预约记录不存在");
        }
        if (appointment.getStatus() != Appointment.STATUS_BOOKED
                && appointment.getStatus() != Appointment.STATUS_EXPIRED) {
            throw new BusinessException(409, "该预约无法取消（已取消或已就诊）");
        }

        // Update status to cancelled
        appointment.setStatus(Appointment.STATUS_CANCELLED);
        appointmentMapper.updateById(appointment);

        // Increment rest quota
        Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
        if (schedule != null) {
            scheduleMapper.updateRestQuota(schedule.getScheduleId(), schedule.getRestQuota() + 1);
        }
    }

    @Override
    @Transactional
    public void finishAppointment(Integer apptId) {
        Appointment appointment = appointmentMapper.selectById(apptId);
        if (appointment == null) {
            throw new BusinessException(404, "预约记录不存在");
        }
        if (appointment.getStatus() != Appointment.STATUS_BOOKED
                && appointment.getStatus() != Appointment.STATUS_EXPIRED) {
            throw new BusinessException(409, "该预约无法完成就诊（非待就诊或已过期状态）");
        }

        appointment.setStatus(Appointment.STATUS_FINISHED);
        appointmentMapper.updateById(appointment);
    }

    @Override
    public List<AppointmentVO> listByPatient(Integer patientId, Integer status) {
        autoExpirePastAppointments();

        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getPatientId, patientId);
        if (status != null) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getCreateTime);
        return buildVOList(appointmentMapper.selectList(wrapper));
    }

    @Override
    public List<AppointmentVO> listBySchedule(Integer scheduleId) {
        autoExpirePastAppointments();

        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getScheduleId, scheduleId);
        wrapper.orderByAsc(Appointment::getCreateTime);
        return buildVOList(appointmentMapper.selectList(wrapper));
    }

    private void autoExpirePastAppointments() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Schedules strictly in the past
        LambdaQueryWrapper<Schedule> pastWrapper = new LambdaQueryWrapper<>();
        pastWrapper.lt(Schedule::getWorkDate, today);
        List<Schedule> pastSchedules = scheduleMapper.selectList(pastWrapper);

        // Schedules for today — check if the shift has ended
        LambdaQueryWrapper<Schedule> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(Schedule::getWorkDate, today);
        List<Schedule> todaySchedules = scheduleMapper.selectList(todayWrapper);
        for (Schedule s : todaySchedules) {
            boolean shiftPassed = switch (s.getShift()) {
                case "上午" -> now.isAfter(LocalTime.of(12, 0));
                case "下午" -> now.isAfter(LocalTime.of(17, 0));
                case "夜诊" -> now.isAfter(LocalTime.of(21, 0));
                default -> false;
            };
            if (shiftPassed) pastSchedules.add(s);
        }

        if (pastSchedules.isEmpty()) return;

        List<Integer> pastIds = pastSchedules.stream()
                .map(Schedule::getScheduleId)
                .collect(Collectors.toList());

        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Appointment::getScheduleId, pastIds)
               .eq(Appointment::getStatus, Appointment.STATUS_BOOKED);

        Appointment updateEntity = new Appointment();
        updateEntity.setStatus(Appointment.STATUS_EXPIRED);
        appointmentMapper.update(updateEntity, wrapper);
    }

    private AppointmentVO buildVO(Appointment appointment) {
        return buildVOList(List.of(appointment)).get(0);
    }

    private List<AppointmentVO> buildVOList(List<Appointment> appointments) {
        if (appointments.isEmpty()) return List.of();

        Set<Integer> scheduleIds = appointments.stream()
                .map(Appointment::getScheduleId).collect(Collectors.toSet());
        Map<Integer, Schedule> scheduleMap = new HashMap<>();
        for (Schedule s : scheduleMapper.selectBatchIds(scheduleIds)) {
            scheduleMap.put(s.getScheduleId(), s);
        }

        Set<String> docIds = scheduleMap.values().stream()
                .map(Schedule::getDocId).collect(Collectors.toSet());
        Map<String, Doctor> doctorMap = new HashMap<>();
        for (Doctor d : doctorMapper.selectBatchIds(docIds)) {
            doctorMap.put(d.getDocId(), d);
        }

        Set<String> deptIds = doctorMap.values().stream()
                .map(Doctor::getDeptId).collect(Collectors.toSet());
        Map<String, Department> deptMap = new HashMap<>();
        for (Department d : departmentMapper.selectBatchIds(deptIds)) {
            deptMap.put(d.getDeptId(), d);
        }

        Set<Integer> patientIds = appointments.stream()
                .map(Appointment::getPatientId).collect(Collectors.toSet());
        Map<Integer, Patient> patientMap = new HashMap<>();
        if (!patientIds.isEmpty()) {
            for (Patient p : patientMapper.selectBatchIds(patientIds)) {
                patientMap.put(p.getPatientId(), p);
            }
        }

        return appointments.stream().map(appt -> {
            AppointmentVO vo = new AppointmentVO();
            vo.setApptId(appt.getApptId());
            vo.setScheduleId(appt.getScheduleId());
            vo.setPatientId(appt.getPatientId());
            vo.setStatus(appt.getStatus());
            vo.setCreateTime(appt.getCreateTime());

            Patient patient = patientMap.get(appt.getPatientId());
            if (patient != null) {
                vo.setRealName(patient.getRealName());
                vo.setPhone(patient.getPhone());
            }

            Schedule schedule = scheduleMap.get(appt.getScheduleId());
            if (schedule != null) {
                vo.setWorkDate(schedule.getWorkDate());
                vo.setShift(schedule.getShift());
                Doctor doctor = doctorMap.get(schedule.getDocId());
                if (doctor != null) {
                    vo.setDocName(doctor.getDocName());
                    Department dept = deptMap.get(doctor.getDeptId());
                    if (dept != null) {
                        vo.setDeptName(dept.getDeptName());
                    }
                }
            }
            return vo;
        }).toList();
    }
}
