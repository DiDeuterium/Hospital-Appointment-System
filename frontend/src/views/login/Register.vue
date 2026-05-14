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
  } catch (e) {
    // 拦截器已弹错误
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div class="card">
      <h2>患者注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" maxlength="18" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio v-for="g in GENDER_OPTIONS" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">注册</el-button>
          <el-button @click="router.push('/login')">返回登录</el-button>
        </el-form-item>
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
  background: #f0f2f5;
}
.card { width: 480px; padding: 32px; background: #fff; border-radius: 8px; box-shadow: 0 8px 24px rgba(0,0,0,.08); }
.card h2 { margin: 0 0 20px; text-align: center; }
</style>
