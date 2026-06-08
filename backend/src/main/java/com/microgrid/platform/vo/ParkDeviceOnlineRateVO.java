package com.microgrid.platform.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParkDeviceOnlineRateVO {
    private Long parkId;
    private String parkName;
    private Long totalDeviceCount;
    private Long onlineDeviceCount;
    private BigDecimal onlineRate;
    private Long alarmCount;
}
