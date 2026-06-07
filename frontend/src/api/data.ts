import request from '@/utils/request'
import type { DeviceRealtimeData } from '@/types/api'

export function getRealtimeData(parkId?: number) {
  return request.get<any, DeviceRealtimeData[]>('/data/realtime', { params: { parkId } })
}

export function getDataByDevice(deviceCode: string) {
  return request.get<any, DeviceRealtimeData>(`/data/device/${deviceCode}`)
}
