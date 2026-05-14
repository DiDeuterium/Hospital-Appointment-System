<script setup>
import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'
</script>

<template>
  <div class="layout">
    <AppHeader />
    <div class="layout-body">
      <AppSidebar />
      <main class="layout-main">
        <router-view v-slot="{ Component, route }">
          <transition name="el-fade-in-linear" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout { height: 100vh; display: flex; flex-direction: column; }
.layout-body { flex: 1; display: flex; min-height: 0; }
.layout-main {
  flex: 1;
  overflow: auto;
  background: #f5f7fa;
}
</style>
