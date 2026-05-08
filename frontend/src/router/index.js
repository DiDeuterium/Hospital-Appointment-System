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

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.meta.title) {
    document.title = `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE || '医院预约挂号系统'}`
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
