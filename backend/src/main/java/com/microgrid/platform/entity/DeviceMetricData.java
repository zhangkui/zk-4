package com.microgrid.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@Table(name = "device_metric_data", indexes = {
        @Index(name = "idx_device_metric_data_device_code_ts", columnList = "device_code, ts DESC"),
        @Index(name = "idx_device_metric_data_metric_code_ts", columnList = "metric_code, ts DESC"),
        @Index(name = "idx_device_metric_data_park_id_ts", columnList = "park_id, ts DESC")
})
@IdClass(DeviceMetricData.DeviceMetricDataId.class)
public class DeviceMetricData {

    @Id
    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @Id
    @Column(name = "device_code", nullable = false, length = 50)
    private String deviceCode;

    @Id
    @Column(name = "metric_code", nullable = false, length = 100)
    private String metricCode;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "park_id")
    private Long parkId;

    @Column(name = "metric_value", nullable = false, precision = 20, scale = 6)
    private BigDecimal metricValue;

    @Column(name = "status_flag")
    private Integer statusFlag = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Data
    public static class DeviceMetricDataId implements Serializable {
        private LocalDateTime ts;
        private String deviceCode;
        private String metricCode;

        public DeviceMetricDataId() {
        }

        public DeviceMetricDataId(LocalDateTime ts, String deviceCode, String metricCode) {
            this.ts = ts;
            this.deviceCode = deviceCode;
            this.metricCode = metricCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceMetricDataId that = (DeviceMetricDataId) o;
            return Objects.equals(ts, that.ts) &&
                    Objects.equals(deviceCode, that.deviceCode) &&
                    Objects.equals(metricCode, that.metricCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ts, deviceCode, metricCode);
        }
    }
}
