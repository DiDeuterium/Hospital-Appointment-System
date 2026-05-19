<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AppIcon from '@/components/AppIcon.vue'

const router = useRouter()
const route = useRoute()
const user = useUserStore()

// 顶部主菜单（C 端体验，菜单项克制，常用功能放主导航，次要功能进下拉）
const menus = [
  { label: '首页', to: '/patient/home' },
  { label: '我的预约', to: '/patient/appointments' }
]

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning', lockScroll: false })
    user.logout()
    router.replace('/login')
  } catch { /* 用户取消 */ }
}

function isActive(to) {
  return route.path.startsWith(to)
}
</script>

<template>
  <header class="navbar">
    <div class="navbar__inner">
      <router-link to="/patient/home" class="navbar__brand">
        <span class="navbar__brand-mark">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
            <path d="M12 5v14M5 12h14" />
          </svg>
        </span>
        <span class="navbar__brand-text">仁心医院</span>
      </router-link>

      <nav class="navbar__menu">
        <router-link
          v-for="m in menus"
          :key="m.to"
          :to="m.to"
          class="navbar__item"
          :class="{ 'is-active': isActive(m.to) }"
        >
          {{ m.label }}
        </router-link>
      </nav>

      <div class="navbar__right">
        <el-dropdown trigger="click">
          <div class="navbar__user">
            <span class="navbar__avatar">{{ (user.displayName || '?').slice(0, 1) }}</span>
            <span class="navbar__name">{{ user.displayName || user.role || '未登录' }}</span>
            <AppIcon name="chevron-down" :size="14" />
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/patient/profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </header>
</template>

<style scoped>
.navbar {
  background: var(--app-bg-elevated);
  border-bottom: 1px solid var(--app-border-light);
  height: var(--app-header-height);
  position: sticky;
  top: 0;
  z-index: 100;
}
.navbar__inner {
  max-width: var(--app-content-max-width);
  margin: 0 auto;
  height: 100%;
  /* 三段对齐：logo 左 / 菜单中 / 用户右
     用 1fr auto 1fr 网格保证左右两栏等宽，中间菜单严格居中 */
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 0 var(--app-sp-6);
  gap: var(--app-sp-4);
}

.navbar__brand {
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
  color: var(--app-text-1);
  font-weight: 600;
  font-size: var(--app-fs-h3);
  /* 占据左栏，内容靠左 */
  justify-self: start;
}
.navbar__brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: var(--app-radius-md);
  background: var(--app-brand-500);
  color: #fff;
  flex-shrink: 0;
}

.navbar__menu {
  display: flex;
  gap: 4px;
  /* 中栏：菜单项整体居中 */
  justify-self: center;
}
.navbar__item {
  padding: 0 var(--app-sp-4);
  height: 36px;
  display: inline-flex;
  align-items: center;
  border-radius: var(--app-radius-md);
  color: var(--app-text-2);
  font-weight: 500;
  font-size: var(--app-fs-body);
  transition: all var(--app-transition-fast);
}
.navbar__item:hover {
  background: var(--app-bg-subtle);
  color: var(--app-text-1);
}
.navbar__item.is-active {
  color: var(--app-brand-700);
  background: var(--app-brand-50);
}

.navbar__user {
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
  padding: 6px var(--app-sp-3);
  border-radius: var(--app-radius-md);
  cursor: pointer;
  transition: background var(--app-transition-fast);
  color: var(--app-text-2);
}
.navbar__user:hover { background: var(--app-bg-subtle); }

/* 右栏：用户区靠右 */
.navbar__right {
  justify-self: end;
}
.navbar__avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--app-brand-100);
  color: var(--app-brand-700);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--app-fs-caption);
}
.navbar__name {
  font-size: var(--app-fs-body);
}
</style>
