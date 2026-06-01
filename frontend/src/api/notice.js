import request from './request'

// ---- 公开：患者端查看公告 ----
export function listNotices() {
  return request.get('/notices')
}
export function getNotice(noticeId) {
  return request.get(`/notices/${noticeId}`)
}

// ---- 管理员：公告管理 ----
export function listAdminNotices(params) {
  return request.get('/admin/notices', { params })
}
export function createNotice(data) {
  return request.post('/admin/notices', data)
}
export function updateNotice(noticeId, data) {
  return request.put(`/admin/notices/${noticeId}`, data)
}
export function offlineNotice(noticeId) {
  return request.put(`/admin/notices/${noticeId}/offline`)
}
