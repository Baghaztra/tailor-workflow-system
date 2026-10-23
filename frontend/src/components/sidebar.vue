<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { currentAdmin, logout } from '@/services/auth'

const route = useRoute()
const router = useRouter()

const menu = [
  { name: 'Dashboard', path: '/' },
  { name: 'Workers', path: '/workers' },
  { name: 'Orders', path: '/orders' },
  { name: 'Finance', path: '/finance' },
]

const isActive = (path: string) =>
  path === '/' ? route.path === '/' : route.path.startsWith(path)

async function signOut() {
  try {
    await logout()
  } finally {
    await router.push({ name: 'login' })
  }
}
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

    <div class="sidebar-footer">
      <div class="sidebar-user">{{ currentAdmin?.username }}</div>
      <button class="btn" @click="signOut">Sign out</button>
    </div>
  </aside>
</template>
