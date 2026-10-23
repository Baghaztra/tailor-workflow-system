import Dashboard from '@/pages/dashboard.vue'
import Finance from '@/pages/finance.vue'
import OrderDetail from '@/pages/order-detail.vue'
import Orders from '@/pages/orders.vue'
import Workers from '@/pages/workers.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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

export default router
