<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { patientLogin, doctorLogin, adminLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { ROLE } from '@/utils/constants'

const router = useRouter()
const route = useRoute()
const user = useUserStore()

const role = ref(ROLE.PATIENT)
const formRef = ref()
const loading = ref(false)

const form = reactive({
  idCard: '',
  docId: '',
  username: '',
  password: ''
})

// 角色切换时，输入框 label / placeholder / prop 跟着变
const fieldConfig = computed(() => {
  if (role.value === ROLE.PATIENT) {
    return { key: 'idCard', label: '身份证号', placeholder: '请输入 18 位身份证号' }
  }
  if (role.value === ROLE.DOCTOR) {
    return { key: 'docId', label: '医生工号', placeholder: '如 DOC001' }
  }
  return { key: 'username', label: '管理员账号', placeholder: '请输入管理员账号' }
})

const rules = computed(() => ({
  [fieldConfig.value.key]: [
    { required: true, message: `请输入${fieldConfig.value.label}`, trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}))

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    let data, profile
    if (role.value === ROLE.PATIENT) {
      data = await patientLogin({ idCard: form.idCard, password: form.password })
      profile = { patientId: data.patientId || data.userId, realName: data.realName || data.name }
    } else if (role.value === ROLE.DOCTOR) {
      data = await doctorLogin({ docId: form.docId, password: form.password })
      profile = { docId: data.docId || data.userId, docName: data.docName || data.name }
    } else {
      data = await adminLogin({ username: form.username, password: form.password })
      profile = { username: data.username || data.userId || form.username, realName: data.realName || data.name }
    }
    if (!data?.token) throw new Error('登录响应缺少 token')
    user.login({ role: role.value, token: data.token, profile })
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    router.replace(redirect || (role.value === ROLE.PATIENT ? '/patient' : '/' + role.value))
  } catch (e) {
    // 业务错误（非 HTTP 错误，如 token 缺失）这里处理；HTTP 错误已由 request.js 拦截器弹
    if (!e.response && e.message) ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <aside class="login-page__brand">
      <div class="login-page__brand-inner">
        <div class="login-page__logo">
          <span class="login-page__logo-mark">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
              <path d="M12 5v14M5 12h14" />
            </svg>
          </span>
          <span>仁心医院</span>
        </div>

        <h1 class="login-page__title">在线预约挂号系统</h1>
        <p class="login-page__lede">挂号 · 查询 · 改约一站式服务</p>

        <ul class="login-page__features">
          <li>
            <span class="login-page__dot" />
            三步快速挂号，告别现场排队
          </li>
          <li>
            <span class="login-page__dot" />
            实时号源查询，提前规划就诊
          </li>
          <li>
            <span class="login-page__dot" />
            一键取消改约，灵活安排时间
          </li>
        </ul>

        <!-- 简洁装饰：抽象医院建筑线稿，仅作背景装饰 -->
        <svg class="login-page__art" viewBox="0 0 240 160" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M40 150V70l60-25l60 25v80z" />
          <path d="M40 150h120" />
          <path d="M95 150v-30h30v30" />
          <rect x="55" y="85" width="14" height="14" />
          <rect x="80" y="85" width="14" height="14" />
          <rect x="105" y="85" width="14" height="14" />
          <rect x="130" y="85" width="14" height="14" />
          <path d="M100 55v-8M96 51h8" stroke-width="2.5" />
          <path d="M170 150v-50l25-12l25 12v50z" />
          <rect x="178" y="115" width="10" height="12" />
          <rect x="202" y="115" width="10" height="12" />
        </svg>
      </div>
    </aside>

    <!-- 右侧登录表单区 -->
    <main class="login-page__form">
      <div class="login-card">
        <header class="login-card__head">
          <h2 class="login-card__title">欢迎回来</h2>
          <p class="login-card__subtitle">请选择身份并登录</p>
        </header>

        <el-radio-group v-model="role" class="login-card__roles" size="large">
          <el-radio-button :value="ROLE.PATIENT">患者</el-radio-button>
          <el-radio-button :value="ROLE.DOCTOR">医生</el-radio-button>
          <el-radio-button :value="ROLE.ADMIN">管理员</el-radio-button>
        </el-radio-group>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          @submit.prevent="handleSubmit"
        >
          <el-form-item :label="fieldConfig.label" :prop="fieldConfig.key">
            <el-input
              v-model="form[fieldConfig.key]"
              :placeholder="fieldConfig.placeholder"
              clearable
              size="large"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              show-password
              placeholder="请输入密码"
              size="large"
            />
          </el-form-item>

          <el-button
            type="primary"
            :loading="loading"
            size="large"
            class="login-card__submit"
            @click="handleSubmit"
          >登 录</el-button>
        </el-form>

        <!-- 固定底部区域，切换角色不引起布局位移 -->
        <div class="login-card__footer">
          <div v-if="role === ROLE.PATIENT">
            还没有账号？
            <router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: var(--app-bg-elevated);
}

/* ===== 左侧品牌区 ===== */
.login-page__brand {
  flex: 1.2;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #F0F7FF 0%, #DBEAFE 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--app-sp-12);
}
/* 浮动光晕装饰（克制使用，半透明） */
.login-page__brand::before,
.login-page__brand::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: var(--app-brand-200);
  opacity: 0.35;
  pointer-events: none;
}
.login-page__brand::before {
  width: 320px;
  height: 320px;
  top: -100px;
  right: -100px;
}
.login-page__brand::after {
  width: 240px;
  height: 240px;
  bottom: -80px;
  left: -60px;
}

.login-page__brand-inner {
  position: relative;
  z-index: 1;
  max-width: 460px;
  width: 100%;
}

.login-page__logo {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  color: var(--app-text-1);
  font-weight: 600;
  font-size: var(--app-fs-h2);
  margin-bottom: var(--app-sp-10);
}
.login-page__logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--app-radius-md);
  background: var(--app-brand-500);
  color: #fff;
  flex-shrink: 0;
}

