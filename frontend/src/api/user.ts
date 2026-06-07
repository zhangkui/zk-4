import request from '@/utils/request'
import type { UserInfo, UserParams, PageParams, PageResult } from '@/types/api'

export function getUserPage(params: PageParams & { keyword?: string }) {
  return request.get<any, PageResult<UserInfo>>('/users/page', { params })
}

export function getUser(id: number) {
  return request.get<any, UserInfo>(`/users/${id}`)
}

export function createUser(params: UserParams) {
  return request.post<any, UserInfo>('/users', params)
}

export function updateUser(id: number, params: Omit<UserParams, 'id' | 'username' | 'password'>) {
  return request.put<any, UserInfo>(`/users/${id}`, params)
}

export function deleteUser(id: number) {
  return request.delete<any, void>(`/users/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return request.put<any, void>(`/users/${id}/status`, null, { params: { status } })
}

export function resetUserPassword(id: number, newPassword: string) {
  return request.put<any, void>(`/users/${id}/password/reset`, null, { params: { newPassword } })
}

export function updateUserPassword(id: number, oldPassword: string, newPassword: string) {
  return request.put<any, void>(`/users/${id}/password`, null, { params: { oldPassword, newPassword } })
}
