package com.microgrid.platform.repository;

import com.microgrid.platform.entity.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {

    List<SysRolePermission> findByRoleId(Long roleId);

    List<SysRolePermission> findByPermissionId(Long permissionId);

    @Transactional
    void deleteByRoleId(Long roleId);

    @Transactional
    void deleteByPermissionId(Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SysRolePermission rp WHERE rp.roleId = :roleId AND rp.permissionId NOT IN :permissionIds")
    void deleteByRoleIdAndPermissionIdNotIn(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}
