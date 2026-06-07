package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.OperationLog;
import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.PermissionDTO;
import com.microgrid.platform.dto.RoleCreateDTO;
import com.microgrid.platform.dto.RoleDTO;
import com.microgrid.platform.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/{id}")
    @RequirePermission("role")
    public Result<RoleDTO> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @GetMapping("/page")
    @RequirePermission("role")
    public Result<PageResult<RoleDTO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "20") Integer pageSize,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(roleService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/list")
    @RequirePermission("role")
    public Result<List<RoleDTO>> list() {
        return Result.success(roleService.list());
    }

    @PostMapping
    @RequirePermission("role")
    @OperationLog(operationType = "CREATE", operationModule = "角色管理", description = "创建角色")
    public Result<RoleDTO> create(@Valid @RequestBody RoleCreateDTO dto) {
        return Result.success(roleService.create(dto));
    }

    @PutMapping("/{id}")
    @RequirePermission("role")
    @OperationLog(operationType = "UPDATE", operationModule = "角色管理", description="更新角色")
    public Result<RoleDTO> update(@PathVariable Long id, @RequestBody RoleCreateDTO dto) {
        return Result.success(roleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("role")
    @OperationLog(operationType = "DELETE", operationModule = "角色管理", description = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("role")
    @OperationLog(operationType = "UPDATE", operationModule = "角色管理", description="更新角色状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        roleService.updateStatus(id, status);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    @RequirePermission("role")
    public Result<List<PermissionDTO>> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getPermissions(id));
    }

    @PostMapping("/{id}/permissions")
    @RequirePermission("role")
    @OperationLog(operationType = "ASSIGN", operationModule = "角色管理", description="分配角色权限")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
}
