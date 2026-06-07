<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">园区管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增园区</el-button>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="园区名称/编码">
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
        <el-table-column prop="parkCode" label="园区编码" width="140" />
        <el-table-column prop="parkName" label="园区名称" width="160" />
        <el-table-column prop="location" label="位置" min-width="200" />
        <el-table-column label="经纬度" width="220">
          <template #default="{ row }">
            {{ row.longitude?.toFixed(6) }}, {{ row.latitude?.toFixed(6) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
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
      width="600px"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="园区编码" prop="parkCode">
          <el-input v-model="formData.parkCode" placeholder="请输入园区编码" />
        </el-form-item>
        <el-form-item label="园区名称" prop="parkName">
          <el-input v-model="formData.parkName" placeholder="请输入园区名称" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="经度" prop="longitude">
          <el-input-number v-model="formData.longitude" :precision="6" :step="0.000001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="纬度" prop="latitude">
          <el-input-number v-model="formData.latitude" :precision="6" :step="0.000001" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
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
import { getParkPage, createPark, updatePark, deletePark, updateParkStatus } from '@/api/park'
import type { ParkInfo, ParkParams } from '@/types/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref<ParkInfo[]>([])

const dialogTitle = computed(() => (editingId.value ? '编辑园区' : '新增园区'))

const formData = reactive<ParkParams>({
  parkCode: '',
  parkName: '',
  location: '',
  longitude: 0,
  latitude: 0,
  description: '',
  status: 1
})

const formRules: FormRules = {
  parkCode: [{ required: true, message: '请输入园区编码', trigger: 'blur' }],
  parkName: [{ required: true, message: '请输入园区名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getParkPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined
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
  pagination.pageNum = 1
  fetchList()
}

function openDialog(row?: ParkInfo) {
  editingId.value = row?.id || null
  if (row) {
    Object.assign(formData, {
      parkCode: row.parkCode,
      parkName: row.parkName,
      location: row.location,
      longitude: row.longitude,
      latitude: row.latitude,
      description: row.description,
      status: row.status
    })
  }
  dialogVisible.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(formData, {
    parkCode: '',
    parkName: '',
    location: '',
    longitude: 0,
    latitude: 0,
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
      await updatePark(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createPark(formData)
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

async function handleStatusChange(row: ParkInfo, status: number) {
  try {
    await ElMessageBox.confirm(
      `确定要${status === 1 ? '启用' : '停用'}园区"${row.parkName}"吗？`,
      '提示',
      { type: 'warning' }
    )
    await updateParkStatus(row.id, status)
    ElMessage.success('操作成功')
    fetchList()
  } catch {
  }
}

async function handleDelete(row: ParkInfo) {
  try {
    await ElMessageBox.confirm(
      `确定要删除园区"${row.parkName}"吗？此操作不可恢复。`,
      '警告',
      { type: 'warning' }
    )
    await deletePark(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
  }
}

onMounted(fetchList)
</script>
