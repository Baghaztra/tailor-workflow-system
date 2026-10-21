<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import { errorMessage } from '@/services/api'
import { createWorker, listWorkers, updateWorker } from '@/services/workers'
import { formatDate } from '@/utils/format'
import { SPECIALIZATIONS } from '@/types'
import type { Specialization, Worker } from '@/types'

const workers = ref<Worker[]>([])
const loading = ref(true)
const error = ref('')

const showModal = ref(false)
const editing = ref<Worker | null>(null)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  name: '',
  specialization: 'CUTTING' as Specialization,
  active: true,
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    workers.value = await listWorkers()
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { name: '', specialization: 'CUTTING', active: true })
  formError.value = ''
  showModal.value = true
}

function openEdit(worker: Worker) {
  editing.value = worker
  Object.assign(form, {
    name: worker.name,
    specialization: worker.specialization,
    active: worker.active,
  })
  formError.value = ''
  showModal.value = true
}

async function save() {
  saving.value = true
  formError.value = ''
  const body = {
    name: form.name,
    specialization: form.specialization,
    active: form.active,
  }

  try {
    if (editing.value) {
      await updateWorker(editing.value.id, body)
    } else {
      await createWorker(body)
    }
    showModal.value = false
    await load()
  } catch (e) {
    formError.value = errorMessage(e)
  } finally {
    saving.value = false
  }
}

async function toggleActive(worker: Worker) {
  try {
    await updateWorker(worker.id, {
      name: worker.name,
      specialization: worker.specialization,
      active: !worker.active,
    })
    await load()
  } catch (e) {
    error.value = errorMessage(e)
  }
}

onMounted(load)
</script>

<template>
  <div class="page-header">
    <div>
      <h1>Workers</h1>
      <p class="subtitle">Manage cutting, sewing, and finishing workers</p>
    </div>
    <button class="btn btn-primary" @click="openCreate">New worker</button>
  </div>

  <div v-if="error" class="alert alert-error">{{ error }}</div>

  <div class="card">
    <p v-if="loading" class="empty">Loading...</p>
    <table v-else class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Specialization</th>
          <th>Status</th>
          <th>Created</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="worker in workers" :key="worker.id">
          <td>{{ worker.id }}</td>
          <td>{{ worker.name }}</td>
          <td>{{ worker.specialization }}</td>
          <td>
            <span class="badge" :class="worker.active ? 'badge-completed' : 'badge-cancelled'">
              {{ worker.active ? 'Active' : 'Inactive' }}
            </span>
          </td>
          <td>{{ formatDate(worker.createdAt) }}</td>
          <td class="actions">
            <button class="btn btn-ghost btn-sm" @click="openEdit(worker)">Edit</button>
            <button class="btn btn-ghost btn-sm" @click="toggleActive(worker)">
              {{ worker.active ? 'Deactivate' : 'Activate' }}
            </button>
          </td>
        </tr>
        <tr v-if="!workers.length">
          <td colspan="6" class="empty">No workers yet.</td>
        </tr>
      </tbody>
    </table>
  </div>

  <BaseModal
    v-if="showModal"
    :title="editing ? 'Edit worker' : 'New worker'"
    @close="showModal = false"
  >
    <form @submit.prevent="save">
      <div v-if="formError" class="alert alert-error">{{ formError }}</div>

      <div class="field">
        <label for="worker-name">Name</label>
        <input id="worker-name" v-model="form.name" required maxlength="100" />
      </div>

      <div class="field">
        <label for="worker-specialization">Specialization</label>
        <select id="worker-specialization" v-model="form.specialization">
          <option v-for="item in SPECIALIZATIONS" :key="item" :value="item">{{ item }}</option>
        </select>
      </div>

      <div class="field">
        <label>
          <input v-model="form.active" type="checkbox" />
          Active
        </label>
      </div>

      <div class="form-actions">
        <button type="button" class="btn btn-ghost" @click="showModal = false">Cancel</button>
        <button type="submit" class="btn btn-primary" :disabled="saving">
          {{ saving ? 'Saving...' : 'Save' }}
        </button>
      </div>
    </form>
  </BaseModal>
</template>
