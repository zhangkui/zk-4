<template>
  <div class="admin-home">
    <div class="page-header">
      <h2 class="page-title">管理中心</h2>
      <div class="header-subtitle">欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}！</div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card stat-blue">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card stat-purple">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.roleCount }}</div>
            <div class="stat-label">角色总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card stat-green">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.parkCount }}</div>
            <div class="stat-label">园区总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card stat-cyan">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.deviceTotalCount }}</div>
            <div class="stat-label">设备总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card stat-orange">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.deviceOnlineCount }}</div>
            <div class="stat-label">在线设备</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card stat-red">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><View /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayLoginCount }}</div>
            <div class="stat-label">今日登录</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :md="8" :span="24">
        <div class="card todo-card">
          <div class="card-header">
            <span class="card-title"><el-icon><Bell /></el-icon>待办提示</span>
          </div>
          <div class="card-body">
            <div class="todo-item" @click="router.push('/user')">
              <div class="todo-icon todo-warning">
                <el-icon><User /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-text">停用用户数</div>
                <div class="todo-num">{{ stats.disabledUserCount }}</div>
              </div>
              <el-icon class="todo-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="todo-item" @click="router.push('/device')">
              <div class="todo-icon todo-danger">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-text">停用设备数</div>
                <div class="todo-num">{{ stats.disabledDeviceCount }}</div>
              </div>
              <el-icon class="todo-arrow"><ArrowRight /></el-icon>
            </div>
            <div class="todo-item" @click="router.push('/park')">
              <div class="todo-icon todo-info">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <div class="todo-content">
                <div class="todo-text">无设备园区数</div>
                <div class="todo-num">{{ stats.parkWithNoDeviceCount }}</div>
              </div>
              <el-icon class="todo-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :md="16" :span="24">
        <div class="card quick-card">
          <div class="card-header">
            <span class="card-title"><el-icon><Lightning /></el-icon>快捷入口</span>
          </div>
          <div class="card-body">
            <el-row :gutter="12">
              <el-col :xs="8" :sm="6" :md="4">
                <div class="quick-item" @click="handleQuickAction('user')">
                  <div class="quick-icon quick-blue">
                    <el-icon :size="26"><Avatar /></el-icon>
                  </div>
                  <div class="quick-label">新增用户</div>
                </div>
              </el-col>
              <el-col :xs="8" :sm="6" :md="4">
                <div class="quick-item" @click="handleQuickAction('park')">
                  <div class="quick-icon quick-green">
                    <el-icon :size="26"><Plus /></el-icon>
                  </div>
                  <div class="quick-label">新增园区</div>
                </div>
              </el-col>
              <el-col :xs="8" :sm="6" :md="4">
                <div class="quick-item" @click="handleQuickAction('device')">
                  <div class="quick-icon quick-cyan">
                    <el-icon :size="26"><Cpu /></el-icon>
                  </div>
                  <div class="quick-label">新增设备</div>
                </div>
              </el-col>
              <el-col :xs="8" :sm="6" :md="4">
                <div class="quick-item" @click="router.push('/role')">
                  <div class="quick-icon quick-purple">
                    <el-icon :size="26"><Lock /></el-icon>
                  </div>
                  <div class="quick-label">角色权限</div>
                </div>
              </el-col>
              <el-col :xs="8" :sm="6" :md="4">
                <div class="quick-item" @click="router.push('/login-log')">
                  <div class="quick-icon quick-orange">
                    <el-icon :size="26"><Tickets /></el-icon>
                  </div>
                  <div class="quick-label">日志审计</div>
                </div>
              </el-col>
              <el-col :xs="8" :sm="6" :md="4">
                <div class="quick-item" @click="router.push('/dashboard')">
                  <div class="quick-icon quick-red">
                    <el-icon :size="26"><Monitor /></el-icon>
                  </div>
                  <div class="quick-label">监测大屏</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :md="12" :span="24">
        <div class="card">
          <div class="card-header">
            <span class="card-title"><el-icon><TrendCharts /></el-icon>运行概览</span>
          </div>
          <div class="card-body">
            <el-table :data="stats.parkOnlineRates" stripe style="width: 100%">
              <el-table-column prop="parkName" label="园区名称" min-width="120" />
              <el-table-column label="设备在线率" min-width="200">
                <template #default="{ row }">
                  <div class="progress-wrap">
                    <el-progress
                      :percentage="Number(row.onlineRate || 0)"
                      :stroke-width="12"
                      :color="getProgressColor(Number(row.onlineRate || 0))"
                    />
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="设备情况" width="140" align="center">
                <template #default="{ row }">
                  <span class="device-count">
                    {{ row.onlineDeviceCount }}/{{ row.totalDeviceCount }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="告警" width="80" align="center">
                <template #default="{ row }">
                  <el-tag size="small" :type="row.alarmCount > 0 ? 'danger' : 'info'">
                    {{ row.alarmCount || 0 }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>

      <el-col :md="12" :span="24">
        <div class="card">
          <div class="card-header">
            <span class="card-title"><el-icon><Clock /></el-icon>最近登录记录</span>
            <el-button type="primary" link size="small" @click="router.push('/login-log')">查看全部</el-button>
          </div>
          <div class="card-body">
            <el-table :data="stats.recentLoginLogs" stripe style="width: 100%" max-height="320">
              <el-table-column prop="username" label="用户名" width="120" />
              <el-table-column prop="loginIp" label="IP地址" width="140" />
              <el-table-column label="状态" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                    {{ row.status === 1 ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="message" label="结果" min-width="100" show-overflow-tooltip />
              <el-table-column prop="loginAt" label="时间" width="170" />
            </el-table>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  UserFilled,
  OfficeBuilding,
  Cpu,
  Connection,
  View,
  Bell,
  Lightning,
  Plus,
  Lock,
  Tickets,
  Monitor,
  TrendCharts,
  Clock,
  ArrowRight,
  Avatar
} from '@element-plus/icons-vue'
import { getAdminHomeStats } from '@/api/adminHome'
import { useUserStore } from '@/store/user'
import type { AdminHomeStats } from '@/types/api'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive<AdminHomeStats>({
  userCount: 0,
  roleCount: 0,
  parkCount: 0,
  deviceTotalCount: 0,
  deviceOnlineCount: 0,
  todayLoginCount: 0,
  disabledUserCount: 0,
  disabledDeviceCount: 0,
  parkWithNoDeviceCount: 0,
  parkOnlineRates: [],
  recentLoginLogs: []
})

const loading = ref(false)

async function fetchStats() {
  loading.value = true
  try {
    const data = await getAdminHomeStats()
    Object.assign(stats, data)
  } catch (err) {
    ElMessage.error('获取首页统计数据失败')
  } finally {
    loading.value = false
  }
}

function handleQuickAction(type: string) {
  switch (type) {
    case 'user':
      router.push('/user')
      break
    case 'park':
      router.push('/park')
      break
    case 'device':
      router.push('/device')
      break
  }
}

function getProgressColor(rate: number) {
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

onMounted(fetchStats)
</script>

<style lang="scss" scoped>
.admin-home {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;

  .page-title {
    margin: 0 0 4px 0;
    font-size: 22px;
    font-weight: 600;
    color: #303133;
  }

  .header-subtitle {
    color: #909399;
    font-size: 14px;
  }
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px 0 rgba(0, 0, 0, 0.1);
  }

  .stat-icon-wrap {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  &.stat-blue .stat-icon-wrap { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
  &.stat-purple .stat-icon-wrap { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
  &.stat-green .stat-icon-wrap { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
  &.stat-cyan .stat-icon-wrap { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
  &.stat-orange .stat-icon-wrap { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
  &.stat-red .stat-icon-wrap { background: linear-gradient(135deg, #30cfd0 0%, #330867 100%); }

  .stat-info {
    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      line-height: 1.2;
    }

    .stat-label {
      font-size: 13px;
      color: #909399;
      margin-top: 4px;
    }
  }
}

.content-row {
  margin-bottom: 16px;
}

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;

  .card-header {
    padding: 16px 20px;
    border-bottom: 1px solid #ebeef5;
    display: flex;
    align-items: center;
    justify-content: space-between;

    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      display: flex;
      align-items: center;
      gap: 8px;

      .el-icon {
        color: #409eff;
      }
    }
  }

  .card-body {
    padding: 16px 20px;
  }
}

.todo-card {
  .todo-item {
    display: flex;
    align-items: center;
    padding: 14px 0;
    border-bottom: 1px solid #f2f6fc;
    cursor: pointer;
    transition: background 0.2s;

    &:last-child {
      border-bottom: none;
    }

    &:hover {
      background: #fafbfc;
    }

    .todo-icon {
      width: 40px;
      height: 40px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-right: 12px;
      flex-shrink: 0;

      &.todo-warning { background: #e6a23c; }
      &.todo-danger { background: #f56c6c; }
      &.todo-info { background: #909399; }
    }

    .todo-content {
      flex: 1;

      .todo-text {
        font-size: 14px;
        color: #606266;
      }

      .todo-num {
        font-size: 22px;
        font-weight: 600;
        color: #303133;
        margin-top: 2px;
      }
    }

    .todo-arrow {
      color: #c0c4cc;
    }
  }
}

.quick-card {
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 8px;
    cursor: pointer;
    border-radius: 8px;
    transition: all 0.2s;
    margin-bottom: 8px;

    &:hover {
      background: #f5f7fa;
      transform: translateY(-2px);
    }

    .quick-icon {
      width: 52px;
      height: 52px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-bottom: 8px;

      &.quick-blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
      &.quick-green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
      &.quick-cyan { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
      &.quick-purple { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
      &.quick-orange { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
      &.quick-red { background: linear-gradient(135deg, #30cfd0 0%, #330867 100%); }
    }

    .quick-label {
      font-size: 13px;
      color: #606266;
    }
  }
}

.progress-wrap {
  padding: 0 8px;
}

.device-count {
  color: #606266;
  font-size: 13px;
  font-weight: 500;
}
</style>
