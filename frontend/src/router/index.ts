import Dashboard from '@/pages/dashboard.vue'
import Finance from '@/pages/finance.vue'
import Login from '@/pages/login.vue'
import OrderDetail from '@/pages/order-detail.vue'
import Orders from '@/pages/orders.vue'
import Workers from '@/pages/workers.vue'
import { authChecked, currentAdmin, fetchMe } from '@/services/auth'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      name: 'login',
      path: '/login',
      component: Login,
      meta: { public: true },
    },
    {
      name: 'dashboard',
      path: '/',
      component: Dashboard,
    },
    {
      name: 'workers',
      path: '/workers',
      component: Workers,
    },
    {
      name: 'orders',
      path: '/orders',
      component: Orders,
    },
    {
      name: 'order-detail',
      path: '/orders/:id',
      component: OrderDetail,
    },
    {
      name: 'finance',
      path: '/finance',
      component: Finance,
    },
  ],
})

router.beforeEach(async (to) => {
  if (to.meta.public) {
    if (to.name === 'login' && authChecked.value && currentAdmin.value) {
      return { name: 'dashboard' }
    }
    return true
  }

  if (!authChecked.value) {
    try {
      await fetchMe()
      authChecked.value = true
    } catch {
      authChecked.value = true
      return { name: 'login', query: { redirect: to.fullPath } }
    }
  }

  if (!currentAdmin.value) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  return true
})

export default router
