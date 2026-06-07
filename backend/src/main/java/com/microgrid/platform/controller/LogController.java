package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.LogQueryDTO;
import com.microgrid.platform.entity.SysLoginLog;
import com.microgrid.platform.entity.SysOperationLog;
import com.microgrid.platform.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/login")
    @RequirePermission("log")
    public Result<PageResult<SysLoginLog>> getLoginLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        LogQueryDTO query = new LogQueryDTO();
        query.setUsername(username);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);

        return Result.success(logService.getLoginLogs(query));
    }

    @GetMapping("/operation")
    @RequirePermission("log")
    public Result<PageResult<SysOperationLog>> getOperationLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        LogQueryDTO query = new LogQueryDTO();
        query.setUsername(username);
        query.setOperationType(operationType);
        query.setOperationModule(operationModule);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPageNum(pageNum);
        query.setPageSize(pageSize);

        return Result.success(logService.getOperationLogs(query));
    }
}
