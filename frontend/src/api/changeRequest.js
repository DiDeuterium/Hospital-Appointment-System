import request from './request'

// ---- 医生端：发起 / 查看 / 撤回排班变更申请 ----

// 发起申请（停诊或修改排班）
export function submitChangeRequest(scheduleId, data) {
  return request.post(`/doctors/schedules/${scheduleId}/change-request`, data)
}

// 我的申请列表（可按 status 过滤）
export function listMyChangeRequests(params) {
  return request.get('/doctors/change-requests', { params })
}

// 撤回待审核申请
export function withdrawChangeRequest(requestId) {
  return request.put(`/doctors/change-requests/${requestId}/withdraw`)
}

// ---- 管理员端：审核 ----

export function listChangeRequests(params) {
  return request.get('/admin/change-requests', { params })
}

export function getChangeRequest(requestId) {
  return request.get(`/admin/change-requests/${requestId}`)
}

// 审核（approved: 1 通过 0 驳回；remark 审核意见）
export function approveChangeRequest(requestId, data) {
  return request.put(`/admin/change-requests/${requestId}/approve`, data)
}
