<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { errorMessage } from '@/services/api'
import { exportPayments, getFinanceReport, getFinanceSummary, listPayments } from '@/services/finance'
import { formatDate, formatPrice } from '@/utils/format'
import type { FinanceReportRow, FinanceSummary, Payment } from '@/types'

const summary = ref<FinanceSummary | null>(null)
const payments = ref<Payment[]>([])
const report = ref<FinanceReportRow[]>([])
const loading = ref(true)
const exporting = ref(false)
const error = ref('')

const from = ref('')
const to = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [summaryData, paymentData, reportData] = await Promise.all([
      getFinanceSummary(),
      listPayments({ from: from.value || undefined, to: to.value || undefined }),
      getFinanceReport({ from: from.value || undefined, to: to.value || undefined }),
    ])
    summary.value = summaryData
    payments.value = paymentData
    report.value = reportData
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

async function download() {
  exporting.value = true
  error.value = ''
  try {
    await exportPayments({ from: from.value || undefined, to: to.value || undefined })
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    exporting.value = false
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
          <button class="btn btn-ghost" :disabled="exporting" @click="download">
            {{ exporting ? 'Exporting...' : 'Export CSV' }}
          </button>
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

    <div class="card">
      <div class="toolbar">
        <h2>Daily report</h2>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Payments</th>
            <th>Revenue</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in report" :key="row.date">
            <td>{{ row.date }}</td>
            <td>{{ row.paymentCount }}</td>
            <td>{{ formatPrice(row.revenue) }}</td>
          </tr>
          <tr v-if="!report.length">
            <td colspan="3" class="empty">No revenue in this period.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
</template>
