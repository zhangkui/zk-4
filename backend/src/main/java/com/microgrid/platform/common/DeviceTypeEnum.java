package com.microgrid.platform.common;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum DeviceTypeEnum {

    PV("PV", "光伏发电", Arrays.asList("power", "energy", "voltage", "current", "status"), "#52C41A"),
    WIND("WIND", "风力发电", Arrays.asList("power", "energy", "wind_speed", "status"), "#1890FF"),
    ESS("ESS", "储能系统", Arrays.asList("power", "soc", "voltage", "current", "temperature", "status"), "#722ED1"),
    PCS("PCS", "变流器", Arrays.asList("power", "voltage", "current", "status"), "#FA8C16"),
    CHARGING("CHARGING", "充电桩", Arrays.asList("power", "energy", "voltage", "current", "charging_status", "status"), "#13C2C2"),
    METER("METER", "电表", Arrays.asList("power", "energy", "voltage", "current", "status"), "#FADB14"),
    LOAD("LOAD", "用电负荷", Arrays.asList("power", "energy", "status"), "#F5222D");

    private final String code;
    private final String name;
    private final List<String> metrics;
    private final String color;

    DeviceTypeEnum(String code, String name, List<String> metrics, String color) {
        this.code = code;
        this.name = name;
        this.metrics = metrics;
        this.color = color;
    }

    public static DeviceTypeEnum fromCode(String code) {
        for (DeviceTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isValidMetric(String deviceType, String metricCode) {
        DeviceTypeEnum type = fromCode(deviceType);
        return type != null && type.getMetrics().contains(metricCode);
    }

    public static String getDescription(String code) {
        DeviceTypeEnum type = fromCode(code);
        return type != null ? type.getName() : code;
    }
}
