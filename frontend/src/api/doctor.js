import request from './request'

export function listDoctors(params) {
  return request.get('/doctors', { params })
}

export function createDoctor(data) {
  return request.post('/doctors', data)
}
export function updateDoctor(docId, data) {
  return request.put(`/doctors/${docId}`, data)
}
export function deleteDoctor(docId) {
  return request.delete(`/doctors/${docId}`)
}

// 医生本人：我的排班
export function myDoctorSchedules(docId, params) {
  return request.get(`/doctors/${docId}/schedules`, { params })
}

// 医生本人：某排班的患者名册
export function listPatientsBySchedule(scheduleId) {
  return request.get(`/doctors/schedules/${scheduleId}/patients`)
}
