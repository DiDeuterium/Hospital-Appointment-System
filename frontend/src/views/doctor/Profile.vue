<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDoctorProfile, updateDoctorProfile, changeDoctorPassword } from '@/api/doctor'
import { listDepartments } from '@/api/department'
import { GENDER_LABEL } from '@/utils/constants'
import { genderEmoji } from '@/utils/booking'
import request from '@/api/request'
import PageHeader from '@/components/PageHeader.vue'
import SectionCard from '@/components/SectionCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import AppIcon from '@/components/AppIcon.vue'

// ---- 资料 ----
const profileRef = ref()
const loadingProfile = ref(false)
const savingProfile = ref(false)
const uploading = ref(false)
const imgError = ref(false)
const deptName = ref('')
const profile = reactive({
  docId: null,
  docName: '',
  gender: 'M',
  title: '',
  deptId: null,
  avatarUrl: '',
  specialty: '',
  status: 1
})

async function loadProfile() {
  loadingProfile.value = true
  try {
    const [data, depts] = await Promise.all([
      getDoctorProfile(),
      listDepartments().catch(() => [])
    ])
    Object.assign(profile, data)
    const dept = (depts || []).find(d => String(d.deptId) === String(data.deptId))
    deptName.value = dept?.deptName || ('科室 ' + (data.deptId ?? ''))
  } catch { /* 拦截器已弹错误 */ } finally {
    loadingProfile.value = false
  }
}

async function handleAvatarUpload(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  uploading.value = true
  try {
    const res = await request.post('/doctors/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    profile.avatarUrl = res.avatarUrl
    imgError.value = false
    ElMessage.success('头像上传成功')
  } catch { /* 拦截器已弹错误 */ } finally {
    uploading.value = false
  }
  return false
}

async function saveProfile() {
  savingProfile.value = true
  try {
    await updateDoctorProfile({
      avatarUrl: profile.avatarUrl || null,
      specialty: profile.specialty || null
    })
    imgError.value = false
    ElMessage.success('资料已保存')
  } catch { /* 拦截器已弹错误 */ } finally {
    savingProfile.value = false
  }
}

// ---- 密码 ----
const pwdRef = ref()
const pwdSaving = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' },
    {
      validator: (_r, v, cb) =>
        v && v === pwdForm.oldPassword ? cb(new Error('新密码不能与原密码相同')) : cb(),
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_r, v, cb) =>
        v === pwdForm.newPassword ? cb() : cb(new Error('两次输入的密码不一致')),
      trigger: 'blur'
    }
  ]
}

