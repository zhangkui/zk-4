<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">设备台账</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增设备</el-button>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="园区">
          <el-select
            v-model="searchForm.parkId"
            placeholder="全部园区"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="park in parkList"
              :key="park.id"
              :label="park.parkName"
              :value="park.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select
            v-model="searchForm.deviceType"
            placeholder="全部类型"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="(label, key) in DeviceTypeMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称/编号">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="data-table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="deviceCode" label="设备编号" width="140" />
        <el-table-column prop="deviceName" label="设备名称" width="160" />
        <el-table-column label="设备类型" width="100">
          <template #default="{ row }">
            {{ DeviceTypeMap[row.deviceType as keyof typeof DeviceTypeMap] || row.deviceType }}
          </template>
        </el-table-column>
        <el-table-column prop="parkName" label="所属园区" width="140" />
        <el-table-column prop="installLocation" label="安装位置" width="140" />
        <el-table-column label="额定功率(kW)" width="120">
          <template #default="{ row }">
            {{ row.ratedPower }}
          </template>
        </el-table-column>
        <el-table-column prop="accessMethod" label="接入方式" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastReportAt" label="最后上报" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goToDetail(row.deviceCode)">详情</el-button>
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
      width="640px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="设备编号" prop="deviceCode">
          <el-input v-model="formData.deviceCode" placeholder="请输入设备编号" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="formData.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="formData.deviceType" placeholder="请选择" style="width: 100%">
            <el-option
              v-for="(label, key) in DeviceTypeMap"
              :key="key"
              :label="label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属园区" prop="parkId">
          <el-select v-model="formData.parkId" placeholder="请选择园区" style="width: 100%">
            <el-option
              v-for="park in parkList"
              :key="park.id"
              :label="park.parkName"
              :value="park.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置" prop="installLocation">
          <el-input v-model="formData.installLocation" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="额定功率(kW)" prop="ratedPower">
          <el-input-number v-model="formData.ratedPower" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="接入方式" prop="accessMethod">
          <el-select v-model="formData.accessMethod" placeholder="请选择" style="width: 100%">
            <el-option label="Modbus RTU" value="MODBUS_RTU" />
            <el-option label="Modbus TCP" value="MODBUS_TCP" />
            <el-option label="MQTT" value="MQTT" />
            <el-option label="OPC UA" value="OPC_UA" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getDevicePage, createDevice, updateDevice, deleteDevice, updateDeviceStatus } from '@/api/device'
import { getParkList } from '@/api/park'
import { DeviceTypeMap } from '@/types/api'
import type { DeviceInfo, DeviceParams, ParkInfo, DeviceType } from '@/types/api'

const router = useRouter()
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const searchForm = reactive({
  keyword: '',
  parkId: undefined as number | undefined,
  deviceType: undefined as DeviceType | undefined
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<DeviceInfo[]>([])
const parkList = ref<ParkInfo[]>([])

const dialogTitle = computed(() => (editingId.value ? '编辑设备' : '新增设备'))

const formData = reactive<DeviceParams>({
  deviceCode: '',
  deviceName: '',
  deviceType: 'PV',
  parkId: 0,
  installLocation: '',
  ratedPower: 0,
  accessMethod: 'MODBUS_TCP',
  status: 1,
  remark: ''
})

const formRules: FormRules = {
  deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  parkId: [{ required: true, message: '请选择园区', trigger: 'change' }],
  installLocation: [{ required: true, message: '请输入安装位置', trigger: 'blur' }],
  ratedPower: [{ required: true, message: '请输入额定功率', trigger: 'blur' }],
  accessMethod: [{ required: true, message: '请选择接入方式', trigger: 'change' }]
}

async function fetchParks() {
  try {
    parkList.value = await getParkList()
  } catch (err) {
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getDevicePage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      parkId: searchForm.parkId,
      deviceType: searchForm.deviceType
    })
    tableData.value = res.list
    pagination.total = res.total
  } catch (err) {
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
  searchForm.parkId = undefined
  searchForm.deviceType = undefined
  pagination.pageNum = 1
  fetchList()
}

function goToDetail(deviceCode: string) {
  router.push(`/device/${deviceCode}`)
}

function openDialog(row?: DeviceInfo) {
  editingId.value = row?.id || null
  if (row) {
    Object.assign(formData, {
      deviceCode: row.deviceCode,
      deviceName: row.deviceName,
      deviceType: row.deviceType,
      parkId: row.parkId,
      installLocation: row.installLocation,
      ratedPower: row.ratedPower,
      accessMethod: row.accessMethod,
      status: row.status,
      remark: row.remark || ''
    })
  }
  dialogVisible.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(formData, {
    deviceCode: '',
    deviceName: '',
    deviceType: 'PV',
    parkId: 0,
    installLocation: '',
    ratedPower: 0,
    accessMethod: 'MODBUS_TCP',
    status: 1,
    remark: ''
  })
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (editingId.value) {
      await updateDevice(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createDevice(formData)
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

async function handleStatusChange(row: DeviceInfo, status: number) {
  try {
    await ElMessageBox.confirm(
      `确定要${status === 1 ? '启用' : '停用'}设备"${row.deviceName}"吗？`,
      '提示',
      { type: 'warning' }
    )
    await updateDeviceStatus(row.id, status)
    ElMessage.success('操作成功')
    fetchList()
  } catch {
  }
}

async function handleDelete(row: DeviceInfo) {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备"${row.deviceName}"吗？此操作不可恢复。`,
      '警告',
      { type: 'warning' }
    )
    await deleteDevice(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
  }
}

onMounted(() => {
  fetchParks()
  fetchList()
})
</script>
