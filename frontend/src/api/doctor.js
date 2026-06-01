import request from './request'

export function listDoctors(params) {
  return request.get('/admin/doctors', { params })
}

export function createDoctor(data) {
  return request.post('/admin/doctors', data)
}
export function updateDoctor(docId, data) {
  return request.put(`/admin/doctors/${docId}`, data)
}
export function deleteDoctor(docId) {
  return request.delete(`/admin/doctors/${docId}`)
}

// 管理员：启用/停用医生（status: 1 正常 0 停用）
export function toggleDoctorStatus(docId, status) {
  return request.put(`/admin/doctors/${docId}/status`, { status })
}

// 医生本人：个人资料（查 / 改头像&擅长 / 改密码）
export function getDoctorProfile() {
  return request.get('/doctors/me')
}
export function updateDoctorProfile(data) {
  return request.put('/doctors/me', data)
}
export function changeDoctorPassword(data) {
  return request.put('/doctors/me/password', data)
}

// 医生本人：我的排班
export function myDoctorSchedules(docId, params) {
  return request.get(`/doctors/${docId}/schedules`, { params })
}

// 医生本人：某排班的患者名册
export function listPatientsBySchedule(scheduleId) {
  return request.get(`/doctors/schedules/${scheduleId}/patients`)
}
