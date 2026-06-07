package com.microgrid.platform.service;

import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.dto.PermissionDTO;
import com.microgrid.platform.dto.RoleCreateDTO;
import com.microgrid.platform.dto.RoleDTO;

import java.util.List;

public interface RoleService {

    RoleDTO getById(Long id);

    RoleDTO getByCode(String roleCode);

    PageResult<RoleDTO> page(Integer pageNum, Integer pageSize, String keyword);

    List<RoleDTO> list();

    RoleDTO create(RoleCreateDTO dto);

    RoleDTO update(Long id, RoleCreateDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    List<PermissionDTO> getPermissions(Long roleId);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<RoleDTO> getUserRoles(Long userId);
}
