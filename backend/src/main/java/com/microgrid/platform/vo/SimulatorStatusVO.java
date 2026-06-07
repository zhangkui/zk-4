package com.microgrid.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulatorStatusVO {

    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private Boolean running;
}
