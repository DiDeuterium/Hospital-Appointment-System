import request from './request'

// 患者/管理员通用：按 deptId / workDate / shift / docId 查
export function listSchedules(params) {
  return request.get('/schedules', { params })
}

export function createSchedule(data) {
  return request.post('/schedules', data)
}
export function updateSchedule(scheduleId, data) {
  return request.put(`/schedules/${scheduleId}`, data)
}
export function deleteSchedule(scheduleId) {
  return request.delete(`/schedules/${scheduleId}`)
}
