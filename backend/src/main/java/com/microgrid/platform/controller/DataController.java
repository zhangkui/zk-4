package com.microgrid.platform.controller;

import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.DataIngestRequest;
import com.microgrid.platform.dto.MetricDataDTO;
import com.microgrid.platform.service.DataIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final DataIngestionService dataIngestionService;

    @PostMapping("/ingest")
    public Result<Void> ingest(@Valid @RequestBody DataIngestRequest request) {
        log.debug("收到数据接入请求: {} 条数据", request.getData() != null ? request.getData().size() : 0);
        dataIngestionService.ingest(request);
        return Result.success();
    }

    @PostMapping("/ingest/single")
    public Result<Void> ingestSingle(@Valid @RequestBody MetricDataDTO dto) {
        dataIngestionService.ingestSingle(dto);
        return Result.success();
    }

    @PostMapping("/ingest/batch")
    public Result<Void> ingestBatch(@Valid @RequestBody List<MetricDataDTO> dataList) {
        dataIngestionService.ingestBatch(dataList);
        return Result.success();
    }

    @PostMapping("/mqtt-test")
    public Result<Void> mqttTest(@RequestParam String topic, @RequestBody String payload) {
        dataIngestionService.ingestFromMqtt(topic, payload);
        return Result.success();
    }
}
