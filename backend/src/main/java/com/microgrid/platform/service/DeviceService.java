package com.microgrid.platform.service;

import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.dto.DeviceCreateDTO;
import com.microgrid.platform.dto.DeviceDTO;

import java.util.List;
import java.util.Map;

public interface DeviceService {

    DeviceDTO getById(Long id);

    DeviceDTO getByCode(String deviceCode);

    PageResult<DeviceDTO> page(Integer pageNum, Integer pageSize, Long parkId, String deviceType, String keyword);

    List<DeviceDTO> list(Long parkId, String deviceType);

    DeviceDTO create(DeviceCreateDTO dto);

    DeviceDTO update(Long id, DeviceCreateDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    Map<String, Integer> getDeviceStatusStats(Long parkId);

    boolean isDeviceOnline(String deviceCode);

    DeviceDTO enrichDeviceWithRealtime(DeviceDTO device);
}
