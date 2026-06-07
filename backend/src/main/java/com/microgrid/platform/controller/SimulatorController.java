package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.OperationLog;
import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.service.SimulatorService;
import com.microgrid.platform.vo.SimulatorOverviewVO;
import com.microgrid.platform.vo.SimulatorStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
public class SimulatorController {

    private final SimulatorService simulatorService;

    @PostMapping("/start/{deviceCode}")
    @RequirePermission("simulator")
    @OperationLog(operationType = "START", operationModule = "数据模拟器", description = "启动单设备模拟器")
    public Result<Boolean> start(@PathVariable String deviceCode) {
        return Result.success(simulatorService.start(deviceCode));
    }

    @PostMapping("/start-all")
    @RequirePermission("simulator")
    @OperationLog(operationType = "START", operationModule = "数据模拟器", description = "启动全部设备模拟器")
    public Result<Boolean> startAll(@RequestParam(required = false) Long parkId) {
        return Result.success(simulatorService.startAll(parkId));
    }

    @PostMapping("/stop/{deviceCode}")
    @RequirePermission("simulator")
    @OperationLog(operationType = "STOP", operationModule = "数据模拟器", description = "停止单设备模拟器")
    public Result<Boolean> stop(@PathVariable String deviceCode) {
        return Result.success(simulatorService.stop(deviceCode));
    }

    @PostMapping("/stop-all")
    @RequirePermission("simulator")
    @OperationLog(operationType = "STOP", operationModule = "数据模拟器", description = "停止全部设备模拟器")
    public Result<Boolean> stopAll(@RequestParam(required = false) Long parkId) {
        return Result.success(simulatorService.stopAll(parkId));
    }

    @GetMapping("/status/{deviceCode}")
    @RequirePermission("simulator")
    public Result<Boolean> isRunning(@PathVariable String deviceCode) {
        return Result.success(simulatorService.isRunning(deviceCode));
    }

    @GetMapping("/status")
    @RequirePermission("simulator")
    public Result<List<SimulatorStatusVO>> listStatus(@RequestParam(required = false) Long parkId) {
        return Result.success(simulatorService.listStatus(parkId));
    }

    @GetMapping("/overview")
    @RequirePermission("simulator")
    public Result<SimulatorOverviewVO> getOverview() {
        return Result.success(simulatorService.getOverview());
    }
}
