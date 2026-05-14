// 大陆 18 位身份证（含校验位规则）
export function isIdCard(value) {
  if (!/^\d{17}[\dXx]$/.test(value)) return false
  const weights = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const checks = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
  let sum = 0
  for (let i = 0; i < 17; i++) sum += Number(value[i]) * weights[i]
  return checks[sum % 11] === value[17].toUpperCase()
}

export function isPhone(value) {
  return /^1[3-9]\d{9}$/.test(value)
}
