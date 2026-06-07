<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">角色管理</h2>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增角色</el-button>
    </div>

    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" @submit.prevent>
        <el-form-item label="角色名称/编码">
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
        <el-table-column prop="roleCode" label="角色编码" width="180" />
        <el-table-column prop="roleName" label="角色名称" width="180" />
        <el-table-column prop="description" label="描述" min-width="260" show-overflow-tooltip />
        <el-table-column label="权限" min-width="300">
          <template #default="{ row }">
            <el-tag
              v-for="perm in row.permissions?.slice(0, 5)"
              :key="perm.id"
              style="margin-right: 4px; margin-bottom: 4px"
              size="small"
            >
              {{ perm.permissionName }}
            </el-tag>
            <el-tag
              v-if="row.permissions && row.permissions.length > 5"
              size="small"
              type="info"
            >
              +{{ row.permissions.length - 5 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDialog(row)">编辑</el-button>
            <el-button type="primary" link size="small" @click="openPermissionDialog(row)">分配权限</el-button>
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
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="2"
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

    <el-dialog
      v-model="permDialogVisible"
      title="分配权限"
      width="500px"
      @closed="resetPermForm"
    >
      <el-tree
        ref="treeRef"
        :data="permTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedPermIds"
        :props="{ label: 'permissionName', children: 'children' }"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handlePermSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type ElTree } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getRolePage, createRole, updateRole, deleteRole, getRolePermissions, setRolePermissions } from '@/api/role'
import type { RoleInfo, PermissionInfo, RoleParams } from '@/types/api'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const treeRef = ref<InstanceType<typeof ElTree>>()
const editingId = ref<number | null>(null)
const tableData = ref<RoleInfo[]>([])
const permTree = ref<PermissionInfo[]>([])
const checkedPermIds = ref<number[]>([])

const searchForm = reactive({
  keyword: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const dialogTitle = computed(() => (editingId.value ? '编辑角色' : '新增角色'))

const formData = reactive<RoleParams>({
  roleCode: '',
  roleName: '',
  description: '',
  status: 1
})

const formRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getRolePage({
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

function openDialog(row?: RoleInfo) {
  editingId.value = row?.id || null
  if (row) {
    Object.assign(formData, {
      roleCode: row.roleCode,
      roleName: row.roleName,
      description: row.description,
      status: row.status
    })
  }
  dialogVisible.value = true
}

function resetForm() {
  editingId.value = null
  Object.assign(formData, {
    roleCode: '',
    roleName: '',
    description: '',
    status: 1
  })
  formRef.value?.resetFields()
}

async function openPermissionDialog(row: RoleInfo) {
  editingId.value = row.id
  try {
    const perms = await getRolePermissions(row.id)
    checkedPermIds.value = perms.map(p => p.id)
    permTree.value = buildTree(perms)
  } catch (err) {
  }
  permDialogVisible.value = true
}

function resetPermForm() {
  editingId.value = null
  checkedPermIds.value = []
}

function buildTree(list: PermissionInfo[]): any[] {
  const types = [...new Set(list.map(p => p.permissionType))]
  return types.map(t => ({
    id: `group_${t}`,
    permissionName: t,
    permissionType: t,
    children: list.filter(p => p.permissionType === t)
  }))
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (editingId.value) {
      await updateRole(editingId.value, formData)
      ElMessage.success('更新成功')
    } else {
      await createRole(formData)
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

async function handlePermSubmit() {
  if (!treeRef.value || !editingId.value) return
  try {
    const checked = treeRef.value.getCheckedKeys(true) as number[]
    const ids = checked.filter(k => typeof k === 'number')
    if (ids.length === 0) {
      ElMessage.warning('请至少选择一个权限')
      return
    }
    submitLoading.value = true
    await setRolePermissions(editingId.value, ids)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
    fetchList()
  } catch (err: any) {
    if (err?.message) ElMessage.error(err.message)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row: RoleInfo) {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色"${row.roleName}"吗？此操作不可恢复。`,
      '警告',
      { type: 'warning' }
    )
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch {
  }
}

onMounted(fetchList)
</script>
