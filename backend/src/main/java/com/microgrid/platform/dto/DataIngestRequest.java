package com.microgrid.platform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DataIngestRequest {

    @NotEmpty(message = "数据不能为空")
    @Valid
    private List<MetricDataDTO> data;
}
