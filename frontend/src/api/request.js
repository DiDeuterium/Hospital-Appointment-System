import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/storage'
import { BIZ_CODE } from '@/utils/constants'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000
})

service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) config.headers.token = token
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
    ElMessage.error(msg)

    if (res.code === BIZ_CODE.UNAUTHORIZED) {
      // dev-mock token 不会被后端认可，不要清登录态
      if (getToken() === 'dev-mock') return Promise.reject(new Error(msg))
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
    ElMessage.error(msg)
    if (status === 401 && getToken() !== 'dev-mock') {
      clearAuth()
      setTimeout(() => { location.href = '/login' }, 300)
    }
    return Promise.reject(new Error(msg))
  }
)

export default service
