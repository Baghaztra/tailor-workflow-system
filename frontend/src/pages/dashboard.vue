<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { errorMessage } from '@/services/api'
import { getDashboard } from '@/services/dashboard'
import { listOrders } from '@/services/orders'
import { formatDate, formatPrice } from '@/utils/format'
import type { Dashboard, Order } from '@/types'

const stats = ref<Dashboard | null>(null)
const orders = ref<Order[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const [dashboardData, orderPage] = await Promise.all([
      getDashboard(),
      listOrders({ size: 5 }),
    ])
    stats.value = dashboardData
    orders.value = orderPage.content
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
        <div class="stat-value">{{ stats?.activeOrders ?? 0 }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Pending pickups</div>
        <div class="stat-value">{{ stats?.pendingPickups ?? 0 }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Active workers</div>
        <div class="stat-value">{{ stats?.activeWorkers ?? 0 }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Total orders</div>
        <div class="stat-value">{{ stats?.totalOrders ?? 0 }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Revenue this month</div>
        <div class="stat-value">{{ formatPrice(stats?.monthRevenue ?? 0) }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Total revenue</div>
        <div class="stat-value">{{ formatPrice(stats?.totalRevenue ?? 0) }}</div>
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
          <tr v-for="order in orders" :key="order.id">
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
          <tr v-if="!orders.length">
            <td colspan="6" class="empty">No orders yet.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
</template>
