<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AppIcon from '@/components/AppIcon.vue'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

// 路由 meta.icon 是字符串，这里映射到 AppIcon 支持的 name
// 后续新增菜单只需在 router/routes/*.js 写 icon 字符串，并补一条映射
const ICON_ALIAS = {
  OfficeBuilding: 'building',
  Calendar: 'calendar',
  User: 'users',
  DataAnalysis: 'dashboard',
  Document: 'file-text',
  // 直接传 AppIcon 原生 name 时也兼容
  home: 'home',
  building: 'building',
  users: 'users',
  calendar: 'calendar',
  dashboard: 'dashboard',
  user: 'user'
}

const menus = computed(() => {
  // 若 role 为空（未注入也未登录），返空让模板显示空状态提示
  if (!user.role) return []
  const all = router.getRoutes()
  const parents = all.filter(r =>
    Array.isArray(r.meta?.roles) &&
    r.meta.roles.includes(user.role) &&
    r.children?.length
  )
  const parent = parents[0]
  if (!parent) return []
  return parent.children
    .filter(c => !c.meta?.hidden)
    .map(c => ({
      path: `${parent.path}/${c.path}`.replace(/\/+/g, '/'),
      title: c.meta?.title || c.name,
      icon: ICON_ALIAS[c.meta?.icon] || c.meta?.icon || ''
    }))
})

const activePath = computed(() => route.path)
function isActive(path) {
  return activePath.value === path || activePath.value.startsWith(path + '/')
}
</script>

<template>
  <aside class="app-sidebar">
    <div class="app-sidebar__brand">
      <span class="app-sidebar__brand-mark">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <path d="M12 5v14M5 12h14" />
        </svg>
      </span>
      <span class="app-sidebar__brand-text">仁心医院</span>
    </div>
    <nav class="app-sidebar__menu">
      <router-link
        v-for="m in menus"
        :key="m.path"
        :to="m.path"
        class="app-sidebar__item"
        :class="{ 'is-active': isActive(m.path) }"
      >
        <AppIcon v-if="m.icon" :name="m.icon" :size="18" class="app-sidebar__icon" />
        <span class="app-sidebar__label">{{ m.title }}</span>
      </router-link>
      <div v-if="!menus.length && user.role" class="app-sidebar__empty">
        暂无菜单（角色：{{ user.role }}）
      </div>
    </nav>
  </aside>
</template>

<style scoped>
.app-sidebar {
  width: var(--app-sidebar-width);
  background: var(--app-bg-elevated);
  border-right: 1px solid var(--app-border-light);
  height: 100vh;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}

.app-sidebar__brand {
  height: var(--app-header-height);
  display: flex;
  align-items: center;
  gap: var(--app-sp-2);
  padding: 0 var(--app-sp-5);
  border-bottom: 1px solid var(--app-border-light);
  font-weight: 600;
  font-size: var(--app-fs-h3);
  color: var(--app-text-1);
  flex-shrink: 0;
}
.app-sidebar__brand-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--app-radius-md);
  background: var(--app-brand-500);
  color: #fff;
  flex-shrink: 0;
}

.app-sidebar__menu {
  flex: 1;
  padding: var(--app-sp-3);
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}
.app-sidebar__item {
  display: flex;
  align-items: center;
  gap: var(--app-sp-3);
  padding: 0 var(--app-sp-3);
  height: 38px;
  border-radius: var(--app-radius-md);
  color: var(--app-text-2);
  font-size: var(--app-fs-body);
  transition: all var(--app-transition-fast);
}
.app-sidebar__item:hover {
  background: var(--app-bg-subtle);
  color: var(--app-text-1);
}
.app-sidebar__item.is-active {
  background: var(--app-brand-50);
  color: var(--app-brand-700);
  font-weight: 500;
}
.app-sidebar__icon { flex-shrink: 0; color: inherit; }
.app-sidebar__label { flex: 1; min-width: 0; }
.app-sidebar__empty {
  padding: var(--app-sp-6) var(--app-sp-3);
  text-align: center;
  color: var(--app-text-4);
  font-size: var(--app-fs-caption);
}
</style>
