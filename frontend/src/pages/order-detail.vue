<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { errorMessage } from '@/services/api'
import { createOrderLog, getOrder, listOrderLogs, updateOrderStatus } from '@/services/orders'
import { listWorkers } from '@/services/workers'
import { formatDate, formatPrice } from '@/utils/format'
import { ORDER_ACTIONS, ORDER_STATUSES, SPECIALIZATIONS } from '@/types'
import type { Order, OrderAction, OrderLog, OrderStage, OrderStatus, Worker } from '@/types'

const route = useRoute()
const orderId = Number(route.params.id)

const order = ref<Order | null>(null)
const logs = ref<OrderLog[]>([])
const workers = ref<Worker[]>([])
const loading = ref(true)
const error = ref('')
const message = ref('')

const statusForm = reactive({
  status: 'CREATED' as OrderStatus,
  stage: '' as '' | OrderStage,
})
const savingStatus = ref(false)

const logForm = reactive({
  action: 'ASSIGNED' as OrderAction,
  stage: '' as '' | OrderStage,
  workerId: '' as '' | number,
  note: '',
})
const savingLog = ref(false)

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
    statusForm.status = orderData.status
    statusForm.stage = orderData.stage ?? ''
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    loading.value = false
  }
}

async function saveStatus() {
  savingStatus.value = true
  message.value = ''
  try {
    order.value = await updateOrderStatus(orderId, {
      status: statusForm.status,
      stage: statusForm.stage || null,
    })
    message.value = 'Status updated.'
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    savingStatus.value = false
  }
}

async function addLog() {
  savingLog.value = true
  message.value = ''
  try {
    await createOrderLog(orderId, {
      action: logForm.action,
      stage: logForm.stage || null,
      workerId: logForm.workerId === '' ? null : Number(logForm.workerId),
      note: logForm.note.trim() || null,
    })
    logForm.note = ''
    logs.value = await listOrderLogs(orderId)
    message.value = 'Log added.'
  } catch (e) {
    error.value = errorMessage(e)
  } finally {
    savingLog.value = false
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

        <form class="card-form" @submit.prevent="saveStatus">
          <h2>Update status</h2>

          <div class="field">
            <label for="order-status">Status</label>
            <select id="order-status" v-model="statusForm.status">
              <option v-for="status in ORDER_STATUSES" :key="status" :value="status">
                {{ status }}
              </option>
            </select>
          </div>

          <div class="field">
            <label for="order-stage">Stage</label>
            <select id="order-stage" v-model="statusForm.stage">
              <option value="">—</option>
              <option v-for="stage in SPECIALIZATIONS" :key="stage" :value="stage">{{ stage }}</option>
            </select>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="savingStatus">
              {{ savingStatus ? 'Saving...' : 'Save status' }}
            </button>
          </div>
        </form>
      </div>

      <div class="card">
        <h2>Add log</h2>
        <form @submit.prevent="addLog">
          <div class="field">
            <label for="log-action">Action</label>
            <select id="log-action" v-model="logForm.action">
              <option v-for="action in ORDER_ACTIONS" :key="action" :value="action">
                {{ action }}
              </option>
            </select>
          </div>

          <div class="field">
            <label for="log-stage">Stage</label>
            <select id="log-stage" v-model="logForm.stage">
              <option value="">—</option>
              <option v-for="stage in SPECIALIZATIONS" :key="stage" :value="stage">{{ stage }}</option>
            </select>
          </div>

          <div class="field">
            <label for="log-worker">Worker</label>
            <select id="log-worker" v-model="logForm.workerId">
              <option value="">Unassigned</option>
              <option v-for="worker in workers" :key="worker.id" :value="worker.id">
                {{ worker.name }} ({{ worker.specialization }})
              </option>
            </select>
          </div>

          <div class="field">
            <label for="log-note">Note</label>
            <textarea id="log-note" v-model="logForm.note" maxlength="500"></textarea>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="savingLog">
              {{ savingLog ? 'Saving...' : 'Add log' }}
            </button>
          </div>
        </form>
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
  </template>
</template>
