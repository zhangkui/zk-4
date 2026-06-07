package com.microgrid.platform.repository;

import com.microgrid.platform.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    Optional<SysRole> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

    List<SysRole> findByStatus(Integer status);

    @Query("SELECT r FROM SysRole r WHERE r.roleCode LIKE %:keyword% OR r.roleName LIKE %:keyword%")
    List<SysRole> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT r FROM SysRole r JOIN SysUserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId")
    List<SysRole> findRolesByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE SysRole r SET r.status = :status WHERE r.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
