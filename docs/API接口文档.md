# 园区级微电网运行监测平台 - API 接口文档

## 通用说明

- 基础路径: `/api`
- 数据格式: JSON
- 字符编码: UTF-8
- 时间格式: `yyyy-MM-dd HH:mm:ss` (东八区)
- 认证方式: Bearer Token (登录接口除外)

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 响应码，200表示成功 |
| message | string | 响应消息 |
| data | object/array/null | 响应数据 |

### 通用响应码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 请求头

```
Authorization: Bearer <token>
Content-Type: application/json
```

### 分页参数

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 20 | 每页条数 |

---

## 1. 认证模块

### 1.1 登录

- **POST** `/api/auth/login`
- 无需认证

请求体:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

响应:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "username": "admin",
    "realName": "系统管理员",
    "roles": ["ADMIN"],
    "permissions": ["dashboard", "park", "device", "..."],
    "loginTime": "2024-01-01 12:00:00"
  }
}
```

### 1.2 登出

- **POST** `/api/auth/logout`

### 1.3 获取当前用户信息

- **GET** `/api/auth/me`

响应 data 结构同登录响应。

### 1.4 刷新 Token

- **POST** `/api/auth/refresh`
- Header: `Authorization: Bearer <token>`

---

## 2. 用户管理

### 2.1 分页查询用户

- **GET** `/api/users/page`
- 权限: `user`

参数: `pageNum`, `pageSize`, `keyword` (用户名/姓名模糊搜索)

### 2.2 获取用户详情

- **GET** `/api/users/{id}`
- 权限: `user`

### 2.3 新增用户

- **POST** `/api/users`
- 权限: `user`

请求体:
```json
{
  "username": "test",
  "realName": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000",
  "password": "admin123",
  "status": 1,
  "roleIds": [1, 2]
}
```

### 2.4 修改用户

- **PUT** `/api/users/{id}`
- 权限: `user`

请求体同新增（不含 password）。

### 2.5 删除用户

- **DELETE** `/api/users/{id}`
- 权限: `user`

### 2.6 启用/停用用户

- **PUT** `/api/users/{id}/status?status=1`
- 权限: `user`
- status: 1=启用, 0=停用

### 2.7 重置密码

- **PUT** `/api/users/{id}/password/reset?newPassword=admin123`
- 权限: `user`

### 2.8 修改自己的密码

- **PUT** `/api/users/{id}/password?oldPassword=xxx&newPassword=yyy`

---

## 3. 角色管理

### 3.1 分页查询角色

- **GET** `/api/roles/page`
- 权限: `role`

### 3.2 获取角色列表

- **GET** `/api/roles/list`
- 权限: `role`

### 3.3 新增角色

- **POST** `/api/roles`
- 权限: `role`

```json
{
  "roleCode": "CUSTOM",
  "roleName": "自定义角色",
  "description": "描述",
  "status": 1
}
```

### 3.4 修改角色

- **PUT** `/api/roles/{id}`
- 权限: `role`

### 3.5 删除角色

- **DELETE** `/api/roles/{id}`
- 权限: `role`

### 3.6 获取角色权限

- **GET** `/api/roles/{id}/permissions`
- 权限: `role`

### 3.7 分配角色权限

- **POST** `/api/roles/{id}/permissions`
- 权限: `role`

请求体: `[1, 2, 3, ...]` (permissionId 数组)

---

## 4. 园区管理

### 4.1 分页查询园区

- **GET** `/api/parks/page`
- 权限: `park`
- 参数: `pageNum`, `pageSize`, `keyword` (名称/编码模糊搜索)

### 4.2 获取园区列表

- **GET** `/api/parks/list`

### 4.3 获取园区详情

- **GET** `/api/parks/{id}`
- 权限: `park`

### 4.4 新增园区

- **POST** `/api/parks`
- 权限: `park`

```json
{
  "parkCode": "PARK-003",
  "parkName": "测试园区",
  "location": "详细地址",
  "longitude": 121.5,
  "latitude": 31.2,
  "description": "描述",
  "status": 1
}
```

### 4.5 修改园区

- **PUT** `/api/parks/{id}`
- 权限: `park`

### 4.6 删除园区

- **DELETE** `/api/parks/{id}`
- 权限: `park`

### 4.7 启用/停用园区

- **PUT** `/api/parks/{id}/status?status=0/1`
- 权限: `park`

---

## 5. 设备台账

### 5.1 分页查询设备

- **GET** `/api/devices/page`
- 权限: `device`

参数: `pageNum`, `pageSize`, `parkId`, `deviceType` (PV/WIND/ESS/PCS/CHARGING/METER/LOAD), `keyword`

### 5.2 获取设备列表

- **GET** `/api/devices/list?parkId=&deviceType=`
- 权限: `device`

### 5.3 获取设备详情（ID）

- **GET** `/api/devices/{id}`
- 权限: `device`

### 5.4 获取设备详情（编码）

- **GET** `/api/devices/code/{deviceCode}`
- 权限: `device`

### 5.5 获取设备实时数据

- **GET** `/api/devices/code/{deviceCode}/realtime`
- 权限: `device`

返回: 包含 metrics、isOnline、currentPower 字段的设备对象

### 5.6 新增设备

- **POST** `/api/devices`
- 权限: `device`

```json
{
  "deviceCode": "PV-TEST",
  "deviceName": "测试光伏",
  "deviceType": "PV",
  "parkId": 1,
  "installLocation": "屋顶",
  "ratedPower": 100.0,
  "accessMethod": "MQTT",
  "status": 1,
  "remark": "备注"
}
```

### 5.7 修改设备

- **PUT** `/api/devices/{id}`
- 权限: `device`

### 5.8 删除设备

- **DELETE** `/api/devices/{id}`
- 权限: `device`

### 5.9 启用/停用设备

- **PUT** `/api/devices/{id}/status?status=0/1`
- 权限: `device`

---

## 6. 监测大屏

### 6.1 园区运行总览

- **GET** `/api/monitor/overview` (别名: `/api/monitor/dashboard`)
- 权限: `dashboard`
- 参数: `parkId` (可选，默认1)

响应:
```json
{
  "code": 200,
  "data": {
    "parkId": 1,
    "parkName": "智慧能源产业园",
    "totalGenerationPower": 850.5,
    "totalLoadPower": 1200.3,
    "pvPower": 750.2,
    "windPower": 100.3,
    "gridPower": 349.8,
    "essPower": -150.0,
    "essSoc": 62.5,
    "chargingOnlineCount": 3,
    "chargingTotalCount": 4,
    "onlineDeviceCount": 18,
    "totalDeviceCount": 20,
    "onlineRate": 90.0,
    "deviceStatusStats": {
      "在线": 18,
      "离线": 1,
      "停用": 1
    }
  }
}
```

> essPower: 正数放电，负数充电

### 6.2 功率趋势曲线

- **GET** `/api/monitor/trend` (别名: `/api/monitor/power-curve`)
- 权限: `dashboard`
- 参数: `parkId`, `hours` (可选，默认1小时)

响应:
```json
{
  "code": 200,
  "data": {
    "generation": [{"ts": "2024-01-01 12:00:00", "value": 800.0}, ...],
    "load": [{"ts": "2024-01-01 12:00:00", "value": 1200.0}, ...],
    "ess": [{"ts": "2024-01-01 12:00:00", "value": -150.0}, ...],
    "pv": [...],
    "wind": [...]
  }
}
```

### 6.3 设备状态统计

- **GET** `/api/monitor/device-status` (别名: `/api/monitor/device-status-pie`)
- 权限: `dashboard`
- 参数: `parkId`

响应:
```json
{
  "code": 200,
  "data": [
    {"status": "1", "statusName": "在线", "count": 18, "name": "在线", "value": 18},
    {"status": "0", "statusName": "离线", "count": 1, "name": "离线", "value": 1},
    {"status": "-1", "statusName": "停用", "count": 1, "name": "停用", "value": 1}
  ]
}
```

### 6.4 单设备运行曲线

- **GET** `/api/monitor/device-curve/{deviceCode}`
- 权限: `dashboard`
- 参数: `hours` (默认24小时)

响应结构同 6.2。

### 6.5 设备实时列表

- **GET** `/api/monitor/realtime-devices`
- 权限: `dashboard`
- 参数: `parkId`

返回 DeviceDTO 数组，含 isOnline、currentPower、metrics。

---

## 7. 历史数据

### 7.1 分页查询历史数据

- **GET** `/api/history/page`
- 权限: `history`

参数: `parkId`, `deviceCode`, `metricCode`, `startTime`, `endTime`, `pageNum`, `pageSize`

### 7.2 历史数据列表（不分页）

- **GET** `/api/history/list` (别名: `/api/history/chart`)
- 权限: `history`

参数: 同上，用于图表展示

响应:
```json
{
  "code": 200,
  "data": [
    {
      "ts": "2024-01-01 12:00:00",
      "deviceCode": "PV-001",
      "deviceId": 1,
      "parkId": 1,
      "metricCode": "activePower",
      "metricValue": 500.5,
      "statusFlag": 1
    }
  ]
}
```

### 7.3 组合查询

- **POST** `/api/history/query`
- 权限: `history`

---

## 8. 数据接入

### 8.1 HTTP 上报（批量）

- **POST** `/api/data/ingest`
- 无需认证（设备端使用）

请求体:
```json
{
  "data": [
    {
      "timestamp": "2024-01-01T12:00:00",
      "deviceCode": "PV-001",
      "metricCode": "activePower",
      "metricValue": 500.5,
      "statusFlag": 1
    }
  ]
}
```

### 8.2 HTTP 上报（单条）

- **POST** `/api/data/ingest/single`
- 无需认证

### 8.3 HTTP 上报（批量数组）

- **POST** `/api/data/ingest/batch`
- 无需认证

### 8.4 MQTT 上报

- 主题: `microgrid/device/{deviceCode}/data`
- Broker: EMQX (默认端口 1883)
- Payload 同单条上报数据 JSON

---

## 9. 日志审计

### 9.1 登录日志

- **GET** `/api/logs/login`
- 权限: `log`

参数: `username`, `startTime`, `endTime`, `pageNum`, `pageSize`

### 9.2 操作日志

- **GET** `/api/logs/operation`
- 权限: `log`

参数: `username`, `operationType` (LOGIN/LOGOUT/CREATE/UPDATE/DELETE/START/STOP/ASSIGN), `operationModule`, `startTime`, `endTime`, `pageNum`, `pageSize`

---

## 10. 数据模拟器

### 10.1 启动全部模拟器

- **POST** `/api/simulator/start-all?parkId=`
- 权限: `simulator`

### 10.2 停止全部模拟器

- **POST** `/api/simulator/stop-all?parkId=`
- 权限: `simulator`

### 10.3 模拟器总状态

- **GET** `/api/simulator/overview`
- 权限: `simulator`

响应:
```json
{
  "code": 200,
  "data": {
    "running": true,
    "startTime": "2024-01-01 12:00:00",
    "speed": 1,
    "deviceCount": 20,
    "runningDeviceCount": 20,
    "dataPointsGenerated": 12500
  }
}
```

### 10.4 各设备模拟器状态

- **GET** `/api/simulator/status?parkId=`
- 权限: `simulator`

### 10.5 启动单设备模拟器

- **POST** `/api/simulator/start/{deviceCode}`
- 权限: `simulator`

### 10.6 停止单设备模拟器

- **POST** `/api/simulator/stop/{deviceCode}`
- 权限: `simulator`

---

## 附录：设备类型与指标

### 设备类型 (deviceType)

| 值 | 名称 | 说明 |
|----|------|------|
| PV | 光伏设备 | 太阳能光伏发电 |
| WIND | 风电设备 | 风力发电 |
| ESS | 储能设备 | 电池储能系统 |
| PCS | 变流器 | 储能变流器、光伏逆变器 |
| CHARGING | 充电桩 | 电动汽车充电设施 |
| METER | 电表 | 电能计量表 |
| LOAD | 负荷设备 | 用电负荷 |

### 常用指标 (metricCode)

| 指标编码 | 名称 | 单位 |
|----------|------|------|
| activePower | 有功功率 | kW |
| reactivePower | 无功功率 | kVar |
| voltage | 电压 | V |
| current | 电流 | A |
| soc | 荷电状态 | % |
| temperature | 温度 | ℃ |
| frequency | 频率 | Hz |
| totalEnergy | 累计电量 | kWh |
| status | 运行状态 | 0/1 |
| chargingStatus | 充电状态 | 0空闲/1充电中/2故障 |
