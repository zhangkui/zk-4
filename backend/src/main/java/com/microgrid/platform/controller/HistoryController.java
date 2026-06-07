package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.HistoryQueryDTO;
import com.microgrid.platform.service.HistoryService;
import com.microgrid.platform.vo.HistoryDataVO;
import com.microgrid.platform.vo.HistoryQueryResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @PostMapping("/query")
    @RequirePermission("history")
    public Result<HistoryQueryResultVO> query(@RequestBody HistoryQueryDTO query) {
        return Result.success(historyService.query(query));
    }

    @GetMapping("/page")
    @RequirePermission("history")
    public Result<PageResult<HistoryDataVO>> queryPage(
            @RequestParam(required = false) Long parkId,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "50") Integer pageSize) {

        HistoryQueryDTO query = new HistoryQueryDTO();
        query.setParkId(parkId);
        query.setDeviceCode(deviceCode);
        query.setMetricCode(metricCode);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);

        return Result.success(historyService.queryPage(query));
    }

    @GetMapping("/list")
    @RequirePermission("history")
    public Result<List<HistoryDataVO>> queryList(
            @RequestParam(required = false) Long parkId,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {

        HistoryQueryDTO query = new HistoryQueryDTO();
        query.setParkId(parkId);
        query.setDeviceCode(deviceCode);
        query.setMetricCode(metricCode);
        query.setStartTime(startTime);
        query.setEndTime(endTime);

        return Result.success(historyService.queryList(query));
    }

    @GetMapping("/chart")
    @RequirePermission("history")
    public Result<List<HistoryDataVO>> queryChart(
            @RequestParam(required = false) Long parkId,
            @RequestParam(required = false) String deviceCode,
            @RequestParam(required = false) String metricCode,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {

        HistoryQueryDTO query = new HistoryQueryDTO();
        query.setParkId(parkId);
        query.setDeviceCode(deviceCode);
        query.setMetricCode(metricCode);
        query.setStartTime(startTime);
        query.setEndTime(endTime);

        return Result.success(historyService.queryList(query));
    }
}
