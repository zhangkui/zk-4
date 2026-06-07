package com.microgrid.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_operation_log", indexes = {
        @Index(name = "idx_sys_operation_log_user_id", columnList = "user_id"),
        @Index(name = "idx_sys_operation_log_operation_at", columnList = "operation_at"),
        @Index(name = "idx_sys_operation_log_type", columnList = "operation_type")
})
public class SysOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Column(name = "operation_module", nullable = false, length = 100)
    private String operationModule;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_path", length = 255)
    private String requestPath;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "result", nullable = false)
    private Integer result = 1;

    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;

    @Column(name = "operation_ip", length = 50)
    private String operationIp;

    @CreationTimestamp
    @Column(name = "operation_at", nullable = false, updatable = false)
    private LocalDateTime operationAt;
}
