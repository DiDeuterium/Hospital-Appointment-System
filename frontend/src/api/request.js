import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/storage'
import { BIZ_CODE } from '@/utils/constants'

// 开发期保护：在本地环境，任何 401 都不触发强制登出
const isLocalDev = typeof location !== 'undefined'
  && (location.hostname === 'localhost' || location.hostname === '127.0.0.1')

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    // 登录/注册是公开端点，不应带 token，避免后端全局拦截器误判
    const url = config.url || ''
    const isPublic = url.includes('/login') || url.includes('/register')
    if (!isPublic) {
      const token = getToken()
      if (token) config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

service.interceptors.response.use(
  response => {
    const res = response.data
    // 非 JSON / 文件流：直接返回
    if (typeof res !== 'object' || res === null) return res

    if (res.code === BIZ_CODE.OK) return res.data

    const msg = res.message || '请求失败'

    // 401 在本地开发 / mock token 时：不弹错误，不登出，静默失败
    if (res.code === BIZ_CODE.UNAUTHORIZED && (getToken() === 'dev-mock' || isLocalDev)) {
      // 开发模式下保留错误消息让调用方自行决定是否弹窗（挂号等写操作需要提示用户）
      console.warn('[DEV] API 返回 401，已跳过登出:', msg)
      return Promise.reject(new Error('[开发模式] ' + (msg || '请使用真实账号登录后重试')))
    }

    ElMessage.error(msg)

    if (res.code === BIZ_CODE.UNAUTHORIZED) {
      clearAuth()
      setTimeout(() => {
        if (location.hash !== '#/login' && location.pathname !== '/login') {
          location.href = '/login'
        }
      }, 300)
    }

    return Promise.reject(new Error(msg))
  },
  error => {
    const status = error.response?.status
    const data = error.response?.data
    const msg = data?.message || error.message || '网络异常，请稍后重试'

    if (status === 401 && (getToken() === 'dev-mock' || isLocalDev)) {
      console.warn('[DEV] HTTP 401，已跳过登出:', msg)
      return Promise.reject(new Error('[开发模式] ' + (msg || '请使用真实账号登录后重试')))
    }

    ElMessage.error(msg)
    if (status === 401) {
      clearAuth()
      setTimeout(() => { location.href = '/login' }, 300)
    }
    return Promise.reject(new Error(msg))
  }
)

export default service
