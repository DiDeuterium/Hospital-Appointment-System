import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import patientRoutes from './routes/patient'
import doctorRoutes from './routes/doctor'
import adminRoutes from './routes/admin'
import { useUserStore } from '@/stores/user'
import { ROLE } from '@/utils/constants'
import { patientLogin, doctorLogin, adminLogin, getMe } from '@/api/auth'

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
const BYPASS_AUTH = false
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

function roleFromPath(path) {
  if (path.startsWith('/patient')) return 'patient'
  if (path.startsWith('/doctor')) return 'doctor'
  if (path.startsWith('/admin')) return 'admin'
  return null
}

// BYPASS_AUTH 注入或重注入一次 demo 登录态
async function bypassInject(role, user) {
  const real = await tryDemoLogin(role)
  if (real) {
    user.login({ role, token: real.token, profile: buildProfile(role, real.raw) })
  } else {
    user.login({ role, token: 'dev-mock', profile: MOCK_PROFILES[role] })
  }
}

router.beforeEach(async (to) => {
  const user = useUserStore()
  if (to.meta.title) {
    document.title = `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE || '医院预约挂号系统'}`
  }

  // 已登录用户访问 /login / /register → 直接跳到对应主页（带 redirect 时跳 redirect）
  // 防止脏状态：已经登录还看到登录表单、提交后又叠加一次登录态
  if ((to.path === '/login' || to.path === '/register') && user.isLoggedIn && user.token !== 'dev-mock') {
    const redirect = typeof to.query.redirect === 'string' ? to.query.redirect : null
    return redirect || HOME_BY_ROLE[user.role] || '/'
  }

  // 公共路由（/login /register /403 /404）：未登录或 dev-mock 状态时放行
  if (to.meta.public) return true

  // BYPASS_AUTH 模式：访问 /patient|/doctor|/admin 时若无登录态，自动注入 demo 登录
  if (BYPASS_AUTH) {
    const role = roleFromPath(to.path)
    if (role && (user.token === 'dev-mock' || !user.token)) {
      await bypassInject(role, user)
    }
  }

  // 未登录 → 跳 /login
  if (!user.isLoggedIn) {
    ElMessage.warning('请先登录')
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 真实 token：每次导航都向后端校验是否仍在 tokenStore 中
  // 这是真正"全局"的守卫——任何时刻后端清掉 token，下次导航就会被拦下
  // dev-mock 跳过（无法对后端验真，专给 BYPASS 后端未启动时用）
  if (user.token !== 'dev-mock') {
    try {
      await getMe()
    } catch {
      // token 失效：先清当前登录态 + 清 BYPASS 缓存
      user.logout()
      try { sessionStorage.removeItem(DEMO_TOKEN_KEY) } catch { /* ignore */ }

      // BYPASS_AUTH 模式：尝试 silent 重新 demo 登录，避免打断 dev 体验
      if (BYPASS_AUTH) {
        const role = roleFromPath(to.path)
        if (role) await bypassInject(role, user)
      }

      // 重登失败（生产模式或 BYPASS 后端未启动）→ 踢到登录页
      if (!user.isLoggedIn || user.token === 'dev-mock') {
        return { path: '/login', query: { redirect: to.fullPath } }
      }
    }
  }

  // 角色不匹配
  const allowed = to.meta.roles
  if (allowed && allowed.length && !allowed.includes(user.role)) {
    return { path: '/403' }
  }

  return true
})

export default router
