<script setup>
import PatientNavbar from './components/PatientNavbar.vue'
</script>

<template>
  <div class="patient-layout">
    <PatientNavbar />
    <main class="patient-layout__main">
      <router-view v-slot="{ Component, route }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </main>
  </div>
</template>

<style scoped>
.patient-layout {
  min-height: 100vh;
  background: var(--app-bg-page);
  display: flex;
  flex-direction: column;
}
.patient-layout__main {
  flex: 1;
}
</style>
