import { ROLE } from '@/utils/constants'

const meta = { roles: [ROLE.ADMIN] }

export default [
  {
    path: '/admin',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta,
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { ...meta, title: '数据看板', icon: 'dashboard' }
      },
      {
        path: 'departments',
        name: 'AdminDepartments',
        component: () => import('@/views/admin/Departments.vue'),
        meta: { ...meta, title: '科室管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'doctors',
        name: 'AdminDoctors',
        component: () => import('@/views/admin/Doctors.vue'),
        meta: { ...meta, title: '医生管理', icon: 'User' }
      },
      {
        path: 'schedules',
        name: 'AdminSchedules',
        component: () => import('@/views/admin/Schedules.vue'),
        meta: { ...meta, title: '排班发布', icon: 'Calendar' }
      }
    ]
  }
]
