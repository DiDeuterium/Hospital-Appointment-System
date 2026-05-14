<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { ROLE_LABEL } from '@/utils/constants'

const router = useRouter()
const user = useUserStore()

async function handleLogout() {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  user.logout()
  router.replace('/login')
}
</script>

<template>
  <header class="app-header">
    <div class="brand">
      <span class="brand-icon">+</span>
      <span class="brand-text">医院预约挂号系统</span>
    </div>
    <div class="actions">
      <span class="role-tag">{{ ROLE_LABEL[user.role] || '' }}</span>
      <span class="user-name">{{ user.displayName }}</span>
      <el-button link type="primary" @click="handleLogout">退出</el-button>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  height: var(--app-header-height);
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.brand { display: flex; align-items: center; gap: 8px; font-weight: 600; color: #303133; }
.brand-icon {
  width: 26px; height: 26px; border-radius: 6px;
  background: var(--el-color-primary); color: #fff;
  display: inline-flex; align-items: center; justify-content: center;
  font-weight: 700;
}
.actions { display: flex; align-items: center; gap: 12px; color: #606266; }
.role-tag {
  padding: 2px 8px; background: var(--el-color-primary-light-9);
  color: var(--el-color-primary); border-radius: 4px; font-size: 12px;
}
</style>
