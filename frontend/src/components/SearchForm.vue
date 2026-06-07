<template>
  <div class="search-bar">
    <el-form :inline="true" :model="formModel" @submit.prevent>
      <slot />
      <el-form-item v-if="showActions">
        <el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <slot name="extra" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { Search, Refresh } from '@element-plus/icons-vue'

interface Props {
  modelValue: Record<string, any>
  loading?: boolean
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  showActions: true
})

const emit = defineEmits<{
  (e: 'search'): void
  (e: 'reset'): void
  (e: 'update:modelValue', val: Record<string, any>): void
}>()

const formModel = new Proxy(props.modelValue, {
  get(target, key) {
    return target[key as string]
  },
  set(target, key, value) {
    target[key as string] = value
    emit('update:modelValue', target)
    return true
  }
})

function handleSearch() {
  emit('search')
}

function handleReset() {
  emit('reset')
}
</script>

<style lang="scss" scoped>
.search-bar {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}
</style>
