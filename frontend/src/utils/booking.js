// 挂号流程：跨页面状态中转（用 sessionStorage 避开后端无 GET /schedules/:id 的问题）
// 用户从 Schedules 选号 → BookConfirm 确认 → 提交 → BookSuccess 展示
// 每个步骤之间通过 sessionStorage 传递 schedule 对象与最终预约结果

const SCHEDULE_KEY = 'hospital:bookingSchedule'
const RESULT_KEY = 'hospital:bookingResult'

export function stashSchedule(schedule) {
  sessionStorage.setItem(SCHEDULE_KEY, JSON.stringify(schedule))
}
export function peekSchedule() {
  try { return JSON.parse(sessionStorage.getItem(SCHEDULE_KEY)) || null }
  catch { return null }
}

export function stashResult(result) {
  sessionStorage.setItem(RESULT_KEY, JSON.stringify(result))
}
export function peekResult() {
  try { return JSON.parse(sessionStorage.getItem(RESULT_KEY)) || null }
  catch { return null }
}

export function clearBooking() {
  sessionStorage.removeItem(SCHEDULE_KEY)
  sessionStorage.removeItem(RESULT_KEY)
}

// 时段 → 时间段字符串（前端固定映射，后端没有此字段）
export const SHIFT_TIME_MAP = {
  '上午': '08:00 - 11:30',
  '下午': '14:00 - 17:00',
  '夜诊': '18:00 - 21:00'
}

// 性别 → 简易头像 emoji
export function genderEmoji(gender) {
  if (gender === 'M') return '👨‍⚕️'
  if (gender === 'F') return '👩‍⚕️'
  return '🩺'
}

// 科室名 → 装饰图标（按关键词推断，纯前端美化）
export function deptIcon(name = '') {
  if (/心/.test(name)) return '❤️'
  if (/眼/.test(name)) return '👁️'
  if (/儿/.test(name)) return '👶'
  if (/口腔|牙/.test(name)) return '🦷'
  if (/骨|外/.test(name)) return '🦴'
  if (/皮肤/.test(name)) return '🧴'
  if (/耳|鼻|喉/.test(name)) return '👂'
  if (/呼吸|肺/.test(name)) return '🫁'
  if (/血/.test(name)) return '🩸'
  if (/脑|神经/.test(name)) return '🧠'
  if (/中医|针灸/.test(name)) return '🌿'
  if (/妇|产/.test(name)) return '🤰'
  return '🏥'
}

// 科室分类（按名称关键词分组）
export const DEPT_CATEGORIES = [
  { key: 'all',     label: '全部' },
  { key: 'internal', label: '内科系', test: (n) => /内/.test(n) },
  { key: 'surgery',  label: '外科系', test: (n) => /外|骨/.test(n) },
  { key: 'wc',       label: '妇儿科', test: (n) => /妇|产|儿/.test(n) },
  { key: 'enth',     label: '五官科', test: (n) => /眼|耳|鼻|喉|口腔|牙/.test(n) },
  { key: 'other',    label: '其他',  test: (n) => !/内|外|骨|妇|产|儿|眼|耳|鼻|喉|口腔|牙/.test(n) }
]

// 日期时间格式化：ISO 转为 YYYY-MM-DD HH:mm:ss
export function formatDateTime(d) {
  if (!d) return '—'
  const s = String(d)
  return s.replace('T', ' ').replace(/\.\d+$/, '').slice(0, 19)
}

// 日期工具：返回 YYYY-MM-DD
export function formatDate(d) {
  const dt = d instanceof Date ? d : new Date(d)
  const y = dt.getFullYear()
  const m = String(dt.getMonth() + 1).padStart(2, '0')
  const day = String(dt.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

// 周几（中文）
export function weekdayCN(d) {
  const wd = (d instanceof Date ? d : new Date(d)).getDay()
  return ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][wd]
}
