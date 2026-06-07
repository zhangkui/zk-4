CREATE EXTENSION IF NOT EXISTS timescaledb;

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

-- ============ 用户与权限 ============

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    status SMALLINT NOT NULL DEFAULT 1,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, role_id)
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGSERIAL PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_name VARCHAR(100) NOT NULL,
    permission_type SMALLINT NOT NULL DEFAULT 1,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    path VARCHAR(255),
    icon VARCHAR(100),
    component VARCHAR(255),
    api_method VARCHAR(10),
    api_path VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES sys_permission(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    login_ip VARCHAR(50),
    user_agent VARCHAR(500),
    status SMALLINT NOT NULL DEFAULT 1,
    message VARCHAR(255),
    login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_login_log_user_id ON sys_login_log(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_login_log_login_at ON sys_login_log(login_at);

CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    operation_type VARCHAR(50) NOT NULL,
    operation_module VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    request_method VARCHAR(10),
    request_path VARCHAR(255),
    request_params TEXT,
    result SMALLINT NOT NULL DEFAULT 1,
    error_msg TEXT,
    operation_ip VARCHAR(50),
    operation_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sys_operation_log_user_id ON sys_operation_log(user_id);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_operation_at ON sys_operation_log(operation_at);
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_type ON sys_operation_log(operation_type);

-- ============ 园区与设备 ============

CREATE TABLE IF NOT EXISTS park (
    id BIGSERIAL PRIMARY KEY,
    park_code VARCHAR(50) NOT NULL UNIQUE,
    park_name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    longitude DECIMAL(10, 6),
    latitude DECIMAL(10, 6),
    description TEXT,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_park_code ON park(park_code);
CREATE INDEX IF NOT EXISTS idx_park_name ON park(park_name);

CREATE TABLE IF NOT EXISTS device (
    id BIGSERIAL PRIMARY KEY,
    device_code VARCHAR(50) NOT NULL UNIQUE,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(30) NOT NULL,
    park_id BIGINT NOT NULL REFERENCES park(id),
    install_location VARCHAR(255),
    rated_power DECIMAL(15, 3),
    access_method VARCHAR(30),
    status SMALLINT NOT NULL DEFAULT 1,
    remark TEXT,
    last_report_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_device_park_id ON device(park_id);
CREATE INDEX IF NOT EXISTS idx_device_type ON device(device_type);
CREATE INDEX IF NOT EXISTS idx_device_status ON device(status);
CREATE INDEX IF NOT EXISTS idx_device_code ON device(device_code);

-- ============ 实时与历史数据 ============

CREATE TABLE IF NOT EXISTS device_realtime (
    id BIGSERIAL PRIMARY KEY,
    device_code VARCHAR(50) NOT NULL UNIQUE,
    device_id BIGINT REFERENCES device(id),
    metric_code VARCHAR(100),
    metric_value DECIMAL(20, 6),
    status_flag SMALLINT DEFAULT 1,
    raw_data JSONB,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_device_realtime_device_code ON device_realtime(device_code);
CREATE INDEX IF NOT EXISTS idx_device_realtime_device_id ON device_realtime(device_id);

CREATE TABLE IF NOT EXISTS device_metric_data (
    ts TIMESTAMP NOT NULL,
    device_code VARCHAR(50) NOT NULL,
    device_id BIGINT,
    park_id BIGINT,
    metric_code VARCHAR(100) NOT NULL,
    metric_value DECIMAL(20, 6) NOT NULL,
    status_flag SMALLINT DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

SELECT create_hypertable('device_metric_data', 'ts', if_not_exists => TRUE);

CREATE INDEX IF NOT EXISTS idx_device_metric_data_device_code_ts ON device_metric_data(device_code, ts DESC);
CREATE INDEX IF NOT EXISTS idx_device_metric_data_metric_code_ts ON device_metric_data(metric_code, ts DESC);
CREATE INDEX IF NOT EXISTS idx_device_metric_data_park_id_ts ON device_metric_data(park_id, ts DESC);

-- ============ 初始数据 ============

INSERT INTO sys_role (role_code, role_name, description, status) VALUES
('ADMIN', '系统管理员', '系统最高权限，拥有所有功能操作权限', 1),
('OPERATOR', '运维人员', '负责设备运维、数据查看与基本配置', 1),
('VIEWER', '只读用户', '仅可查看监测数据和报表', 1)
ON CONFLICT (role_code) DO NOTHING;

INSERT INTO sys_user (username, password, real_name, email, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@microgrid.local', 1),
('operator', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '运维人员', 'operator@microgrid.local', 1),
('viewer', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '只读用户', 'viewer@microgrid.local', 1)
ON CONFLICT (username) DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'operator' AND r.role_code = 'OPERATOR'
ON CONFLICT DO NOTHING;

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'viewer' AND r.role_code = 'VIEWER'
ON CONFLICT DO NOTHING;

INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, sort_order, path, icon, component) VALUES
('dashboard', '监测大屏', 1, 0, 1, '/dashboard', 'DataAnalysis', 'Dashboard'),
('park', '园区管理', 1, 0, 2, '/park', 'OfficeBuilding', 'ParkList'),
('device', '设备台账', 1, 0, 3, '/device', 'Cpu', 'DeviceList'),
('device-detail', '设备详情', 1, 0, 4, '/device-detail', 'Monitor', 'DeviceDetail'),
('history', '历史数据', 1, 0, 5, '/history', 'Histogram', 'HistoryData'),
('user', '用户管理', 1, 0, 6, '/user', 'User', 'UserList'),
('role', '角色管理', 1, 0, 7, '/role', 'UserFilled', 'RoleList'),
('log', '日志审计', 1, 0, 8, '/log', 'Document', 'LogAudit'),
('simulator', '数据模拟器', 1, 0, 9, '/simulator', 'MagicStick', 'Simulator')
ON CONFLICT (permission_code) DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'OPERATOR' AND p.permission_code IN ('dashboard','park','device','device-detail','history','simulator')
ON CONFLICT DO NOTHING;

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM sys_role r, sys_permission p
WHERE r.role_code = 'VIEWER' AND p.permission_code IN ('dashboard','device-detail','history')
ON CONFLICT DO NOTHING;

INSERT INTO park (park_code, park_name, location, longitude, latitude, description, status) VALUES
('PARK-001', '智慧能源产业园', '上海市浦东新区张江高科技园区', 121.5578, 31.2047, '示范型智慧微电网园区，包含光伏、风电、储能、充电桩等多种能源设施', 1),
('PARK-002', '绿色商务园区', '北京市海淀区中关村软件园', 116.3100, 40.0499, '综合商务办公园区，重点优化峰谷用电经济性', 1)
ON CONFLICT (park_code) DO NOTHING;

INSERT INTO device (device_code, device_name, device_type, park_id, install_location, rated_power, access_method, status, remark) VALUES
('PV-001', '屋顶光伏阵列A', 'PV', (SELECT id FROM park WHERE park_code = 'PARK-001'), 'A栋屋顶', 500.000, 'MQTT', 1, '单晶硅组件，2000块'),
('PV-002', '屋顶光伏阵列B', 'PV', (SELECT id FROM park WHERE park_code = 'PARK-001'), 'B栋屋顶', 300.000, 'MQTT', 1, '单晶硅组件，1200块'),
('PV-003', '车棚光伏', 'PV', (SELECT id FROM park WHERE park_code = 'PARK-001'), '停车场车棚', 200.000, 'MQTT', 1, '薄膜组件'),
('WT-001', '小型风机1号', 'WIND', (SELECT id FROM park WHERE park_code = 'PARK-001'), '园区东北角', 50.000, 'MQTT', 1, '垂直轴风机'),
('WT-002', '小型风机2号', 'WIND', (SELECT id FROM park WHERE park_code = 'PARK-001'), '园区西北角', 50.000, 'MQTT', 1, '垂直轴风机'),
('ESS-001', '磷酸铁锂储能柜1', 'ESS', (SELECT id FROM park WHERE park_code = 'PARK-001'), '能源站1号', 1000.000, 'MQTT', 1, '额定容量2MWh'),
('PCS-001', '储能变流器1号', 'PCS', (SELECT id FROM park WHERE park_code = 'PARK-001'), '能源站1号', 500.000, 'MQTT', 1, '双向变流器'),
('PCS-002', '光伏逆变器1号', 'PCS', (SELECT id FROM park WHERE park_code = 'PARK-001'), 'A栋配电间', 500.000, 'HTTP', 1, '组串式逆变器'),
('CP-001', '直流快充桩A1', 'CHARGING', (SELECT id FROM park WHERE park_code = 'PARK-001'), '停车场A区', 60.000, 'MQTT', 1, '60kW直流快充'),
('CP-002', '直流快充桩A2', 'CHARGING', (SELECT id FROM park WHERE park_code = 'PARK-001'), '停车场A区', 60.000, 'MQTT', 1, '60kW直流快充'),
('CP-003', '交流慢充桩B1', 'CHARGING', (SELECT id FROM park WHERE park_code = 'PARK-001'), '停车场B区', 7.000, 'MQTT', 1, '7kW交流慢充'),
('CP-004', '交流慢充桩B2', 'CHARGING', (SELECT id FROM park WHERE park_code = 'PARK-001'), '停车场B区', 7.000, 'MQTT', 1, '7kW交流慢充'),
('METER-001', '总进线电表', 'METER', (SELECT id FROM park WHERE park_code = 'PARK-001'), '高压配电室', 2000.000, 'MQTT', 1, '关口计量表'),
('LOAD-001', 'A栋办公楼负荷', 'LOAD', (SELECT id FROM park WHERE park_code = 'PARK-001'), 'A栋低压柜', 800.000, 'MQTT', 1, '综合用电负荷'),
('LOAD-002', 'B栋办公楼负荷', 'LOAD', (SELECT id FROM park WHERE park_code = 'PARK-001'), 'B栋低压柜', 600.000, 'MQTT', 1, '综合用电负荷'),
('LOAD-003', '生产车间负荷', 'LOAD', (SELECT id FROM park WHERE park_code = 'PARK-001'), '车间配电间', 1000.000, 'MQTT', 1, '工业生产负荷'),
('PV-101', '办公区光伏', 'PV', (SELECT id FROM park WHERE park_code = 'PARK-002'), '主楼屋顶', 400.000, 'MQTT', 1, '单晶组件'),
('ESS-101', '储能柜1号', 'ESS', (SELECT id FROM park WHERE park_code = 'PARK-002'), '地下配电室', 500.000, 'MQTT', 1, '额定容量1MWh'),
('CP-101', '充电桩1号', 'CHARGING', (SELECT id FROM park WHERE park_code = 'PARK-002'), '地下车库', 60.000, 'MQTT', 1, '直流快充'),
('METER-101', '园区总电表', 'METER', (SELECT id FROM park WHERE park_code = 'PARK-002'), '高压配电室', 1500.000, 'MQTT', 1, '关口计量表'),
('LOAD-101', '主楼办公负荷', 'LOAD', (SELECT id FROM park WHERE park_code = 'PARK-002'), '主楼低压柜', 900.000, 'MQTT', 1, '综合办公负荷')
ON CONFLICT (device_code) DO NOTHING;
