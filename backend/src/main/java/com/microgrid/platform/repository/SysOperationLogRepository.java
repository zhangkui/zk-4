package com.microgrid.platform.repository;

import com.microgrid.platform.entity.SysOperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SysOperationLogRepository extends JpaRepository<SysOperationLog, Long> {

    Page<SysOperationLog> findByUsernameOrderByOperationAtDesc(String username, Pageable pageable);

    Page<SysOperationLog> findAllByOrderByOperationAtDesc(Pageable pageable);

    @Query("SELECT l FROM SysOperationLog l WHERE " +
            "(:username IS NULL OR l.username LIKE %:username%) AND " +
            "(:operationType IS NULL OR l.operationType = :operationType) AND " +
            "(:operationModule IS NULL OR l.operationModule LIKE %:operationModule%) AND " +
            "(:startTime IS NULL OR l.operationAt >= :startTime) AND " +
            "(:endTime IS NULL OR l.operationAt <= :endTime) " +
            "ORDER BY l.operationAt DESC")
    Page<SysOperationLog> searchLogs(@Param("username") String username,
                                     @Param("operationType") String operationType,
                                     @Param("operationModule") String operationModule,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     Pageable pageable);

    @Query("SELECT COUNT(l) FROM SysOperationLog l WHERE l.operationAt >= :startTime")
    Long countByOperationAtAfter(@Param("startTime") LocalDateTime startTime);
}
