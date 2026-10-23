<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { errorMessage } from '@/services/api'
import { login } from '@/services/auth'

const route = useRoute()
const router = useRouter()

const form = reactive({
  username: '',
  password: '',
})
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await login(form.username, form.password)
    const target = route.query.redirect
    const redirect =
      typeof target === 'string' && target.startsWith('/') && !target.startsWith('//') ? target : '/'
    await router.replace(redirect)
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="card login-card">
      <h1>Tailor Workflow</h1>
      <p class="subtitle">Admin sign in</p>

      <div v-if="error" class="alert alert-error">{{ error }}</div>

      <form @submit.prevent="submit">
        <div class="field">
          <label for="login-username">Username</label>
          <input
            id="login-username"
            v-model="form.username"
            autocomplete="username"
            required
          />
        </div>

        <div class="field">
          <label for="login-password">Password</label>
          <input
            id="login-password"
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            required
          />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? 'Signing in...' : 'Sign in' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
