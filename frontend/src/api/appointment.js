import request from './request'

// 创建预约（核心接口）
export function createAppointment(data) {
  return request.post('/appointments', data)
}

// 取消预约（可附带取消原因）
export function cancelAppointment(apptId, cancelReason) {
  return request.put(`/appointments/${apptId}/cancel`, { cancelReason: cancelReason || null })
}

// 完成就诊（医生操作）
export function finishAppointment(apptId) {
  return request.put(`/appointments/${apptId}/finish`)
}

// 挂号费模拟支付
export function payAppointment(apptId) {
  return request.post(`/appointments/${apptId}/pay`)
}

// 我的预约
export function listMyAppointments(patientId, params) {
  return request.get(`/appointments/patients/${patientId}`, { params })
}
