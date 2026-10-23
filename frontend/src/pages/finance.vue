<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { errorMessage } from '@/services/api'
import { getFinanceSummary, listPayments } from '@/services/finance'
import { formatDate, formatPrice } from '@/utils/format'
import type { FinanceSummary, Payment } from '@/types'

const summary = ref<FinanceSummary | null>(null)
const payments = ref<Payment[]>([])
const loading = ref(true)
const error = ref('')

const from = ref('')
const to = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [summaryData, paymentData] = await Promise.all([
      getFinanceSummary(),
      listPayments({ from: from.value || undefined, to: to.value || undefined }),
    ])
    summary.value = summaryData
    payments.value = paymentData
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

watch([from, to], load)
onMounted(load)
</script>

<template>
  <div class="page-header">
    <div>
      <h1>Finance</h1>
      <p class="subtitle">Payment records and revenue</p>
    </div>
  </div>

  <div v-if="error" class="alert alert-error">{{ error }}</div>
  <p v-if="loading" class="empty">Loading...</p>

  <template v-else>
    <div class="stat-grid">
      <div class="stat">
        <div class="stat-label">Today</div>
        <div class="stat-value">{{ formatPrice(summary?.todayRevenue ?? 0) }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">This month</div>
        <div class="stat-value">{{ formatPrice(summary?.monthRevenue ?? 0) }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Total revenue</div>
        <div class="stat-value">{{ formatPrice(summary?.totalRevenue ?? 0) }}</div>
      </div>
      <div class="stat">
        <div class="stat-label">Payments</div>
        <div class="stat-value">{{ summary?.paymentCount ?? 0 }}</div>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <h2>Payments</h2>
        <div class="filters">
          <div class="field">
            <label for="payment-from">From</label>
            <input id="payment-from" v-model="from" type="date" />
          </div>
          <div class="field">
            <label for="payment-to">To</label>
            <input id="payment-to" v-model="to" type="date" />
          </div>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>Order</th>
            <th>Customer</th>
            <th>Amount</th>
            <th>Method</th>
            <th>Paid at</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="payment in payments" :key="payment.id">
            <td>
              <RouterLink :to="`/orders/${payment.orderId}`">#{{ payment.orderId }}</RouterLink>
            </td>
            <td>{{ payment.customerName }}</td>
            <td>{{ formatPrice(payment.amount) }}</td>
            <td>{{ payment.method }}</td>
            <td>{{ formatDate(payment.paidAt) }}</td>
          </tr>
          <tr v-if="!payments.length">
            <td colspan="5" class="empty">No payments found.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
</template>
