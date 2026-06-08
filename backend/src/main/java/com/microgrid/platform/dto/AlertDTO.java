package com.microgrid.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertDTO {

    private Long id;
    private String alertNo;
    private Long ruleId;
    private String ruleName;
    private String ruleCode;
    private String ruleType;
    private String ruleTypeName;
    private String alertLevel;
    private String alertLevelName;
    private Long parkId;
    private String parkName;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String deviceTypeName;
    private BigDecimal triggerValue;
    private BigDecimal thresholdValue;
    private String metricCode;
    private String alertStatus;
    private String alertStatusName;
    private LocalDateTime triggerTime;
    private LocalDateTime recoveryTime;
    private String alertMessage;
    private String rawData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
