<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

// 从注册的路由中提取当前角色的可见菜单
const menus = computed(() => {
  const all = router.getRoutes()
  // 取出当前角色顶级布局路由的孩子
  const parents = all.filter(r =>
    Array.isArray(r.meta?.roles) &&
    r.meta.roles.includes(user.role) &&
    r.children?.length
  )
  // 一个角色只对应一个 layout 父路由
  const parent = parents[0]
  if (!parent) return []
  return parent.children
    .filter(c => !c.meta?.hidden)
    .map(c => ({
      path: `${parent.path}/${c.path}`.replace(/\/+/g, '/'),
      title: c.meta?.title || c.name,
      icon: c.meta?.icon
    }))
})

const activePath = computed(() => route.path)
</script>

<template>
  <aside class="app-sidebar">
    <el-menu
      :default-active="activePath"
      router
      background-color="#001529"
      text-color="#cfd8e3"
      active-text-color="#fff"
    >
      <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
        <el-icon v-if="m.icon"><component :is="m.icon" /></el-icon>
        <span>{{ m.title }}</span>
      </el-menu-item>
    </el-menu>
  </aside>
</template>

<style scoped>
.app-sidebar {
  width: var(--app-sidebar-width);
  background: #001529;
  height: calc(100vh - var(--app-header-height));
  overflow-y: auto;
}
.app-sidebar :deep(.el-menu) {
  border-right: none;
}
</style>
