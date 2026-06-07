package com.microgrid.platform.repository;

import com.microgrid.platform.entity.DeviceRealtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRealtimeRepository extends JpaRepository<DeviceRealtime, Long> {

    Optional<DeviceRealtime> findByDeviceCode(String deviceCode);

    List<DeviceRealtime> findByDeviceId(Long deviceId);

    @Query("SELECT dr FROM DeviceRealtime dr WHERE dr.deviceCode IN :deviceCodes")
    List<DeviceRealtime> findByDeviceCodes(@Param("deviceCodes") List<String> deviceCodes);

    @Query("SELECT dr FROM DeviceRealtime dr JOIN Device d ON dr.deviceCode = d.deviceCode WHERE d.parkId = :parkId")
    List<DeviceRealtime> findByParkId(@Param("parkId") Long parkId);

    @Query("SELECT COALESCE(SUM(dr.metricValue), 0) FROM DeviceRealtime dr " +
            "JOIN Device d ON dr.deviceCode = d.deviceCode " +
            "WHERE d.parkId = :parkId AND d.deviceType IN :deviceTypes AND dr.metricCode = 'power'")
    BigDecimal sumPowerByParkAndDeviceTypes(@Param("parkId") Long parkId, @Param("deviceTypes") List<String> deviceTypes);

    @Modifying
    @Query("UPDATE DeviceRealtime dr SET dr.metricCode = :metricCode, dr.metricValue = :metricValue, " +
            "dr.rawData = :rawData, dr.updatedAt = :updatedAt WHERE dr.deviceCode = :deviceCode")
    void updateByDeviceCode(@Param("deviceCode") String deviceCode,
                            @Param("metricCode") String metricCode,
                            @Param("metricValue") BigDecimal metricValue,
                            @Param("rawData") Object rawData,
                            @Param("updatedAt") LocalDateTime updatedAt);
}
