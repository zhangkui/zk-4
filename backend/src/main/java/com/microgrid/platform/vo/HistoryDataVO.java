package com.microgrid.platform.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HistoryDataVO {

    private LocalDateTime ts;
    private String deviceCode;
    private String deviceName;
    private String metricCode;
    private String metricName;
    private BigDecimal metricValue;
    private String unit;
}
