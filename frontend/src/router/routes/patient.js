import { ROLE } from '@/utils/constants'

const meta = { roles: [ROLE.PATIENT] }

export default [
  {
    path: '/patient',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta,
    redirect: '/patient/home',
    children: [
      {
        path: 'home',
        name: 'PatientHome',
        component: () => import('@/views/patient/Home.vue'),
        meta: { ...meta, title: '科室浏览', icon: 'OfficeBuilding' }
      },
      {
        path: 'doctors/:deptId',
        name: 'PatientDoctors',
        component: () => import('@/views/patient/Doctors.vue'),
        meta: { ...meta, title: '医生列表', hidden: true }
      },
      {
        path: 'schedules/:deptId',
        name: 'PatientSchedules',
        component: () => import('@/views/patient/Schedules.vue'),
        meta: { ...meta, title: '排班号源', hidden: true }
      },
      {
        path: 'appointments',
        name: 'PatientAppointments',
        component: () => import('@/views/patient/Appointments.vue'),
        meta: { ...meta, title: '我的预约', icon: 'Calendar' }
      }
    ]
  }
]
