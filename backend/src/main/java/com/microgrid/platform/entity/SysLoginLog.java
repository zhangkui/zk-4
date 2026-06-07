package com.microgrid.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_login_log", indexes = {
        @Index(name = "idx_sys_login_log_user_id", columnList = "user_id"),
        @Index(name = "idx_sys_login_log_login_at", columnList = "login_at")
})
public class SysLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "login_ip", length = 50)
    private String loginIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "message", length = 255)
    private String message;

    @CreationTimestamp
    @Column(name = "login_at", nullable = false, updatable = false)
    private LocalDateTime loginAt;
}
