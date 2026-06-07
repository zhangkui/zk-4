import request from '@/utils/request'
import type { HistoryDataQuery, HistoryDataVO, PageResult } from '@/types/api'

export function getHistoryPage(params: HistoryDataQuery) {
  return request.get<any, PageResult<HistoryDataVO>>('/history/page', { params })
}

export function getHistoryList(params: Omit<HistoryDataQuery, 'pageNum' | 'pageSize'>) {
  return request.get<any, HistoryDataVO[]>('/history/list', { params })
}
