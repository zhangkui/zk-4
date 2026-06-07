import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    component: DefaultLayout,
    redirect: '/dashboard',
    meta: { hidden: true },
    children: []
  }
]

export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '监测大屏', icon: 'Monitor', perm: 'dashboard' }
  },
  {
    path: '/park',
    name: 'ParkList',
    component: () => import('@/views/ParkList.vue'),
    meta: { title: '园区管理', icon: 'OfficeBuilding', perm: 'park' }
  },
  {
    path: '/device',
    name: 'DeviceList',
    component: () => import('@/views/DeviceList.vue'),
    meta: { title: '设备台账', icon: 'Cpu', perm: 'device' }
  },
  {
    path: '/device/:deviceCode',
    name: 'DeviceDetail',
    component: () => import('@/views/DeviceDetail.vue'),
    meta: { title: '设备详情', icon: 'Cpu', perm: 'device-detail', hidden: true }
  },
  {
    path: '/history',
    name: 'HistoryData',
    component: () => import('@/views/HistoryData.vue'),
    meta: { title: '历史数据', icon: 'Histogram', perm: 'history' }
  },
  {
    path: '/user',
    name: 'UserList',
    component: () => import('@/views/UserList.vue'),
    meta: { title: '用户管理', icon: 'User', perm: 'user' }
  },
  {
    path: '/role',
    name: 'RoleList',
    component: () => import('@/views/RoleList.vue'),
    meta: { title: '角色管理', icon: 'UserFilled', perm: 'role' }
  },
  {
    path: '/log',
    name: 'LogAudit',
    component: () => import('@/views/LogAudit.vue'),
    meta: { title: '日志审计', icon: 'Document', perm: 'log' }
  },
  {
    path: '/simulator',
    name: 'Simulator',
    component: () => import('@/views/Simulator.vue'),
    meta: { title: '数据模拟器', icon: 'Setting', perm: 'simulator' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

const whiteList = ['/login']

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (token) {
    if (to.path === '/login') {
      next('/dashboard')
      return
    }

    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch {
        userStore.resetState()
        next('/login')
        return
      }
    }

    const accessedRoutes = filterAsyncRoutes(asyncRoutes, userStore.permissions)
    accessedRoutes.forEach(route => {
      if (!router.hasRoute(route.name as string)) {
        router.addRoute('/', route)
      }
    })

    next({ ...to, replace: true })
    return
  }

  if (whiteList.includes(to.path)) {
    next()
  } else {
    next('/login')
  }
})

function filterAsyncRoutes(routes: RouteRecordRaw[], permissions: string[]): RouteRecordRaw[] {
  const res: RouteRecordRaw[] = []
  routes.forEach(route => {
    const tmp = { ...route }
    if (hasPermission(permissions, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, permissions)
      }
      res.push(tmp)
    }
  })
  return res
}

function hasPermission(permissions: string[], route: RouteRecordRaw): boolean {
  if (route.meta && route.meta.perm) {
    return permissions.includes('*') || permissions.includes('ADMIN') || permissions.includes(route.meta.perm as string)
  }
  return true
}

export default router
