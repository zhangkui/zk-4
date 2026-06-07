package com.microgrid.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name = "device_realtime", indexes = {
        @Index(name = "idx_device_realtime_device_code", columnList = "device_code"),
        @Index(name = "idx_device_realtime_device_id", columnList = "device_id")
})
public class DeviceRealtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_code", nullable = false, unique = true, length = 50)
    private String deviceCode;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "metric_code", length = 100)
    private String metricCode;

    @Column(name = "metric_value", precision = 20, scale = 6)
    private BigDecimal metricValue;

    @Column(name = "status_flag")
    private Integer statusFlag = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_data", columnDefinition = "jsonb")
    private Map<String, Object> rawData;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
