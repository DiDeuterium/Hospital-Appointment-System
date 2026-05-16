import request from './request'

// 创建预约（核心接口）
export function createAppointment(data) {
  return request.post('/appointments', data)
}

// 取消预约
export function cancelAppointment(apptId) {
  return request.put(`/appointments/${apptId}/cancel`)
}

// 我的预约
export function listMyAppointments(patientId, params) {
  return request.get(`/appointments/patients/${patientId}`, { params })
}
