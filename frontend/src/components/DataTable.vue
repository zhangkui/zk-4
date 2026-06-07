<template>
  <div class="data-table-card">
    <div class="table-header" v-if="$slots.header || showPagination">
      <slot name="header" />
    </div>
    <el-table :data="data" v-loading="loading" stripe style="width: 100%" v-bind="$attrs">
      <slot />
    </el-table>
    <div class="pagination-wrap" v-if="showPagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
        :layout="layout"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  data: any[]
  loading?: boolean
  total?: number
  pageNum?: number
  pageSize?: number
  pageSizes?: number[]
  layout?: string
  showPagination?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  total: 0,
  pageNum: 1,
  pageSize: 10,
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  showPagination: true
})

const emit = defineEmits<{
  (e: 'update:pageNum', val: number): void
  (e: 'update:pageSize', val: number): void
  (e: 'change', val: { pageNum: number; pageSize: number }): void
}>()

const currentPage = computed({
  get: () => props.pageNum,
  set: (val) => emit('update:pageNum', val)
})

function handleSizeChange(size: number) {
  emit('update:pageSize', size)
  emit('change', { pageNum: 1, pageSize: size })
}

function handlePageChange(page: number) {
  emit('update:pageNum', page)
  emit('change', { pageNum: page, pageSize: props.pageSize })
}
</script>

<style lang="scss" scoped>
.data-table-card {
  background: #fff;
  padding: 16px;
  border-radius: 4px;

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .pagination-wrap {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
