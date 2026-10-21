<template>
  <div class="app-shell">
    <Sidebar />
    <main class="content">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/sidebar.vue'
</script>
