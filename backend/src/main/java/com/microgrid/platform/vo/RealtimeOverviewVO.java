package com.microgrid.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class RealtimeOverviewVO {

    private Long parkId;
    private String parkName;
    private BigDecimal totalGenerationPower;
    private BigDecimal totalLoadPower;
    private BigDecimal essPower;
    private BigDecimal essSoc;
    private Integer chargingOnlineCount;
    private Integer chargingTotalCount;
    private Integer onlineDeviceCount;
    private Integer totalDeviceCount;
    private Double onlineRate;
    private BigDecimal pvPower;
    private BigDecimal windPower;
    private BigDecimal gridPower;
    private Map<String, Integer> deviceStatusStats;
}
