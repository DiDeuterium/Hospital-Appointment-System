<script setup>
import AppSidebar from './components/AppSidebar.vue'
import AppHeader from './components/AppHeader.vue'
</script>

<template>
  <div class="default-layout">
    <AppSidebar />
    <div class="default-layout__body">
      <AppHeader />
      <main class="default-layout__main">
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
.default-layout {
  height: 100vh;
  display: flex;
  background: var(--app-bg-page);
}
.default-layout__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.default-layout__main {
  flex: 1;
  overflow: auto;
}
</style>
