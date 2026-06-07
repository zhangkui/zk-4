package com.microgrid.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParkCreateDTO {

    @NotBlank(message = "园区编码不能为空")
    private String parkCode;

    @NotBlank(message = "园区名称不能为空")
    private String parkName;

    private String location;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String description;
    private Integer status = 1;
}
