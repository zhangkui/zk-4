package com.microgrid.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class MetricDataDTO {

    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    @NotBlank(message = "指标编码不能为空")
    private String metricCode;

    @NotNull(message = "指标值不能为空")
    private BigDecimal metricValue;

    private LocalDateTime ts;

    private Integer statusFlag = 1;

    private Map<String, Object> rawData;
}
