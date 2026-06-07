package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.dto.HistoryQueryDTO;
import com.microgrid.platform.entity.Device;
import com.microgrid.platform.entity.DeviceMetricData;
import com.microgrid.platform.repository.DeviceMetricDataRepository;
import com.microgrid.platform.repository.DeviceRepository;
import com.microgrid.platform.service.HistoryService;
import com.microgrid.platform.vo.HistoryDataVO;
import com.microgrid.platform.vo.HistoryQueryResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final DeviceMetricDataRepository metricDataRepository;
    private final DeviceRepository deviceRepository;

    private static final Map<String, String> METRIC_NAME_MAP = new HashMap<>();
    private static final Map<String, String> METRIC_UNIT_MAP = new HashMap<>();

    static {
        METRIC_NAME_MAP.put("power", "有功功率");
        METRIC_NAME_MAP.put("soc", "SOC");
        METRIC_NAME_MAP.put("voltage", "电压");
        METRIC_NAME_MAP.put("current", "电流");
        METRIC_NAME_MAP.put("temperature", "温度");
        METRIC_NAME_MAP.put("energy", "累计电量");
        METRIC_NAME_MAP.put("status", "状态");
        METRIC_NAME_MAP.put("charging_status", "充电状态");
        METRIC_NAME_MAP.put("wind_speed", "风速");

        METRIC_UNIT_MAP.put("power", "kW");
        METRIC_UNIT_MAP.put("soc", "%");
        METRIC_UNIT_MAP.put("voltage", "V");
        METRIC_UNIT_MAP.put("current", "A");
        METRIC_UNIT_MAP.put("temperature", "℃");
        METRIC_UNIT_MAP.put("energy", "kWh");
        METRIC_UNIT_MAP.put("wind_speed", "m/s");
    }

    @Override
    public PageResult<HistoryDataVO> queryPage(HistoryQueryDTO query) {
        fillDefaultTime(query);

        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 50;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "ts"));

        Page<DeviceMetricData> page = metricDataRepository.findByConditions(
                query.getParkId(),
                query.getDeviceCode(),
                query.getMetricCode(),
                query.getStartTime(),
                query.getEndTime(),
                pageable
        );

        List<String> deviceCodes = page.getContent().stream()
                .map(DeviceMetricData::getDeviceCode)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Device> deviceMap = buildDeviceMap(deviceCodes);

        List<HistoryDataVO> voList = page.getContent().stream()
                .map(d -> convertToVO(d, deviceMap))
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), pageNum, pageSize, voList);
    }

    @Override
    public List<HistoryDataVO> queryList(HistoryQueryDTO query) {
        fillDefaultTime(query);

        List<DeviceMetricData> list = metricDataRepository.findListByConditions(
                query.getParkId(),
                query.getDeviceCode(),
                query.getMetricCode(),
                query.getStartTime(),
                query.getEndTime()
        );

        List<String> deviceCodes = list.stream()
                .map(DeviceMetricData::getDeviceCode)
                .distinct()
                .collect(Collectors.toList());
        Map<String, Device> deviceMap = buildDeviceMap(deviceCodes);

        return list.stream()
                .map(d -> convertToVO(d, deviceMap))
                .collect(Collectors.toList());
    }

    @Override
    public HistoryQueryResultVO query(HistoryQueryDTO query) {
        HistoryQueryResultVO result = new HistoryQueryResultVO();
        result.setPageData(queryPage(query));
        result.setChartData(queryList(query));
        return result;
    }

    private void fillDefaultTime(HistoryQueryDTO query) {
        if (query.getEndTime() == null) {
            query.setEndTime(LocalDateTime.now());
        }
        if (query.getStartTime() == null) {
            query.setStartTime(query.getEndTime().minusHours(1));
        }
    }

    private Map<String, Device> buildDeviceMap(List<String> deviceCodes) {
        Map<String, Device> map = new HashMap<>();
        if (deviceCodes == null || deviceCodes.isEmpty()) {
            return map;
        }
        for (String code : deviceCodes) {
            deviceRepository.findByDeviceCode(code).ifPresent(d -> map.put(code, d));
        }
        return map;
    }

    private HistoryDataVO convertToVO(DeviceMetricData data, Map<String, Device> deviceMap) {
        HistoryDataVO vo = new HistoryDataVO();
        vo.setTs(data.getTs());
        vo.setDeviceCode(data.getDeviceCode());
        vo.setMetricCode(data.getMetricCode());
        vo.setMetricValue(data.getMetricValue() != null ? data.getMetricValue() : BigDecimal.ZERO);

        Device device = deviceMap.get(data.getDeviceCode());
        if (device != null) {
            vo.setDeviceName(device.getDeviceName());
        }

        vo.setMetricName(METRIC_NAME_MAP.getOrDefault(data.getMetricCode(), data.getMetricCode()));
        vo.setUnit(METRIC_UNIT_MAP.get(data.getMetricCode()));

        return vo;
    }
}
