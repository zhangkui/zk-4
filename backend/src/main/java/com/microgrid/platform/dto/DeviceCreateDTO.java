package com.microgrid.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceCreateDTO {

    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @NotNull(message = "园区ID不能为空")
    private Long parkId;

    private String installLocation;
    private BigDecimal ratedPower;
    private String accessMethod;
    private Integer status = 1;
    private String remark;
}
