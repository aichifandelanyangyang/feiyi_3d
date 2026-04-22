<template>
  <!-- 主应用容器 -->
  <div class="app-container">
    <RouterView />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
</script>

<style scoped>
.app-container {
  width: 100%;
  min-height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
}
</style>
