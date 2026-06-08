package com.microgrid.platform.dto;

import lombok.Data;

@Data
public class AlertQueryDTO {

    private Long parkId;
    private String deviceCode;
    private String alertLevel;
    private String alertStatus;
    private String ruleType;
    private String startTime;
    private String endTime;
    private String keyword;
}
