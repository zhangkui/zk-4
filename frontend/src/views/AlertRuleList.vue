<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">告警规则</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增规则</el-button>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="规则类型">
          <el-select v-model="searchForm.ruleType" placeholder="全部类型" clearable style="width: 180px">
            <el-option
              v-for="(label, key) in AlertRuleTypeMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="searchForm.alertLevel" placeholder="全部级别" clearable style="width: 140px">
            <el-option
              v-for="(label, key) in AlertLevelMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称/编码">
          <el-input v-model="searchForm.keyword" placeholder="请输入" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="data-table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="ruleCode" label="规则编码" width="140" />
        <el-table-column prop="ruleName" label="规则名称" width="160" />
        <el-table-column label="规则类型" width="140">
          <template #default="{ row }">
            {{ AlertRuleTypeMap[row.ruleType as keyof typeof AlertRuleTypeMap] || row.ruleType }}
          </template>
        </el-table-column>
        <el-table-column label="告警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="AlertLevelTagTypeMap[row.alertLevel as keyof typeof AlertLevelTagTypeMap]">
              {{ AlertLevelMap[row.alertLevel as keyof typeof AlertLevelMap] || row.alertLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生效范围" width="180">
          <template #default="{ row }">
            <span>{{ AlertScopeTypeMap[row.scopeType as keyof typeof AlertScopeTypeMap] || row.scopeType }}</span>
            <span v-if="row.parkName" class="sub-text">（{{ row.parkName }}）</span>
            <span v-else-if="row.deviceTypeName" class="sub-text">（{{ row.deviceTypeName }}）</span>
            <span v-else-if="row.deviceCode" class="sub-text">（{{ row.deviceCode }}）</span>
          </template>
        </el-table-column>
        <el-table-column label="阈值条件" min-width="180">
          <template #default="{ row }">
            <span v-if="row.thresholdOperator && row.thresholdValue !== undefined">
              {{ AlertOperatorMap[row.thresholdOperator as keyof typeof AlertOperatorMap] }}
              {{ row.thresholdValue }}
              <span v-if="row.thresholdOperator === 'BETWEEN' || row.thresholdOperator === 'NOT_BETWEEN'">
                ~ {{ row.thresholdValue2 }}
              </span>
            </span>
            <span v-else-if="row.durationSeconds > 0">
              持续 {{ row.durationSeconds }} 秒
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="持续时长" width="120">
          <template #default="{ row }">
            {{ row.durationSeconds > 0 ? row.durationSeconds + ' 秒' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1"
              type="warning"
              link
              size="small"
              @click="handleStatusChange(row, 0)"
            >
              停用
            </el-button>
            <el-button
              v-else
              type="success"
              link
              size="small"
              @click="handleStatusChange(row, 1)"
            >
              启用
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="720px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="规则编码" prop="ruleCode">
          <el-input v-model="formData.ruleCode" placeholder="请输入规则编码" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="formData.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="formData.ruleType" placeholder="请选择" style="width: 100%" @change="handleRuleTypeChange">
            <el-option
              v-for="(label, key) in AlertRuleTypeMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="告警级别" prop="alertLevel">
          <el-select v-model="formData.alertLevel" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="(label, key) in AlertLevelMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>

        <el-divider>生效范围配置</el-divider>

        <el-form-item label="生效范围" prop="scopeType">
          <el-radio-group v-model="formData.scopeType">
            <el-radio value="ALL">全部设备</el-radio>
            <el-radio value="PARK">指定园区</el-radio>
            <el-radio value="DEVICE_TYPE">指定设备类型</el-radio>
            <el-radio value="DEVICE">指定设备</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.scopeType === 'PARK'" label="园区" prop="parkId">
          <el-select v-model="formData.parkId" placeholder="请选择园区" style="width: 100%">
            <el-option
              v-for="park in parkList"
              :key="park.id"
              :label="park.parkName"
              :value="park.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.scopeType === 'DEVICE_TYPE'" label="设备类型" prop="deviceType">
          <el-select v-model="formData.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option
              v-for="(label, key) in DeviceTypeMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.scopeType === 'DEVICE'" label="设备" prop="deviceCode">
          <el-select v-model="formData.deviceCode" filterable placeholder="请选择设备" style="width: 100%">
            <el-option
              v-for="device in deviceList"
              :key="device.deviceCode"
              :label="`${device.deviceName} (${device.deviceCode})`"
              :value="device.deviceCode"
            />
          </el-select>
        </el-form-item>

        <el-divider>触发条件配置</el-divider>

        <el-form-item v-if="showThresholdConfig" label="阈值操作符" prop="thresholdOperator">
          <el-select v-model="formData.thresholdOperator" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="(label, key) in AlertOperatorMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="showThresholdConfig" label="阈值1" prop="thresholdValue">
          <el-input-number v-model="formData.thresholdValue" :precision="3" style="width: 100%" />
        </el-form-item>
        <el-form-item
          v-if="showThresholdConfig && (formData.thresholdOperator === 'BETWEEN' || formData.thresholdOperator === 'NOT_BETWEEN')"
          label="阈值2"
          prop="thresholdValue2"
        >
          <el-input-number v-model="formData.thresholdValue2" :precision="3" style="width: 100%" />
        </el-form-item>
        <el-form-item label="持续时长(秒)" prop="durationSeconds">
          <el-input-number v-model="formData.durationSeconds" :min="0" style="width: 100%" />
        </el-form-item>

        <el-divider>恢复条件配置</el-divider>

        <el-form-item label="恢复操作符" prop="recoveryOperator">
          <el-select v-model="formData.recoveryOperator" placeholder="请选择（可选）" clearable style="width: 100%">
            <el-option
              v-for="(label, key) in AlertOperatorMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.recoveryOperator" label="恢复阈值1" prop="recoveryValue">
          <el-input-number v-model="formData.recoveryValue" :precision="3" style="width: 100%" />
        </el-form-item>
        <el-form-item
          v-if="formData.recoveryOperator && (formData.recoveryOperator === 'BETWEEN' || formData.recoveryOperator === 'NOT_BETWEEN')"
          label="恢复阈值2"
          prop="recoveryValue2"
        >
          <el-input-number v-model="formData.recoveryValue2" :precision="3" style="width: 100%" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import {
  getAlertRulePage,
  createAlertRule,
  updateAlertRule,
  deleteAlertRule,
  updateAlertRuleStatus
} from '@/api/alert'
import { getParkList } from '@/api/park'
import { getDeviceList } from '@/api/device'
import {
  AlertRuleTypeMap,
  AlertLevelMap,
  AlertLevelTagTypeMap,
  AlertScopeTypeMap,
  AlertOperatorMap,
  DeviceTypeMap
} from '@/types/api'
import type {
  AlertRuleInfo,
  AlertRuleParams,
  AlertRuleType,
  AlertLevel,
  AlertScopeType,
  ParkInfo,
  DeviceInfo
} from '@/types/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const searchForm = reactive({
  keyword: '',
  ruleType: undefined as AlertRuleType | undefined,
  alertLevel: undefined as AlertLevel | undefined,
  status: undefined as number | undefined
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<AlertRuleInfo[]>([])
const parkList = ref<ParkInfo[]>([])
const deviceList = ref<DeviceInfo[]>([])

const dialogTitle = computed(() => (editingId.value ? '编辑告警规则' : '新增告警规则'))

const showThresholdConfig = computed(() => {
  const t = formData.ruleType
  return t === 'POWER_ABNORMAL' || t === 'VOLTAGE_ABNORMAL' || t === 'ESS_SOC_HIGH' || t === 'ESS_SOC_LOW'
})

const formData = reactive<AlertRuleParams>({
  ruleCode: '',
  ruleName: '',
  ruleType: 'DEVICE_OFFLINE',
  alertLevel: 'WARNING',
  scopeType: 'ALL',
  durationSeconds: 300,
  status: 1
})

const formRules: FormRules = {
  ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }],
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  alertLevel: [{ required: true, message: '请选择告警级别', trigger: 'change' }],
  scopeType: [{ required: true, message: '请选择生效范围', trigger: 'change' }],
  durationSeconds: [{ required: true, message: '请输入持续时长', trigger: 'blur' }]
}

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
    const res = await getAlertRulePage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      ruleType: searchForm.ruleType,
      alertLevel: searchForm.alertLevel,
      status: searchForm.status
    })
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
  searchForm.keyword = ''
  searchForm.ruleType = undefined
  searchForm.alertLevel = undefined
  searchForm.status = undefined
  pagination.pageNum = 1
  fetchList()
}

