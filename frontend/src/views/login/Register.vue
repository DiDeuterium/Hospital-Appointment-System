<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { patientRegister } from '@/api/auth'
import { GENDER_OPTIONS } from '@/utils/constants'
import { isIdCard, isPhone } from '@/utils/validators'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const agreed = ref(false)

const form = reactive({
  idCard: '',
  realName: '',
  gender: 'M',
  phone: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    {
      validator: (_r, v, cb) => (isIdCard(v) ? cb() : cb(new Error('身份证号格式不正确'))),
      trigger: 'blur'
    }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    {
      validator: (_r, v, cb) => (isPhone(v) ? cb() : cb(new Error('手机号格式不正确'))),
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_r, v, cb) => (v === form.password ? cb() : cb(new Error('两次密码不一致'))),
      trigger: 'blur'
    }
  ]
}

async function handleSubmit() {
  await formRef.value.validate()
  if (!agreed.value) {
    ElMessage.warning('请先阅读并同意服务条款')
    return
  }
  loading.value = true
  try {
    await patientRegister({
      idCard: form.idCard,
      realName: form.realName,
      gender: form.gender,
      phone: form.phone,
      password: form.password
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch { /* 拦截器已弹错误 */ } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <router-link to="/login" class="register-card__back">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
        返回登录
      </router-link>

      <header class="register-card__head">
        <h1 class="register-card__title">患者注册</h1>
        <p class="register-card__subtitle">填写真实信息，方便就诊时核验</p>
      </header>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" maxlength="18" placeholder="18 位身份证号" size="large" />
          <div class="register-card__hint">用于实名认证，信息不会公开</div>
        </el-form-item>

        <div class="register-card__row">
          <el-form-item label="真实姓名" prop="realName" class="register-card__col-grow">
            <el-input v-model="form.realName" placeholder="请输入姓名" size="large" />
          </el-form-item>
          <el-form-item label="性别" prop="gender" class="register-card__col-fix">
            <el-radio-group v-model="form.gender" class="register-card__gender">
              <el-radio-button v-for="g in GENDER_OPTIONS" :key="g.value" :value="g.value">{{ g.label }}</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </div>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" size="large" />
        </el-form-item>

        <el-form-item label="设置密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" size="large" />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" size="large" />
        </el-form-item>

        <el-checkbox v-model="agreed" class="register-card__agreement">
          我已阅读并同意 <a href="javascript:void(0)" @click.prevent>《服务条款》</a> 与 <a href="javascript:void(0)" @click.prevent>《隐私协议》</a>
        </el-checkbox>

        <el-button
          type="primary"
          :loading="loading"
          size="large"
          class="register-card__submit"
          @click="handleSubmit"
        >完成注册</el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-bg-page);
  padding: var(--app-sp-8) var(--app-sp-6);
}
.register-card {
  width: 100%;
  max-width: 460px;
  background: var(--app-bg-elevated);
  border-radius: var(--app-radius-xl);
  padding: var(--app-sp-10) var(--app-sp-8);
  box-shadow: var(--app-shadow-sm);
  position: relative;
}

.register-card__back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--app-text-3);
  font-size: var(--app-fs-caption);
  margin-bottom: var(--app-sp-6);
  transition: color var(--app-transition-fast);
}
.register-card__back:hover { color: var(--app-brand-600); }

.register-card__head {
  margin-bottom: var(--app-sp-8);
}
.register-card__title {
  font-size: var(--app-fs-h1);
  color: var(--app-text-1);
  font-weight: 600;
  margin-bottom: var(--app-sp-2);
}
.register-card__subtitle {
  color: var(--app-text-3);
  font-size: var(--app-fs-body);
}

.register-card__hint {
  margin-top: 6px;
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  line-height: 1.4;
}

.register-card__row {
  display: flex;
  gap: var(--app-sp-3);
}
.register-card__col-grow { flex: 1; min-width: 0; }
.register-card__col-fix  { width: 140px; flex-shrink: 0; }

.register-card__gender { width: 100%; display: flex; }
.register-card__gender :deep(.el-radio-button) { flex: 1; }
.register-card__gender :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 0;
  height: 40px;
  line-height: 40px;
}

.register-card__agreement {
  margin-bottom: var(--app-sp-5);
  color: var(--app-text-2);
}
.register-card__agreement :deep(.el-checkbox__label) {
  font-size: var(--app-fs-caption);
  color: var(--app-text-2);
}
.register-card__agreement a {
  color: var(--app-brand-600);
}

.register-card__submit {
  width: 100%;
  letter-spacing: 2px;
}
</style>
