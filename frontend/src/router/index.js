import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import patientRoutes from './routes/patient'
import doctorRoutes from './routes/doctor'
import adminRoutes from './routes/admin'
import { useUserStore } from '@/stores/user'
import { ROLE } from '@/utils/constants'
import { patientLogin, doctorLogin, adminLogin } from '@/api/auth'

const HOME_BY_ROLE = {
  [ROLE.PATIENT]: '/patient/home',
  [ROLE.DOCTOR]: '/doctor/schedules',
  [ROLE.ADMIN]: '/admin/departments'
}

const baseRoutes = [
  {
    path: '/',
    redirect: () => {
      const user = useUserStore()
      return user.isLoggedIn ? HOME_BY_ROLE[user.role] || '/login' : '/login'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { public: true, title: '患者注册' }
  }
]

const errorRoutes = [
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { public: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    ...baseRoutes,
    ...patientRoutes,
    ...doctorRoutes,
    ...adminRoutes,
    ...errorRoutes
  ],
  scrollBehavior: () => ({ top: 0 })
})

// ============ 临时开发开关 ============
// BYPASS_AUTH = true：跳过登录/权限校验，自动用测试账号向真实后端登录拿到 token。
//   - 患者 → 用 data.sql 第一条患者 (赵小明 110101199001011234 / 123456) 登录
//   - 医生 → 用 data.sql 第一条医生 (张伟 DOC001 / 123456) 登录
//   - 管理员 → 用 admin / admin123 登录
//   - 登录成功后 token 缓存到 sessionStorage，同标签页内不复登
//   - 登录失败则回退到 mock 假 token（只可读，不可写）
// ⚠ 上线/联调前必须改回 false 并删除本节全部逻辑。
const BYPASS_AUTH = true
const DEMO_CREDENTIALS = {
  patient: { login: patientLogin, payload: { idCard: '110101199001011234', password: '123456' } },
  doctor:  { login: doctorLogin,  payload: { docId: 'DOC001', password: '123456' } },
  admin:   { login: adminLogin,   payload: { username: 'admin', password: 'admin123' } }
}
const MOCK_PROFILES = {
  patient: { patientId: 1, realName: '测试患者' },
  doctor:  { docId: 'DOC001', docName: '测试医生' },
  admin:   { username: 'dev-admin' }
}

const DEMO_TOKEN_KEY = 'hospital:demoToken'

function getCachedDemoAuth(role) {
  try {
    const obj = JSON.parse(sessionStorage.getItem(DEMO_TOKEN_KEY) || '{}')
    // 缓存存的是 { token, raw } 完整对象
    if (obj.role === role && obj.token) return obj
  } catch { /* ignore */ }
  return null
}

async function tryDemoLogin(role) {
  const cached = getCachedDemoAuth(role)
  if (cached) return { token: cached.token, raw: cached.raw }

  const cred = DEMO_CREDENTIALS[role]
  if (!cred) return null

  try {
    const data = await cred.login(cred.payload)
    if (data?.token) {
      // 缓存完整认证信息
      sessionStorage.setItem(DEMO_TOKEN_KEY, JSON.stringify({ role, token: data.token, raw: data }))
      return { token: data.token, raw: data }
    }
  } catch { /* 后端未启动或密码不对 → 回退 mock */ }
  return null
}

function buildProfile(role, data) {
  if (!data) return MOCK_PROFILES[role]
  if (role === 'patient') return { patientId: data.patientId || data.userId, realName: data.realName || data.name }
  if (role === 'doctor')  return { docId: data.docId || data.userId, docName: data.docName || data.name }
  return { username: data.username || data.userId || 'admin', realName: data.realName || data.name }
}
// ======================================

router.beforeEach(async (to) => {
  const user = useUserStore()
  if (to.meta.title) {
    document.title = `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE || '医院预约挂号系统'}`
  }

  if (BYPASS_AUTH) {
    const targetRole =
      to.path.startsWith('/patient') ? 'patient' :
      to.path.startsWith('/doctor') ? 'doctor' :
      to.path.startsWith('/admin') ? 'admin' : null
    const isMock = user.token === 'dev-mock' || !user.token

    if (targetRole && isMock) {
      // 尝试用测试账号真实登录
      const real = await tryDemoLogin(targetRole)
      if (real) {
        user.login({
          role: targetRole,
          token: real.token,
          profile: buildProfile(targetRole, real.raw)
        })
      } else {
        user.login({ role: targetRole, token: 'dev-mock', profile: MOCK_PROFILES[targetRole] })
      }
    }
    return true
  }

  // 公共路由放行
  if (to.meta.public) return true

  // 未登录
  if (!user.isLoggedIn) {
    ElMessage.warning('请先登录')
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 角色不匹配
  const allowed = to.meta.roles
  if (allowed && allowed.length && !allowed.includes(user.role)) {
    return { path: '/403' }
  }

  return true
})

export default router
