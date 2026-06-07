import request from '@/utils/request'
import type { SimulatorStatus, SimulatorDeviceStatus } from '@/types/api'

export function getSimulatorOverview() {
  return request.get<any, SimulatorStatus>('/simulator/overview')
}

export function startSimulatorAll(parkId?: number) {
  return request.post<any, void>('/simulator/start-all', null, { params: { parkId } })
}

export function stopSimulatorAll(parkId?: number) {
  return request.post<any, void>('/simulator/stop-all', null, { params: { parkId } })
}

export function getSimulatorStatus(parkId?: number) {
  return request.get<any, SimulatorDeviceStatus[]>('/simulator/status', { params: { parkId } })
}
