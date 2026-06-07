package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.OperationLog;
import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.ParkCreateDTO;
import com.microgrid.platform.dto.ParkDTO;
import com.microgrid.platform.service.ParkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parks")
@RequiredArgsConstructor
public class ParkController {

    private final ParkService parkService;

    @GetMapping("/{id}")
    @RequirePermission("park")
    public Result<ParkDTO> getById(@PathVariable Long id) {
        return Result.success(parkService.getById(id));
    }

    @GetMapping("/page")
    @RequirePermission("park")
    public Result<PageResult<ParkDTO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "20") Integer pageSize,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(parkService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/list")
    public Result<List<ParkDTO>> list() {
        return Result.success(parkService.list());
    }

    @PostMapping
    @RequirePermission("park")
    @OperationLog(operationType = "CREATE", operationModule = "园区管理", description = "创建园区")
    public Result<ParkDTO> create(@Valid @RequestBody ParkCreateDTO dto) {
        return Result.success(parkService.create(dto));
    }

    @PutMapping("/{id}")
    @RequirePermission("park")
    @OperationLog(operationType = "UPDATE", operationModule = "园区管理", description = "更新园区")
    public Result<ParkDTO> update(@PathVariable Long id, @RequestBody ParkCreateDTO dto) {
        return Result.success(parkService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("park")
    @OperationLog(operationType = "DELETE", operationModule = "园区管理", description = "删除园区")
    public Result<Void> delete(@PathVariable Long id) {
        parkService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("park")
    @OperationLog(operationType = "UPDATE", operationModule = "园区管理", description = "更新园区状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        parkService.updateStatus(id, status);
        return Result.success();
    }
}
