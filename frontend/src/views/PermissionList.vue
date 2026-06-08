<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">权限管理</h2>
    </div>

    <div class="data-table-card">
      <el-table :data="permissionTree" v-loading="loading" stripe row-key="id" style="width: 100%">
        <el-table-column prop="permissionName" label="权限名称" min-width="180" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="180" />
        <el-table-column label="权限类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.permissionType === 1 ? 'primary' : 'success'">
              {{ row.permissionType === 1 ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="120" />
        <el-table-column label="API信息" min-width="200">
          <template #default="{ row }">
            <span v-if="row.apiMethod && row.apiPath">
              <el-tag size="small" type="info">{{ row.apiMethod }}</el-tag>
              <span style="margin-left: 6px">{{ row.apiPath }}</span>
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPermissionTree } from '@/api/permission'
import type { PermissionInfo } from '@/types/api'

const loading = ref(false)
const permissionTree = ref<PermissionInfo[]>([])

async function fetchList() {
  loading.value = true
  try {
    permissionTree.value = await getPermissionTree()
  } catch (err) {
  } finally {
    loading.value = false
  }
}

onMounted(fetchList)
</script>
