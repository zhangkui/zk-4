<template>
  <div class="stat-card" :class="gradientClass">
    <div class="card-label">{{ label }}</div>
    <div class="card-value">
      {{ displayValue }}
      <span class="unit" v-if="unit">{{ unit }}</span>
    </div>
    <el-icon class="card-icon" v-if="icon">
      <component :is="icon" />
    </el-icon>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Component } from 'vue'

interface Props {
  label: string
  value: number | string
  unit?: string
  precision?: number
  gradient?: 'blue' | 'green' | 'orange' | 'purple' | 'red' | 'cyan'
  icon?: Component
}

const props = withDefaults(defineProps<Props>(), {
  precision: 2,
  gradient: 'blue'
})

const gradientClass = computed(() => `gradient-${props.gradient}`)

const displayValue = computed(() => {
  if (typeof props.value === 'string') return props.value
  return props.value.toFixed(props.precision)
})
</script>

<style lang="scss" scoped>
.stat-card {
  background: rgba(19, 23, 64, 0.8);
  border: 1px solid #1f2a5a;
  border-radius: 8px;
  padding: 20px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
  }

  &.gradient-blue::before { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
  &.gradient-green::before { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
  &.gradient-orange::before { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
  &.gradient-purple::before { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
  &.gradient-red::before { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
  &.gradient-cyan::before { background: linear-gradient(135deg, #30cfd0 0%, #330867 100%); }

  .card-label {
    font-size: 14px;
    color: #8492c4;
    margin-bottom: 12px;
  }

  .card-value {
    font-size: 32px;
    font-weight: 700;
    color: #fff;

    .unit {
      font-size: 14px;
      font-weight: 400;
      color: #8492c4;
      margin-left: 4px;
    }
  }

  .card-icon {
    position: absolute;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
    font-size: 48px;
    opacity: 0.15;
    color: #fff;
  }
}
</style>
