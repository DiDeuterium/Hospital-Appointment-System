import { ROLE } from '@/utils/constants'

const meta = { roles: [ROLE.DOCTOR] }

export default [
  {
    path: '/doctor',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta,
    redirect: '/doctor/schedules',
    children: [
      {
        path: 'schedules',
        name: 'DoctorSchedules',
        component: () => import('@/views/doctor/Schedules.vue'),
        meta: { ...meta, title: '我的排班', icon: 'Calendar' }
      },
      {
        path: 'schedules/:scheduleId/patients',
        name: 'DoctorPatients',
        component: () => import('@/views/doctor/PatientRoster.vue'),
        meta: { ...meta, title: '患者名册', hidden: true }
      }
    ]
  }
]
