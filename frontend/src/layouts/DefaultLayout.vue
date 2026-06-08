<template>
  <el-container class="layout-container">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <el-icon :size="28" color="#409eff"><Monitor /></el-icon>
        <span v-if="!appStore.sidebarCollapsed" class="logo-text">微电网监测平台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        :default-openeds="defaultOpeneds"
        router
        background-color="#001529"
        text-color="#c8d6ff"
        active-text-color="#409eff"
      >
        <template v-for="group in groupedMenus" :key="group.title">
          <el-sub-menu v-if="group.children.length > 0" :index="group.title">
            <template #title>
              <el-icon v-if="group.icon">
                <component :is="group.icon" />
              </el-icon>
              <span>{{ group.title }}</span>
            </template>
            <el-menu-item
              v-for="route in group.children"
              :key="route.path"
              :index="route.path"
            >
              <el-icon v-if="route.meta?.icon">
                <component :is="route.meta.icon" />
              </el-icon>
              <template #title>{{ route.meta?.title }}</template>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin-home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.group">{{ currentRoute.meta.group }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta?.title && currentRoute.path !== '/admin-home'">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
import { asyncRoutes, menuGroups } from '@/router'
import type { MenuGroup } from '@/router'
import type { RouteRecordRaw } from 'vue-router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const currentRoute = computed(() => route)
const activeMenu = computed(() => route.path)

const defaultOpeneds = computed(() => {
  const group = route.meta?.group
  return group ? [group as string] : ['管理中心']
})

const groupedMenus = computed((): MenuGroup[] => {
  const perms = userStore.permissions
  const filteredRoutes = asyncRoutes.filter(r => {
    if (r.meta?.hidden) return false
    if (!r.meta?.perm) return true
    return perms.includes('*') || perms.includes('ADMIN') || perms.includes(r.meta.perm as string)
  }) as RouteRecordRaw[]

  const groups: MenuGroup[] = menuGroups.map(g => ({ ...g, children: [] }))
  const groupMap = new Map(groups.map(g => [g.title, g]))

  filteredRoutes.forEach(r => {
    const groupName = (r.meta?.group as string) || '其他'
    const group = groupMap.get(groupName)
    if (group) {
      group.children.push(r)
    }
  })

  return groups.filter(g => g.children.length > 0)
})

async function handleUserCommand(command: string) {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logoutAction()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
    }
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100%;
}

.sidebar {
  background: #001529;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    border-bottom: 1px solid #1f2a5a;
    color: #fff;

    .logo-text {
      font-size: 16px;
      font-weight: 600;
      white-space: nowrap;
    }
  }

  .el-menu {
    border-right: none;
  }

  :deep(.el-sub-menu__title:hover) {
    background-color: rgba(64, 158, 255, 0.1);
  }

  :deep(.el-menu-item:hover) {
    background-color: rgba(64, 158, 255, 0.1);
  }
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 60px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      padding: 8px;
      border-radius: 4px;

      &:hover {
        background: #f5f7fa;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;

      &:hover {
        background: #f5f7fa;
      }

      .username {
        color: #303133;
      }
    }
  }
}

.main-content {
  background: #f0f2f5;
  padding: 0;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
