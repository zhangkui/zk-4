import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { storage } from '@/utils/storage'
import { login, logout, getAuthMe } from '@/api/auth'
import type { LoginParams, UserInfo, LoginResult } from '@/types/api'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(storage.getToken())
  const userInfo = ref<UserInfo | null>(storage.getUserInfo())
  const roleCodes = ref<string[]>(userInfo.value?.roles?.map((r: any) => r.roleCode) || [])
  const permissions = ref<string[]>([...roleCodes.value, ...(userInfo.value?.permissions || [])])
  const menus = ref<string[]>([])

  const isLoggedIn = computed(() => !!token.value)

  function hasPermission(perm: string): boolean {
    if (!perm) return true
    return permissions.value.includes(perm) || permissions.value.includes('*') || permissions.value.includes('ADMIN')
  }

  function hasRole(role: string): boolean {
    if (!userInfo.value?.roles) return false
    return userInfo.value.roles.some(r => r.roleCode === role)
  }

  function buildUserInfoFromLogin(res: LoginResult): UserInfo {
    return {
      id: res.userId,
      username: res.username,
      realName: res.realName,
      email: '',
      phone: '',
      status: 1,
      roles: res.roles.map(code => ({
        id: 0,
        roleCode: code,
        roleName: code,
        description: '',
        status: 1
      })),
      permissions: res.permissions
    }
  }

  async function loginAction(params: LoginParams) {
    const res = await login(params)
    token.value = res.token
    const info = buildUserInfoFromLogin(res)
    userInfo.value = info
    permissions.value = [...res.roles, ...res.permissions]
    storage.setToken(res.token)
    storage.setUserInfo(info)
    menus.value = generateMenus(permissions.value)
    return res
  }

  async function logoutAction() {
    try {
      await logout()
    } finally {
      resetState()
    }
  }

  async function fetchUserInfo() {
    const res = await getAuthMe()
    const info = buildUserInfoFromLogin(res)
    userInfo.value = info
    permissions.value = [...res.roles, ...res.permissions]
    storage.setUserInfo(info)
    menus.value = generateMenus(permissions.value)
    return info
  }

  function resetState() {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    menus.value = []
    storage.clear()
  }

  function generateMenus(perms: string[]): string[] {
    const allMenus = [
      { path: '/admin-home', perm: 'admin-home' },
      { path: '/dashboard', perm: 'dashboard' },
      { path: '/park', perm: 'park' },
      { path: '/device', perm: 'device' },
      { path: '/history', perm: 'history' },
      { path: '/user', perm: 'user' },
      { path: '/role', perm: 'role' },
      { path: '/permission', perm: 'permission' },
      { path: '/login-log', perm: 'log' },
      { path: '/operation-log', perm: 'log' },
      { path: '/profile', perm: 'profile' },
      { path: '/simulator', perm: 'simulator' }
    ]
    if (perms.includes('*') || perms.includes('ADMIN')) {
      return allMenus.map(m => m.path)
    }
    return allMenus.filter(m => perms.includes(m.perm)).map(m => m.path)
  }

  return {
    token,
    userInfo,
    permissions,
    menus,
    isLoggedIn,
    hasPermission,
    hasRole,
    loginAction,
    logoutAction,
    fetchUserInfo,
    resetState
  }
})
