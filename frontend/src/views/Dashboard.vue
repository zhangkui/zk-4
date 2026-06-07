<template>
  <div class="dashboard-container">
    <div class="page-header-row">
      <h1 class="page-title">园区级微电网运行监测大屏</h1>
      <div class="park-selector">
        <el-select
          v-model="selectedParkId"
          placeholder="选择园区"
          style="width: 200px"
          @change="handleParkChange"
        >
          <el-option label="全部园区" :value="undefined" />
          <el-option
            v-for="park in parkList"
            :key="park.id"
            :label="park.parkName"
            :value="park.id"
          />
        </el-select>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card gradient-blue">
          <div class="card-label">光伏发电功率</div>
          <div class="card-value">
            {{ formatPower(stats.pvPower) }}
            <span class="unit">kW</span>
          </div>
          <el-icon class="card-icon"><Sunny /></el-icon>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card gradient-green">
          <div class="card-label">风力发电功率</div>
          <div class="card-value">
            {{ formatPower(stats.windPower) }}
            <span class="unit">kW</span>
          </div>
          <el-icon class="card-icon"><Wind /></el-icon>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card gradient-orange">
          <div class="card-label">负荷功率</div>
          <div class="card-value">
            {{ formatPower(stats.totalLoadPower) }}
            <span class="unit">kW</span>
          </div>
          <el-icon class="card-icon"><Odometer /></el-icon>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card gradient-purple">
          <div class="card-label">储能充放电功率</div>
          <div class="card-value">
            {{ formatPower(stats.essPower) }}
            <span class="unit">kW</span>
          </div>
          <el-icon class="card-icon"><Battery /></el-icon>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card gradient-red">
          <div class="card-label">储能SOC</div>
          <div class="card-value">
            {{ stats.essSoc.toFixed(1) }}
            <span class="unit">%</span>
          </div>
          <el-icon class="card-icon"><Lightning /></el-icon>
        </div>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <div class="stat-card gradient-cyan">
          <div class="card-label">在线设备数</div>
          <div class="card-value">
            {{ stats.onlineDeviceCount }}
            <span class="unit">/{{ stats.totalDeviceCount }}</span>
          </div>
          <el-icon class="card-icon"><Cpu /></el-icon>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="24">
        <div class="chart-card">
          <div class="chart-title">发电 / 负荷 / 储能 功率曲线（实时）</div>
          <v-chart :option="powerCurveOption" style="height: 320px" autoresize />
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts-row">
      <el-col :md="14" :span="24">
        <div class="chart-card">
          <div class="chart-title">实时设备列表</div>
          <el-table :data="realtimeDevices" style="width: 100%" max-height="320">
            <el-table-column prop="deviceCode" label="设备编号" width="140" />
            <el-table-column prop="deviceName" label="设备名称" width="140" />
            <el-table-column label="设备类型" width="100">
              <template #default="{ row }">
                {{ deviceTypeLabel(row.deviceType) }}
              </template>
            </el-table-column>
            <el-table-column label="当前功率(kW)" width="120">
              <template #default="{ row }">
                {{ row.currentPower?.toFixed(2) || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.isOnline ? 'success' : 'info'" size="small">
                  {{ row.isOnline ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="goToDetail(row.deviceCode)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :md="10" :span="24">
        <div class="chart-card">
          <div class="chart-title">设备状态分布</div>
          <v-chart :option="devicePieOption" style="height: 320px" autoresize />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, h } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import * as echarts from 'echarts/core'
import { getMonitorOverview, getMonitorDeviceStatus, getMonitorTrend, getMonitorRealtimeDevices } from '@/api/monitor'
import { getParkList } from '@/api/park'
import { DeviceTypeMap } from '@/types/api'
import type { DashboardStats, DeviceStatusPieData, TrendData, RealtimeDeviceInfo, ParkInfo, TrendPoint } from '@/types/api'

use([CanvasRenderer, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const router = useRouter()

const selectedParkId = ref<number | undefined>(undefined)
const parkList = ref<ParkInfo[]>([])
const stats = reactive<DashboardStats>({
  parkId: 0,
  parkName: '',
  totalGenerationPower: 0,
  totalLoadPower: 0,
  essPower: 0,
  essSoc: 0,
  chargingOnlineCount: 0,
  chargingTotalCount: 0,
  onlineDeviceCount: 0,
  totalDeviceCount: 0,
  onlineRate: 0,
  pvPower: 0,
  windPower: 0,
  gridPower: 0,
  deviceStatusStats: {}
})
const pieData = ref<DeviceStatusPieData[]>([])
const trendData = ref<TrendData>({
  generation: [],
  load: [],
  ess: [],
  pv: [],
  wind: []
})
const realtimeDevices = ref<RealtimeDeviceInfo[]>([])

let timer: ReturnType<typeof setInterval> | null = null

const darkTextStyle = {
  color: '#c8d6ff'
}

const powerCurveOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(19, 23, 64, 0.95)',
    borderColor: '#1f2a5a',
    textStyle: darkTextStyle
  },
  legend: {
    data: ['发电', '负荷', '储能'],
    textStyle: darkTextStyle,
    top: 0
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: 40,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trendData.value.generation.map(d => d.ts),
    axisLine: { lineStyle: { color: '#1f2a5a' } },
    axisLabel: { color: '#8492c4' }
  },
  yAxis: {
    type: 'value',
    name: '功率 (kW)',
    nameTextStyle: { color: '#8492c4' },
    axisLine: { show: false },
    axisLabel: { color: '#8492c4' },
    splitLine: { lineStyle: { color: '#1f2a5a' } }
  },
  series: [
    {
      name: '发电',
      type: 'line',
      smooth: true,
      data: trendData.value.generation.map(d => d.value),
      lineStyle: { color: '#38ef7d', width: 2 },
      itemStyle: { color: '#38ef7d' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(56, 239, 125, 0.3)' },
          { offset: 1, color: 'rgba(56, 239, 125, 0)' }
        ])
      }
    },
    {
      name: '负荷',
      type: 'line',
      smooth: true,
      data: trendData.value.load.map(d => d.value),
      lineStyle: { color: '#f5576c', width: 2 },
      itemStyle: { color: '#f5576c' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(245, 87, 108, 0.3)' },
          { offset: 1, color: 'rgba(245, 87, 108, 0)' }
        ])
      }
    },
    {
      name: '储能',
      type: 'line',
      smooth: true,
      data: trendData.value.ess.map(d => d.value),
      lineStyle: { color: '#f093fb', width: 2 },
      itemStyle: { color: '#f093fb' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(240, 147, 251, 0.3)' },
          { offset: 1, color: 'rgba(240, 147, 251, 0)' }
        ])
      }
    }
  ]
}))

