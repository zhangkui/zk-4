<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <el-button :icon="ArrowLeft" @click="goBack">返回</el-button>
        <span class="page-title" style="margin-left: 12px">设备详情</span>
      </div>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :span="24">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>基础信息</span>
              <el-tag :type="device?.status === 1 ? 'success' : 'info'" size="small">
                {{ device?.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="设备编号">{{ device?.deviceCode || '-' }}</el-descriptions-item>
            <el-descriptions-item label="设备名称">{{ device?.deviceName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="设备类型">{{ device ? DeviceTypeMap[device.deviceType] : '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属园区">{{ device?.parkName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="安装位置">{{ device?.installLocation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="额定功率">{{ device ? `${device.ratedPower} kW` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="接入方式">{{ device?.accessMethod || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最后上报">{{ device?.lastReportAt || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注">{{ device?.remark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :md="12" :span="24" style="margin-top: 16px">
        <el-card>
          <template #header>
            <span>实时数据</span>
          </template>
          <div class="realtime-grid">
            <div class="realtime-item">
              <div class="item-label">当前功率</div>
              <div class="item-value">{{ realtime?.currentPower?.toFixed(2) || '-' }}
                <span class="unit">kW</span>
              </div>
            </div>
            <div class="realtime-item">
              <div class="item-label">在线状态</div>
              <div class="item-value">
                <el-tag :type="realtime?.isOnline ? 'success' : 'info'" size="small">
                  {{ realtime?.isOnline ? '在线' : '离线' }}
                </el-tag>
              </div>
            </div>
            <div class="realtime-item" v-if="realtime?.metrics">
              <div class="item-label">SOC</div>
              <div class="item-value">{{ (realtime.metrics.soc as number)?.toFixed(1) || '-' }}
                <span class="unit">%</span>
              </div>
            </div>
            <div class="realtime-item" v-if="realtime?.metrics">
              <div class="item-label">电压</div>
              <div class="item-value">{{ (realtime.metrics.voltage as number)?.toFixed(1) || '-' }}
                <span class="unit">V</span>
              </div>
            </div>
            <div class="realtime-item" v-if="realtime?.metrics">
              <div class="item-label">电流</div>
              <div class="item-value">{{ (realtime.metrics.current as number)?.toFixed(2) || '-' }}
                <span class="unit">A</span>
              </div>
            </div>
            <div class="realtime-item">
              <div class="item-label">数据时间</div>
              <div class="item-value small">{{ realtime?.lastReportAt || '-' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :md="12" :span="24" style="margin-top: 16px">
        <el-card>
          <template #header>
            <span>24小时功率曲线</span>
          </template>
          <v-chart :option="curveOption" style="height: 300px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getDeviceByCode, getDeviceRealtimeByCode } from '@/api/device'
import { getMonitorDeviceCurve } from '@/api/monitor'
import { DeviceTypeMap } from '@/types/api'
import type { DeviceInfo, TrendData, TrendPoint } from '@/types/api'

const route = useRoute()
const router = useRouter()
const deviceCode = computed(() => route.params.deviceCode as string)

const loading = ref(false)
const device = ref<DeviceInfo | null>(null)
const realtime = ref<DeviceInfo | null>(null)
const curveData = ref<TrendData>({
  generation: [],
  load: [],
  ess: [],
  pv: [],
  wind: []
})

let timer: ReturnType<typeof setInterval> | null = null

const curveOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    data: ['发电', '负荷', '储能']
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: curveData.value.generation.map(d => d.ts)
  },
  yAxis: {
    type: 'value',
    name: '功率 (kW)'
  },
  series: [
    {
      name: '发电',
      type: 'line',
      smooth: true,
      data: curveData.value.generation.map(d => d.value),
      lineStyle: { color: '#38ef7d', width: 2 },
      itemStyle: { color: '#38ef7d' }
    },
    {
      name: '负荷',
      type: 'line',
      smooth: true,
      data: curveData.value.load.map(d => d.value),
      lineStyle: { color: '#f5576c', width: 2 },
      itemStyle: { color: '#f5576c' }
    },
    {
      name: '储能',
      type: 'line',
      smooth: true,
      data: curveData.value.ess.map(d => d.value),
      lineStyle: { color: '#f093fb', width: 2 },
      itemStyle: { color: '#f093fb' }
    }
  ]
}))

function goBack() {
  router.back()
}

async function fetchDevice() {
  loading.value = true
  try {
    device.value = await getDeviceByCode(deviceCode.value)
  } catch (err) {
  } finally {
    loading.value = false
  }
}

async function fetchRealtime() {
  try {
    realtime.value = await getDeviceRealtimeByCode(deviceCode.value)
  } catch (err) {
  }
}

async function fetchCurve() {
  try {
    curveData.value = await getMonitorDeviceCurve(deviceCode.value, 24)
  } catch (err) {
  }
}

function refreshAll() {
  fetchRealtime()
  fetchCurve()
}

onMounted(() => {
  fetchDevice()
  refreshAll()
  timer = setInterval(refreshAll, 5000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }
}

.info-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }
}

.realtime-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;

  .realtime-item {
    background: #f5f7fa;
    border-radius: 6px;
    padding: 16px;

    .item-label {
      font-size: 13px;
      color: #909399;
      margin-bottom: 8px;
    }

    .item-value {
      font-size: 24px;
      font-weight: 600;
      color: #303133;

      &.small {
        font-size: 14px;
        color: #606266;
      }

      .unit {
        font-size: 13px;
        font-weight: 400;
        color: #909399;
        margin-left: 4px;
      }
    }
  }
}
</style>
