package com.hospital.service;

import com.hospital.dto.request.AppointmentRequest;
import com.hospital.dto.response.AppointmentVO;

import java.util.List;

public interface AppointmentService {
    AppointmentVO makeAppointment(AppointmentRequest request);
    void cancelAppointment(Integer apptId, String cancelReason);
    void finishAppointment(Integer apptId);
    void payAppointment(Integer apptId);
    List<AppointmentVO> listByPatient(Integer patientId, Integer status);
    List<AppointmentVO> listBySchedule(Integer scheduleId);
}
