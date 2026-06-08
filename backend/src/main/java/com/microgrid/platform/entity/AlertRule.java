package com.microgrid.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alert_rule", indexes = {
        @Index(name = "idx_alert_rule_type", columnList = "rule_type"),
        @Index(name = "idx_alert_rule_status", columnList = "status"),
        @Index(name = "idx_alert_rule_level", columnList = "alert_level")
})
public class AlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "rule_code", nullable = false, unique = true, length = 50)
    private String ruleCode;

    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    @Column(name = "alert_level", nullable = false, length = 20)
    private String alertLevel;

    @Column(name = "scope_type", nullable = false, length = 20)
    private String scopeType;

    @Column(name = "scope_value", length = 500)
    private String scopeValue;

    @Column(name = "park_id")
    private Long parkId;

    @Column(name = "device_type", length = 30)
    private String deviceType;

    @Column(name = "device_code", length = 100)
    private String deviceCode;

    @Column(name = "threshold_operator", length = 10)
    private String thresholdOperator;

    @Column(name = "threshold_value", precision = 20, scale = 6)
    private BigDecimal thresholdValue;

    @Column(name = "threshold_value2", precision = 20, scale = 6)
    private BigDecimal thresholdValue2;

    @Column(name = "metric_code", length = 100)
    private String metricCode;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds = 0;

    @Column(name = "recovery_operator", length = 20)
    private String recoveryOperator;

    @Column(name = "recovery_value", precision = 20, scale = 6)
    private BigDecimal recoveryValue;

    @Column(name = "recovery_value2", precision = 20, scale = 6)
    private BigDecimal recoveryValue2;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
