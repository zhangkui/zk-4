package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.DeviceDTO;
import com.microgrid.platform.service.MonitorService;
import com.microgrid.platform.vo.DeviceStatusStatVO;
import com.microgrid.platform.vo.RealtimeOverviewVO;
import com.microgrid.platform.vo.TrendDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("/overview")
    @RequirePermission("dashboard")
    public Result<RealtimeOverviewVO> getOverview(@RequestParam(required = false, defaultValue = "1") Long parkId) {
        return Result.success(monitorService.getOverview(parkId));
    }

    @GetMapping("/dashboard")
    @RequirePermission("dashboard")
    public Result<RealtimeOverviewVO> getDashboard(@RequestParam(required = false, defaultValue = "1") Long parkId) {
        return Result.success(monitorService.getOverview(parkId));
    }

    @GetMapping("/trend")
    @RequirePermission("dashboard")
    public Result<TrendDataVO> getTrend(@RequestParam(required = false, defaultValue = "1") Long parkId,
                                        @RequestParam(required = false, defaultValue = "1") Integer hours) {
        return Result.success(monitorService.getTrend(parkId, hours));
    }

    @GetMapping("/power-curve")
    @RequirePermission("dashboard")
    public Result<TrendDataVO> getPowerCurve(@RequestParam(required = false, defaultValue = "1") Long parkId,
                                             @RequestParam(required = false, defaultValue = "1") Integer hours) {
        return Result.success(monitorService.getTrend(parkId, hours));
    }

    @GetMapping("/device-status")
    @RequirePermission("dashboard")
    public Result<List<DeviceStatusStatVO>> getDeviceStatusStats(@RequestParam(required = false, defaultValue = "1") Long parkId) {
        return Result.success(monitorService.getDeviceStatusStats(parkId));
    }

    @GetMapping("/device-status-pie")
    @RequirePermission("dashboard")
    public Result<List<DeviceStatusStatVO>> getDeviceStatusPie(@RequestParam(required = false, defaultValue = "1") Long parkId) {
        return Result.success(monitorService.getDeviceStatusStats(parkId));
    }

    @GetMapping("/device-curve/{deviceCode}")
    @RequirePermission("dashboard")
    public Result<TrendDataVO> getDeviceCurve(@PathVariable String deviceCode,
                                              @RequestParam(required = false, defaultValue = "24") Integer hours) {
        return Result.success(monitorService.getDeviceCurve(deviceCode, hours));
    }

    @GetMapping("/realtime-devices")
    @RequirePermission("dashboard")
    public Result<List<DeviceDTO>> getRealtimeDevices(@RequestParam(required = false, defaultValue = "1") Long parkId) {
        return Result.success(monitorService.getRealtimeDevices(parkId));
    }
}
