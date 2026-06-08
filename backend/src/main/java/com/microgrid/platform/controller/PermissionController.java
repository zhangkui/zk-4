package com.microgrid.platform.controller;

import com.microgrid.platform.annotation.RequirePermission;
import com.microgrid.platform.common.Result;
import com.microgrid.platform.dto.PermissionDTO;
import com.microgrid.platform.entity.SysPermission;
import com.microgrid.platform.repository.SysPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final SysPermissionRepository permissionRepository;

    @GetMapping("/list")
    @RequirePermission("permission")
    public Result<List<PermissionDTO>> list() {
        List<SysPermission> all = permissionRepository.findAllOrderBySort();
        List<PermissionDTO> dtoList = all.stream().map(p -> {
            PermissionDTO dto = new PermissionDTO();
            BeanUtils.copyProperties(p, dto);
            return dto;
        }).collect(Collectors.toList());

        Map<Long, List<PermissionDTO>> childrenMap = dtoList.stream()
                .collect(Collectors.groupingBy(d -> d.getParentId() == null ? 0L : d.getParentId()));

        List<PermissionDTO> roots = new ArrayList<>();
        for (PermissionDTO dto : dtoList) {
            Long parentId = dto.getParentId() == null ? 0L : dto.getParentId();
            if (parentId == 0L) {
                roots.add(dto);
            }
            List<PermissionDTO> children = childrenMap.get(dto.getId());
            if (children != null && !children.isEmpty()) {
                dto.setChildren(children);
            }
        }

        return Result.success(roots);
    }

    @GetMapping("/flat")
    @RequirePermission("permission")
    public Result<List<PermissionDTO>> flatList() {
        List<SysPermission> all = permissionRepository.findAllOrderBySort();
        List<PermissionDTO> dtoList = all.stream().map(p -> {
            PermissionDTO dto = new PermissionDTO();
            BeanUtils.copyProperties(p, dto);
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtoList);
    }
}
