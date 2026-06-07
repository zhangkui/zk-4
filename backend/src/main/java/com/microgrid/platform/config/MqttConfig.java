package com.microgrid.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mqtt")
public class MqttConfig {

    private boolean enabled = false;
    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;
    private String topic;
    private Integer qos = 1;
}
