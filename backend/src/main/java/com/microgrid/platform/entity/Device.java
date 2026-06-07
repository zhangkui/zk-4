package com.microgrid.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device", indexes = {
        @Index(name = "idx_device_park_id", columnList = "park_id"),
        @Index(name = "idx_device_type", columnList = "device_type"),
        @Index(name = "idx_device_status", columnList = "status"),
        @Index(name = "idx_device_code", columnList = "device_code")
})
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_code", nullable = false, unique = true, length = 50)
    private String deviceCode;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_type", nullable = false, length = 30)
    private String deviceType;

    @Column(name = "park_id", nullable = false)
    private Long parkId;

    @Column(name = "install_location", length = 255)
    private String installLocation;

    @Column(name = "rated_power", precision = 15, scale = 3)
    private BigDecimal ratedPower;

    @Column(name = "access_method", length = 30)
    private String accessMethod;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "last_report_at")
    private LocalDateTime lastReportAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private String parkName;
}
