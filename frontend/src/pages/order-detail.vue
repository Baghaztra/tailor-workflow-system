<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import BaseModal from '@/components/BaseModal.vue'
import { errorMessage } from '@/services/api'
import {
  assignOrder,
  cancelOrder,
  completeOrder,
  getOrder,
  listOrderLogs,
  payOrder,
  pickupOrder,
  startOrder,
} from '@/services/orders'
import { listWorkers } from '@/services/workers'
import { formatDate, formatPrice } from '@/utils/format'
import { PAYMENT_METHODS } from '@/types'
import type { Order, OrderLog, PaymentMethod, Worker } from '@/types'

const route = useRoute()
const orderId = Number(route.params.id)

const order = ref<Order | null>(null)
const logs = ref<OrderLog[]>([])
const workers = ref<Worker[]>([])
const loading = ref(true)
const error = ref('')
const message = ref('')
const busy = ref(false)

const showAssign = ref(false)
const selectedWorkerId = ref<'' | number>('')

const showPay = ref(false)
const payForm = reactive({
  amount: '',
  method: 'CASH' as PaymentMethod,
})

const activeWorkers = computed(() => workers.value.filter((worker) => worker.active))

const canAssign = computed(() => {
  const current = order.value
  return (
    current !== null &&
    current.stage === null &&
    (current.status === 'CREATED' || current.status === 'IN_PROGRESS')
  )
})
const canPickup = computed(() => order.value?.status === 'READY_FOR_PICKUP')
const canPay = computed(() => order.value?.status === 'PICKED_UP')
const canCancel = computed(() => {
  const current = order.value
  return current !== null && current.status !== 'COMPLETED' && current.status !== 'CANCELLED'
})

const stageStarted = computed(() => {
  const stage = order.value?.stage
  if (!stage) {
    return false
  }
  for (let index = logs.value.length - 1; index >= 0; index--) {
    const log = logs.value[index]
    if (!log || log.stage !== stage) {
      continue
    }
    if (log.action === 'STARTED') {
      return true
    }
    if (log.action === 'ASSIGNED' || log.action === 'COMPLETED') {
      return false
    }
  }
  return false
})

