<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">历史数据查询</h2>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="queryForm" @submit.prevent>
        <el-form-item label="园区">
          <el-select
            v-model="queryForm.parkId"
            placeholder="全部园区"
            clearable
            style="width: 180px"
            @change="handleParkChange"
          >
            <el-option
              v-for="park in parkList"
              :key="park.id"
              :label="park.parkName"
              :value="park.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备">
          <el-select
            v-model="queryForm.deviceCode"
            placeholder="选择设备"
            clearable
            filterable
            style="width: 220px"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.deviceCode"
              :label="`${device.deviceName} (${device.deviceCode})`"
              :value="device.deviceCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" :loading="loading" @click="handleQuery">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>数据趋势图</span>
          </template>
          <v-chart :option="chartOption" style="height: 340px" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <div class="data-table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="ts" label="时间" width="200" />
        <el-table-column prop="deviceCode" label="设备编号" width="160" />
        <el-table-column prop="metricCode" label="指标编码" width="140" />
        <el-table-column label="数值" width="140">
          <template #default="{ row }">
            {{ Number(row.metricValue).toFixed(3) }}
          </template>
        </el-table-column>
        <el-table-column prop="statusFlag" label="状态标志" width="120" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getHistoryPage, getHistoryList } from '@/api/history'
import { getParkList } from '@/api/park'
import { getDeviceList } from '@/api/device'
import type { ParkInfo, DeviceInfo, HistoryDataVO } from '@/types/api'

const loading = ref(false)
const parkList = ref<ParkInfo[]>([])
const deviceList = ref<DeviceInfo[]>([])
const chartData = ref<HistoryDataVO[]>([])
const tableData = ref<HistoryDataVO[]>([])

const defaultRange = [
  dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
  dayjs().format('YYYY-MM-DD HH:mm:ss')
]
const dateRange = ref<string[]>(defaultRange)

const queryForm = reactive({
  parkId: undefined as number | undefined,
  deviceCode: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const chartOption = computed(() => ({
  tooltip: {
    trigger: 'axis'
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
    data: chartData.value.map(d => d.ts)
  },
  yAxis: {
    type: 'value'
  },
  dataZoom: [
    { type: 'inside', start: 0, end: 100 },
    { type: 'slider', start: 0, end: 100 }
  ],
  series: [
    {
      type: 'line',
      smooth: true,
      data: chartData.value.map(d => Number(d.metricValue).toFixed(3)),
      lineStyle: { color: '#409eff' },
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0)' }
          ]
        }
      }
    }
  ]
}))

async function fetchParks() {
  try {
    parkList.value = await getParkList()
  } catch (err) {
  }
}

async function fetchDevices(parkId?: number) {
  try {
    deviceList.value = await getDeviceList({ parkId })
  } catch (err) {
  }
}

function handleParkChange() {
  queryForm.deviceCode = ''
  fetchDevices(queryForm.parkId)
}

async function fetchList() {
  if (!dateRange.value || dateRange.value.length < 2) return
  loading.value = true
  try {
    const res = await getHistoryPage({
      parkId: queryForm.parkId,
      deviceCode: queryForm.deviceCode || undefined,
      startTime: dateRange.value[0],
      endTime: dateRange.value[1],
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.list
    pagination.total = res.total
  } catch (err) {
  } finally {
    loading.value = false
  }
}

async function fetchChart() {
  if (!dateRange.value || dateRange.value.length < 2) return
  try {
    chartData.value = await getHistoryList({
      parkId: queryForm.parkId,
      deviceCode: queryForm.deviceCode || undefined,
      startTime: dateRange.value[0],
      endTime: dateRange.value[1]
    })
  } catch (err) {
  }
}

function handleQuery() {
  pagination.pageNum = 1
  fetchList()
  fetchChart()
}

function handleReset() {
  queryForm.parkId = undefined
  queryForm.deviceCode = ''
  dateRange.value = defaultRange
  pagination.pageNum = 1
  fetchDevices()
  fetchList()
  fetchChart()
}

onMounted(() => {
  fetchParks()
  fetchDevices()
  fetchList()
  fetchChart()
})
</script>
