package com.microgrid.platform.common;

import java.util.HashMap;
import java.util.Map;

public class AlertConstants {

    public static final String RULE_TYPE_DEVICE_OFFLINE = "DEVICE_OFFLINE";
    public static final String RULE_TYPE_DATA_MISSING = "DATA_MISSING";
    public static final String RULE_TYPE_POWER_ABNORMAL = "POWER_ABNORMAL";
    public static final String RULE_TYPE_VOLTAGE_ABNORMAL = "VOLTAGE_ABNORMAL";
    public static final String RULE_TYPE_ESS_SOC_HIGH = "ESS_SOC_HIGH";
    public static final String RULE_TYPE_ESS_SOC_LOW = "ESS_SOC_LOW";

    public static final Map<String, String> RULE_TYPE_MAP = new HashMap<>();
    static {
        RULE_TYPE_MAP.put(RULE_TYPE_DEVICE_OFFLINE, "设备离线告警");
        RULE_TYPE_MAP.put(RULE_TYPE_DATA_MISSING, "数据缺失告警");
        RULE_TYPE_MAP.put(RULE_TYPE_POWER_ABNORMAL, "功率异常告警");
        RULE_TYPE_MAP.put(RULE_TYPE_VOLTAGE_ABNORMAL, "电压异常告警");
        RULE_TYPE_MAP.put(RULE_TYPE_ESS_SOC_HIGH, "储能SOC过高告警");
        RULE_TYPE_MAP.put(RULE_TYPE_ESS_SOC_LOW, "储能SOC过低告警");
    }

    public static final String ALERT_LEVEL_CRITICAL = "CRITICAL";
    public static final String ALERT_LEVEL_MAJOR = "MAJOR";
    public static final String ALERT_LEVEL_MINOR = "MINOR";
    public static final String ALERT_LEVEL_WARNING = "WARNING";

    public static final Map<String, String> ALERT_LEVEL_MAP = new HashMap<>();
    static {
        ALERT_LEVEL_MAP.put(ALERT_LEVEL_CRITICAL, "紧急");
        ALERT_LEVEL_MAP.put(ALERT_LEVEL_MAJOR, "重要");
        ALERT_LEVEL_MAP.put(ALERT_LEVEL_MINOR, "次要");
        ALERT_LEVEL_MAP.put(ALERT_LEVEL_WARNING, "提示");
    }

    public static final String SCOPE_TYPE_ALL = "ALL";
    public static final String SCOPE_TYPE_PARK = "PARK";
    public static final String SCOPE_TYPE_DEVICE_TYPE = "DEVICE_TYPE";
    public static final String SCOPE_TYPE_DEVICE = "DEVICE";

    public static final Map<String, String> SCOPE_TYPE_MAP = new HashMap<>();
    static {
        SCOPE_TYPE_MAP.put(SCOPE_TYPE_ALL, "全部设备");
        SCOPE_TYPE_MAP.put(SCOPE_TYPE_PARK, "指定园区");
        SCOPE_TYPE_MAP.put(SCOPE_TYPE_DEVICE_TYPE, "指定设备类型");
        SCOPE_TYPE_MAP.put(SCOPE_TYPE_DEVICE, "指定设备");
    }

    public static final String ALERT_STATUS_TRIGGERED = "TRIGGERED";
    public static final String ALERT_STATUS_RECOVERED = "RECOVERED";

    public static final Map<String, String> ALERT_STATUS_MAP = new HashMap<>();
    static {
        ALERT_STATUS_MAP.put(ALERT_STATUS_TRIGGERED, "告警中");
        ALERT_STATUS_MAP.put(ALERT_STATUS_RECOVERED, "已恢复");
    }

    public static final String OPERATOR_GT = "GT";
    public static final String OPERATOR_GTE = "GTE";
    public static final String OPERATOR_LT = "LT";
    public static final String OPERATOR_LTE = "LTE";
    public static final String OPERATOR_EQ = "EQ";
    public static final String OPERATOR_NEQ = "NEQ";
    public static final String OPERATOR_BETWEEN = "BETWEEN";
    public static final String OPERATOR_NOT_BETWEEN = "NOT_BETWEEN";

    public static final Map<String, String> OPERATOR_MAP = new HashMap<>();
    static {
        OPERATOR_MAP.put(OPERATOR_GT, "大于");
        OPERATOR_MAP.put(OPERATOR_GTE, "大于等于");
        OPERATOR_MAP.put(OPERATOR_LT, "小于");
        OPERATOR_MAP.put(OPERATOR_LTE, "小于等于");
        OPERATOR_MAP.put(OPERATOR_EQ, "等于");
        OPERATOR_MAP.put(OPERATOR_NEQ, "不等于");
        OPERATOR_MAP.put(OPERATOR_BETWEEN, "在范围内");
        OPERATOR_MAP.put(OPERATOR_NOT_BETWEEN, "不在范围内");
    }
}
