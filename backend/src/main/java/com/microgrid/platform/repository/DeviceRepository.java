package com.microgrid.platform.repository;

import com.microgrid.platform.entity.Device;
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
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceCode(String deviceCode);

    boolean existsByDeviceCode(String deviceCode);

    List<Device> findByParkId(Long parkId);

    List<Device> findByDeviceType(String deviceType);

    List<Device> findByParkIdAndDeviceType(Long parkId, String deviceType);

    List<Device> findByStatus(Integer status);

    @Query("SELECT d FROM Device d WHERE " +
            "(:parkId IS NULL OR d.parkId = :parkId) AND " +
            "(:deviceType IS NULL OR d.deviceType = :deviceType) AND " +
            "(:keyword IS NULL OR d.deviceCode LIKE %:keyword% OR d.deviceName LIKE %:keyword%)")
    Page<Device> searchDevices(@Param("parkId") Long parkId,
                               @Param("deviceType") String deviceType,
                               @Param("keyword") String keyword,
                               Pageable pageable);

    @Query("SELECT d.deviceType, COUNT(d) FROM Device d WHERE d.parkId = :parkId GROUP BY d.deviceType")
    List<Object[]> countByDeviceTypeForPark(@Param("parkId") Long parkId);

    @Query("SELECT COUNT(d) FROM Device d WHERE d.parkId = :parkId")
    Long countByParkId(@Param("parkId") Long parkId);

    @Query("SELECT COUNT(d) FROM Device d WHERE d.parkId = :parkId AND d.lastReportAt >= :time")
    Long countOnlineByParkId(@Param("parkId") Long parkId, @Param("time") LocalDateTime time);

    @Query("SELECT d.status, COUNT(d) FROM Device d WHERE d.parkId = :parkId GROUP BY d.status")
    List<Object[]> countByStatusForPark(@Param("parkId") Long parkId);

    @Modifying
    @Query("UPDATE Device d SET d.status = :status WHERE d.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Modifying
    @Query("UPDATE Device d SET d.lastReportAt = :lastReportAt WHERE d.id = :id")
    void updateLastReportAt(@Param("id") Long id, @Param("lastReportAt") LocalDateTime lastReportAt);

    @Modifying
    @Query("UPDATE Device d SET d.lastReportAt = :lastReportAt WHERE d.deviceCode = :deviceCode")
    void updateLastReportAtByCode(@Param("deviceCode") String deviceCode, @Param("lastReportAt") LocalDateTime lastReportAt);
}
