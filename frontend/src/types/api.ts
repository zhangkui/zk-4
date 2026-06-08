export interface Result<T = any> {
  code: number
  message: string
  data: T
}

export interface PageParams {
  pageNum: number
  pageSize: number
}

export interface PageResult<T = any> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  realName: string
  roles: string[]
  permissions: string[]
  loginTime: string
}

export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  status: number
  roles: RoleInfo[]
  permissions: string[]
}

export interface RoleInfo {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: number
  permissions?: PermissionInfo[]
}

export interface PermissionInfo {
  id: number
  permissionCode: string
  permissionName: string
  permissionType: string
  parentId: number
  path: string
  icon: string
}

export interface ParkInfo {
  id: number
  parkCode: string
  parkName: string
  location: string
  longitude: number
  latitude: number
  description: string
  status: number
}

export interface ParkParams {
  id?: number
  parkCode: string
  parkName: string
  location: string
  longitude?: number
  latitude?: number
  description?: string
  status: number
}

export type DeviceType = 'PV' | 'WIND' | 'ESS' | 'PCS' | 'CHARGING' | 'METER' | 'LOAD'

export const DeviceTypeMap: Record<DeviceType, string> = {
  PV: '光伏',
  WIND: '风电',
  ESS: '储能',
  PCS: '变流器',
  CHARGING: '充电桩',
  METER: '电表',
  LOAD: '负荷'
}

export interface DeviceInfo {
  id: number
  deviceCode: string
  deviceName: string
  deviceType: DeviceType
  parkId: number
  parkName: string
  installLocation: string
  ratedPower: number
  accessMethod: string
  status: number
  remark: string
  lastReportAt?: string
  isOnline?: boolean
  currentPower?: number
  metrics?: Record<string, any>
}

export interface DeviceParams {
  id?: number
  deviceCode: string
  deviceName: string
  deviceType: DeviceType
  parkId: number
  installLocation: string
  ratedPower: number
  accessMethod: string
  status: number
  remark?: string
}

export interface TrendPoint {
  ts: string
  value: number
}

export interface TrendData {
  generation: TrendPoint[]
  load: TrendPoint[]
  ess: TrendPoint[]
  pv: TrendPoint[]
  wind: TrendPoint[]
}

export interface DashboardStats {
  parkId: number
  parkName: string
  totalGenerationPower: number
  totalLoadPower: number
  essPower: number
  essSoc: number
  chargingOnlineCount: number
  chargingTotalCount: number
  onlineDeviceCount: number
  totalDeviceCount: number
  onlineRate: number
  pvPower: number
  windPower: number
  gridPower: number
  deviceStatusStats: Record<string, number>
}

export interface DeviceStatusPieData {
  status: string
  statusName: string
  count: number
  name: string
  value: number
}

export interface RealtimeDeviceInfo {
  id: number
  deviceCode: string
  deviceName: string
  deviceType: DeviceType
  deviceTypeName: string
  parkId: number
  parkName: string
  installLocation: string
  ratedPower: number
  accessMethod: string
  status: number
  remark: string
  lastReportAt: string
  isOnline: boolean
  currentPower: number
  metrics: Record<string, any>
}

export interface HistoryDataVO {
  ts: string
  deviceCode: string
  deviceId: number
  parkId: number
  metricCode: string
  metricValue: number
  statusFlag: string
}

export interface HistoryDataQuery {
  parkId?: number
  deviceCode?: string
  metricCode?: string
  startTime: string
  endTime: string
  pageNum?: number
  pageSize?: number
}

export interface SimulatorStatus {
  running: boolean
  startTime?: string
  speed: number
  deviceCount: number
  runningDeviceCount: number
  dataPointsGenerated: number
}

export interface SimulatorDeviceStatus {
  deviceCode: string
  running: boolean
  dataPointsGenerated: number
  startTime: string
}

export interface SysLoginLog {
  id: number
  userId: number
  username: string
  loginIp: string
  userAgent: string
  status: number
  message: string
  loginAt: string
}

export interface SysOperationLog {
  id: number
  userId: number
  username: string
  operationType: string
  operationModule: string
  description: string
  requestMethod: string
  requestPath: string
  requestParams: string
  result: string
  errorMsg: string
  operationIp: string
  operationAt: string
}

export interface UserParams {
  id?: number
  username: string
  realName: string
  email?: string
  phone?: string
  password?: string
  roleIds: number[]
  status: number
}

export interface RoleParams {
  id?: number
  roleCode: string
  roleName: string
  description?: string
  status: number
}

export interface DeviceRealtimeData {
  deviceCode: string
  deviceName: string
  deviceType: string
  parkId: number
  parkName: string
  isOnline: boolean
  currentPower: number
  metrics: Record<string, any>
  lastReportAt: string
}

export interface ParkDeviceOnlineRate {
  parkId: number
  parkName: string
  totalDeviceCount: number
  onlineDeviceCount: number
  onlineRate: number
  alarmCount: number
}

