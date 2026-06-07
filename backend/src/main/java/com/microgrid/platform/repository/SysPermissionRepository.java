package com.microgrid.platform.repository;

import com.microgrid.platform.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    Optional<SysPermission> findByPermissionCode(String permissionCode);

    List<SysPermission> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<SysPermission> findByPermissionTypeOrderBySortOrderAsc(Integer permissionType);

    @Query("SELECT p FROM SysPermission p ORDER BY p.sortOrder ASC")
    List<SysPermission> findAllOrderBySort();

    @Query("SELECT DISTINCT p FROM SysPermission p " +
            "JOIN SysRolePermission rp ON p.id = rp.permissionId " +
            "JOIN SysUserRole ur ON rp.roleId = ur.roleId " +
            "WHERE ur.userId = :userId ORDER BY p.sortOrder ASC")
    List<SysPermission> findPermissionsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p.permissionCode FROM SysPermission p " +
            "JOIN SysRolePermission rp ON p.id = rp.permissionId " +
            "JOIN SysUserRole ur ON rp.roleId = ur.roleId " +
            "WHERE ur.userId = :userId")
    List<String> findPermissionCodesByUserId(@Param("userId") Long userId);

    @Query("SELECT p FROM SysPermission p " +
            "JOIN SysRolePermission rp ON p.id = rp.permissionId " +
            "WHERE rp.roleId = :roleId ORDER BY p.sortOrder ASC")
    List<SysPermission> findPermissionsByRoleId(@Param("roleId") Long roleId);
}
