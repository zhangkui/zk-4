package com.microgrid.platform.service;

import com.microgrid.platform.dto.DataIngestRequest;
import com.microgrid.platform.dto.MetricDataDTO;

import java.util.List;

public interface DataIngestionService {

    void ingest(DataIngestRequest request);

    void ingestSingle(MetricDataDTO dto);

    void ingestBatch(List<MetricDataDTO> dataList);

    void ingestFromMqtt(String topic, String payload);
}
