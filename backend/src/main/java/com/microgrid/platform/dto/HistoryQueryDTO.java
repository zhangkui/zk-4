package com.microgrid.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryQueryDTO {

    private Long parkId;
    private String deviceCode;
    private String metricCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 50;
}
