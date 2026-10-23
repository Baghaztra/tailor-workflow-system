<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { errorMessage } from '@/services/api'
import { getFinanceSummary } from '@/services/finance'
import { listOrders } from '@/services/orders'
import { listWorkers } from '@/services/workers'
import { formatDate, formatPrice } from '@/utils/format'
import type { FinanceSummary, Order, Worker } from '@/types'

const orders = ref<Order[]>([])
const workers = ref<Worker[]>([])
const finance = ref<FinanceSummary | null>(null)
const loading = ref(true)
const error = ref('')

const activeOrders = computed(() =>
  orders.value.filter(
    (order) =>
      order.status === 'CREATED' ||
      order.status === 'IN_PROGRESS' ||
      order.status === 'READY_FOR_PICKUP',
  ),
)
const pendingPickups = computed(() =>
  orders.value.filter((order) => order.status === 'READY_FOR_PICKUP'),
)
const activeWorkers = computed(() => workers.value.filter((worker) => worker.active))
const recentOrders = computed(() => [...orders.value].sort((a, b) => b.id - a.id).slice(0, 5))

onMounted(async () => {
  try {
    const [orderList, workerList, financeData] = await Promise.all([
      listOrders(),
      listWorkers(),
      getFinanceSummary(),
    ])
    orders.value = orderList
    workers.value = workerList
    finance.value = financeData
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-header">
    <div>
      <h1>Dashboard</h1>
      <p class="subtitle">Overview of orders, workers, and revenue</p>
    </div>
  </div>

  <div v-if="error" class="alert alert-error">{{ error }}</div>
  <p v-if="loading" class="empty">Loading...</p>

  <template v-else>
    <div class="stat-grid">
      <div class="stat">
        <div class="stat-label">Active orders</div>
        <div class="stat-value">{{ activeOrders.length }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Pending pickups</div>
        <div class="stat-value">{{ pendingPickups.length }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Active workers</div>
        <div class="stat-value">{{ activeWorkers.length }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Total orders</div>
        <div class="stat-value">{{ orders.length }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Revenue this month</div>
        <div class="stat-value">{{ formatPrice(finance?.monthRevenue ?? 0) }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Total revenue</div>
        <div class="stat-value">{{ formatPrice(finance?.totalRevenue ?? 0) }}</div>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <h2>Recent orders</h2>
        <RouterLink to="/orders">View all</RouterLink>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Customer</th>
            <th>Status</th>
            <th>Stage</th>
            <th>Total</th>
            <th>Updated</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in recentOrders" :key="order.id">
            <td><RouterLink :to="`/orders/${order.id}`">#{{ order.id }}</RouterLink></td>
            <td>{{ order.customerName }}</td>
            <td>
              <span class="badge" :class="`badge-${order.status.toLowerCase()}`">
                {{ order.status }}
              </span>
            </td>
            <td>{{ order.stage ?? '—' }}</td>
            <td>{{ formatPrice(order.totalPrice) }}</td>
            <td>{{ formatDate(order.updatedAt) }}</td>
          </tr>
          <tr v-if="!recentOrders.length">
            <td colspan="6" class="empty">No orders yet.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
</template>
