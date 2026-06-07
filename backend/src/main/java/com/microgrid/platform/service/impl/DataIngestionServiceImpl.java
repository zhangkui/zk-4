package com.microgrid.platform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgrid.platform.common.BusinessException;
import com.microgrid.platform.common.DeviceTypeEnum;
import com.microgrid.platform.common.ResultCode;
import com.microgrid.platform.dto.DataIngestRequest;
import com.microgrid.platform.dto.MetricDataDTO;
import com.microgrid.platform.entity.Device;
import com.microgrid.platform.entity.DeviceMetricData;
import com.microgrid.platform.entity.DeviceRealtime;
import com.microgrid.platform.repository.DeviceMetricDataRepository;
import com.microgrid.platform.repository.DeviceRealtimeRepository;
import com.microgrid.platform.repository.DeviceRepository;
import com.microgrid.platform.service.DataIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataIngestionServiceImpl implements DataIngestionService {

    private final DeviceRepository deviceRepository;
    private final DeviceMetricDataRepository metricDataRepository;
    private final DeviceRealtimeRepository deviceRealtimeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void ingest(DataIngestRequest request) {
        if (request == null || request.getData() == null || request.getData().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }
        ingestBatch(request.getData());
    }

    @Override
    @Transactional
    public void ingestSingle(MetricDataDTO dto) {
        processSingleMetric(dto);
    }

    @Override
    @Transactional
    public void ingestBatch(List<MetricDataDTO> dataList) {
        for (MetricDataDTO dto : dataList) {
            try {
                processSingleMetric(dto);
            } catch (Exception e) {
                log.warn("处理单条数据失败: deviceCode={}, metricCode={}, error={}",
                        dto.getDeviceCode(), dto.getMetricCode(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void ingestFromMqtt(String topic, String payload) {
        try {
            log.debug("收到MQTT消息: topic={}, payload={}", topic, payload);

            String deviceCode = extractDeviceCodeFromTopic(topic);
            if (deviceCode == null) {
                log.warn("无法从主题解析设备编码: {}", topic);
                return;
            }

            List<MetricDataDTO> dataList = parseMqttPayload(deviceCode, payload);
            if (dataList != null && !dataList.isEmpty()) {
                ingestBatch(dataList);
            }
        } catch (Exception e) {
            log.error("处理MQTT消息失败: topic={}, error={}", topic, e.getMessage(), e);
        }
    }

    private void processSingleMetric(MetricDataDTO dto) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceCode(dto.getDeviceCode());
        if (deviceOpt.isEmpty()) {
            throw new BusinessException(ResultCode.DEVICE_NOT_FOUND, "设备不存在: " + dto.getDeviceCode());
        }

        Device device = deviceOpt.get();

        if (!DeviceTypeEnum.isValidMetric(device.getDeviceType(), dto.getMetricCode())) {
            log.debug("指标 {} 对于设备类型 {} 可能不是标准指标，仍然保存", dto.getMetricCode(), device.getDeviceType());
        }

        LocalDateTime ts = dto.getTs() != null ? dto.getTs() : LocalDateTime.now();

        DeviceMetricData metricData = new DeviceMetricData();
        metricData.setTs(ts);
        metricData.setDeviceCode(device.getDeviceCode());
        metricData.setDeviceId(device.getId());
        metricData.setParkId(device.getParkId());
        metricData.setMetricCode(dto.getMetricCode());
        metricData.setMetricValue(dto.getMetricValue());
        metricData.setStatusFlag(dto.getStatusFlag() != null ? dto.getStatusFlag() : 1);
        try {
            metricDataRepository.save(metricData);
        } catch (Exception e) {
            log.debug("保存时序数据失败（可能重复）: {} - {} - {}", dto.getDeviceCode(), dto.getMetricCode(), ts);
        }

        updateDeviceRealtime(device, dto, ts);

        device.setLastReportAt(ts);
        deviceRepository.save(device);
    }

    private void updateDeviceRealtime(Device device, MetricDataDTO dto, LocalDateTime ts) {
        DeviceRealtime realtime = deviceRealtimeRepository.findByDeviceCode(device.getDeviceCode())
                .orElseGet(() -> {
                    DeviceRealtime dr = new DeviceRealtime();
                    dr.setDeviceCode(device.getDeviceCode());
                    dr.setDeviceId(device.getId());
                    return dr;
                });

        if ("power".equals(dto.getMetricCode())) {
            realtime.setMetricCode(dto.getMetricCode());
            realtime.setMetricValue(dto.getMetricValue());
        }

        Map<String, Object> rawData = realtime.getRawData();
        if (rawData == null) {
            rawData = new HashMap<>();
        }
        rawData.put(dto.getMetricCode(), dto.getMetricValue());
        if (dto.getRawData() != null) {
            rawData.putAll(dto.getRawData());
        }
        realtime.setRawData(rawData);

        if (dto.getStatusFlag() != null) {
            realtime.setStatusFlag(dto.getStatusFlag());
        }
        realtime.setUpdatedAt(ts);

        deviceRealtimeRepository.save(realtime);
    }

    private String extractDeviceCodeFromTopic(String topic) {
        String[] parts = topic.split("/");
        if (parts.length >= 4) {
            return parts[3];
        }
        return null;
    }

    private List<MetricDataDTO> parseMqttPayload(String deviceCode, String payload) {
        List<MetricDataDTO> result = new ArrayList<>();
        try {
            Map<String, Object> payloadMap = objectMapper.readValue(payload,
                    new TypeReference<Map<String, Object>>() {});

            Object tsObj = payloadMap.get("ts");
            Object timestampObj = payloadMap.get("timestamp");
            Object timeObj = payloadMap.get("time");
            LocalDateTime ts = null;
            if (tsObj != null) {
                ts = parseTimestamp(tsObj);
            } else if (timestampObj != null) {
                ts = parseTimestamp(timestampObj);
            } else if (timeObj != null) {
                ts = parseTimestamp(timeObj);
            }

            Object dataObj = payloadMap.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metrics = (Map<String, Object>) dataObj;
                for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                    MetricDataDTO dto = buildMetricDTO(deviceCode, entry.getKey(), entry.getValue(), ts, payloadMap);
                    if (dto != null) {
                        result.add(dto);
                    }
                }
            } else {
                for (Map.Entry<String, Object> entry : payloadMap.entrySet()) {
                    if ("ts".equals(entry.getKey()) || "timestamp".equals(entry.getKey())
                            || "time".equals(entry.getKey()) || "data".equals(entry.getKey())) {
                        continue;
                    }
                    MetricDataDTO dto = buildMetricDTO(deviceCode, entry.getKey(), entry.getValue(), ts, payloadMap);
                    if (dto != null) {
                        result.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析MQTT payload失败: deviceCode={}, payload={}, error={}", deviceCode, payload, e.getMessage());
        }
        return result;
    }

    private MetricDataDTO buildMetricDTO(String deviceCode, String metricCode, Object value,
                                         LocalDateTime ts, Map<String, Object> rawData) {
        BigDecimal metricValue = toBigDecimal(value);
        if (metricValue == null) {
            return null;
        }
        MetricDataDTO dto = new MetricDataDTO();
        dto.setDeviceCode(deviceCode);
        dto.setMetricCode(metricCode);
        dto.setMetricValue(metricValue);
        dto.setTs(ts != null ? ts : LocalDateTime.now());
        Map<String, Object> raw = new HashMap<>();
        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            if (!"data".equals(entry.getKey())) {
                raw.put(entry.getKey(), entry.getValue());
            }
        }
        dto.setRawData(raw);
        return dto;
    }

    private LocalDateTime parseTimestamp(Object obj) {
        if (obj instanceof Number) {
            long millis = ((Number) obj).longValue();
            if (millis > 1_000_000_000_000L) {
                return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millis),
                        java.time.ZoneId.systemDefault());
            } else {
                return LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond(millis),
                        java.time.ZoneId.systemDefault());
            }
        } else if (obj instanceof String) {
            try {
                return LocalDateTime.parse((String) obj);
            } catch (Exception e) {
                return LocalDateTime.now();
            }
        }
        return LocalDateTime.now();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
