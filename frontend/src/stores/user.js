import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ROLE } from '@/utils/constants'
import { getToken, setToken, getUser, setUser, clearAuth } from '@/utils/storage'

/**
 * 登录态：
 *   role     角色（'patient' | 'doctor' | 'admin'）
 *   token    后端返回的 token
 *   profile  用户信息（不同角色字段不同：patient: { patientId, realName }；doctor: { docId, docName }；admin: { username }）
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(getToken())
  const stored = getUser()
  const role = ref(stored?.role || '')
  const profile = ref(stored?.profile || null)

  const isLoggedIn = computed(() => !!token.value && !!role.value)
  const displayName = computed(() => {
    if (!profile.value || !role.value) return ''
    return profile.value.realName
      || profile.value.docName
      || profile.value.name
      || profile.value.username
      || profile.value.userId
      || role.value
  })

  function login({ role: r, token: t, profile: p }) {
    role.value = r
    token.value = t
    profile.value = p
    setToken(t)
    setUser({ role: r, profile: p })
  }

  function logout() {
    role.value = ''
    token.value = ''
    profile.value = null
    clearAuth()
  }

  // 个人中心修改资料后，局部合并 profile 并同步到 localStorage，
  // 避免覆盖 token / 触发完整 login 流程
  function updateProfile(partial) {
    profile.value = { ...(profile.value || {}), ...partial }
    setUser({ role: role.value, profile: profile.value })
  }

  function isRole(...roles) {
    return roles.includes(role.value)
  }

  return {
    token,
    role,
    profile,
    isLoggedIn,
    displayName,
    login,
    logout,
    updateProfile,
    isRole,
    ROLE
  }
})
