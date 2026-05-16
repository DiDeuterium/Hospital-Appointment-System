import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import patientRoutes from './routes/patient'
import doctorRoutes from './routes/doctor'
import adminRoutes from './routes/admin'
import { useUserStore } from '@/stores/user'
import { ROLE } from '@/utils/constants'

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
// BYPASS_AUTH = true：跳过登录/权限校验，按访问路径自动注入对应角色的 mock 登录态，
//                     便于 UI 设计阶段直接访问任意页面查看效果。
// ⚠ 上线/联调前必须改回 false 并删除 mock 注入逻辑。
const BYPASS_AUTH = true
const MOCK_PROFILES = {
  patient: { patientId: 1, realName: '测试患者' },
  doctor: { docId: 'DOC001', docName: '测试医生' },
  admin: { username: 'dev-admin' }
}
// ======================================

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.meta.title) {
    document.title = `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE || '医院预约挂号系统'}`
  }

  if (BYPASS_AUTH) {
    // 按路由前缀推断角色；若已登录真实账号则不覆盖
    const targetRole =
      to.path.startsWith('/patient') ? 'patient' :
      to.path.startsWith('/doctor') ? 'doctor' :
      to.path.startsWith('/admin') ? 'admin' : null
    const isMock = user.token === 'dev-mock' || !user.token
    if (targetRole && isMock) {
      user.login({ role: targetRole, token: 'dev-mock', profile: MOCK_PROFILES[targetRole] })
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
