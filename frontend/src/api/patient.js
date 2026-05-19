import request from './request'

// 个人中心：查 / 改资料 / 改密码
export function getMyProfile() {
  return request.get('/patients/me')
}

export function updateMyProfile(data) {
  return request.put('/patients/me', data)
}

export function changeMyPassword(data) {
  return request.put('/patients/me/password', data)
}
