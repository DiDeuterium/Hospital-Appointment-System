import { ROLE } from '@/utils/constants'

const meta = { roles: [ROLE.PATIENT] }

export default [
  {
    path: '/patient',
    component: () => import('@/layouts/PatientLayout.vue'),
    meta,
    redirect: '/patient/home',
    children: [
      {
        path: 'home',
        name: 'PatientHome',
        component: () => import('@/views/patient/Home.vue'),
        meta: { ...meta, title: '首页' }
      },
      {
        path: 'departments',
        name: 'PatientDepartments',
        component: () => import('@/views/patient/Departments.vue'),
        meta: { ...meta, title: '选择科室' }
      },
      {
        path: 'departments/:deptId/doctors',
        name: 'PatientDoctors',
        component: () => import('@/views/patient/Doctors.vue'),
        meta: { ...meta, title: '医生列表', hidden: true }
      },
      {
        path: 'departments/:deptId/doctors/:docId',
        name: 'PatientDoctorDetail',
        component: () => import('@/views/patient/DoctorDetail.vue'),
        meta: { ...meta, title: '医生详情', hidden: true }
      },
      {
        path: 'departments/:deptId/schedules',
        name: 'PatientSchedules',
        component: () => import('@/views/patient/Schedules.vue'),
        meta: { ...meta, title: '排班选号', hidden: true }
      },
      {
        path: 'book/confirm',
        name: 'PatientBookConfirm',
        component: () => import('@/views/patient/BookConfirm.vue'),
        meta: { ...meta, title: '挂号确认', hidden: true }
      },
      {
        path: 'book/success',
        name: 'PatientBookSuccess',
        component: () => import('@/views/patient/BookSuccess.vue'),
        meta: { ...meta, title: '挂号成功', hidden: true }
      },
      {
        path: 'appointments',
        name: 'PatientAppointments',
        component: () => import('@/views/patient/Appointments.vue'),
        meta: { ...meta, title: '我的预约' }
      },
      {
        path: 'appointments/:apptId',
        name: 'PatientApptDetail',
        component: () => import('@/views/patient/ApptDetail.vue'),
        meta: { ...meta, title: '预约详情', hidden: true }
      },
      {
        path: 'profile',
        name: 'PatientProfile',
        component: () => import('@/views/patient/Profile.vue'),
        meta: { ...meta, title: '个人中心' }
      }
    ]
  }
]
