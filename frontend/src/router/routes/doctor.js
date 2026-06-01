import { ROLE } from '@/utils/constants'

const meta = { roles: [ROLE.DOCTOR] }

export default [
  {
    path: '/doctor',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta,
    redirect: '/doctor/workbench',
    children: [
      {
        path: 'workbench',
        name: 'DoctorWorkbench',
        component: () => import('@/views/doctor/Workbench.vue'),
        meta: { ...meta, title: '工作台', icon: 'dashboard' }
      },
      {
        path: 'schedules',
        name: 'DoctorSchedules',
        component: () => import('@/views/doctor/Schedules.vue'),
        meta: { ...meta, title: '我的排班', icon: 'calendar' }
      },
      {
        path: 'schedules/:scheduleId/patients',
        name: 'DoctorPatients',
        component: () => import('@/views/doctor/PatientRoster.vue'),
        meta: { ...meta, title: '患者名册', hidden: true }
      },
      {
        path: 'change-requests',
        name: 'DoctorChangeRequests',
        component: () => import('@/views/doctor/ChangeRequests.vue'),
        meta: { ...meta, title: '我的申请', icon: 'file-text' }
      },
      {
        path: 'profile',
        name: 'DoctorProfile',
        component: () => import('@/views/doctor/Profile.vue'),
        meta: { ...meta, title: '个人资料', icon: 'user' }
      }
    ]
  }
]
