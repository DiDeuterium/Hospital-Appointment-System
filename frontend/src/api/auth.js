import request from './request'

// 患者
export function patientRegister(data) {
  return request.post('/patients/register', data)
}
export function patientLogin(data) {
  return request.post('/patients/login', data)
}

// 医生
export function doctorLogin(data) {
  return request.post('/doctors/login', data)
}

// 管理员
export function adminLogin(data) {
  return request.post('/admin/login', data)
}
