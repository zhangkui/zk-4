package com.microgrid.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParkDTO {

    private Long id;
    private String parkCode;
    private String parkName;
    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deviceCount;
    private Integer onlineDeviceCount;
}
