import request from '@/utils/request'
import type { PermissionInfo } from '@/types/api'

export function getPermissionTree() {
  return request.get<any, PermissionInfo[]>('/permissions/list')
}

export function getPermissionFlatList() {
  return request.get<any, PermissionInfo[]>('/permissions/flat')
}
