package com.hospital.service;

import com.hospital.dto.request.ScheduleRequest;
import com.hospital.dto.response.ScheduleVO;
import com.hospital.entity.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<ScheduleVO> list(Integer deptId, Integer docId, LocalDate workDate, String shift);
    List<ScheduleVO> listAvailable(Integer deptId, LocalDate workDate, String shift);
    Schedule getById(Integer scheduleId);
    void add(ScheduleRequest request);
    void update(Integer scheduleId, ScheduleRequest request);
    void delete(Integer scheduleId);
}
