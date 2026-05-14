<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { ROLE_LABEL } from '@/utils/constants'
import AppIcon from '@/components/AppIcon.vue'

const router = useRouter()
const user = useUserStore()

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
    user.logout()
    router.replace('/login')
  } catch { /* 取消 */ }
}
</script>

<template>
  <header class="app-header">
    <div class="app-header__title">
      <!-- 标题交给各页面自己的 PageHeader 组件展示，这里只做用户态 -->
    </div>
    <div class="app-header__right">
      <el-dropdown trigger="click">
        <div class="app-header__user">
          <span class="app-header__avatar">{{ (user.displayName || '?').slice(0, 1) }}</span>
          <div class="app-header__user-meta">
            <div class="app-header__name">{{ user.displayName || '未登录' }}</div>
            <div class="app-header__role">{{ ROLE_LABEL[user.role] || '' }}</div>
          </div>
          <AppIcon name="chevron-down" :size="14" />
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  height: var(--app-header-height);
  background: var(--app-bg-elevated);
  border-bottom: 1px solid var(--app-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--app-sp-6);
  flex-shrink: 0;
}

.app-header__user {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  padding: 6px var(--app-sp-3);
  border-radius: var(--app-radius-md);
  cursor: pointer;
  transition: background var(--app-transition-fast);
  color: var(--app-text-2);
}
.app-header__user:hover { background: var(--app-bg-subtle); }

.app-header__avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--app-brand-100);
  color: var(--app-brand-700);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--app-fs-caption);
}
.app-header__user-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1;
}
.app-header__name {
  font-size: var(--app-fs-body);
  color: var(--app-text-1);
  font-weight: 500;
}
.app-header__role {
  font-size: var(--app-fs-tiny);
  color: var(--app-text-3);
}
</style>
