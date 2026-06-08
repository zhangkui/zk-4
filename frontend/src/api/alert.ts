import request from '@/utils/request'
import type {
  AlertRuleInfo,
  AlertRuleParams,
  AlertInfo,
  AlertRuleType,
  AlertLevel,
  PageParams,
  PageResult,
  AlertQueryParams
} from '@/types/api'

export function getAlertRuleConstants() {
  return request.get<any, any>('/alerts/rules/constants')
}

export function getAlertRulePage(
  params: PageParams & {
    ruleType?: AlertRuleType
    alertLevel?: AlertLevel
    status?: number
    keyword?: string
  }
) {
  return request.get<any, PageResult<AlertRuleInfo>>('/alerts/rules/page', { params })
}

export function getAlertRuleList(params?: { ruleType?: AlertRuleType; status?: number }) {
  return request.get<any, AlertRuleInfo[]>('/alerts/rules/list', { params })
}

export function getAlertRule(id: number) {
  return request.get<any, AlertRuleInfo>(`/alerts/rules/${id}`)
}

export function createAlertRule(params: AlertRuleParams) {
  return request.post<any, AlertRuleInfo>('/alerts/rules', params)
}

export function updateAlertRule(id: number, params: AlertRuleParams) {
  return request.put<any, AlertRuleInfo>(`/alerts/rules/${id}`, params)
}

export function deleteAlertRule(id: number) {
  return request.delete<any, void>(`/alerts/rules/${id}`)
}

export function updateAlertRuleStatus(id: number, status: number) {
  return request.put<any, void>(`/alerts/rules/${id}/status`, null, { params: { status } })
}

export function getAlert(id: number) {
  return request.get<any, AlertInfo>(`/alerts/${id}`)
}

export function getAlertPage(params: PageParams & AlertQueryParams) {
  return request.get<any, PageResult<AlertInfo>>('/alerts/page', { params })
}

export function processAlertRules() {
  return request.post<any, void>('/alerts/process')
}
