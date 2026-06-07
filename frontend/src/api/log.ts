import request from '@/utils/request'
import type { SysLoginLog, SysOperationLog, PageParams, PageResult } from '@/types/api'

export function getLoginLogPage(params: PageParams & { username?: string; startTime?: string; endTime?: string }) {
  return request.get<any, PageResult<SysLoginLog>>('/logs/login', { params })
}

export function getOperationLogPage(params: PageParams & { username?: string; operationType?: string; operationModule?: string; startTime?: string; endTime?: string }) {
  return request.get<any, PageResult<SysOperationLog>>('/logs/operation', { params })
}
