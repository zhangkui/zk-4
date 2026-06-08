<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">告警中心</h2>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="园区">
          <el-select
            v-model="searchForm.parkId"
            placeholder="全部园区"
            clearable
            style="width: 160px"
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
            v-model="searchForm.deviceCode"
            filterable
            placeholder="全部设备"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="device in deviceList"
              :key="device.deviceCode"
              :label="`${device.deviceName} (${device.deviceCode})`"
              :value="device.deviceCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="searchForm.alertLevel" placeholder="全部级别" clearable style="width: 120px">
            <el-option
              v-for="(label, key) in AlertLevelMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="告警状态">
          <el-select v-model="searchForm.alertStatus" placeholder="全部状态" clearable style="width: 120px">
            <el-option
              v-for="(label, key) in AlertStatusMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="告警类型">
          <el-select v-model="searchForm.ruleType" placeholder="全部类型" clearable style="width: 160px">
            <el-option
              v-for="(label, key) in AlertRuleTypeMap"
              :key="key"
              :label="label"
              :value="key"
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
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="告警信息/设备名" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="data-table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="alertNo" label="告警编号" width="180" />
        <el-table-column label="告警级别" width="90">
          <template #default="{ row }">
            <el-tag :type="AlertLevelTagTypeMap[row.alertLevel as keyof typeof AlertLevelTagTypeMap]">
              {{ AlertLevelMap[row.alertLevel as keyof typeof AlertLevelMap] || row.alertLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="告警类型" width="140">
          <template #default="{ row }">
            {{ AlertRuleTypeMap[row.ruleType as keyof typeof AlertRuleTypeMap] || row.ruleType }}
          </template>
        </el-table-column>
        <el-table-column label="告警状态" width="100">
          <template #default="{ row }">
            <el-tag :type="AlertStatusTagTypeMap[row.alertStatus as keyof typeof AlertStatusTagTypeMap]">
              {{ AlertStatusMap[row.alertStatus as keyof typeof AlertStatusMap] || row.alertStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parkName" label="园区" width="120" />
        <el-table-column label="设备" width="160">
          <template #default="{ row }">
            {{ row.deviceName || row.deviceCode }}
          </template>
        </el-table-column>
        <el-table-column prop="deviceCode" label="设备编号" width="140" />
        <el-table-column label="设备类型" width="100">
          <template #default="{ row }">
            {{ DeviceTypeMap[row.deviceType as keyof typeof DeviceTypeMap] || row.deviceType || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="触发值/阈值" width="140">
          <template #default="{ row }">
            <span v-if="row.triggerValue !== undefined && row.thresholdValue !== undefined">
              {{ row.triggerValue }} / {{ row.thresholdValue }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="alertMessage" label="告警信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="triggerTime" label="触发时间" width="180" />
        <el-table-column prop="recoveryTime" label="恢复时间" width="180">
          <template #default="{ row }">
            {{ row.recoveryTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">详情</el-button>
          </template>
        </el-table-column>
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

    <el-dialog v-model="detailVisible" title="告警详情" width="720px">
      <el-descriptions v-if="currentAlert" :column="2" border>
        <el-descriptions-item label="告警编号">{{ currentAlert.alertNo }}</el-descriptions-item>
        <el-descriptions-item label="告警状态">
          <el-tag :type="AlertStatusTagTypeMap[currentAlert.alertStatus as keyof typeof AlertStatusTagTypeMap]">
            {{ AlertStatusMap[currentAlert.alertStatus as keyof typeof AlertStatusMap] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警级别">
          <el-tag :type="AlertLevelTagTypeMap[currentAlert.alertLevel as keyof typeof AlertLevelTagTypeMap]">
            {{ AlertLevelMap[currentAlert.alertLevel as keyof typeof AlertLevelMap] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警类型">
          {{ AlertRuleTypeMap[currentAlert.ruleType as keyof typeof AlertRuleTypeMap] }}
        </el-descriptions-item>
        <el-descriptions-item label="来源规则">{{ currentAlert.ruleName }}（{{ currentAlert.ruleCode }}）</el-descriptions-item>
        <el-descriptions-item label="指标编码">{{ currentAlert.metricCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="园区">{{ currentAlert.parkName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentAlert.deviceName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="设备编号">{{ currentAlert.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="设备类型">
          {{ DeviceTypeMap[currentAlert.deviceType as keyof typeof DeviceTypeMap] || currentAlert.deviceType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="触发值">
          {{ currentAlert.triggerValue !== undefined ? currentAlert.triggerValue : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="阈值">
          {{ currentAlert.thresholdValue !== undefined ? currentAlert.thresholdValue : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="触发时间">{{ currentAlert.triggerTime }}</el-descriptions-item>
        <el-descriptions-item label="恢复时间">{{ currentAlert.recoveryTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="告警信息" :span="2">{{ currentAlert.alertMessage || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentAlert.rawData" label="原始数据" :span="2">
          <pre style="max-height: 200px; overflow: auto; background: #f5f7fa; padding: 10px; margin: 0; font-size: 12px;">{{ formatRawData(currentAlert.rawData) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getAlertPage, getAlert } from '@/api/alert'
import { getParkList } from '@/api/park'
import { getDeviceList } from '@/api/device'
import {
  AlertRuleTypeMap,
  AlertLevelMap,
  AlertLevelTagTypeMap,
  AlertStatusMap,
  AlertStatusTagTypeMap,
  DeviceTypeMap
} from '@/types/api'
import type {
  AlertInfo,
  AlertQueryParams,
  AlertRuleType,
  AlertLevel,
  AlertStatus,
  ParkInfo,
  DeviceInfo
} from '@/types/api'

const loading = ref(false)
const detailVisible = ref(false)
const currentAlert = ref<AlertInfo | null>(null)
const dateRange = ref<string[]>([])

const searchForm = reactive<AlertQueryParams>({
  parkId: undefined,
  deviceCode: undefined,
  alertLevel: undefined,
  alertStatus: undefined,
  ruleType: undefined,
  startTime: undefined,
  endTime: undefined,
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const tableData = ref<AlertInfo[]>([])
const parkList = ref<ParkInfo[]>([])
const deviceList = ref<DeviceInfo[]>([])

async function fetchParks() {
  try {
    parkList.value = await getParkList()
  } catch {}
}

async function fetchDevices() {
  try {
    deviceList.value = await getDeviceList()
  } catch {}
}

async function fetchList() {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      parkId: searchForm.parkId,
      deviceCode: searchForm.deviceCode,
      alertLevel: searchForm.alertLevel,
      alertStatus: searchForm.alertStatus,
      ruleType: searchForm.ruleType
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getAlertPage(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch {
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  fetchList()
}

function handleReset() {
  searchForm.parkId = undefined
  searchForm.deviceCode = undefined
  searchForm.alertLevel = undefined
  searchForm.alertStatus = undefined
  searchForm.ruleType = undefined
  searchForm.keyword = ''
  dateRange.value = []
  pagination.pageNum = 1
  fetchList()
}

async function openDetail(row: AlertInfo) {
  try {
    currentAlert.value = await getAlert(row.id)
    detailVisible.value = true
  } catch {
    currentAlert.value = row
    detailVisible.value = true
  }
}

function formatRawData(data: string) {
  try {
    return JSON.stringify(JSON.parse(data), null, 2)
  } catch {
    return data
  }
}

onMounted(() => {
  fetchParks()
  fetchDevices()
  fetchList()
})
</script>
