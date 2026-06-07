package com.microgrid.platform.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SimulatorOverviewVO {

    private Boolean running;
    private LocalDateTime startTime;
    private Integer speed;
    private Integer deviceCount;
    private Integer runningDeviceCount;
    private Long dataPointsGenerated;
}
