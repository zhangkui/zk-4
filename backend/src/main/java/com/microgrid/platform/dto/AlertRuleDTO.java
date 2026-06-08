package com.microgrid.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertRuleDTO {

    private Long id;
    private String ruleName;
    private String ruleCode;
    private String ruleType;
    private String ruleTypeName;
    private String alertLevel;
    private String alertLevelName;
    private String scopeType;
    private String scopeTypeName;
    private String scopeValue;
    private Long parkId;
    private String parkName;
    private String deviceType;
    private String deviceTypeName;
    private String deviceCode;
    private String thresholdOperator;
    private BigDecimal thresholdValue;
    private BigDecimal thresholdValue2;
    private String metricCode;
    private Integer durationSeconds;
    private String recoveryOperator;
    private BigDecimal recoveryValue;
    private BigDecimal recoveryValue2;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
