package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.AlertConstants;
import com.microgrid.platform.common.BusinessException;
import com.microgrid.platform.common.DeviceTypeEnum;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.ResultCode;
import com.microgrid.platform.dto.AlertDTO;
import com.microgrid.platform.dto.AlertQueryDTO;
import com.microgrid.platform.dto.AlertRuleCreateDTO;
import com.microgrid.platform.dto.AlertRuleDTO;
import com.microgrid.platform.entity.Alert;
import com.microgrid.platform.entity.AlertRule;
import com.microgrid.platform.entity.Device;
import com.microgrid.platform.entity.DeviceRealtime;
import com.microgrid.platform.entity.Park;
import com.microgrid.platform.repository.AlertRepository;
import com.microgrid.platform.repository.AlertRuleRepository;
import com.microgrid.platform.repository.DeviceRealtimeRepository;
import com.microgrid.platform.repository.DeviceRepository;
import com.microgrid.platform.repository.ParkRepository;
import com.microgrid.platform.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertRepository alertRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceRealtimeRepository deviceRealtimeRepository;
    private final ParkRepository parkRepository;

    @Override
    public AlertRuleDTO getRuleById(Long id) {
        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ALERT_RULE_NOT_FOUND));
        return convertRuleToDTO(rule);
    }

    @Override
    public AlertRuleDTO getRuleByCode(String ruleCode) {
        AlertRule rule = alertRuleRepository.findByRuleCode(ruleCode)
                .orElseThrow(() -> new BusinessException(ResultCode.ALERT_RULE_NOT_FOUND));
        return convertRuleToDTO(rule);
    }

    @Override
    public PageResult<AlertRuleDTO> pageRules(Integer pageNum, Integer pageSize, String ruleType, String alertLevel, Integer status, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<AlertRule> page = alertRuleRepository.searchRules(ruleType, alertLevel, status, keyword, pageable);
        List<AlertRuleDTO> dtoList = page.getContent().stream()
                .map(this::convertRuleToDTO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotalElements(), pageNum, pageSize, dtoList);
    }

    @Override
    public List<AlertRuleDTO> listRules(String ruleType, Integer status) {
        List<AlertRule> rules;
        if (ruleType != null && status != null) {
            rules = alertRuleRepository.findAll().stream()
                    .filter(r -> ruleType.equals(r.getRuleType()) && status.equals(r.getStatus()))
                    .collect(Collectors.toList());
        } else if (ruleType != null) {
            rules = alertRuleRepository.findAll().stream()
                    .filter(r -> ruleType.equals(r.getRuleType()))
                    .collect(Collectors.toList());
        } else if (status != null) {
            rules = alertRuleRepository.findByStatus(status);
        } else {
            rules = alertRuleRepository.findAll();
        }
        return rules.stream().map(this::convertRuleToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AlertRuleDTO createRule(AlertRuleCreateDTO dto) {
        if (alertRuleRepository.existsByRuleCode(dto.getRuleCode())) {
            throw new BusinessException(ResultCode.ALERT_RULE_CODE_EXISTS);
        }
        AlertRule rule = new AlertRule();
        copyRuleFromDTO(rule, dto);
        alertRuleRepository.save(rule);
        log.info("创建告警规则: {}", rule.getRuleCode());
        return convertRuleToDTO(rule);
    }

    @Override
    @Transactional
    public AlertRuleDTO updateRule(Long id, AlertRuleCreateDTO dto) {
        AlertRule rule = alertRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ALERT_RULE_NOT_FOUND));
        if (!rule.getRuleCode().equals(dto.getRuleCode())
                && alertRuleRepository.existsByRuleCode(dto.getRuleCode())) {
            throw new BusinessException(ResultCode.ALERT_RULE_CODE_EXISTS);
        }
        copyRuleFromDTO(rule, dto);
        alertRuleRepository.save(rule);
        log.info("更新告警规则: {}", rule.getRuleCode());
        return convertRuleToDTO(rule);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        if (!alertRuleRepository.existsById(id)) {
            throw new BusinessException(ResultCode.ALERT_RULE_NOT_FOUND);
        }
        alertRuleRepository.deleteById(id);
        log.info("删除告警规则: {}", id);
    }

    @Override
    @Transactional
    public void updateRuleStatus(Long id, Integer status) {
        if (!alertRuleRepository.existsById(id)) {
            throw new BusinessException(ResultCode.ALERT_RULE_NOT_FOUND);
        }
        alertRuleRepository.updateStatus(id, status);
        log.info("更新告警规则状态: id={}, status={}", id, status);
    }

    @Override
    public AlertDTO getAlertById(Long id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ALERT_NOT_FOUND));
        return convertAlertToDTO(alert);
    }

    @Override
    public PageResult<AlertDTO> pageAlerts(Integer pageNum, Integer pageSize, AlertQueryDTO query) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "triggerTime"));
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (query.getStartTime() != null && !query.getStartTime().isEmpty()) {
            startTime = LocalDateTime.parse(query.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (query.getEndTime() != null && !query.getEndTime().isEmpty()) {
            endTime = LocalDateTime.parse(query.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        Page<Alert> page = alertRepository.searchAlerts(
                query.getParkId(),
                query.getDeviceCode(),
                query.getAlertLevel(),
                query.getAlertStatus(),
                query.getRuleType(),
                startTime,
                endTime,
                query.getKeyword(),
                pageable
        );
        List<AlertDTO> dtoList = page.getContent().stream()
                .map(this::convertAlertToDTO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotalElements(), pageNum, pageSize, dtoList);
    }

    @Override
    @Transactional
    public void processAlertRules() {
        log.debug("开始处理告警规则...");
        List<AlertRule> enabledRules = alertRuleRepository.findAllEnabledRules();
        List<Device> allDevices = deviceRepository.findAll();
        Map<Long, Park> parkMap = parkRepository.findAll().stream()
                .collect(Collectors.toMap(Park::getId, p -> p));
        Map<String, Device> deviceMap = allDevices.stream()
                .collect(Collectors.toMap(Device::getDeviceCode, d -> d));
        Map<String, DeviceRealtime> realtimeMap = deviceRealtimeRepository.findAll().stream()
                .collect(Collectors.toMap(DeviceRealtime::getDeviceCode, r -> r));

        for (AlertRule rule : enabledRules) {
            List<Device> targetDevices = filterDevicesByScope(rule, allDevices, parkMap);
            for (Device device : targetDevices) {
                try {
                    processRuleForDevice(rule, device, parkMap, realtimeMap);
                } catch (Exception e) {
                    log.error("处理告警规则出错: ruleCode={}, deviceCode={}", rule.getRuleCode(), device.getDeviceCode(), e);
                }
            }
        }
        log.debug("告警规则处理完成");
    }

    private List<Device> filterDevicesByScope(AlertRule rule, List<Device> allDevices, Map<Long, Park> parkMap) {
        List<Device> result = new ArrayList<>();
        String scopeType = rule.getScopeType();
        switch (scopeType) {
            case AlertConstants.SCOPE_TYPE_ALL:
                return new ArrayList<>(allDevices);
            case AlertConstants.SCOPE_TYPE_PARK:
                if (rule.getParkId() != null) {
                    return allDevices.stream()
                            .filter(d -> rule.getParkId().equals(d.getParkId()))
                            .collect(Collectors.toList());
                }
                break;
            case AlertConstants.SCOPE_TYPE_DEVICE_TYPE:
                if (rule.getDeviceType() != null) {
                    return allDevices.stream()
                            .filter(d -> rule.getDeviceType().equals(d.getDeviceType()))
                            .collect(Collectors.toList());
                }
                break;
            case AlertConstants.SCOPE_TYPE_DEVICE:
                if (rule.getDeviceCode() != null) {
                    return allDevices.stream()
                            .filter(d -> rule.getDeviceCode().equals(d.getDeviceCode()))
                            .collect(Collectors.toList());
                }
                break;
        }
        return result;
    }

    private void processRuleForDevice(AlertRule rule, Device device, Map<Long, Park> parkMap, Map<String, DeviceRealtime> realtimeMap) {
        DeviceRealtime realtime = realtimeMap.get(device.getDeviceCode());
        boolean triggered = false;
        BigDecimal triggerValue = null;
        String alertMessage = "";

        switch (rule.getRuleType()) {
            case AlertConstants.RULE_TYPE_DEVICE_OFFLINE:
                triggered = checkDeviceOffline(rule, device, realtime);
                alertMessage = "设备离线超过阈值";
                break;
            case AlertConstants.RULE_TYPE_DATA_MISSING:
                triggered = checkDataMissing(rule, device, realtime);
                alertMessage = "数据缺失超过阈值";
                break;
            case AlertConstants.RULE_TYPE_POWER_ABNORMAL:
                if (realtime != null && realtime.getRawData() != null) {
                    Object powerObj = realtime.getRawData().get("power");
                    if (powerObj != null) {
                        BigDecimal power = new BigDecimal(powerObj.toString());
                        triggerValue = power;
                        triggered = checkThreshold(rule.getThresholdOperator(), power, rule.getThresholdValue(), rule.getThresholdValue2());
                        alertMessage = "功率异常: 当前值=" + power;
                    }
                }
                break;
            case AlertConstants.RULE_TYPE_VOLTAGE_ABNORMAL:
                if (realtime != null && realtime.getRawData() != null) {
                    Object voltageObj = realtime.getRawData().get("voltage");
                    if (voltageObj != null) {
                        BigDecimal voltage = new BigDecimal(voltageObj.toString());
                        triggerValue = voltage;
                        triggered = checkThreshold(rule.getThresholdOperator(), voltage, rule.getThresholdValue(), rule.getThresholdValue2());
                        alertMessage = "电压异常: 当前值=" + voltage;
                    }
                }
                break;
            case AlertConstants.RULE_TYPE_ESS_SOC_HIGH:
            case AlertConstants.RULE_TYPE_ESS_SOC_LOW:
                if (realtime != null && realtime.getRawData() != null) {
                    Object socObj = realtime.getRawData().get("soc");
                    if (socObj != null) {
                        BigDecimal soc = new BigDecimal(socObj.toString());
                        triggerValue = soc;
                        triggered = checkThreshold(rule.getThresholdOperator(), soc, rule.getThresholdValue(), rule.getThresholdValue2());
                        alertMessage = (AlertConstants.RULE_TYPE_ESS_SOC_HIGH.equals(rule.getRuleType()) ?
                                "储能SOC过高: 当前值=" + soc : "储能SOC过低: 当前值=" + soc);
                    }
                }
                break;
            default:
                return;
        }

        Optional<Alert> existingAlertOpt = alertRepository.findActiveAlert(rule.getId(), device.getDeviceCode());

        if (triggered) {
            if (existingAlertOpt.isPresent()) {
                Alert existing = existingAlertOpt.get();
                existing.setRawData(realtime != null ? realtime.getRawData().toString() : null);
                alertRepository.save(existing);
            } else {
                createAlert(rule, device, parkMap, triggerValue, alertMessage);
            }
        } else {
            if (existingAlertOpt.isPresent()) {
                Alert existing = existingAlertOpt.get();
                boolean recovered = checkRecovery(rule, device, realtime);
                if (recovered) {
                    existing.setAlertStatus(AlertConstants.ALERT_STATUS_RECOVERED);
                    existing.setRecoveryTime(LocalDateTime.now());
                    alertRepository.save(existing);
                    log.info("告警已恢复: alertNo={}, deviceCode={}", existing.getAlertNo(), device.getDeviceCode());
                }
            }
        }
    }

    private boolean checkDeviceOffline(AlertRule rule, Device device, DeviceRealtime realtime) {
        LocalDateTime lastReport = device.getLastReportAt();
        if (lastReport == null && realtime != null) {
            lastReport = realtime.getUpdatedAt();
        }
        if (lastReport == null) {
            return true;
        }
        long secondsSinceReport = ChronoUnit.SECONDS.between(lastReport, LocalDateTime.now());
        int threshold = rule.getDurationSeconds() > 0 ? rule.getDurationSeconds() : 300;
        return secondsSinceReport > threshold;
    }

    private boolean checkDataMissing(AlertRule rule, Device device, DeviceRealtime realtime) {
        if (realtime == null) {
            return true;
        }
        LocalDateTime updated = realtime.getUpdatedAt();
        if (updated == null) {
            return true;
        }
        long secondsSinceUpdate = ChronoUnit.SECONDS.between(updated, LocalDateTime.now());
        int threshold = rule.getDurationSeconds() > 0 ? rule.getDurationSeconds() : 60;
        return secondsSinceUpdate > threshold;
    }

    private boolean checkThreshold(String operator, BigDecimal value, BigDecimal threshold, BigDecimal threshold2) {
        if (operator == null || value == null || threshold == null) {
            return false;
        }
        int cmp = value.compareTo(threshold);
        switch (operator) {
            case AlertConstants.OPERATOR_GT:
                return cmp > 0;
            case AlertConstants.OPERATOR_GTE:
                return cmp >= 0;
            case AlertConstants.OPERATOR_LT:
                return cmp < 0;
            case AlertConstants.OPERATOR_LTE:
                return cmp <= 0;
            case AlertConstants.OPERATOR_EQ:
                return cmp == 0;
            case AlertConstants.OPERATOR_NEQ:
                return cmp != 0;
            case AlertConstants.OPERATOR_BETWEEN:
                return threshold2 != null && cmp >= 0 && value.compareTo(threshold2) <= 0;
            case AlertConstants.OPERATOR_NOT_BETWEEN:
                return threshold2 != null && (cmp < 0 || value.compareTo(threshold2) > 0);
            default:
                return false;
        }
    }

    private boolean checkRecovery(AlertRule rule, Device device, DeviceRealtime realtime) {
        if (rule.getRecoveryOperator() == null || rule.getRecoveryValue() == null) {
            return true;
        }
        if (realtime == null || realtime.getRawData() == null) {
            return false;
        }
        String metricCode = rule.getMetricCode();
        if (metricCode == null) {
            return !checkDeviceOffline(rule, device, realtime);
        }
        Object valObj = realtime.getRawData().get(metricCode);
        if (valObj == null) {
            return false;
        }
        BigDecimal value = new BigDecimal(valObj.toString());
        return !checkThreshold(rule.getRecoveryOperator(), value, rule.getRecoveryValue(), rule.getRecoveryValue2());
    }

    private void createAlert(AlertRule rule, Device device, Map<Long, Park> parkMap, BigDecimal triggerValue, String message) {
        Alert alert = new Alert();
        alert.setAlertNo("ALT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        alert.setRuleId(rule.getId());
        alert.setRuleName(rule.getRuleName());
        alert.setRuleCode(rule.getRuleCode());
        alert.setRuleType(rule.getRuleType());
        alert.setAlertLevel(rule.getAlertLevel());
        alert.setParkId(device.getParkId());
        Park park = parkMap.get(device.getParkId());
        if (park != null) {
            alert.setParkName(park.getParkName());
        }
        alert.setDeviceId(device.getId());
        alert.setDeviceCode(device.getDeviceCode());
        alert.setDeviceName(device.getDeviceName());
        alert.setDeviceType(device.getDeviceType());
        alert.setTriggerValue(triggerValue);
        alert.setThresholdValue(rule.getThresholdValue());
        alert.setMetricCode(rule.getMetricCode());
        alert.setAlertStatus(AlertConstants.ALERT_STATUS_TRIGGERED);
        alert.setTriggerTime(LocalDateTime.now());
        alert.setAlertMessage(message);
        alertRepository.save(alert);
        log.info("生成告警: alertNo={}, deviceCode={}, ruleCode={}", alert.getAlertNo(), device.getDeviceCode(), rule.getRuleCode());
    }

    private AlertRuleDTO convertRuleToDTO(AlertRule rule) {
        AlertRuleDTO dto = new AlertRuleDTO();
        dto.setId(rule.getId());
        dto.setRuleName(rule.getRuleName());
        dto.setRuleCode(rule.getRuleCode());
        dto.setRuleType(rule.getRuleType());
        dto.setRuleTypeName(AlertConstants.RULE_TYPE_MAP.getOrDefault(rule.getRuleType(), rule.getRuleType()));
        dto.setAlertLevel(rule.getAlertLevel());
        dto.setAlertLevelName(AlertConstants.ALERT_LEVEL_MAP.getOrDefault(rule.getAlertLevel(), rule.getAlertLevel()));
        dto.setScopeType(rule.getScopeType());
        dto.setScopeTypeName(AlertConstants.SCOPE_TYPE_MAP.getOrDefault(rule.getScopeType(), rule.getScopeType()));
        dto.setScopeValue(rule.getScopeValue());
        dto.setParkId(rule.getParkId());
        if (rule.getParkId() != null) {
            parkRepository.findById(rule.getParkId()).ifPresent(p -> dto.setParkName(p.getParkName()));
        }
        dto.setDeviceType(rule.getDeviceType());
        dto.setDeviceTypeName(DeviceTypeEnum.getDescription(rule.getDeviceType()));
        dto.setDeviceCode(rule.getDeviceCode());
        dto.setThresholdOperator(rule.getThresholdOperator());
        dto.setThresholdValue(rule.getThresholdValue());
        dto.setThresholdValue2(rule.getThresholdValue2());
        dto.setMetricCode(rule.getMetricCode());
        dto.setDurationSeconds(rule.getDurationSeconds());
        dto.setRecoveryOperator(rule.getRecoveryOperator());
        dto.setRecoveryValue(rule.getRecoveryValue());
        dto.setRecoveryValue2(rule.getRecoveryValue2());
        dto.setDescription(rule.getDescription());
        dto.setStatus(rule.getStatus());
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedAt(rule.getUpdatedAt());
        return dto;
    }

    private AlertDTO convertAlertToDTO(Alert alert) {
        AlertDTO dto = new AlertDTO();
        dto.setId(alert.getId());
        dto.setAlertNo(alert.getAlertNo());
        dto.setRuleId(alert.getRuleId());
        dto.setRuleName(alert.getRuleName());
        dto.setRuleCode(alert.getRuleCode());
        dto.setRuleType(alert.getRuleType());
        dto.setRuleTypeName(AlertConstants.RULE_TYPE_MAP.getOrDefault(alert.getRuleType(), alert.getRuleType()));
        dto.setAlertLevel(alert.getAlertLevel());
        dto.setAlertLevelName(AlertConstants.ALERT_LEVEL_MAP.getOrDefault(alert.getAlertLevel(), alert.getAlertLevel()));
        dto.setParkId(alert.getParkId());
        dto.setParkName(alert.getParkName());
        dto.setDeviceId(alert.getDeviceId());
        dto.setDeviceCode(alert.getDeviceCode());
        dto.setDeviceName(alert.getDeviceName());
        dto.setDeviceType(alert.getDeviceType());
        dto.setDeviceTypeName(DeviceTypeEnum.getDescription(alert.getDeviceType()));
        dto.setTriggerValue(alert.getTriggerValue());
        dto.setThresholdValue(alert.getThresholdValue());
        dto.setMetricCode(alert.getMetricCode());
        dto.setAlertStatus(alert.getAlertStatus());
        dto.setAlertStatusName(AlertConstants.ALERT_STATUS_MAP.getOrDefault(alert.getAlertStatus(), alert.getAlertStatus()));
        dto.setTriggerTime(alert.getTriggerTime());
        dto.setRecoveryTime(alert.getRecoveryTime());
        dto.setAlertMessage(alert.getAlertMessage());
        dto.setRawData(alert.getRawData());
        dto.setCreatedAt(alert.getCreatedAt());
        dto.setUpdatedAt(alert.getUpdatedAt());
        return dto;
    }

    private void copyRuleFromDTO(AlertRule rule, AlertRuleCreateDTO dto) {
        rule.setRuleName(dto.getRuleName());
        rule.setRuleCode(dto.getRuleCode());
        rule.setRuleType(dto.getRuleType());
        rule.setAlertLevel(dto.getAlertLevel());
        rule.setScopeType(dto.getScopeType());
        rule.setScopeValue(dto.getScopeValue());
        rule.setParkId(dto.getParkId());
        rule.setDeviceType(dto.getDeviceType());
        rule.setDeviceCode(dto.getDeviceCode());
        rule.setThresholdOperator(dto.getThresholdOperator());
        rule.setThresholdValue(dto.getThresholdValue());
        rule.setThresholdValue2(dto.getThresholdValue2());
        rule.setMetricCode(dto.getMetricCode());
        rule.setDurationSeconds(dto.getDurationSeconds() != null ? dto.getDurationSeconds() : 0);
        rule.setRecoveryOperator(dto.getRecoveryOperator());
        rule.setRecoveryValue(dto.getRecoveryValue());
        rule.setRecoveryValue2(dto.getRecoveryValue2());
        rule.setDescription(dto.getDescription());
        rule.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
    }
}
