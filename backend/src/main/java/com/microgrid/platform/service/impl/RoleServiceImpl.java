package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.BusinessException;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.ResultCode;
import com.microgrid.platform.dto.PermissionDTO;
import com.microgrid.platform.dto.RoleCreateDTO;
import com.microgrid.platform.dto.RoleDTO;
import com.microgrid.platform.entity.SysPermission;
import com.microgrid.platform.entity.SysRole;
import com.microgrid.platform.entity.SysRolePermission;
import com.microgrid.platform.repository.SysPermissionRepository;
import com.microgrid.platform.repository.SysRolePermissionRepository;
import com.microgrid.platform.repository.SysRoleRepository;
import com.microgrid.platform.service.RoleService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final EntityManager entityManager;

    @Override
    public RoleDTO getById(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ROLE_NOT_FOUND));
        return convertToDTO(role);
    }

    @Override
    public RoleDTO getByCode(String roleCode) {
        SysRole role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new BusinessException(ResultCode.ROLE_NOT_FOUND));
        return convertToDTO(role);
    }

    @Override
    public PageResult<RoleDTO> page(Integer pageNum, Integer pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<SysRole> page;

        if (StringUtils.hasText(keyword)) {
            List<SysRole> list = roleRepository.searchByKeyword(keyword);
            long total = list.size();
            List<SysRole> pageList = list.stream()
                    .skip((long) (pageNum - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());
            page = new org.springframework.data.domain.PageImpl<>(pageList, pageable, total);
        } else {
            page = roleRepository.findAll(pageable);
        }

        List<RoleDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), pageNum, pageSize, dtoList);
    }

    @Override
    public List<RoleDTO> list() {
        return roleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleDTO create(RoleCreateDTO dto) {
        if (roleRepository.existsByRoleCode(dto.getRoleCode())) {
            throw new BusinessException(ResultCode.ROLE_CODE_EXISTS);
        }

        SysRole role = new SysRole();
        role.setRoleCode(dto.getRoleCode());
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        role = roleRepository.save(role);

        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            final Long roleId = role.getId();
            dto.getPermissionIds().forEach(permissionId -> {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionRepository.save(rp);
            });
        }

        log.info("创建角色成功: {}", role.getRoleCode());
        return convertToDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO update(Long id, RoleCreateDTO dto) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.ROLE_NOT_FOUND));

        if (dto.getRoleCode() != null) role.setRoleCode(dto.getRoleCode());
        if (dto.getRoleName() != null) role.setRoleName(dto.getRoleName());
        if (dto.getDescription() != null) role.setDescription(dto.getDescription());
        if (dto.getStatus() != null) role.setStatus(dto.getStatus());

        role = roleRepository.save(role);

        if (dto.getPermissionIds() != null) {
            rolePermissionRepository.deleteByRoleId(id);
            entityManager.flush();
            List<Long> uniqueIds = dto.getPermissionIds().stream().distinct().toList();
            final Long roleId = id;
            uniqueIds.forEach(permissionId -> {
                if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
                    SysRolePermission rp = new SysRolePermission();
                    rp.setRoleId(roleId);
                    rp.setPermissionId(permissionId);
                    rolePermissionRepository.save(rp);
                }
            });
        }

        log.info("更新角色成功: {}", role.getRoleCode());
        return convertToDTO(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new BusinessException(ResultCode.ROLE_NOT_FOUND);
        }
        rolePermissionRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
        log.info("删除角色成功: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        roleRepository.updateStatus(id, status);
        log.info("更新角色状态: id={}, status={}", id, status);
    }

    @Override
    public List<PermissionDTO> getPermissions(Long roleId) {
        List<SysPermission> permissions = permissionRepository.findPermissionsByRoleId(roleId);
        return buildPermissionTree(permissions);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionRepository.deleteByRoleId(roleId);
        entityManager.flush();
        if (permissionIds != null) {
            List<Long> uniqueIds = permissionIds.stream().distinct().toList();
            uniqueIds.forEach(permissionId -> {
                if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
                    SysRolePermission rp = new SysRolePermission();
                    rp.setRoleId(roleId);
                    rp.setPermissionId(permissionId);
                    rolePermissionRepository.save(rp);
                }
            });
        }
        log.info("分配角色权限成功: roleId={}", roleId);
    }

    @Override
    public List<RoleDTO> getUserRoles(Long userId) {
        List<SysRole> roles = roleRepository.findRolesByUserId(userId);
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private RoleDTO convertToDTO(SysRole role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        dto.setDescription(role.getDescription());
        dto.setStatus(role.getStatus());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }

    private List<PermissionDTO> buildPermissionTree(List<SysPermission> permissions) {
        Map<Long, PermissionDTO> dtoMap = permissions.stream()
                .collect(Collectors.toMap(SysPermission::getId, this::convertPermissionToDTO));

        List<PermissionDTO> roots = new ArrayList<>();
        for (SysPermission p : permissions) {
            PermissionDTO dto = dtoMap.get(p.getId());
            if (p.getParentId() == null || p.getParentId() == 0) {
                roots.add(dto);
            } else {
                PermissionDTO parent = dtoMap.get(p.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        return roots;
    }

    private PermissionDTO convertPermissionToDTO(SysPermission p) {
        PermissionDTO dto = new PermissionDTO();
        dto.setId(p.getId());
        dto.setPermissionCode(p.getPermissionCode());
        dto.setPermissionName(p.getPermissionName());
        dto.setPermissionType(p.getPermissionType());
        dto.setParentId(p.getParentId());
        dto.setSortOrder(p.getSortOrder());
        dto.setPath(p.getPath());
        dto.setIcon(p.getIcon());
        dto.setComponent(p.getComponent());
        dto.setApiMethod(p.getApiMethod());
        dto.setApiPath(p.getApiPath());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
