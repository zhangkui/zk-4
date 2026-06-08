package com.microgrid.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AlertRuleCreateDTO {

    private Long id;

    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "规则编码不能为空")
    private String ruleCode;

    @NotBlank(message = "规则类型不能为空")
    private String ruleType;

    @NotBlank(message = "告警级别不能为空")
    private String alertLevel;

    @NotBlank(message = "生效范围类型不能为空")
    private String scopeType;

    private String scopeValue;

    private Long parkId;

    private String deviceType;

    private String deviceCode;

    private String thresholdOperator;

    private BigDecimal thresholdValue;

    private BigDecimal thresholdValue2;

    private String metricCode;

    @NotNull(message = "持续时长不能为空")
    private Integer durationSeconds;

    private String recoveryOperator;

    private BigDecimal recoveryValue;

    private BigDecimal recoveryValue2;

    private String description;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