const canStart = computed(() => {
  const current = order.value
  return current?.status === 'IN_PROGRESS' && current.stage !== null && !stageStarted.value
})
const canComplete = computed(() => {
  const current = order.value
  return current?.status === 'IN_PROGRESS' && current.stage !== null && stageStarted.value
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [orderData, logData, workerData] = await Promise.all([
      getOrder(orderId),
      listOrderLogs(orderId),
      listWorkers(),
    ])
    order.value = orderData
    logs.value = logData
    workers.value = workerData
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

async function run(request: () => Promise<Order>, success: string) {
  busy.value = true
  error.value = ''
  message.value = ''
  try {
    order.value = await request()
    logs.value = await listOrderLogs(orderId)
    message.value = success
    return true
  } catch (e) {
    error.value = errorMessage(e)
    return false
  } finally {
    busy.value = false
  }
}

async function assign() {
  if (selectedWorkerId.value === '') {
    return
  }
  const assigned = await run(
    () => assignOrder(orderId, Number(selectedWorkerId.value)),
    'Worker assigned.',
  )
  if (assigned) {
    showAssign.value = false
    selectedWorkerId.value = ''
  }
}

const start = () => run(() => startOrder(orderId), 'Work started.')
const complete = () => run(() => completeOrder(orderId), 'Stage completed.')
const pickup = () => run(() => pickupOrder(orderId), 'Order picked up.')

function openPay() {
  payForm.amount = order.value?.totalPrice == null ? '' : String(order.value.totalPrice)
  payForm.method = 'CASH'
  showPay.value = true
}

async function recordPayment() {
  if (payForm.amount === '') {
    return
  }
  const paid = await run(
    () => payOrder(orderId, { amount: Number(payForm.amount), method: payForm.method }),
    'Payment recorded.',
  )
  if (paid) {
    showPay.value = false
  }
}

function cancel() {
  if (window.confirm(`Cancel order #${orderId}?`)) {
    run(() => cancelOrder(orderId), 'Order cancelled.')
  }
}

onMounted(load)
</script>

<template>
  <div class="page-header">
    <div>
      <RouterLink to="/orders">← Back to orders</RouterLink>
      <h1>Order #{{ orderId }}</h1>
    </div>
  </div>

  <div v-if="error" class="alert alert-error">{{ error }}</div>
  <div v-if="message" class="alert alert-success">{{ message }}</div>
  <p v-if="loading" class="empty">Loading...</p>

  <template v-else-if="order">
    <div class="grid-2">
      <div class="card">
        <h2>Details</h2>
        <dl class="detail-list">
          <dt>Customer</dt>
          <dd>{{ order.customerName }}</dd>
          <dt>Status</dt>
          <dd>
            <span class="badge" :class="`badge-${order.status.toLowerCase()}`">
              {{ order.status }}
            </span>
          </dd>
          <dt>Stage</dt>
          <dd>{{ order.stage ?? '—' }}</dd>
          <dt>Total</dt>
          <dd>{{ formatPrice(order.totalPrice) }}</dd>
          <dt>Created</dt>
          <dd>{{ formatDate(order.createdAt) }}</dd>
          <dt>Updated</dt>
          <dd>{{ formatDate(order.updatedAt) }}</dd>
        </dl>
      </div>

      <div class="card">
        <h2>Workflow</h2>

        <div class="workflow-actions">
          <button v-if="canAssign" class="btn btn-primary" :disabled="busy" @click="showAssign = true">
            Assign worker
          </button>
          <button v-if="canStart" class="btn btn-primary" :disabled="busy" @click="start">Start</button>
          <button v-if="canComplete" class="btn btn-primary" :disabled="busy" @click="complete">
            Complete
          </button>
          <button v-if="canPickup" class="btn btn-primary" :disabled="busy" @click="pickup">
            Mark picked up
          </button>
          <button v-if="canPay" class="btn btn-primary" :disabled="busy" @click="openPay">
            Record payment
          </button>
          <button v-if="canCancel" class="btn btn-danger" :disabled="busy" @click="cancel">
            Cancel order
          </button>
        </div>

        <p v-if="!canAssign && !canStart && !canComplete && !canPickup && !canPay && !canCancel" class="empty">
          No actions available.
        </p>
      </div>
    </div>

    <div class="card">
      <h2>Timeline</h2>
      <ul v-if="logs.length" class="timeline">
        <li v-for="log in logs" :key="log.id" class="timeline-item">
          <strong>{{ log.action }}</strong>
          <span v-if="log.stage"> · {{ log.stage }}</span>
          <div class="timeline-meta">
            {{ formatDate(log.createdAt) }}
            <span v-if="log.workerId"> · worker #{{ log.workerId }}</span>
          </div>
          <div v-if="log.note">{{ log.note }}</div>
        </li>
      </ul>
      <p v-else class="empty">No logs yet.</p>
    </div>

    <BaseModal v-if="showAssign" title="Assign worker" @close="showAssign = false">
      <form @submit.prevent="assign">
        <div class="field">
          <label for="assign-worker">Worker</label>
          <select id="assign-worker" v-model="selectedWorkerId" required>
            <option value="" disabled>Select a worker</option>
            <option v-for="worker in activeWorkers" :key="worker.id" :value="worker.id">
              {{ worker.name }} ({{ worker.specialization }})
            </option>
          </select>
        </div>

        <p v-if="!activeWorkers.length" class="empty">No active workers available.</p>

        <div class="form-actions">
          <button type="button" class="btn btn-ghost" @click="showAssign = false">Cancel</button>
          <button
            type="submit"
            class="btn btn-primary"
            :disabled="busy || selectedWorkerId === ''"
          >
            Assign
          </button>
        </div>
      </form>
    </BaseModal>

    <BaseModal v-if="showPay" title="Record payment" @close="showPay = false">
      <form @submit.prevent="recordPayment">
        <div class="field">
          <label for="payment-amount">Amount</label>
          <input
            id="payment-amount"
            v-model="payForm.amount"
            type="number"
            min="0.01"
            step="0.01"
            required
          />
        </div>

        <div class="field">
          <label for="payment-method">Method</label>
          <select id="payment-method" v-model="payForm.method">
            <option v-for="method in PAYMENT_METHODS" :key="method" :value="method">
              {{ method }}
            </option>
          </select>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-ghost" @click="showPay = false">Cancel</button>
          <button type="submit" class="btn btn-primary" :disabled="busy || payForm.amount === ''">
            {{ busy ? 'Saving...' : 'Record payment' }}
          </button>
        </div>
      </form>
    </BaseModal>
  </template>
</template>
