package com.microgrid.platform.repository;

import com.microgrid.platform.entity.DeviceMetricData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceMetricDataRepository extends JpaRepository<DeviceMetricData, DeviceMetricData.DeviceMetricDataId> {

    @Query("SELECT d FROM DeviceMetricData d WHERE " +
            "(:parkId IS NULL OR d.parkId = :parkId) AND " +
            "(:deviceCode IS NULL OR d.deviceCode = :deviceCode) AND " +
            "(:metricCode IS NULL OR d.metricCode = :metricCode) AND " +
            "d.ts BETWEEN :startTime AND :endTime " +
            "ORDER BY d.ts DESC")
    Page<DeviceMetricData> findByConditions(@Param("parkId") Long parkId,
                                            @Param("deviceCode") String deviceCode,
                                            @Param("metricCode") String metricCode,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime,
                                            Pageable pageable);

    @Query("SELECT d FROM DeviceMetricData d WHERE " +
            "(:parkId IS NULL OR d.parkId = :parkId) AND " +
            "(:deviceCode IS NULL OR d.deviceCode = :deviceCode) AND " +
            "(:metricCode IS NULL OR d.metricCode = :metricCode) AND " +
            "d.ts BETWEEN :startTime AND :endTime " +
            "ORDER BY d.ts ASC")
    List<DeviceMetricData> findListByConditions(@Param("parkId") Long parkId,
                                                @Param("deviceCode") String deviceCode,
                                                @Param("metricCode") String metricCode,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    @Query("SELECT d.ts, SUM(d.metricValue) FROM DeviceMetricData d " +
            "JOIN Device dev ON d.deviceCode = dev.deviceCode " +
            "WHERE dev.parkId = :parkId AND dev.deviceType IN :deviceTypes AND d.metricCode = 'power' " +
            "AND d.ts BETWEEN :startTime AND :endTime " +
            "GROUP BY d.ts ORDER BY d.ts ASC")
    List<Object[]> sumPowerTrendByParkAndDeviceTypes(@Param("parkId") Long parkId,
                                                     @Param("deviceTypes") List<String> deviceTypes,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    @Query("SELECT d FROM DeviceMetricData d WHERE d.deviceCode = :deviceCode AND d.metricCode = :metricCode ORDER BY d.ts DESC LIMIT 1")
    DeviceMetricData findLatestByDeviceCodeAndMetricCode(@Param("deviceCode") String deviceCode, @Param("metricCode") String metricCode);

    @Query("SELECT COALESCE(SUM(d.metricValue), 0) FROM DeviceMetricData d " +
            "WHERE d.deviceCode = :deviceCode AND d.metricCode = :metricCode AND d.ts BETWEEN :startTime AND :endTime")
    BigDecimal sumMetricValue(@Param("deviceCode") String deviceCode,
                              @Param("metricCode") String metricCode,
                              @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);
}
