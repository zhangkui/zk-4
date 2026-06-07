import request from '@/utils/request'
import type { DashboardStats, DeviceStatusPieData, TrendData, RealtimeDeviceInfo } from '@/types/api'

export function getMonitorOverview(parkId?: number) {
  return request.get<any, DashboardStats>('/monitor/overview', { params: { parkId } })
}

export function getMonitorTrend(parkId?: number, hours?: number) {
  return request.get<any, TrendData>('/monitor/trend', { params: { parkId, hours } })
}

export function getMonitorDeviceStatus(parkId?: number) {
  return request.get<any, DeviceStatusPieData[]>('/monitor/device-status', { params: { parkId } })
}

export function getMonitorDeviceCurve(deviceCode: string, hours?: number) {
  return request.get<any, TrendData>(`/monitor/device-curve/${deviceCode}`, { params: { hours } })
}

export function getMonitorRealtimeDevices(parkId?: number) {
  return request.get<any, RealtimeDeviceInfo[]>('/monitor/realtime-devices', { params: { parkId } })
}
