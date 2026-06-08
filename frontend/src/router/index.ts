import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

export interface MenuGroup {
  title: string
  icon?: string
  children: RouteRecordRaw[]
}

export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    name: 'Root',
    component: DefaultLayout,
    redirect: '/admin-home',
    meta: { hidden: true },
    children: []
  }
]

export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/admin-home',
    name: 'AdminHome',
    component: () => import('@/views/AdminHome.vue'),
    meta: { title: '首页', icon: 'HomeFilled', perm: 'admin-home', group: '管理中心' }
  },
  {
    path: '/user',
    name: 'UserList',
    component: () => import('@/views/UserList.vue'),
    meta: { title: '用户管理', icon: 'User', perm: 'user', group: '系统管理' }
  },
  {
    path: '/role',
    name: 'RoleList',
    component: () => import('@/views/RoleList.vue'),
    meta: { title: '角色管理', icon: 'UserFilled', perm: 'role', group: '系统管理' }
  },
  {
    path: '/permission',
    name: 'PermissionList',
    component: () => import('@/views/PermissionList.vue'),
    meta: { title: '权限管理', icon: 'Lock', perm: 'permission', group: '系统管理' }
  },
  {
    path: '/login-log',
    name: 'LoginLog',
    component: () => import('@/views/LogAudit.vue'),
    meta: { title: '登录日志', icon: 'Tickets', perm: 'log', group: '系统管理' }
  },
  {
    path: '/operation-log',
    name: 'OperationLog',
    component: () => import('@/views/OperationLog.vue'),
    meta: { title: '操作日志', icon: 'Notebook', perm: 'log', group: '系统管理' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', icon: 'Avatar', perm: 'profile', group: '系统管理' }
  },
  {
    path: '/park',
    name: 'ParkList',
    component: () => import('@/views/ParkList.vue'),
    meta: { title: '园区管理', icon: 'OfficeBuilding', perm: 'park', group: '基础档案' }
  },
  {
    path: '/device',
    name: 'DeviceList',
    component: () => import('@/views/DeviceList.vue'),
    meta: { title: '设备台账', icon: 'Cpu', perm: 'device', group: '基础档案' }
  },
  {
    path: '/device/:deviceCode',
    name: 'DeviceDetail',
    component: () => import('@/views/DeviceDetail.vue'),
    meta: { title: '设备详情', icon: 'Cpu', perm: 'device-detail', hidden: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '监测大屏', icon: 'Monitor', perm: 'dashboard', group: '运行监控' }
  },
  {
    path: '/history',
    name: 'HistoryData',
    component: () => import('@/views/HistoryData.vue'),
    meta: { title: '历史数据', icon: 'Histogram', perm: 'history', group: '运行监控' }
  },
  {
    path: '/simulator',
    name: 'Simulator',
    component: () => import('@/views/Simulator.vue'),
    meta: { title: '数据模拟器', icon: 'Setting', perm: 'simulator', group: '运行监控' }
  },
  {
    path: '/log',
    name: 'LogAudit',
    component: () => import('@/views/LogAudit.vue'),
    meta: { title: '日志审计', icon: 'Document', perm: 'log', hidden: true }
  }
]

export const menuGroups: MenuGroup[] = [
  { title: '管理中心', icon: 'HomeFilled', children: [] },
  { title: '系统管理', icon: 'Setting', children: [] },
  { title: '基础档案', icon: 'Files', children: [] },
  { title: '运行监控', icon: 'Monitor', children: [] }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

const whiteList = ['/login']
let dynamicRoutesAdded = false

router.beforeEach(async (to, _from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (token) {
    if (to.path === '/login') {
      next('/admin-home')
      return
    }

    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
        dynamicRoutesAdded = false
      } catch {
        userStore.resetState()
        dynamicRoutesAdded = false
        next('/login')
        return
      }
    }

    if (!dynamicRoutesAdded) {
      const accessedRoutes = filterAsyncRoutes(asyncRoutes, userStore.permissions)
      accessedRoutes.forEach(route => {
        if (!router.hasRoute(route.name as string)) {
          router.addRoute('Root', route)
        }
      })
      dynamicRoutesAdded = true
      next({ ...to, replace: true })
      return
    }

    next()
    return
  }

  dynamicRoutesAdded = false
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
