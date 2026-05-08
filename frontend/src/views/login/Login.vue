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

const rules = computed(() => ({
  idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  docId: [{ required: true, message: '请输入医生工号', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}))

async function handleSubmit() {
  await formRef.value.validate()
  loading.value = true
  try {
    let data, profile
    if (role.value === ROLE.PATIENT) {
      data = await patientLogin({ idCard: form.idCard, password: form.password })
      profile = { patientId: data.patientId, realName: data.realName }
    } else if (role.value === ROLE.DOCTOR) {
      data = await doctorLogin({ docId: form.docId, password: form.password })
      profile = { docId: data.docId, docName: data.docName }
    } else {
      data = await adminLogin({ username: form.username, password: form.password })
      profile = { username: form.username }
    }
    user.login({ role: role.value, token: data.token, profile })
    ElMessage.success('登录成功')
    const redirect = route.query.redirect
    router.replace(redirect || (role.value === ROLE.PATIENT ? '/patient' : '/' + role.value))
  } catch (e) {
    // 拦截器已弹错误
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="title">医院预约挂号系统</h2>
      <p class="subtitle">请选择角色登录</p>

      <el-radio-group v-model="role" class="role-tabs" size="large">
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
        <el-form-item v-if="role === ROLE.PATIENT" label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" clearable />
        </el-form-item>
        <el-form-item v-else-if="role === ROLE.DOCTOR" label="医生工号" prop="docId">
          <el-input v-model="form.docId" placeholder="如 DOC001" clearable />
        </el-form-item>
        <el-form-item v-else label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="管理员账号" clearable />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <el-button
          type="primary"
          :loading="loading"
          class="submit"
          @click="handleSubmit"
        >登 录</el-button>
      </el-form>

      <div v-if="role === ROLE.PATIENT" class="footer">
        还没有账号？
        <router-link to="/register">前往注册</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
}
.login-card {
  width: 380px;
  padding: 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
}
.title { margin: 0 0 4px; text-align: center; color: #303133; }
.subtitle { margin: 0 0 20px; text-align: center; color: #909399; font-size: 13px; }
.role-tabs { display: flex; justify-content: center; margin-bottom: 20px; }
.submit { width: 100%; }
.footer { margin-top: 16px; text-align: center; font-size: 13px; color: #909399; }
</style>
