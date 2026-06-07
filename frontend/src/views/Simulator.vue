<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">数据模拟器控制</h2>
    </div>

    <el-row :gutter="16">
      <el-col :md="8" :span="24">
        <el-card>
          <template #header>
            <span>运行状态</span>
          </template>
          <div class="status-card">
            <div class="status-indicator" :class="{ running: status.running }">
              <div class="pulse" v-if="status.running"></div>
              <el-icon :size="48">
                <VideoPlay v-if="status.running" />
                <VideoPause v-else />
              </el-icon>
            </div>
            <div class="status-text">
              <h3 :class="{ running: status.running }">
                {{ status.running ? '运行中' : '已停止' }}
              </h3>
              <p v-if="status.running && status.startTime">
                启动时间：{{ status.startTime }}
              </p>
            </div>
          </div>
          <div class="action-btns">
            <el-button
              v-if="!status.running"
              type="primary"
              size="large"
              :icon="VideoPlay"
              :loading="actionLoading"
              @click="handleStart"
            >
              启动模拟
            </el-button>
            <el-button
              v-else
              type="danger"
              size="large"
              :icon="VideoPause"
              :loading="actionLoading"
              @click="handleStop"
            >
              停止模拟
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :md="16" :span="24">
        <el-card>
          <template #header>
            <span>模拟参数说明</span>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="模拟速度">
              <el-tag type="primary">{{ status.speed }}x</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="设备数量">
              {{ status.deviceCount }} 台
            </el-descriptions-item>
            <el-descriptions-item label="运行设备数">
              {{ status.runningDeviceCount }} 台
            </el-descriptions-item>
            <el-descriptions-item label="已生成数据点" :span="2">
              <span class="highlight">{{ status.dataPointsGenerated?.toLocaleString() || 0 }}</span> 条
            </el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <h4 style="margin-bottom: 12px">设备模拟范围：</h4>
          <el-table :data="simParams" size="small" border>
            <el-table-column prop="type" label="设备类型" width="140" />
            <el-table-column prop="minPower" label="最小功率(kW)" />
            <el-table-column prop="maxPower" label="最大功率(kW)" />
            <el-table-column prop="description" label="说明" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px" v-if="status.running">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>实时模拟数据生成速率</span>
          </template>
          <v-chart :option="rateChartOption" style="height: 260px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getSimulatorOverview, startSimulatorAll, stopSimulatorAll } from '@/api/simulator'
import type { SimulatorStatus } from '@/types/api'

const actionLoading = ref(false)
const status = reactive<SimulatorStatus>({
  running: false,
  speed: 1,
  deviceCount: 0,
  runningDeviceCount: 0,
  dataPointsGenerated: 0
})

const simParams = [
  { type: '光伏(PV)', minPower: 0, maxPower: 500, description: '白天发电，夜间为0' },
  { type: '风电(WIND)', minPower: 0, maxPower: 300, description: '随机波动' },
  { type: '储能(ESS)', minPower: -200, maxPower: 200, description: '充放电，SOC 10%-90%' },
  { type: '变流器(PCS)', minPower: -300, maxPower: 300, description: '双向变流' },
  { type: '充电桩(CHARGING)', minPower: 0, maxPower: 120, description: '充电状态随机' },
  { type: '电表(METER)', minPower: 50, maxPower: 800, description: '计量总功率' },
  { type: '负荷(LOAD)', minPower: 100, maxPower: 600, description: '日常用电负荷' }
]

const rateData = ref<{ time: string; value: number }[]>([])

const rateChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: rateData.value.map(d => d.time)
  },
  yAxis: { type: 'value', name: '条/秒' },
  series: [{
    type: 'line',
    smooth: true,
    data: rateData.value.map(d => d.value),
    areaStyle: { color: 'rgba(64, 158, 255, 0.2)' },
    lineStyle: { color: '#409eff' }
  }]
}))

let timer: ReturnType<typeof setInterval> | null = null
let rateTimer: ReturnType<typeof setInterval> | null = null
let lastPoints = 0

async function fetchStatus() {
  try {
    const data = await getSimulatorOverview()
    Object.assign(status, data)
  } catch (err) {
  }
}

async function handleStart() {
  try {
    await ElMessageBox.confirm('确定要启动数据模拟器吗？', '提示', { type: 'info' })
    actionLoading.value = true
    await startSimulatorAll()
    ElMessage.success('模拟器已启动')
    await fetchStatus()
    lastPoints = status.dataPointsGenerated || 0
    rateData.value = []
  } catch (err: any) {
    if (err?.message) ElMessage.error(err.message)
  } finally {
    actionLoading.value = false
  }
}

async function handleStop() {
  try {
    await ElMessageBox.confirm('确定要停止数据模拟器吗？', '提示', { type: 'warning' })
    actionLoading.value = true
    await stopSimulatorAll()
    ElMessage.success('模拟器已停止')
    await fetchStatus()
  } catch (err: any) {
    if (err?.message) ElMessage.error(err.message)
  } finally {
    actionLoading.value = false
  }
}

function collectRate() {
  const now = dayjs().format('HH:mm:ss')
  const current = status.dataPointsGenerated || 0
  const rate = Math.max(0, current - lastPoints)
  lastPoints = current
  rateData.value.push({ time: now, value: rate })
  if (rateData.value.length > 30) rateData.value.shift()
}

onMounted(() => {
  fetchStatus()
  timer = setInterval(fetchStatus, 3000)
  rateTimer = setInterval(collectRate, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (rateTimer) clearInterval(rateTimer)
})
</script>

<style lang="scss" scoped>
.page-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    .page-title {
      font-size: 20px;
      font-weight: 600;
    }
  }
}

.status-card {
  text-align: center;
  padding: 20px 0;

  .status-indicator {
    display: inline-block;
    position: relative;
    color: #909399;
    padding: 20px;
    border-radius: 50%;
    background: #f5f7fa;
    transition: all 0.3s;

    &.running {
      color: #67c23a;
      background: rgba(103, 194, 58, 0.1);
    }

    .pulse {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 50%;
      border: 2px solid #67c23a;
      animation: pulse 1.5s ease-out infinite;
    }
  }

  @keyframes pulse {
    0% { transform: scale(1); opacity: 1; }
    100% { transform: scale(1.6); opacity: 0; }
  }

  .status-text {
    margin-top: 16px;

    h3 {
      font-size: 20px;
      color: #909399;
      margin-bottom: 8px;

      &.running { color: #67c23a; }
    }

    p { color: #909399; font-size: 13px; }
  }
}

.action-btns {
  text-align: center;
  margin-top: 16px;
}

.highlight {
  color: #409eff;
  font-size: 18px;
  font-weight: 600;
}
</style>
