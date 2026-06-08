<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input
            v-model="searchForm.operationType"
            placeholder="请输入"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-input
            v-model="searchForm.operationModule"
            placeholder="请输入"
            clearable
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 340px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="data-table-card">
      <el-table :data="operationLogs" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="operationModule" label="模块" width="140" />
        <el-table-column prop="operationType" label="操作类型" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方法" width="100" />
        <el-table-column prop="requestPath" label="请求路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operationIp" label="IP地址" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 'success' ? 'success' : 'danger'" size="small">
              {{ row.result === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationAt" label="时间" width="200" />
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
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import { getOperationLogPage } from '@/api/log'
import type { SysOperationLog } from '@/types/api'

const loading = ref(false)
const operationLogs = ref<SysOperationLog[]>([])

const defaultRange = [
  dayjs().subtract(7, 'day').format('YYYY-MM-DD HH:mm:ss'),
  dayjs().format('YYYY-MM-DD HH:mm:ss')
]
const dateRange = ref<string[]>(defaultRange)

const searchForm = reactive({
  username: '',
  operationType: '',
  operationModule: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getOperationLogPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      username: searchForm.username || undefined,
      operationType: searchForm.operationType || undefined,
      operationModule: searchForm.operationModule || undefined,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1]
    })
    operationLogs.value = res.list
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
  searchForm.username = ''
  searchForm.operationType = ''
  searchForm.operationModule = ''
  dateRange.value = defaultRange
  pagination.pageNum = 1
  fetchList()
}

onMounted(fetchList)
</script>
