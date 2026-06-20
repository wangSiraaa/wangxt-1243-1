import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'DataBoard' }
      },
      {
        path: 'personnel',
        name: 'Personnel',
        component: () => import('@/views/Personnel.vue'),
        meta: { title: '人员管理', icon: 'User' }
      },
      {
        path: 'qualification',
        name: 'Qualification',
        component: () => import('@/views/Qualification.vue'),
        meta: { title: '资质管理', icon: 'Certificate' }
      },
      {
        path: 'customer',
        name: 'Customer',
        component: () => import('@/views/Customer.vue'),
        meta: { title: '客户管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'customer-point',
        name: 'CustomerPoint',
        component: () => import('@/views/CustomerPoint.vue'),
        meta: { title: '客户点位管理', icon: 'Location' }
      },
      {
        path: 'shift-template',
        name: 'ShiftTemplate',
        component: () => import('@/views/ShiftTemplate.vue'),
        meta: { title: '班次模板管理', icon: 'Calendar' }
      },
      {
        path: 'shift-schedule',
        name: 'ShiftSchedule',
        component: () => import('@/views/ShiftSchedule.vue'),
        meta: { title: '排班管理', icon: 'Clock' }
      },
      {
        path: 'shift-exchange',
        name: 'ShiftExchange',
        component: () => import('@/views/ShiftExchange.vue'),
        meta: { title: '换班管理', icon: 'SwitchButton' }
      },
      {
        path: 'patrol-event',
        name: 'PatrolEvent',
        component: () => import('@/views/PatrolEvent.vue'),
        meta: { title: '巡更事件管理', icon: 'Warning' }
      },
      {
        path: 'penalty',
        name: 'Penalty',
        component: () => import('@/views/Penalty.vue'),
        meta: { title: '扣罚管理', icon: 'Finished' }
      },
      {
        path: 'settlement',
        name: 'Settlement',
        component: () => import('@/views/Settlement.vue'),
        meta: { title: '结算管理', icon: 'Money' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  NProgress.start()
  const userStore = useUserStore()
  const token = userStore.token

  if (to.meta.requiresAuth !== false && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
