package com.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentVO;
import com.hospital.entity.*;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.*;
import com.hospital.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;
    private final DepartmentMapper departmentMapper;
    private final PatientMapper patientMapper;
    private final PaymentRecordMapper paymentRecordMapper;

    public AppointmentServiceImpl(AppointmentMapper appointmentMapper,
                                   ScheduleMapper scheduleMapper,
                                   DoctorMapper doctorMapper,
                                   DepartmentMapper departmentMapper,
                                   PatientMapper patientMapper,
                                   PaymentRecordMapper paymentRecordMapper) {
        this.appointmentMapper = appointmentMapper;
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
        this.departmentMapper = departmentMapper;
        this.patientMapper = patientMapper;
        this.paymentRecordMapper = paymentRecordMapper;
    }

    @Override
    @Transactional
    public AppointmentVO makeAppointment(AppointmentRequest request) {
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在");
        }
        if (schedule.getStatus() != null && schedule.getStatus() == 0) {
            throw new BusinessException(409, "该排班已停诊，无法预约");
        }
        if (schedule.getRestQuota() <= 0) {
            throw new BusinessException(409, "该排班号源已约满");
        }

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

        // Check duplicate
        LambdaQueryWrapper<Appointment> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(Appointment::getPatientId, request.getPatientId())
                .eq(Appointment::getScheduleId, request.getScheduleId())
                .eq(Appointment::getStatus, Appointment.STATUS_BOOKED);
        if (appointmentMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException(409, "您已预约过该排班，请勿重复挂号");
        }

        // Generate queue number: max existing + 1
        LambdaQueryWrapper<Appointment> qWrapper = new LambdaQueryWrapper<>();
        qWrapper.eq(Appointment::getScheduleId, request.getScheduleId())
                .orderByDesc(Appointment::getQueueNumber)
                .last("LIMIT 1");
        Appointment lastAppt = appointmentMapper.selectOne(qWrapper);
        int queueNumber = (lastAppt != null) ? lastAppt.getQueueNumber() + 1 : 1;

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setScheduleId(request.getScheduleId());
        appointment.setQueueNumber(queueNumber);
        appointment.setStatus(Appointment.STATUS_BOOKED);
        appointmentMapper.insert(appointment);

        // Decrement rest quota
        scheduleMapper.updateRestQuota(schedule.getScheduleId(), schedule.getRestQuota() - 1);

        // Create payment record
        PaymentRecord pr = new PaymentRecord();
        pr.setApptId(appointment.getApptId());
        pr.setAmount(schedule.getFee() != null ? schedule.getFee() : BigDecimal.ZERO);
        pr.setPayStatus(0);
        pr.setPayMethod("模拟支付");
        paymentRecordMapper.insert(pr);

        return buildVO(appointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Integer apptId, String cancelReason) {
        Appointment appointment = appointmentMapper.selectById(apptId);
        if (appointment == null) {
            throw new BusinessException(404, "预约记录不存在");
        }
        if (appointment.getStatus() != Appointment.STATUS_BOOKED
                && appointment.getStatus() != Appointment.STATUS_EXPIRED) {
            throw new BusinessException(409, "该预约无法取消（已取消或已就诊）");
        }

        appointment.setStatus(Appointment.STATUS_CANCELLED);
        appointment.setCancelReason(cancelReason);
        appointment.setUpdateTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

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
        appointment.setUpdateTime(LocalDateTime.now());
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
        wrapper.orderByAsc(Appointment::getQueueNumber);
        return buildVOList(appointmentMapper.selectList(wrapper));
    }

    @Override
    @Transactional
    public void payAppointment(Integer apptId) {
        Appointment appointment = appointmentMapper.selectById(apptId);
        if (appointment == null) {
            throw new BusinessException(404, "预约记录不存在");
        }
        LambdaQueryWrapper<PaymentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentRecord::getApptId, apptId);
        PaymentRecord pr = paymentRecordMapper.selectOne(wrapper);
        if (pr == null) {
            throw new BusinessException(404, "支付记录不存在");
        }
        if (pr.getPayStatus() == 1) {
            throw new BusinessException(409, "该预约已完成支付，不能重复支付");
        }
        pr.setPayStatus(1);
        pr.setPayTime(LocalDateTime.now());
        paymentRecordMapper.updateById(pr);
    }

    // ---- helper methods ----

    private void autoExpirePastAppointments() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LambdaQueryWrapper<Schedule> pastWrapper = new LambdaQueryWrapper<>();
        pastWrapper.lt(Schedule::getWorkDate, today);
        List<Schedule> pastSchedules = new ArrayList<>(scheduleMapper.selectList(pastWrapper));

        LambdaQueryWrapper<Schedule> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(Schedule::getWorkDate, today);
        for (Schedule s : scheduleMapper.selectList(todayWrapper)) {
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
                .toList();

        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Appointment::getScheduleId, pastIds)
               .eq(Appointment::getStatus, Appointment.STATUS_BOOKED);

        Appointment updateEntity = new Appointment();
        updateEntity.setStatus(Appointment.STATUS_EXPIRED);
        updateEntity.setUpdateTime(LocalDateTime.now());
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

        Set<Integer> docIds = scheduleMap.values().stream()
                .map(Schedule::getDocId).collect(Collectors.toSet());
        Map<Integer, Doctor> doctorMap = new HashMap<>();
        if (!docIds.isEmpty()) {
            for (Doctor d : doctorMapper.selectBatchIds(docIds)) {
                doctorMap.put(d.getDocId(), d);
            }
        }

        Set<Integer> deptIds = doctorMap.values().stream()
                .map(Doctor::getDeptId).collect(Collectors.toSet());
        Map<Integer, Department> deptMap = new HashMap<>();
        if (!deptIds.isEmpty()) {
            for (Department d : departmentMapper.selectBatchIds(deptIds)) {
                deptMap.put(d.getDeptId(), d);
            }
        }

        Set<Integer> patientIds = appointments.stream()
                .map(Appointment::getPatientId).collect(Collectors.toSet());
        Map<Integer, Patient> patientMap = new HashMap<>();
        if (!patientIds.isEmpty()) {
            for (Patient p : patientMapper.selectBatchIds(patientIds)) {
                patientMap.put(p.getPatientId(), p);
            }
        }

        // Load payment records
        Set<Integer> apptIds = appointments.stream()
                .map(Appointment::getApptId).collect(Collectors.toSet());
        Map<Integer, PaymentRecord> paymentMap = new HashMap<>();
        if (!apptIds.isEmpty()) {
            LambdaQueryWrapper<PaymentRecord> prWrapper = new LambdaQueryWrapper<>();
            prWrapper.in(PaymentRecord::getApptId, apptIds);
            for (PaymentRecord pr : paymentRecordMapper.selectList(prWrapper)) {
                paymentMap.put(pr.getApptId(), pr);
            }
        }

        return appointments.stream().map(appt -> {
            AppointmentVO vo = new AppointmentVO();
            vo.setApptId(appt.getApptId());
            vo.setScheduleId(appt.getScheduleId());
            vo.setPatientId(appt.getPatientId());
            vo.setQueueNumber(appt.getQueueNumber());
            vo.setStatus(appt.getStatus());
            vo.setCancelReason(appt.getCancelReason());
            vo.setCreateTime(appt.getCreateTime());
            vo.setUpdateTime(appt.getUpdateTime());

            Patient patient = patientMap.get(appt.getPatientId());
            if (patient != null) {
                vo.setPatientName(patient.getRealName());
                vo.setPatientGender(patient.getGender());
                vo.setPatientPhone(patient.getPhone());
            }

            Schedule schedule = scheduleMap.get(appt.getScheduleId());
            if (schedule != null) {
                vo.setWorkDate(schedule.getWorkDate());
                vo.setShift(schedule.getShift());
                vo.setFee(schedule.getFee());
                Doctor doctor = doctorMap.get(schedule.getDocId());
                if (doctor != null) {
                    vo.setDocId(doctor.getDocId());
                    vo.setDocName(doctor.getDocName());
                    vo.setTitle(doctor.getTitle());
                    vo.setDeptId(doctor.getDeptId());
                    Department dept = deptMap.get(doctor.getDeptId());
                    if (dept != null) {
                        vo.setDeptName(dept.getDeptName());
                    }
                }
            }

            PaymentRecord pr = paymentMap.get(appt.getApptId());
            if (pr != null) {
                vo.setPaymentId(pr.getPaymentId());
                vo.setAmount(pr.getAmount());
                vo.setPayStatus(pr.getPayStatus());
                vo.setPayMethod(pr.getPayMethod());
                vo.setPayTime(pr.getPayTime());
            }

            return vo;
        }).toList();
    }
}