function handleRuleTypeChange(type: AlertRuleType) {
  if (type === 'DEVICE_OFFLINE') {
    formData.durationSeconds = 300
    formData.thresholdOperator = undefined
    formData.thresholdValue = undefined
  } else if (type === 'DATA_MISSING') {
    formData.durationSeconds = 60
    formData.thresholdOperator = undefined
    formData.thresholdValue = undefined
  } else if (type === 'POWER_ABNORMAL') {
    formData.thresholdOperator = 'GTE'
    formData.thresholdValue = 1000
    formData.durationSeconds = 60
    formData.metricCode = 'power'
  } else if (type === 'VOLTAGE_ABNORMAL') {
    formData.thresholdOperator = 'NOT_BETWEEN'
    formData.thresholdValue = 200
    formData.thresholdValue2 = 260
    formData.durationSeconds = 30
    formData.metricCode = 'voltage'
  } else if (type === 'ESS_SOC_HIGH') {
    formData.thresholdOperator = 'GTE'
    formData.thresholdValue = 95
    formData.durationSeconds = 60
    formData.metricCode = 'soc'
  } else if (type === 'ESS_SOC_LOW') {
    formData.thresholdOperator = 'LTE'
    formData.thresholdValue = 10
    formData.durationSeconds = 60
    formData.metricCode = 'soc'
  }
}

