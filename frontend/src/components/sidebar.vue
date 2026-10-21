<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const menu = [
  { name: 'Dashboard', path: '/' },
  { name: 'Workers', path: '/workers' },
  { name: 'Orders', path: '/orders' },
]

const isActive = (path: string) =>
  path === '/' ? route.path === '/' : route.path.startsWith(path)
</script>

<template>
  <aside class="sidebar">
    <div class="brand">Tailor Workflow</div>
    <nav>
      <RouterLink
        v-for="item in menu"
        :key="item.path"
        :to="item.path"
        class="nav-link"
        :class="{ active: isActive(item.path) }"
      >
        {{ item.name }}
      </RouterLink>
    </nav>
  </aside>
</template>
