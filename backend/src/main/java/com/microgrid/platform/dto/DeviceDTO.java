package com.microgrid.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class DeviceDTO {

    private Long id;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String deviceTypeName;
    private Long parkId;
    private String parkName;
    private String installLocation;
    private BigDecimal ratedPower;
    private String accessMethod;
    private Integer status;
    private String remark;
    private LocalDateTime lastReportAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isOnline;
    private BigDecimal currentPower;
    private Map<String, Object> metrics;
}
