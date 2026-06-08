package com.microgrid.platform.repository;

import com.microgrid.platform.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findByAlertNo(String alertNo);

    @Query("SELECT a FROM Alert a WHERE " +
            "(:parkId IS NULL OR a.parkId = :parkId) AND " +
            "(:deviceCode IS NULL OR a.deviceCode = :deviceCode) AND " +
            "(:alertLevel IS NULL OR a.alertLevel = :alertLevel) AND " +
            "(:alertStatus IS NULL OR a.alertStatus = :alertStatus) AND " +
            "(:ruleType IS NULL OR a.ruleType = :ruleType) AND " +
            "(:startTime IS NULL OR a.triggerTime >= :startTime) AND " +
            "(:endTime IS NULL OR a.triggerTime <= :endTime) AND " +
            "(:keyword IS NULL OR a.alertMessage LIKE %:keyword% OR a.deviceName LIKE %:keyword% OR a.ruleName LIKE %:keyword%)")
    Page<Alert> searchAlerts(@Param("parkId") Long parkId,
                             @Param("deviceCode") String deviceCode,
                             @Param("alertLevel") String alertLevel,
                             @Param("alertStatus") String alertStatus,
                             @Param("ruleType") String ruleType,
                             @Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime,
                             @Param("keyword") String keyword,
                             Pageable pageable);

    @Query("SELECT a FROM Alert a WHERE a.ruleId = :ruleId AND a.deviceCode = :deviceCode AND a.alertStatus = 'TRIGGERED'")
    Optional<Alert> findActiveAlert(@Param("ruleId") Long ruleId, @Param("deviceCode") String deviceCode);

    @Query("SELECT a FROM Alert a WHERE a.alertStatus = 'TRIGGERED'")
    List<Alert> findAllActiveAlerts();

    @Modifying
    @Query("UPDATE Alert a SET a.alertStatus = :status, a.recoveryTime = :recoveryTime WHERE a.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") String status, @Param("recoveryTime") LocalDateTime recoveryTime);
}
