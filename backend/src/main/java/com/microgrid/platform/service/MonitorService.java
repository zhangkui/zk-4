package com.microgrid.platform.service;

import com.microgrid.platform.dto.DeviceDTO;
import com.microgrid.platform.vo.DeviceStatusStatVO;
import com.microgrid.platform.vo.RealtimeOverviewVO;
import com.microgrid.platform.vo.TrendDataVO;

import java.util.List;

public interface MonitorService {

    RealtimeOverviewVO getOverview(Long parkId);

    TrendDataVO getTrend(Long parkId, Integer hours);

    List<DeviceStatusStatVO> getDeviceStatusStats(Long parkId);

    TrendDataVO getDeviceCurve(String deviceCode, Integer hours);

    List<DeviceDTO> getRealtimeDevices(Long parkId);
}
