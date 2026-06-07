package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.Constants;
import com.microgrid.platform.dto.DeviceDTO;
import com.microgrid.platform.entity.Device;
import com.microgrid.platform.entity.DeviceMetricData;
import com.microgrid.platform.entity.DeviceRealtime;
import com.microgrid.platform.entity.Park;
import com.microgrid.platform.repository.DeviceMetricDataRepository;
import com.microgrid.platform.repository.DeviceRealtimeRepository;
import com.microgrid.platform.repository.DeviceRepository;
import com.microgrid.platform.repository.ParkRepository;
import com.microgrid.platform.service.DeviceService;
import com.microgrid.platform.service.MonitorService;
import com.microgrid.platform.vo.DeviceStatusStatVO;
import com.microgrid.platform.vo.RealtimeOverviewVO;
import com.microgrid.platform.vo.TrendDataVO;
import com.microgrid.platform.vo.TrendPointVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final DeviceRepository deviceRepository;
    private final DeviceRealtimeRepository deviceRealtimeRepository;
    private final DeviceMetricDataRepository deviceMetricDataRepository;
    private final ParkRepository parkRepository;
    private final DeviceService deviceService;

    @Override
    public RealtimeOverviewVO getOverview(Long parkId) {
        RealtimeOverviewVO vo = new RealtimeOverviewVO();
        vo.setParkId(parkId);

        Park park = parkRepository.findById(parkId).orElse(null);
        if (park != null) {
            vo.setParkName(park.getParkName());
        }

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(Constants.DEVICE_TIMEOUT_SECONDS);

        List<DeviceRealtime> realtimes = deviceRealtimeRepository.findByParkId(parkId);

        BigDecimal pvPower = BigDecimal.ZERO;
        BigDecimal windPower = BigDecimal.ZERO;
        BigDecimal essPower = BigDecimal.ZERO;
        BigDecimal essSoc = BigDecimal.ZERO;
        int essCount = 0;
        BigDecimal loadPower = BigDecimal.ZERO;

        List<Device> devices = deviceRepository.findByParkId(parkId);
        Map<String, Device> deviceMap = new HashMap<>();
        for (Device d : devices) {
            deviceMap.put(d.getDeviceCode(), d);
        }

        int chargingOnline = 0;
        int chargingTotal = 0;
        int onlineCount = 0;

        for (DeviceRealtime rt : realtimes) {
            Device device = deviceMap.get(rt.getDeviceCode());
            if (device == null) continue;

            boolean isOnline = device.getLastReportAt() != null && device.getLastReportAt().isAfter(threshold);
            if (isOnline) onlineCount++;

            String type = device.getDeviceType();
            BigDecimal power = rt.getMetricValue() != null ? rt.getMetricValue() : BigDecimal.ZERO;

            if ("PV".equals(type)) {
                pvPower = pvPower.add(power);
            } else if ("WIND".equals(type)) {
                windPower = windPower.add(power);
            } else if ("ESS".equals(type)) {
                essPower = essPower.add(power);
                if (rt.getRawData() != null && rt.getRawData().containsKey("soc")) {
                    Object socObj = rt.getRawData().get("soc");
                    if (socObj instanceof Number) {
                        essSoc = essSoc.add(BigDecimal.valueOf(((Number) socObj).doubleValue()));
                        essCount++;
                    }
                }
            } else if ("LOAD".equals(type) || "METER".equals(type)) {
                loadPower = loadPower.add(power);
            } else if ("CHARGING".equals(type)) {
                chargingTotal++;
                if (isOnline) chargingOnline++;
            }
        }

        for (Device device : devices) {
            if ("CHARGING".equals(device.getDeviceType())) {
                chargingTotal = Math.max(chargingTotal, 1);
            }
        }

        if (essCount > 0) {
            essSoc = essSoc.divide(BigDecimal.valueOf(essCount), 2, BigDecimal.ROUND_HALF_UP);
        }

        vo.setPvPower(pvPower);
        vo.setWindPower(windPower);
        vo.setEssPower(essPower);
        vo.setEssSoc(essSoc);
        vo.setTotalGenerationPower(pvPower.add(windPower));
        vo.setTotalLoadPower(loadPower);
        vo.setGridPower(vo.getTotalLoadPower().subtract(vo.getTotalGenerationPower()).add(essPower));
        vo.setChargingOnlineCount(chargingOnline);
        vo.setChargingTotalCount(chargingTotal);
        vo.setOnlineDeviceCount(onlineCount);
        vo.setTotalDeviceCount(devices.size());
        vo.setOnlineRate(devices.size() > 0 ? (onlineCount * 100.0 / devices.size()) : 0);

        vo.setDeviceStatusStats(getDeviceStatusStatsMap(parkId, devices, threshold));

        return vo;
    }

    @Override
    public TrendDataVO getTrend(Long parkId, Integer hours) {
        if (hours == null || hours <= 0) {
            hours = 1;
        }
        TrendDataVO vo = new TrendDataVO();
        vo.setGeneration(new ArrayList<>());
        vo.setLoad(new ArrayList<>());
        vo.setEss(new ArrayList<>());
        vo.setPv(new ArrayList<>());
        vo.setWind(new ArrayList<>());

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);

        List<Object[]> pvTrend = deviceMetricDataRepository.sumPowerTrendByParkAndDeviceTypes(
                parkId, Arrays.asList("PV"), startTime, endTime);
        List<Object[]> windTrend = deviceMetricDataRepository.sumPowerTrendByParkAndDeviceTypes(
                parkId, Arrays.asList("WIND"), startTime, endTime);
        List<Object[]> essTrend = deviceMetricDataRepository.sumPowerTrendByParkAndDeviceTypes(
                parkId, Arrays.asList("ESS"), startTime, endTime);
        List<Object[]> loadTrend = deviceMetricDataRepository.sumPowerTrendByParkAndDeviceTypes(
                parkId, Arrays.asList("LOAD", "METER"), startTime, endTime);

        Map<LocalDateTime, BigDecimal> pvMap = buildTrendMap(pvTrend);
        Map<LocalDateTime, BigDecimal> windMap = buildTrendMap(windTrend);
        Map<LocalDateTime, BigDecimal> essMap = buildTrendMap(essTrend);
        Map<LocalDateTime, BigDecimal> loadMap = buildTrendMap(loadTrend);

        int minutesPerPoint = Math.max(1, hours * 60 / 60);
        LocalDateTime cursor = startTime.withSecond(0).withNano(0);
        while (!cursor.isAfter(endTime)) {
            LocalDateTime t = cursor;
            BigDecimal pv = findClosestValue(pvMap, t);
            BigDecimal wind = findClosestValue(windMap, t);
            BigDecimal ess = findClosestValue(essMap, t);
            BigDecimal load = findClosestValue(loadMap, t);

            vo.getPv().add(new TrendPointVO(t, pv));
            vo.getWind().add(new TrendPointVO(t, wind));
            vo.getEss().add(new TrendPointVO(t, ess));
            vo.getLoad().add(new TrendPointVO(t, load));
            vo.getGeneration().add(new TrendPointVO(t, pv.add(wind)));

            cursor = cursor.plusMinutes(minutesPerPoint);
        }

        return vo;
    }

    @Override
    public List<DeviceStatusStatVO> getDeviceStatusStats(Long parkId) {
        List<DeviceStatusStatVO> result = new ArrayList<>();
        Map<String, Integer> stats = getDeviceStatusStatsMap(parkId, null, null);
        result.add(new DeviceStatusStatVO("online", "在线", stats.getOrDefault("online", 0)));
        result.add(new DeviceStatusStatVO("offline", "离线", stats.getOrDefault("offline", 0)));
        result.add(new DeviceStatusStatVO("fault", "故障", stats.getOrDefault("fault", 0)));
        return result;
    }

    private Map<String, Integer> getDeviceStatusStatsMap(Long parkId, List<Device> devices, LocalDateTime threshold) {
        if (threshold == null) {
            threshold = LocalDateTime.now().minusSeconds(Constants.DEVICE_TIMEOUT_SECONDS);
        }
        if (devices == null) {
            devices = deviceRepository.findByParkId(parkId);
        }

        Map<String, Integer> stats = new HashMap<>();
        int online = 0;
        int offline = 0;
        int fault = 0;

        for (Device d : devices) {
            if (d.getStatus() != null && d.getStatus() == 2) {
                fault++;
            } else if (d.getLastReportAt() != null && d.getLastReportAt().isAfter(threshold)) {
                online++;
            } else {
                offline++;
            }
        }

        stats.put("online", online);
        stats.put("offline", offline);
        stats.put("fault", fault);
        return stats;
    }

    private Map<LocalDateTime, BigDecimal> buildTrendMap(List<Object[]> data) {
        Map<LocalDateTime, BigDecimal> map = new HashMap<>();
        if (data != null) {
            for (Object[] row : data) {
                if (row.length >= 2 && row[0] instanceof LocalDateTime ts && row[1] instanceof BigDecimal v) {
                    map.put(ts, v);
                } else if (row.length >= 2 && row[0] instanceof LocalDateTime ts && row[1] instanceof Number n) {
                    map.put(ts, BigDecimal.valueOf(n.doubleValue()));
                }
            }
        }
        return map;
    }

    private BigDecimal findClosestValue(Map<LocalDateTime, BigDecimal> map, LocalDateTime target) {
        if (map.isEmpty()) return BigDecimal.ZERO;
        if (map.containsKey(target)) return map.get(target);

        BigDecimal closest = BigDecimal.ZERO;
        long minDiff = Long.MAX_VALUE;

        for (Map.Entry<LocalDateTime, BigDecimal> entry : map.entrySet()) {
            long diff = Math.abs(java.time.Duration.between(target, entry.getKey()).toSeconds());
            if (diff < minDiff && diff < 300) {
                minDiff = diff;
                closest = entry.getValue();
            }
        }

        return closest;
    }

    @Override
    public TrendDataVO getDeviceCurve(String deviceCode, Integer hours) {
        if (hours == null || hours <= 0) {
            hours = 24;
        }
        TrendDataVO vo = new TrendDataVO();
        vo.setGeneration(new ArrayList<>());
        vo.setLoad(new ArrayList<>());
        vo.setEss(new ArrayList<>());
        vo.setPv(new ArrayList<>());
        vo.setWind(new ArrayList<>());

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);

        List<DeviceMetricData> dataList = deviceMetricDataRepository.findListByConditions(
                null, deviceCode, null, startTime, endTime);

        Map<String, Map<LocalDateTime, BigDecimal>> metricMap = new HashMap<>();
        for (DeviceMetricData data : dataList) {
            String metricCode = data.getMetricCode();
            metricMap.computeIfAbsent(metricCode, k -> new HashMap<>())
                    .put(data.getTs(), data.getMetricValue());
        }

        int minutesPerPoint = Math.max(1, hours * 60 / 60);
        LocalDateTime cursor = startTime.withSecond(0).withNano(0);

        Map<String, List<TrendPointVO>> curveMap = new HashMap<>();
        while (!cursor.isAfter(endTime)) {
            LocalDateTime t = cursor;
            for (Map.Entry<String, Map<LocalDateTime, BigDecimal>> entry : metricMap.entrySet()) {
                String metricCode = entry.getKey();
                BigDecimal value = findClosestValue(entry.getValue(), t);
                curveMap.computeIfAbsent(metricCode, k -> new ArrayList<>())
                        .add(new TrendPointVO(t, value));
            }
            cursor = cursor.plusMinutes(minutesPerPoint);
        }

        if (curveMap.containsKey("power")) {
            vo.setGeneration(curveMap.get("power"));
        }
        if (curveMap.containsKey("soc")) {
            vo.setEss(curveMap.get("soc"));
        }

        return vo;
    }

    @Override
    public List<DeviceDTO> getRealtimeDevices(Long parkId) {
        List<Device> devices = deviceRepository.findByParkId(parkId);
        return devices.stream()
                .map(d -> {
                    DeviceDTO dto = convertToSimpleDTO(d);
                    return deviceService.enrichDeviceWithRealtime(dto);
                })
                .collect(Collectors.toList());
    }

    private DeviceDTO convertToSimpleDTO(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setDeviceCode(device.getDeviceCode());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());
        dto.setParkId(device.getParkId());
        dto.setInstallLocation(device.getInstallLocation());
        dto.setRatedPower(device.getRatedPower());
        dto.setAccessMethod(device.getAccessMethod());
        dto.setStatus(device.getStatus());
        dto.setRemark(device.getRemark());
        dto.setLastReportAt(device.getLastReportAt());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());
        return dto;
    }
}
