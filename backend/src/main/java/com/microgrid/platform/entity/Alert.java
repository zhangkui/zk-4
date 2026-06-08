package com.microgrid.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alert", indexes = {
        @Index(name = "idx_alert_rule_id", columnList = "rule_id"),
        @Index(name = "idx_alert_device_code", columnList = "device_code"),
        @Index(name = "idx_alert_status", columnList = "alert_status"),
        @Index(name = "idx_alert_level", columnList = "alert_level"),
        @Index(name = "idx_alert_park_id", columnList = "park_id"),
        @Index(name = "idx_alert_trigger_time", columnList = "trigger_time"),
        @Index(name = "idx_alert_unique", columnList = "rule_id, device_code, alert_status", unique = false)
})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_no", nullable = false, unique = true, length = 50)
    private String alertNo;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "rule_code", nullable = false, length = 50)
    private String ruleCode;

    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    @Column(name = "alert_level", nullable = false, length = 20)
    private String alertLevel;

    @Column(name = "park_id")
    private Long parkId;

    @Column(name = "park_name", length = 100)
    private String parkName;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_code", nullable = false, length = 50)
    private String deviceCode;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "device_type", length = 30)
    private String deviceType;

    @Column(name = "trigger_value", precision = 20, scale = 6)
    private BigDecimal triggerValue;

    @Column(name = "threshold_value", precision = 20, scale = 6)
    private BigDecimal thresholdValue;

    @Column(name = "metric_code", length = 100)
    private String metricCode;

    @Column(name = "alert_status", nullable = false, length = 20)
    private String alertStatus;

    @Column(name = "trigger_time", nullable = false)
    private LocalDateTime triggerTime;

    @Column(name = "recovery_time")
    private LocalDateTime recoveryTime;

    @Column(name = "alert_message", length = 500)
    private String alertMessage;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
