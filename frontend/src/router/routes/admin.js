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
      },
      {
        path: 'change-requests',
        name: 'AdminChangeRequests',
        component: () => import('@/views/admin/ChangeRequests.vue'),
        meta: { ...meta, title: '变更审核', icon: 'refresh' }
      },
      {
        path: 'payments',
        name: 'AdminPayments',
        component: () => import('@/views/admin/Payments.vue'),
        meta: { ...meta, title: '支付记录', icon: 'credit-card' }
      },
      {
        path: 'notices',
        name: 'AdminNotices',
        component: () => import('@/views/admin/Notices.vue'),
        meta: { ...meta, title: '公告管理', icon: 'bell' }
      }
    ]
  }
]
