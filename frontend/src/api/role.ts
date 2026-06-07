import request from '@/utils/request'
import type { RoleInfo, RoleParams, PermissionInfo, PageParams, PageResult } from '@/types/api'

export function getRolePage(params: PageParams & { keyword?: string }) {
  return request.get<any, PageResult<RoleInfo>>('/roles/page', { params })
}

export function getRoleList() {
  return request.get<any, RoleInfo[]>('/roles/list')
}

export function getRole(id: number) {
  return request.get<any, RoleInfo>(`/roles/${id}`)
}

export function createRole(params: RoleParams) {
  return request.post<any, RoleInfo>('/roles', params)
}

export function updateRole(id: number, params: RoleParams) {
  return request.put<any, RoleInfo>(`/roles/${id}`, params)
}

export function deleteRole(id: number) {
  return request.delete<any, void>(`/roles/${id}`)
}

export function getRolePermissions(id: number) {
  return request.get<any, PermissionInfo[]>(`/roles/${id}/permissions`)
}

export function setRolePermissions(id: number, permissionIds: number[]) {
  return request.post<any, void>(`/roles/${id}/permissions`, permissionIds)
}
