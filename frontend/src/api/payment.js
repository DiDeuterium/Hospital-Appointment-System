import request from './request'

// 管理员：挂号费模拟支付记录台账
// params: { payStatus?, startTime?, endTime? }（时间为 ISO LocalDateTime 字符串）
export function listPayments(params) {
  return request.get('/admin/payments', { params })
}

// 患者：我的支付记录
export function listMyPayments() {
  return request.get('/patients/me/payments')
}
