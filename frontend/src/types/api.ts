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
