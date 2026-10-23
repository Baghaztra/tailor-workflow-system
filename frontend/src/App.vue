<script setup lang="ts">
import MainLayout from '@/layouts/main.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <MainLayout v-if="route.name !== 'login'">
    <router-view />
  </MainLayout>
  <router-view v-else />
</template>
