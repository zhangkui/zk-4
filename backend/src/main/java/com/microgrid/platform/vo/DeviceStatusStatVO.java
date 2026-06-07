package com.microgrid.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusStatVO {

    private String status;
    private String statusName;
    private Integer count;
    private String name;
    private Integer value;

    public DeviceStatusStatVO(String status, String statusName, Integer count) {
        this.status = status;
        this.statusName = statusName;
        this.count = count;
        this.name = statusName;
        this.value = count;
    }
}
