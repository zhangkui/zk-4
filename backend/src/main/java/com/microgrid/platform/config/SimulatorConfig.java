package com.microgrid.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "simulator")
public class SimulatorConfig {

    private boolean enabled = false;
    private long intervalMs = 1000;
}
