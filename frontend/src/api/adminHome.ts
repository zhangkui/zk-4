import request from '@/utils/request'
import type { AdminHomeStats } from '@/types/api'

export function getAdminHomeStats() {
  return request.get<any, AdminHomeStats>('/admin-home/stats')
}
