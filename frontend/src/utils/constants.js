// 角色枚举
export const ROLE = Object.freeze({
  PATIENT: 'patient',
  DOCTOR: 'doctor',
  ADMIN: 'admin'
})

export const ROLE_LABEL = {
  [ROLE.PATIENT]: '患者',
  [ROLE.DOCTOR]: '医生',
  [ROLE.ADMIN]: '管理员'
}

// 预约状态：1-已预约 2-已取消 3-已完成
export const APPT_STATUS = Object.freeze({
  BOOKED: 1,
  CANCELLED: 2,
  FINISHED: 3
})

export const APPT_STATUS_LABEL = {
  [APPT_STATUS.BOOKED]: '已预约',
  [APPT_STATUS.CANCELLED]: '已取消',
  [APPT_STATUS.FINISHED]: '已完成'
}

export const APPT_STATUS_TAG_TYPE = {
  [APPT_STATUS.BOOKED]: 'primary',
  [APPT_STATUS.CANCELLED]: 'info',
  [APPT_STATUS.FINISHED]: 'success'
}

// 时段
export const SHIFT_OPTIONS = [
  { value: '上午', label: '上午' },
  { value: '下午', label: '下午' }
]

// 性别
export const GENDER_OPTIONS = [
  { value: 'M', label: '男' },
  { value: 'F', label: '女' }
]

export const GENDER_LABEL = { M: '男', F: '女' }

// 业务码
export const BIZ_CODE = Object.freeze({
  OK: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  SERVER_ERROR: 500
})
