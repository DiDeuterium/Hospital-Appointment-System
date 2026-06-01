package com.hospital.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hospital.dto.request.ChangeRequestDTO;
import com.hospital.entity.*;
import com.hospital.exception.BusinessException;
import com.hospital.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleChangeRequestService {

    private final ScheduleChangeRequestMapper requestMapper;
    private final ScheduleMapper scheduleMapper;
    private final AppointmentMapper appointmentMapper;
    private final DoctorMapper doctorMapper;

    public ScheduleChangeRequestService(ScheduleChangeRequestMapper requestMapper,
                                         ScheduleMapper scheduleMapper,
                                         AppointmentMapper appointmentMapper,
                                         DoctorMapper doctorMapper) {
        this.requestMapper = requestMapper;
        this.scheduleMapper = scheduleMapper;
        this.appointmentMapper = appointmentMapper;
        this.doctorMapper = doctorMapper;
    }

    // ==================== 医生端 ====================

    @Transactional
    public ScheduleChangeRequest submitChangeRequest(Integer docId, Integer scheduleId, ChangeRequestDTO dto) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(404, "排班不存在");
        }
        if (!schedule.getDocId().equals(docId)) {
            throw new BusinessException(403, "只能对自己的排班发起申请");
        }

        long activeApptCount = countActiveAppointments(scheduleId);

        if (dto.getChangeType() == ScheduleChangeRequest.TYPE_CANCEL) {
            validateCancelRequest(schedule, activeApptCount);
        } else if (dto.getChangeType() == ScheduleChangeRequest.TYPE_MODIFY) {
            validateModifyRequest(dto, schedule, activeApptCount);
        } else {
            throw new BusinessException(400, "无效的变更类型");
        }

        ScheduleChangeRequest req = new ScheduleChangeRequest();
        req.setScheduleId(scheduleId);
        req.setChangeType(dto.getChangeType());
        req.setTargetWorkDate(dto.getTargetWorkDate());
        req.setTargetShift(dto.getTargetShift());
        req.setTargetTotalQuota(dto.getTargetTotalQuota());
        req.setReason(dto.getReason());
        req.setStatus(ScheduleChangeRequest.STATUS_PENDING);
        requestMapper.insert(req);
        return req;
    }

    public List<ScheduleChangeRequest> listByDoctor(Integer docId, Integer status) {
        // Find all schedules for this doctor
        LambdaQueryWrapper<Schedule> schWrapper = new LambdaQueryWrapper<>();
        schWrapper.eq(Schedule::getDocId, docId);
        List<Integer> scheduleIds = scheduleMapper.selectList(schWrapper).stream()
                .map(Schedule::getScheduleId).toList();
        if (scheduleIds.isEmpty()) return List.of();

        LambdaQueryWrapper<ScheduleChangeRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ScheduleChangeRequest::getScheduleId, scheduleIds);
        if (status != null) {
            wrapper.eq(ScheduleChangeRequest::getStatus, status);
        }
        wrapper.orderByDesc(ScheduleChangeRequest::getApplyTime);
        return requestMapper.selectList(wrapper);
    }

    @Transactional
    public void withdraw(Integer docId, Integer requestId) {
        ScheduleChangeRequest req = requestMapper.selectById(requestId);
        if (req == null) {
            throw new BusinessException(404, "申请不存在");
        }
        // Verify this request belongs to the doctor
        Schedule schedule = scheduleMapper.selectById(req.getScheduleId());
        if (schedule == null || !schedule.getDocId().equals(docId)) {
            throw new BusinessException(403, "只能撤回自己的申请");
        }
        if (req.getStatus() != ScheduleChangeRequest.STATUS_PENDING) {
            throw new BusinessException(409, "仅待审核申请可以撤回");
        }
        req.setStatus(ScheduleChangeRequest.STATUS_WITHDRAWN);
        requestMapper.updateById(req);
    }

    // ==================== 管理员端 ====================

    public List<ScheduleChangeRequest> listAll(Integer status) {
        LambdaQueryWrapper<ScheduleChangeRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ScheduleChangeRequest::getStatus, status);
        }
        wrapper.orderByAsc(ScheduleChangeRequest::getStatus)
               .orderByDesc(ScheduleChangeRequest::getApplyTime);
        return requestMapper.selectList(wrapper);
    }

    public ScheduleChangeRequest getById(Integer requestId) {
        ScheduleChangeRequest req = requestMapper.selectById(requestId);
        if (req == null) {
            throw new BusinessException(404, "申请不存在");
        }
        return req;
    }

    @Transactional
    public void approve(Integer adminId, Integer requestId, String remark) {
        // Optimistic locking: only update if status=1 (pending)
        LambdaUpdateWrapper<ScheduleChangeRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ScheduleChangeRequest::getRequestId, requestId)
                     .eq(ScheduleChangeRequest::getStatus, ScheduleChangeRequest.STATUS_PENDING)
                     .set(ScheduleChangeRequest::getStatus, ScheduleChangeRequest.STATUS_APPROVED)
                     .set(ScheduleChangeRequest::getAuditAdminId, adminId)
                     .set(ScheduleChangeRequest::getAuditTime, LocalDateTime.now())
                     .set(ScheduleChangeRequest::getAuditRemark, remark);

        int rows = requestMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException(409, "申请已被其他管理员处理");
        }

        ScheduleChangeRequest req = requestMapper.selectById(requestId);
        Schedule schedule = scheduleMapper.selectById(req.getScheduleId());

        if (req.getChangeType() == ScheduleChangeRequest.TYPE_CANCEL) {
            // Re-check no active appointments
            long activeCount = countActiveAppointments(req.getScheduleId());
            if (activeCount > 0) {
                throw new BusinessException(409, "该排班已有有效预约，无法停诊");
            }
            schedule.setStatus(0);
            scheduleMapper.updateById(schedule);

        } else if (req.getChangeType() == ScheduleChangeRequest.TYPE_MODIFY) {
            applyModify(schedule, req);
        }
    }

    @Transactional
    public void reject(Integer adminId, Integer requestId, String remark) {
        LambdaUpdateWrapper<ScheduleChangeRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ScheduleChangeRequest::getRequestId, requestId)
                     .eq(ScheduleChangeRequest::getStatus, ScheduleChangeRequest.STATUS_PENDING)
                     .set(ScheduleChangeRequest::getStatus, ScheduleChangeRequest.STATUS_REJECTED)
                     .set(ScheduleChangeRequest::getAuditAdminId, adminId)
                     .set(ScheduleChangeRequest::getAuditTime, LocalDateTime.now())
                     .set(ScheduleChangeRequest::getAuditRemark, remark);

        int rows = requestMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new BusinessException(409, "申请已被其他管理员处理");
        }
    }

    // ==================== 校验方法 ====================

    private void validateCancelRequest(Schedule schedule, long activeApptCount) {
        if (schedule.getStatus() != null && schedule.getStatus() == 0) {
            throw new BusinessException(409, "该排班已停诊");
        }
        if (activeApptCount > 0) {
            throw new BusinessException(409, "该排班已有患者预约，不能申请停诊");
        }
    }

    private void validateModifyRequest(ChangeRequestDTO dto, Schedule schedule, long activeApptCount) {
        if (dto.getTargetWorkDate() == null && dto.getTargetShift() == null && dto.getTargetTotalQuota() == null) {
            throw new BusinessException(400, "修改排班申请至少填写一个目标字段");
        }

        // If modifying date or shift, check no active appointments
        if ((dto.getTargetWorkDate() != null || dto.getTargetShift() != null) && activeApptCount > 0) {
            throw new BusinessException(409, "该排班已有有效预约，不允许修改日期和时段");
        }

        // Check target date/shift conflict with existing schedules of the same doctor
        if (dto.getTargetWorkDate() != null || dto.getTargetShift() != null) {
            LocalDate targetDate = dto.getTargetWorkDate() != null ? dto.getTargetWorkDate() : schedule.getWorkDate();
            String targetShift = dto.getTargetShift() != null ? dto.getTargetShift() : schedule.getShift();

            LambdaQueryWrapper<Schedule> conflictWrapper = new LambdaQueryWrapper<>();
            conflictWrapper.eq(Schedule::getDocId, schedule.getDocId())
                          .eq(Schedule::getWorkDate, targetDate)
                          .eq(Schedule::getShift, targetShift)
                          .ne(Schedule::getScheduleId, schedule.getScheduleId());
            if (scheduleMapper.selectCount(conflictWrapper) > 0) {
                throw new BusinessException(409, "目标日期和时段已有该医生的其他排班");
            }
        }

        // If modifying total quota, new >= booked count
        if (dto.getTargetTotalQuota() != null) {
            int booked = schedule.getTotalQuota() - schedule.getRestQuota();
            if (dto.getTargetTotalQuota() < booked) {
                throw new BusinessException(409, "目标总号源不能小于已预约人数(" + booked + ")");
            }
        }

        // Validate target shift
        if (dto.getTargetShift() != null
                && !dto.getTargetShift().equals("上午")
                && !dto.getTargetShift().equals("下午")
                && !dto.getTargetShift().equals("夜诊")) {
            throw new BusinessException(400, "目标时段只能是上午、下午或夜诊");
        }
    }

    private void applyModify(Schedule schedule, ScheduleChangeRequest req) {
        long activeCount = countActiveAppointments(req.getScheduleId());

        if (req.getTargetWorkDate() != null || req.getTargetShift() != null) {
            if (activeCount > 0) {
                throw new BusinessException(409, "该排班已有有效预约，不允许修改日期和时段");
            }
            // Re-check conflict
            LocalDate targetDate = req.getTargetWorkDate() != null ? req.getTargetWorkDate() : schedule.getWorkDate();
            String targetShift = req.getTargetShift() != null ? req.getTargetShift() : schedule.getShift();
            LambdaQueryWrapper<Schedule> conflictWrapper = new LambdaQueryWrapper<>();
            conflictWrapper.eq(Schedule::getDocId, schedule.getDocId())
                          .eq(Schedule::getWorkDate, targetDate)
                          .eq(Schedule::getShift, targetShift)
                          .ne(Schedule::getScheduleId, schedule.getScheduleId());
            if (scheduleMapper.selectCount(conflictWrapper) > 0) {
                throw new BusinessException(409, "目标日期和时段已有该医生的其他排班");
            }
        }

        if (req.getTargetWorkDate() != null) schedule.setWorkDate(req.getTargetWorkDate());
        if (req.getTargetShift() != null) schedule.setShift(req.getTargetShift());

        if (req.getTargetTotalQuota() != null) {
            int booked = schedule.getTotalQuota() - schedule.getRestQuota();
            if (req.getTargetTotalQuota() < booked) {
                throw new BusinessException(409, "目标总号源不能小于已预约人数");
            }
            int newRestQuota = schedule.getRestQuota() + (req.getTargetTotalQuota() - schedule.getTotalQuota());
            schedule.setTotalQuota(req.getTargetTotalQuota());
            schedule.setRestQuota(newRestQuota);
        }

        scheduleMapper.updateById(schedule);
    }

    private long countActiveAppointments(Integer scheduleId) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getScheduleId, scheduleId)
               .eq(Appointment::getStatus, Appointment.STATUS_BOOKED);
        return appointmentMapper.selectCount(wrapper);
    }
}