export interface AdminHomeStats {
  userCount: number
  roleCount: number
  parkCount: number
  deviceTotalCount: number
  deviceOnlineCount: number
  todayLoginCount: number
  disabledUserCount: number
  disabledDeviceCount: number
  parkWithNoDeviceCount: number
  parkOnlineRates: ParkDeviceOnlineRate[]
  recentLoginLogs: SysLoginLog[]
}

export type AlertRuleType = 'DEVICE_OFFLINE' | 'DATA_MISSING' | 'POWER_ABNORMAL' | 'VOLTAGE_ABNORMAL' | 'ESS_SOC_HIGH' | 'ESS_SOC_LOW'
export type AlertLevel = 'CRITICAL' | 'MAJOR' | 'MINOR' | 'WARNING'
export type AlertScopeType = 'ALL' | 'PARK' | 'DEVICE_TYPE' | 'DEVICE'
export type AlertStatus = 'TRIGGERED' | 'RECOVERED'
export type AlertOperator = 'GT' | 'GTE' | 'LT' | 'LTE' | 'EQ' | 'NEQ' | 'BETWEEN' | 'NOT_BETWEEN'

export const AlertRuleTypeMap: Record<AlertRuleType, string> = {
  DEVICE_OFFLINE: '设备离线告警',
  DATA_MISSING: '数据缺失告警',
  POWER_ABNORMAL: '功率异常告警',
  VOLTAGE_ABNORMAL: '电压异常告警',
  ESS_SOC_HIGH: '储能SOC过高告警',
  ESS_SOC_LOW: '储能SOC过低告警'
}

export const AlertLevelMap: Record<AlertLevel, string> = {
  CRITICAL: '紧急',
  MAJOR: '重要',
  MINOR: '次要',
  WARNING: '提示'
}

export const AlertLevelTagTypeMap: Record<AlertLevel, string> = {
  CRITICAL: 'danger',
  MAJOR: 'danger',
  MINOR: 'warning',
  WARNING: 'info'
}

export const AlertScopeTypeMap: Record<AlertScopeType, string> = {
  ALL: '全部设备',
  PARK: '指定园区',
  DEVICE_TYPE: '指定设备类型',
  DEVICE: '指定设备'
}

export const AlertStatusMap: Record<AlertStatus, string> = {
  TRIGGERED: '告警中',
  RECOVERED: '已恢复'
}

export const AlertStatusTagTypeMap: Record<AlertStatus, string> = {
  TRIGGERED: 'danger',
  RECOVERED: 'success'
}

export const AlertOperatorMap: Record<AlertOperator, string> = {
  GT: '大于',
  GTE: '大于等于',
  LT: '小于',
  LTE: '小于等于',
  EQ: '等于',
  NEQ: '不等于',
  BETWEEN: '在范围内',
  NOT_BETWEEN: '不在范围内'
}

export interface AlertRuleInfo {
  id: number
  ruleName: string
  ruleCode: string
  ruleType: AlertRuleType
  ruleTypeName: string
  alertLevel: AlertLevel
  alertLevelName: string
  scopeType: AlertScopeType
  scopeTypeName: string
  scopeValue?: string
  parkId?: number
  parkName?: string
  deviceType?: DeviceType
  deviceTypeName?: string
  deviceCode?: string
  thresholdOperator?: AlertOperator
  thresholdValue?: number
  thresholdValue2?: number
  metricCode?: string
  durationSeconds: number
  recoveryOperator?: AlertOperator
  recoveryValue?: number
  recoveryValue2?: number
  description?: string
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface AlertRuleParams {
  id?: number
  ruleName: string
  ruleCode: string
  ruleType: AlertRuleType
  alertLevel: AlertLevel
  scopeType: AlertScopeType
  scopeValue?: string
  parkId?: number
  deviceType?: DeviceType
  deviceCode?: string
  thresholdOperator?: AlertOperator
  thresholdValue?: number
  thresholdValue2?: number
  metricCode?: string
  durationSeconds: number
  recoveryOperator?: AlertOperator
  recoveryValue?: number
  recoveryValue2?: number
  description?: string
  status: number
}

export interface AlertInfo {
  id: number
  alertNo: string
  ruleId: number
  ruleName: string
  ruleCode: string
  ruleType: AlertRuleType
  ruleTypeName: string
  alertLevel: AlertLevel
  alertLevelName: string
  parkId?: number
  parkName?: string
  deviceId?: number
  deviceCode: string
  deviceName?: string
  deviceType?: DeviceType
  deviceTypeName?: string
  triggerValue?: number
  thresholdValue?: number
  metricCode?: string
  alertStatus: AlertStatus
  alertStatusName: string
  triggerTime: string
  recoveryTime?: string
  alertMessage?: string
  rawData?: string
  createdAt?: string
  updatedAt?: string
}

export interface AlertQueryParams {
  parkId?: number
  deviceCode?: string
  alertLevel?: AlertLevel
  alertStatus?: AlertStatus
  ruleType?: AlertRuleType
  startTime?: string
  endTime?: string
  keyword?: string
}