async function savePassword() {
  await pwdRef.value.validate()
  pwdSaving.value = true
  try {
    await changeDoctorPassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    pwdRef.value.clearValidate()
    ElMessage.success('密码已修改')
  } catch { /* 拦截器已弹错误 */ } finally {
    pwdSaving.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <div class="page-container">
    <PageHeader
      title="个人资料"
      subtitle="维护你的头像、擅长领域与登录密码"
      :breadcrumbs="[{ label: '工作台', to: '/doctor' }, { label: '个人资料' }]"
    />

    <!-- 展示信息 + 可编辑项 -->
    <SectionCard title="基本资料" class="section-gap" v-loading="loadingProfile">
      <template #extra>
        <span class="hint">
          <AppIcon name="info" :size="14" />
          姓名、性别、职称、科室等由管理员维护
        </span>
      </template>

      <div class="head">
        <div class="head__avatar">
          <img
            v-if="profile.avatarUrl && !imgError"
            :src="profile.avatarUrl"
            :alt="profile.docName"
            class="head__img"
            @error="imgError = true"
          />
          <span v-else>{{ genderEmoji(profile.gender) }}</span>
        </div>
        <div class="head__info">
          <div class="head__name-row">
            <h2 class="head__name">{{ profile.docName }}</h2>
            <StatusTag v-if="profile.title" type="primary" size="small">{{ profile.title }}</StatusTag>
            <StatusTag :type="profile.status === 0 ? 'danger' : 'success'" size="small">
              {{ profile.status === 0 ? '已停用' : '在职' }}
            </StatusTag>
          </div>
          <div class="head__meta">
            <span>工号 {{ profile.docId }}</span>
            <span class="head__sep">·</span>
            <span>{{ GENDER_LABEL[profile.gender] || '—' }}</span>
            <span class="head__sep">·</span>
            <span>{{ deptName }}</span>
          </div>
        </div>
      </div>

      <el-form ref="profileRef" :model="profile" label-position="top" @submit.prevent="saveProfile">
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-upload
              :show-file-list="false"
              :http-request="handleAvatarUpload"
              accept="image/*"
            >
              <div class="avatar-upload__preview">
                <img
                  v-if="profile.avatarUrl && !imgError"
                  :src="profile.avatarUrl"
                  :alt="profile.docName"
                  class="avatar-upload__img"
                  @error="imgError = true"
                />
                <span v-else class="avatar-upload__emoji">{{ genderEmoji(profile.gender) }}</span>
                <div class="avatar-upload__overlay">
                  <AppIcon name="camera" :size="14" />
                  <span>更换</span>
                </div>
              </div>
            </el-upload>
            <span class="avatar-upload__hint">支持 JPG / PNG，点击更换</span>
          </div>
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-input v-model="profile.specialty" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="填写你的擅长方向，将展示给患者" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" size="large" :loading="savingProfile" @click="saveProfile">保存资料</el-button>
        </div>
      </el-form>
    </SectionCard>

    <!-- 修改密码 -->
    <SectionCard title="修改密码" class="section-gap">
      <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-position="top" @submit.prevent="savePassword">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password size="large" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" size="large" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password size="large" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" size="large" :loading="pwdSaving" @click="savePassword">修改密码</el-button>
        </div>
      </el-form>
    </SectionCard>
  </div>
</template>

<style scoped>
.page-container {
  max-width: 720px;
  margin: 0 auto;
  padding: var(--app-sp-6) var(--app-sp-6) var(--app-sp-12);
}
.section-gap { margin-bottom: var(--app-sp-6); }
.hint { display: inline-flex; align-items: center; gap: 4px; color: var(--app-text-3); font-size: var(--app-fs-caption); }

.head { display: flex; align-items: center; gap: var(--app-sp-4); margin-bottom: var(--app-sp-6); }
.head__avatar {
  width: 72px; height: 72px; border-radius: 50%; background: var(--app-brand-50);
  display: inline-flex; align-items: center; justify-content: center; font-size: 34px; flex-shrink: 0; overflow: hidden;
}
.head__img { width: 100%; height: 100%; object-fit: cover; }
.head__name-row { display: flex; align-items: center; gap: var(--app-sp-2); margin-bottom: 4px; }
.head__name { font-size: var(--app-fs-h2); font-weight: 600; color: var(--app-text-1); margin: 0; }
.head__meta { font-size: var(--app-fs-caption); color: var(--app-text-3); display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.head__sep { color: var(--app-text-4); }

.form-actions { display: flex; justify-content: flex-end; margin-top: var(--app-sp-2); }

.avatar-upload { display: flex; flex-direction: column; align-items: center; gap: var(--app-sp-2); }
.avatar-upload__preview {
  width: 88px; height: 88px; border-radius: 50%; position: relative; cursor: pointer;
  background: var(--app-brand-50); display: flex; align-items: center; justify-content: center; overflow: hidden;
}
.avatar-upload__img { width: 100%; height: 100%; object-fit: cover; }
.avatar-upload__emoji { font-size: 40px; }
.avatar-upload__overlay {
  position: absolute; inset: 0; background: rgba(0,0,0,.35); color: #fff;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 2px; font-size: var(--app-fs-caption); opacity: 0; transition: opacity var(--app-transition-fast);
}
.avatar-upload__preview:hover .avatar-upload__overlay { opacity: 1; }
.avatar-upload__hint { color: var(--app-text-3); font-size: var(--app-fs-caption); }
</style>