const devicePieOption = computed(() => ({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(19, 23, 64, 0.95)',
    borderColor: '#1f2a5a',
    textStyle: darkTextStyle
  },
  legend: {
    orient: 'vertical',
    right: '5%',
    top: 'center',
    textStyle: darkTextStyle
  },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#0a0e27',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}: {d}%',
        color: '#c8d6ff'
      },
      data: pieData.value.map((item, idx) => ({
        name: item.name || item.statusName,
        value: item.value || item.count,
        itemStyle: {
          color: ['#67c23a', '#909399', '#e6a23c'][idx % 3]
        }
      }))
    }
  ]
}))

function formatPower(val: number): string {
  return (val || 0).toFixed(2)
}

function deviceTypeLabel(type: string): string {
  return (DeviceTypeMap as any)[type] || type
}

function goToDetail(deviceCode: string) {
  router.push(`/device/${deviceCode}`)
}

async function fetchParks() {
  try {
    parkList.value = await getParkList()
  } catch (err) {
  }
}

async function fetchDashboard() {
  try {
    const data = await getMonitorOverview(selectedParkId.value)
    Object.assign(stats, data)
  } catch (err) {
  }
}

async function fetchPieData() {
  try {
    pieData.value = await getMonitorDeviceStatus(selectedParkId.value)
  } catch (err) {
  }
}

async function fetchTrendData() {
  try {
    const data = await getMonitorTrend(selectedParkId.value, 24)
    trendData.value = data
  } catch (err) {
  }
}

async function fetchRealtimeDevices() {
  try {
    realtimeDevices.value = await getMonitorRealtimeDevices(selectedParkId.value)
  } catch (err) {
  }
}

function refreshAll() {
  fetchDashboard()
  fetchPieData()
  fetchTrendData()
  fetchRealtimeDevices()
}

function handleParkChange() {
  refreshAll()
}

onMounted(() => {
  fetchParks()
  refreshAll()
  timer = setInterval(refreshAll, 3000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  min-height: 100%;
  padding: 20px;
}

.page-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 0 20px rgba(102, 126, 234, 0.5);
    margin: 0;
  }

  .park-selector :deep(.el-input__wrapper) {
    background: rgba(19, 23, 64, 0.8);
    border: 1px solid #1f2a5a;
    box-shadow: none;
  }

  .park-selector :deep(.el-input__inner) {
    color: #c8d6ff;
  }
}

.stats-row {
  margin-bottom: 16px;
}

.charts-row {
  margin-bottom: 16px;
}

:deep(.el-table) {
  --el-table-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-header-bg-color: rgba(19, 23, 64, 0.6);
  --el-table-border-color: #1f2a5a;
  --el-table-text-color: #c8d6ff;
  --el-table-header-text-color: #8492c4;
  --el-table-row-hover-bg-color: rgba(31, 42, 90, 0.5);
}
</style>
