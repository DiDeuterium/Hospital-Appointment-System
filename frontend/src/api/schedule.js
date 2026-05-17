import request from './request'

// 患者公开：必传 deptId，可选 workDate / shift
export function listSchedules(params) {
  return request.get('/schedules', { params })
}

// 管理员：接收 docId / workDate（语义与公开接口不同）
export function listAdminSchedules(params) {
  return request.get('/admin/schedules', { params })
}

export function createSchedule(data) {
  return request.post('/admin/schedules', data)
}
export function updateSchedule(scheduleId, data) {
  return request.put(`/admin/schedules/${scheduleId}`, data)
}
export function deleteSchedule(scheduleId) {
  return request.delete(`/admin/schedules/${scheduleId}`)
}
