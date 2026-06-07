package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.BusinessException;
import com.microgrid.platform.common.Constants;
import com.microgrid.platform.common.DeviceTypeEnum;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.ResultCode;
import com.microgrid.platform.dto.DeviceCreateDTO;
import com.microgrid.platform.dto.DeviceDTO;
import com.microgrid.platform.entity.Device;
import com.microgrid.platform.entity.DeviceRealtime;
import com.microgrid.platform.entity.Park;
import com.microgrid.platform.repository.DeviceRealtimeRepository;
import com.microgrid.platform.repository.DeviceRepository;
import com.microgrid.platform.repository.ParkRepository;
import com.microgrid.platform.service.DeviceService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final ParkRepository parkRepository;
    private final DeviceRealtimeRepository deviceRealtimeRepository;

    @Override
    public DeviceDTO getById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DEVICE_NOT_FOUND));
        return convertToDTO(device);
    }

    @Override
    public DeviceDTO getByCode(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new BusinessException(ResultCode.DEVICE_NOT_FOUND));
        return convertToDTO(device);
    }

    @Override
    public PageResult<DeviceDTO> page(Integer pageNum, Integer pageSize, Long parkId, String deviceType, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Device> page = deviceRepository.searchDevices(parkId, deviceType, keyword, pageable);

        List<DeviceDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), pageNum, pageSize, dtoList);
    }

    @Override
    public List<DeviceDTO> list(Long parkId, String deviceType) {
        List<Device> devices;
        if (parkId != null && deviceType != null) {
            devices = deviceRepository.findByParkIdAndDeviceType(parkId, deviceType);
        } else if (parkId != null) {
            devices = deviceRepository.findByParkId(parkId);
        } else if (deviceType != null) {
            devices = deviceRepository.findByDeviceType(deviceType);
        } else {
            devices = deviceRepository.findAll();
        }
        return devices.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeviceDTO create(DeviceCreateDTO dto) {
        if (deviceRepository.existsByDeviceCode(dto.getDeviceCode())) {
            throw new BusinessException(ResultCode.DEVICE_CODE_EXISTS);
        }
        if (!parkRepository.existsById(dto.getParkId())) {
            throw new BusinessException(ResultCode.PARK_NOT_FOUND);
        }

        Device device = new Device();
        device.setDeviceCode(dto.getDeviceCode());
        device.setDeviceName(dto.getDeviceName());
        device.setDeviceType(dto.getDeviceType());
        device.setParkId(dto.getParkId());
        device.setInstallLocation(dto.getInstallLocation());
        device.setRatedPower(dto.getRatedPower());
        device.setAccessMethod(dto.getAccessMethod());
        device.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        device.setRemark(dto.getRemark());
        device = deviceRepository.save(device);

        log.info("创建设备成功: {}", device.getDeviceCode());
        return convertToDTO(device);
    }

    @Override
    @Transactional
    public DeviceDTO update(Long id, DeviceCreateDTO dto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DEVICE_NOT_FOUND));

        if (dto.getDeviceName() != null) device.setDeviceName(dto.getDeviceName());
        if (dto.getDeviceType() != null) device.setDeviceType(dto.getDeviceType());
        if (dto.getParkId() != null) device.setParkId(dto.getParkId());
        if (dto.getInstallLocation() != null) device.setInstallLocation(dto.getInstallLocation());
        if (dto.getRatedPower() != null) device.setRatedPower(dto.getRatedPower());
        if (dto.getAccessMethod() != null) device.setAccessMethod(dto.getAccessMethod());
        if (dto.getStatus() != null) device.setStatus(dto.getStatus());
        if (dto.getRemark() != null) device.setRemark(dto.getRemark());

        device = deviceRepository.save(device);
        log.info("更新设备成功: {}", device.getDeviceCode());
        return convertToDTO(device);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new BusinessException(ResultCode.DEVICE_NOT_FOUND);
        }
        deviceRepository.deleteById(id);
        log.info("删除设备成功: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        deviceRepository.updateStatus(id, status);
        log.info("更新设备状态: id={}, status={}", id, status);
    }

    @Override
    public Map<String, Integer> getDeviceStatusStats(Long parkId) {
        List<Object[]> results = deviceRepository.countByStatusForPark(parkId);
        Map<String, Integer> stats = new HashMap<>();
        stats.put("online", 0);
        stats.put("offline", 0);
        stats.put("fault", 0);

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(Constants.DEVICE_TIMEOUT_SECONDS);
        Long total = deviceRepository.countByParkId(parkId);
        Long online = deviceRepository.countOnlineByParkId(parkId, threshold);
        stats.put("online", online != null ? online.intValue() : 0);
        stats.put("offline", (total != null ? total.intValue() : 0) - (online != null ? online.intValue() : 0));

        for (Object[] row : results) {
            Integer status = (Integer) row[0];
            Long count = (Long) row[1];
            if (status != null && status == 2) {
                stats.put("fault", count != null ? count.intValue() : 0);
            }
        }
        return stats;
    }

    @Override
    public boolean isDeviceOnline(String deviceCode) {
        Device device = deviceRepository.findByDeviceCode(deviceCode).orElse(null);
        if (device == null || device.getLastReportAt() == null) {
            return false;
        }
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(Constants.DEVICE_TIMEOUT_SECONDS);
        return device.getLastReportAt().isAfter(threshold);
    }

    @Override
    public DeviceDTO enrichDeviceWithRealtime(DeviceDTO device) {
        if (device == null) {
            return null;
        }

        if (device.getLastReportAt() != null) {
            LocalDateTime threshold = LocalDateTime.now().minusSeconds(Constants.DEVICE_TIMEOUT_SECONDS);
            device.setIsOnline(device.getLastReportAt().isAfter(threshold));
        } else {
            device.setIsOnline(false);
        }

        DeviceRealtime realtime = deviceRealtimeRepository.findByDeviceCode(device.getDeviceCode()).orElse(null);
        if (realtime != null) {
            if (realtime.getMetricValue() != null) {
                device.setCurrentPower(realtime.getMetricValue());
            }
            Map<String, Object> metrics = new HashMap<>();
            if (realtime.getRawData() != null) {
                metrics.putAll(realtime.getRawData());
            }
            if (realtime.getMetricCode() != null && realtime.getMetricValue() != null) {
                metrics.put(realtime.getMetricCode(), realtime.getMetricValue());
            }
            device.setMetrics(metrics);
        }

        return device;
    }

    private DeviceDTO convertToDTO(Device device) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setDeviceCode(device.getDeviceCode());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());

        DeviceTypeEnum typeEnum = DeviceTypeEnum.fromCode(device.getDeviceType());
        if (typeEnum != null) {
            dto.setDeviceTypeName(typeEnum.getName());
        }

        dto.setParkId(device.getParkId());
        Park park = parkRepository.findById(device.getParkId()).orElse(null);
        if (park != null) {
            dto.setParkName(park.getParkName());
        }

        dto.setInstallLocation(device.getInstallLocation());
        dto.setRatedPower(device.getRatedPower());
        dto.setAccessMethod(device.getAccessMethod());
        dto.setStatus(device.getStatus());
        dto.setRemark(device.getRemark());
        dto.setLastReportAt(device.getLastReportAt());
        dto.setCreatedAt(device.getCreatedAt());
        dto.setUpdatedAt(device.getUpdatedAt());

        return enrichDeviceWithRealtime(dto);
    }
}
