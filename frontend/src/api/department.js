import request from './request'

// 列表（含模糊搜索）
export function listDepartments(params) {
  return request.get('/departments', { params })
}

// 该科室下的医生
export function listDoctorsByDept(deptId) {
  return request.get(`/departments/${deptId}/doctors`)
}

// 管理员 CRUD
export function createDepartment(data) {
  return request.post('/admin/departments', data)
}
export function updateDepartment(deptId, data) {
  return request.put(`/admin/departments/${deptId}`, data)
}
export function deleteDepartment(deptId) {
  return request.delete(`/admin/departments/${deptId}`)
}
