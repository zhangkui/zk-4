import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ParkInfo } from '@/types/api'

export const useAppStore = defineStore('app', () => {
  const currentPark = ref<ParkInfo | null>(null)
  const sidebarCollapsed = ref(false)

  const currentParkId = computed(() => currentPark.value?.id)

  function setCurrentPark(park: ParkInfo | null) {
    currentPark.value = park
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return {
    currentPark,
    currentParkId,
    sidebarCollapsed,
    setCurrentPark,
    toggleSidebar
  }
})
