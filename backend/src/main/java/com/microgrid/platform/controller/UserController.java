package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.OperationLog;
import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.UserCreateDTO;
import com.microgrid.platform.dto.UserDTO;
import com.microgrid.platform.dto.UserUpdateDTO;
import com.microgrid.platform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @RequirePermission("user")
    public Result<UserDTO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping("/page")
    @RequirePermission("user")
    public Result<PageResult<UserDTO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "20") Integer pageSize,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(userService.page(pageNum, pageSize, keyword));
    }

    @GetMapping("/list")
    @RequirePermission("user")
    public Result<List<UserDTO>> list() {
        return Result.success(userService.list());
    }

    @PostMapping
    @RequirePermission("user")
    @OperationLog(operationType = "CREATE", operationModule = "用户管理", description = "创建用户")
    public Result<UserDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        return Result.success(userService.create(dto));
    }

    @PutMapping("/{id}")
    @RequirePermission("user")
    @OperationLog(operationType = "UPDATE", operationModule = "用户管理", description = "更新用户")
    public Result<UserDTO> update(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        return Result.success(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user")
    @OperationLog(operationType = "DELETE", operationModule = "用户管理", description = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @RequirePermission("user")
    @OperationLog(operationType = "UPDATE", operationModule = "用户管理", description = "更新用户状态")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/password/reset")
    @RequirePermission("user")
    @OperationLog(operationType = "UPDATE", operationModule = "用户管理", description = "重置用户密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    @OperationLog(operationType = "UPDATE", operationModule = "用户管理", description = "修改用户密码")
    public Result<Boolean> updatePassword(@PathVariable Long id,
                                           @RequestParam String oldPassword,
                                           @RequestParam String newPassword) {
        return Result.success(userService.updatePassword(id, oldPassword, newPassword));
    }
}
