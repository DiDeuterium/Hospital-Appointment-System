<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyProfile, updateMyProfile, changeMyPassword } from '@/api/patient'
import { GENDER_OPTIONS, GENDER_LABEL } from '@/utils/constants'
import { isPhone } from '@/utils/validators'
import PageHeader from '@/components/PageHeader.vue'
import SectionCard from '@/components/SectionCard.vue'
import AppIcon from '@/components/AppIcon.vue'

const user = useUserStore()

// ---- 资料卡片 ----
const profileRef = ref()
const profileLoading = ref(false)
const profileSaving = ref(false)
const profile = reactive({
  patientId: null,
  idCard: '',
  realName: '',
  gender: 'M',
  phone: ''
})

const profileRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender:   [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (_r, v, cb) => (isPhone(v) ? cb() : cb(new Error('手机号格式不正确'))),
      trigger: 'blur'
    }
  ]
}

function maskIdCard(id) {
  if (!id || id.length < 8) return id || ''
  return id.slice(0, 4) + '********' + id.slice(-4)
}

async function loadProfile() {
  profileLoading.value = true
  try {
    const data = await getMyProfile()
    Object.assign(profile, data)
  } catch { /* 拦截器已弹错误 */ } finally {
    profileLoading.value = false
  }
}

async function saveProfile() {
  await profileRef.value.validate()
  profileSaving.value = true
  try {
    const data = await updateMyProfile({
      realName: profile.realName,
      gender: profile.gender,
      phone: profile.phone
    })
    Object.assign(profile, data)
    // 同步 navbar 显示名 + localStorage
    user.updateProfile({ realName: data.realName })
    ElMessage.success('资料已保存')
  } catch { /* 拦截器已弹错误 */ } finally {
    profileSaving.value = false
  }
}

// ---- 密码卡片 ----
const pwdRef = ref()
const pwdSaving = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少 6 位', trigger: 'blur' },
    {
      validator: (_r, v, cb) =>
        v && v === pwdForm.oldPassword
          ? cb(new Error('新密码不能与原密码相同'))
          : cb(),
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
    await changeMyPassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
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
      title="个人中心"
      subtitle="管理你的基本资料与登录密码"
      :breadcrumbs="[{ label: '首页', to: '/patient/home' }, { label: '个人中心' }]"
    />

    <!-- 基本资料 -->
    <SectionCard title="基本资料" class="section-gap">
      <template #extra>
        <span class="hint">
          <AppIcon name="info" :size="14" />
          身份证号为实名核验依据，如需修改请前往窗口
        </span>
      </template>

      <el-form
        ref="profileRef"
        v-loading="profileLoading"
        :model="profile"
        :rules="profileRules"
        label-position="top"
        @submit.prevent="saveProfile"
      >
        <!-- 只读字段：身份证号（脱敏显示） -->
        <el-form-item label="身份证号">
          <el-input :model-value="maskIdCard(profile.idCard)" disabled size="large" />
        </el-form-item>

        <div class="form-row">
          <el-form-item label="真实姓名" prop="realName" class="col-grow">
            <el-input v-model="profile.realName" maxlength="20" size="large" />
          </el-form-item>
          <el-form-item label="性别" prop="gender" class="col-fix">
            <el-radio-group v-model="profile.gender" class="gender">
              <el-radio-button v-for="g in GENDER_OPTIONS" :key="g.value" :value="g.value">
                {{ g.label }}
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profile.phone" maxlength="11" size="large" />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" size="large" :loading="profileSaving" @click="saveProfile">
            保存资料
          </el-button>
        </div>
      </el-form>
    </SectionCard>

    <!-- 修改密码 -->
    <SectionCard title="修改密码" class="section-gap">
      <template #extra>
        <span class="hint">
          <AppIcon name="info" :size="14" />
          修改成功后当前登录态保留，下次登录请使用新密码
        </span>
      </template>

      <el-form
        ref="pwdRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-position="top"
        @submit.prevent="savePassword"
      >
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
          <el-button type="primary" size="large" :loading="pwdSaving" @click="savePassword">
            修改密码
          </el-button>
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

.hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--app-text-3);
  font-size: var(--app-fs-caption);
}

.form-row {
  display: flex;
  gap: var(--app-sp-3);
}
.col-grow { flex: 1; min-width: 0; }
.col-fix  { width: 140px; flex-shrink: 0; }

.gender { width: 100%; display: flex; }
.gender :deep(.el-radio-button) { flex: 1; }
.gender :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 0;
  height: 40px;
  line-height: 40px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--app-sp-2);
}

@media (max-width: 640px) {
  .form-row { flex-direction: column; gap: 0; }
  .col-fix { width: 100%; }
}
</style>