function openDialog(row?: AlertRuleInfo) {
  editingId.value = row?.id || null
  if (row) {
    Object.assign(formData, {
      ruleCode: row.ruleCode,
      ruleName: row.ruleName,
      ruleType: row.ruleType,
      alertLevel: row.alertLevel,
      scopeType: row.scopeType,
      scopeValue: row.scopeValue,
      parkId: row.parkId,
      deviceType: row.deviceType,
      deviceCode: row.deviceCode,
      thresholdOperator: row.thresholdOperator,
      thresholdValue: row.thresholdValue,
      thresholdValue2: row.thresholdValue2,
      metricCode: row.metricCode,
      durationSeconds: row.durationSeconds,
      recoveryOperator: row.recoveryOperator,
      recoveryValue: row.recoveryValue,
      recoveryValue2: row.recoveryValue2,
      description: row.description || '',
      status: row.status
    })
  }
  dialogVisible.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(formData, {
    ruleCode: '',
    ruleName: '',
    ruleType: 'DEVICE_OFFLINE',
    alertLevel: 'WARNING',
    scopeType: 'ALL' as AlertScopeType,
    scopeValue: undefined,
    parkId: undefined,
    deviceType: undefined,
    deviceCode: undefined,
    thresholdOperator: undefined,
    thresholdValue: undefined,
    thresholdValue2: undefined,
    metricCode: undefined,
    durationSeconds: 300,
    recoveryOperator: undefined,
    recoveryValue: undefined,
    recoveryValue2: undefined,
    description: '',
    status: 1
  })
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (editingId.value) {
      await updateAlertRule(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createAlertRule(formData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (err: any) {
    if (err?.message) ElMessage.error(err.message)
  } finally {
    submitLoading.value = false
  }
}

async function handleStatusChange(row: AlertRuleInfo, status: number) {
  try {
    await ElMessageBox.confirm(
      `确定要${status === 1 ? '启用' : '停用'}规则"${row.ruleName}"吗？`,
      '提示',
      { type: 'warning' }
    )
    await updateAlertRuleStatus(row.id, status)
    ElMessage.success('操作成功')
    fetchList()
  } catch {}
}

async function handleDelete(row: AlertRuleInfo) {
  try {
    await ElMessageBox.confirm(
      `确定要删除规则"${row.ruleName}"吗？此操作不可恢复。`,
      '警告',
      { type: 'warning' }
    )
    await deleteAlertRule(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {}
}

onMounted(() => {
  fetchParks()
  fetchDevices()
  fetchList()
})
</script>

<style lang="scss" scoped>
.sub-text {
  color: #909399;
  font-size: 12px;
}
</style>
