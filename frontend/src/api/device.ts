import request from '@/utils/request'
import type { DeviceInfo, DeviceParams, PageParams, PageResult, DeviceType } from '@/types/api'

export function getDevicePage(params: PageParams & { keyword?: string; parkId?: number; deviceType?: DeviceType }) {
  return request.get<any, PageResult<DeviceInfo>>('/devices/page', { params })
}

export function getDeviceList(params?: { parkId?: number; deviceType?: DeviceType }) {
  return request.get<any, DeviceInfo[]>('/devices/list', { params })
}

export function getDevice(id: number) {
  return request.get<any, DeviceInfo>(`/devices/${id}`)
}

export function getDeviceByCode(deviceCode: string) {
  return request.get<any, DeviceInfo>(`/devices/code/${deviceCode}`)
}

export function getDeviceRealtimeByCode(deviceCode: string) {
  return request.get<any, DeviceInfo>(`/devices/code/${deviceCode}/realtime`)
}

export function createDevice(params: DeviceParams) {
  return request.post<any, DeviceInfo>('/devices', params)
}

export function updateDevice(id: number, params: DeviceParams) {
  return request.put<any, DeviceInfo>(`/devices/${id}`, params)
}

export function deleteDevice(id: number) {
  return request.delete<any, void>(`/devices/${id}`)
}

export function updateDeviceStatus(id: number, status: number) {
  return request.put<any, void>(`/devices/${id}/status`, null, { params: { status } })
}