.login-page__title {
  font-size: 34px;
  font-weight: 600;
  color: var(--app-text-1);
  line-height: 1.25;
  margin-bottom: var(--app-sp-3);
}
.login-page__lede {
  font-size: var(--app-fs-h3);
  color: var(--app-text-2);
  margin-bottom: var(--app-sp-10);
}

.login-page__features {
  list-style: none;
  padding: 0;
  margin: 0;
}
.login-page__features li {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  padding: 10px 0;
  color: var(--app-text-2);
  font-size: var(--app-fs-body);
}
.login-page__dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--app-brand-500);
  flex-shrink: 0;
}

/* 底部装饰插画 */
.login-page__art {
  position: absolute;
  right: 5%;
  bottom: 6%;
  width: 280px;
  height: auto;
  color: var(--app-brand-400);
  opacity: 0.35;
  pointer-events: none;
}

/* ===== 右侧表单区 ===== */
.login-page__form {
  flex: 1;
  min-width: 440px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--app-sp-10);
}
.login-card {
  width: 100%;
  max-width: 360px;
}
.login-card__head {
  margin-bottom: var(--app-sp-8);
}
.login-card__title {
  font-size: var(--app-fs-h1);
  color: var(--app-text-1);
  margin-bottom: var(--app-sp-2);
  font-weight: 600;
}
.login-card__subtitle {
  color: var(--app-text-3);
  font-size: var(--app-fs-body);
}

.login-card__roles {
  display: flex;
  width: 100%;
  margin-bottom: var(--app-sp-6);
}
.login-card__roles :deep(.el-radio-button) {
  flex: 1;
}
.login-card__roles :deep(.el-radio-button__inner) {
  width: 100%;
  height: 48px;
  line-height: 48px;
  font-size: var(--app-fs-h3);
  font-weight: 500;
  padding: 0;
  border-radius: 0;
}
.login-card__roles :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-top-left-radius: var(--app-radius-md);
  border-bottom-left-radius: var(--app-radius-md);
}
.login-card__roles :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-top-right-radius: var(--app-radius-md);
  border-bottom-right-radius: var(--app-radius-md);
}

.login-card__submit {
  width: 100%;
  margin-top: var(--app-sp-3);
  letter-spacing: 4px;
}

.login-card__footer {
  margin-top: var(--app-sp-5);
  text-align: center;
  font-size: var(--app-fs-caption);
  color: var(--app-text-3);
  min-height: 20px;   /* 即使 v-if 不渲染也占位，避免 layout shift */
}
.login-card__footer a {
  color: var(--app-brand-600);
  font-weight: 500;
}

/* ===== 窄屏：隐藏左侧品牌区 ===== */
@media (max-width: 960px) {
  .login-page__brand { display: none; }
  .login-page__form { flex: 1; min-width: 0; padding: var(--app-sp-6); }
}
</style>
