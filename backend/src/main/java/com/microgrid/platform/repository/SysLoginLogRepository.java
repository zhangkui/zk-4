package com.microgrid.platform.repository;

import com.microgrid.platform.entity.SysLoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SysLoginLogRepository extends JpaRepository<SysLoginLog, Long> {

    Page<SysLoginLog> findByUsernameOrderByLoginAtDesc(String username, Pageable pageable);

    Page<SysLoginLog> findAllByOrderByLoginAtDesc(Pageable pageable);

    @Query("SELECT l FROM SysLoginLog l WHERE " +
            "(:username IS NULL OR l.username LIKE %:username%) AND " +
            "(:startTime IS NULL OR l.loginAt >= :startTime) AND " +
            "(:endTime IS NULL OR l.loginAt <= :endTime) " +
            "ORDER BY l.loginAt DESC")
    Page<SysLoginLog> searchLogs(@Param("username") String username,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 Pageable pageable);

    List<SysLoginLog> findTop10ByUserIdOrderByLoginAtDesc(Long userId);

    @Query("SELECT COUNT(l) FROM SysLoginLog l WHERE l.loginAt >= :startTime")
    Long countByLoginAtAfter(@Param("startTime") LocalDateTime startTime);
}
