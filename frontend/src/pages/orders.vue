<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import BaseModal from '@/components/BaseModal.vue'
import { errorMessage } from '@/services/api'
import { cancelOrder as cancelOrderRequest, createOrder, listOrders, updateOrder } from '@/services/orders'
import { formatDate, formatPrice } from '@/utils/format'
import { ORDER_STATUSES } from '@/types'
import type { Order, OrderStatus } from '@/types'

const orders = ref<Order[]>([])
const loading = ref(true)
const error = ref('')
const statusFilter = ref<'' | OrderStatus>('')
const search = ref('')
const page = ref(0)
const size = 10
const total = ref(0)
const totalPages = ref(0)
let searchTimer: ReturnType<typeof setTimeout> | undefined

const showModal = ref(false)
const editing = ref<Order | null>(null)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  customerName: '',
  totalPrice: '',
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await listOrders({
      status: statusFilter.value || undefined,
      search: search.value.trim() || undefined,
      page: page.value,
      size,
    })
    orders.value = result.content
    total.value = result.totalElements
    totalPages.value = result.totalPages
    if (!result.content.length && page.value > 0) {
      page.value--
      await load()
    }
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { customerName: '', totalPrice: '' })
  formError.value = ''
  showModal.value = true
}

function openEdit(order: Order) {
  editing.value = order
  Object.assign(form, {
    customerName: order.customerName,
    totalPrice: order.totalPrice == null ? '' : String(order.totalPrice),
  })
  formError.value = ''
  showModal.value = true
}

async function save() {
  saving.value = true
  formError.value = ''
  const body = {
    customerName: form.customerName,
    totalPrice: form.totalPrice === '' ? null : Number(form.totalPrice),
  }

  try {
    if (editing.value) {
      await updateOrder(editing.value.id, body)
    } else {
      await createOrder(body)
      page.value = 0
    }
    showModal.value = false
    await load()
  } catch (e) {
    formError.value = errorMessage(e)
  } finally {
    saving.value = false
  }
}

async function cancelOrder(order: Order) {
  if (!window.confirm(`Cancel order #${order.id}?`)) {
    return
  }

  try {
    await cancelOrderRequest(order.id)
    await load()
  } catch (e) {
    error.value = errorMessage(e)
  }
}

watch(statusFilter, () => {
  page.value = 0
  load()
})
watch(search, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    page.value = 0
    load()
  }, 300)
})
onMounted(load)
</script>

<template>
  <div class="page-header">
    <div>
      <h1>Orders</h1>
      <p class="subtitle">Customer orders and production progress</p>
    </div>
    <button class="btn btn-primary" @click="openCreate">New order</button>
  </div>

  <div v-if="error" class="alert alert-error">{{ error }}</div>

  <div class="card">
    <div class="toolbar">
      <div class="filters">
        <div class="field">
          <label for="order-search">Search</label>
          <input id="order-search" v-model="search" placeholder="Customer name" />
        </div>
        <div class="field">
          <label for="status-filter">Status</label>
          <select id="status-filter" v-model="statusFilter">
            <option value="">All</option>
            <option v-for="status in ORDER_STATUSES" :key="status" :value="status">{{ status }}</option>
          </select>
        </div>
      </div>
    </div>

    <p v-if="loading" class="empty">Loading...</p>
    <table v-else class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Customer</th>
          <th>Status</th>
          <th>Stage</th>
          <th>Total</th>
          <th>Updated</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="order in orders" :key="order.id">
          <td>#{{ order.id }}</td>
          <td>{{ order.customerName }}</td>
          <td>
            <span class="badge" :class="`badge-${order.status.toLowerCase()}`">
              {{ order.status }}
            </span>
          </td>
          <td>{{ order.stage ?? '—' }}</td>
          <td>{{ formatPrice(order.totalPrice) }}</td>
          <td>{{ formatDate(order.updatedAt) }}</td>
          <td class="actions">
            <RouterLink :to="`/orders/${order.id}`" class="btn btn-ghost btn-sm">View</RouterLink>
            <button class="btn btn-ghost btn-sm" @click="openEdit(order)">Edit</button>
            <button
              v-if="order.status !== 'CANCELLED' && order.status !== 'COMPLETED'"
              class="btn btn-danger btn-sm"
              @click="cancelOrder(order)"
            >
              Cancel
            </button>
          </td>
        </tr>
        <tr v-if="!orders.length">
          <td colspan="7" class="empty">No orders found.</td>
        </tr>
      </tbody>
    </table>

    <div class="pager">
      <span>{{ total }} orders</span>
      <div class="actions">
        <button class="btn btn-ghost btn-sm" :disabled="page === 0" @click="page--; load()">
          Previous
        </button>
        <span>Page {{ page + 1 }} / {{ totalPages || 1 }}</span>
        <button
          class="btn btn-ghost btn-sm"
          :disabled="page >= totalPages - 1"
          @click="page++; load()"
        >
          Next
        </button>
      </div>
    </div>
  </div>

  <BaseModal
    v-if="showModal"
    :title="editing ? `Edit order #${editing.id}` : 'New order'"
    @close="showModal = false"
  >
    <form @submit.prevent="save">
      <div v-if="formError" class="alert alert-error">{{ formError }}</div>

      <div class="field">
        <label for="order-customer">Customer name</label>
        <input id="order-customer" v-model="form.customerName" required maxlength="100" />
      </div>

      <div class="field">
        <label for="order-total">Total price</label>
        <input id="order-total" v-model="form.totalPrice" type="number" min="0" step="0.01" />
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
