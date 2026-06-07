import request from '@/utils/request'
import type { ParkInfo, ParkParams, PageParams, PageResult } from '@/types/api'

export function getParkPage(params: PageParams & { keyword?: string }) {
  return request.get<any, PageResult<ParkInfo>>('/parks/page', { params })
}

export function getParkList() {
  return request.get<any, ParkInfo[]>('/parks/list')
}

export function getPark(id: number) {
  return request.get<any, ParkInfo>(`/parks/${id}`)
}

export function createPark(params: ParkParams) {
  return request.post<any, ParkInfo>('/parks', params)
}

export function updatePark(id: number, params: ParkParams) {
  return request.put<any, ParkInfo>(`/parks/${id}`, params)
}

export function deletePark(id: number) {
  return request.delete<any, void>(`/parks/${id}`)
}

export function updateParkStatus(id: number, status: number) {
  return request.put<any, void>(`/parks/${id}/status`, null, { params: { status } })
}
