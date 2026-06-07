import request from '@/utils/request'
import type { LoginParams, LoginResult, UserInfo } from '@/types/api'

export function login(params: LoginParams) {
  return request.post<any, LoginResult>('/auth/login', params)
}

export function logout() {
  return request.post<any, void>('/auth/logout')
}

export function getAuthMe() {
  return request.get<any, LoginResult>('/auth/me')
}

export function updatePassword(oldPassword: string, newPassword: string) {
  return request.put<any, void>('/auth/password', { oldPassword, newPassword })
}
