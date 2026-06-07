package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.OperationLog;
import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.DeviceCreateDTO;
import com.microgrid.platform.dto.DeviceDTO;
import com.microgrid.platform.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/{id}")
    @RequirePermission("device")
    public Result<DeviceDTO> getById(@PathVariable Long id) {
        return Result.success(deviceService.getById(id));
    }

    @GetMapping("/code/{deviceCode}")
    @RequirePermission("device")
    public Result<DeviceDTO> getByCode(@PathVariable String deviceCode) {
        DeviceDTO device = deviceService.getByCode(deviceCode);
        return Result.success(deviceService.enrichDeviceWithRealtime(device));
    }

    @GetMapping("/code/{deviceCode}/realtime")
    @RequirePermission("device")
    public Result<DeviceDTO> getRealtimeByCode(@PathVariable String deviceCode) {
        DeviceDTO device = deviceService.getByCode(deviceCode);
        return Result.success(deviceService.enrichDeviceWithRealtime(device));
    }

    @GetMapping("/page")
    @RequirePermission("device")
    public Result<PageResult<DeviceDTO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "20") Integer pageSize,
                                              @RequestParam(required = false) Long parkId,
                                              @RequestParam(required = false) String deviceType,
                                              @RequestParam(required = false) String keyword) {
        return Result.success(deviceService.page(pageNum, pageSize, parkId, deviceType, keyword));
    }

    @GetMapping("/list")
    @RequirePermission("device")
    public Result<List<DeviceDTO>> list(@RequestParam(required = false) Long parkId,
                                        @RequestParam(required = false) String deviceType) {
        return Result.success(deviceService.list(parkId, deviceType));
    }

    @PostMapping
    @RequirePermission("device")
    @OperationLog(operationType = "CREATE", operationModule = "设备管理", description = "创建设备")
    public Result<DeviceDTO> create(@Valid @RequestBody DeviceCreateDTO dto) {
        return Result.success(deviceService.create(dto));
    }

    @PutMapping("/{id}")
    @RequirePermission("device")
    @OperationLog(operationType = "UPDATE", operationModule = "设备管理", description = "更新设备")
    public Result<DeviceDTO> update(@PathVariable Long id, @RequestBody DeviceCreateDTO dto) {
        return Result.success(deviceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("device")
    @OperationLog(operationType = "DELETE", operationModule = "设备管理", description = "删除设备")
    public Result<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("device")
    @OperationLog(operationType = "UPDATE", operationModule = "设备管理", description = "更新设备状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        deviceService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/status-stats")
    @RequirePermission("device")
    public Result<Map<String, Integer>> getDeviceStatusStats(@RequestParam Long parkId) {
        return Result.success(deviceService.getDeviceStatusStats(parkId));
    }
}
