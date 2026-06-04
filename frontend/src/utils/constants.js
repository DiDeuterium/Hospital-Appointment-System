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

// 预约状态：1-已预约 2-已取消 3-已完成 4-已过期
export const APPT_STATUS = Object.freeze({
  BOOKED: 1,
  CANCELLED: 2,
  FINISHED: 3,
  EXPIRED: 4
})

export const APPT_STATUS_LABEL = {
  [APPT_STATUS.BOOKED]: '已预约',
  [APPT_STATUS.CANCELLED]: '已取消',
  [APPT_STATUS.FINISHED]: '已完成',
  [APPT_STATUS.EXPIRED]: '已过期'
}

export const APPT_STATUS_TAG_TYPE = {
  [APPT_STATUS.BOOKED]: 'primary',
  [APPT_STATUS.CANCELLED]: 'info',
  [APPT_STATUS.FINISHED]: 'success',
  [APPT_STATUS.EXPIRED]: 'warning'
}

// 时段
export const SHIFT_OPTIONS = [
  { value: '上午', label: '上午' },
  { value: '下午', label: '下午' },
  { value: '夜诊', label: '夜诊' }
]

// 挂号费模拟支付状态：0-待支付 1-已支付 2-已关闭 3-已退款
export const PAY_STATUS = Object.freeze({
  UNPAID: 0,
  PAID: 1,
  CLOSED: 2,
  REFUNDED: 3
})

export const PAY_STATUS_LABEL = {
  [PAY_STATUS.UNPAID]: '待支付',
  [PAY_STATUS.PAID]: '已支付',
  [PAY_STATUS.CLOSED]: '已关闭',
  [PAY_STATUS.REFUNDED]: '已退款'
}

export const PAY_STATUS_TAG_TYPE = {
  [PAY_STATUS.UNPAID]: 'warning',
  [PAY_STATUS.PAID]: 'success',
  [PAY_STATUS.CLOSED]: 'info',
  [PAY_STATUS.REFUNDED]: 'danger'
}

// 排班变更类型：1-停诊 2-修改排班
export const CHANGE_TYPE = Object.freeze({
  CANCEL: 1,
  MODIFY: 2
})

export const CHANGE_TYPE_LABEL = {
  [CHANGE_TYPE.CANCEL]: '停诊',
  [CHANGE_TYPE.MODIFY]: '修改排班'
}

// 排班变更审核状态：1-待审核 2-已通过 3-已驳回 4-已撤回
export const CHANGE_STATUS = Object.freeze({
  PENDING: 1,
  APPROVED: 2,
  REJECTED: 3,
  WITHDRAWN: 4
})

export const CHANGE_STATUS_LABEL = {
  [CHANGE_STATUS.PENDING]: '待审核',
  [CHANGE_STATUS.APPROVED]: '已通过',
  [CHANGE_STATUS.REJECTED]: '已驳回',
  [CHANGE_STATUS.WITHDRAWN]: '已撤回'
}

export const CHANGE_STATUS_TAG_TYPE = {
  [CHANGE_STATUS.PENDING]: 'warning',
  [CHANGE_STATUS.APPROVED]: 'success',
  [CHANGE_STATUS.REJECTED]: 'danger',
  [CHANGE_STATUS.WITHDRAWN]: 'info'
}

// 排班出诊状态：1-正常 0-停诊
export const SCHEDULE_STATUS = Object.freeze({
  NORMAL: 1,
  STOPPED: 0
})

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
