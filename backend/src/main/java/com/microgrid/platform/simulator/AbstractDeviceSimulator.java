package com.microgrid.platform.simulator;

import com.microgrid.platform.dto.MetricDataDTO;
import com.microgrid.platform.service.DataIngestionService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractDeviceSimulator {

    protected final String deviceCode;
    protected final DataIngestionService dataIngestionService;
    protected final ScheduledExecutorService scheduler;
    protected final long intervalMs;

    protected ScheduledFuture<?> future;
    protected volatile boolean running = false;
    protected volatile long dataPointsGenerated = 0;
    protected LocalDateTime startTime;

    protected AbstractDeviceSimulator(String deviceCode,
                                       DataIngestionService dataIngestionService,
                                       long intervalMs) {
        this.deviceCode = deviceCode;
        this.dataIngestionService = dataIngestionService;
        this.intervalMs = intervalMs;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "simulator-" + deviceCode);
            t.setDaemon(true);
            return t;
        });
    }

    public synchronized void start() {
        if (running) {
            log.debug("模拟器已在运行: {}", deviceCode);
            return;
        }
        running = true;
        startTime = LocalDateTime.now();
        future = scheduler.scheduleAtFixedRate(this::doSimulate, 0, intervalMs, TimeUnit.MILLISECONDS);
        log.info("启动模拟器: {}", deviceCode);
    }

    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (future != null) {
            future.cancel(false);
        }
        scheduler.shutdownNow();
        log.info("停止模拟器: {}", deviceCode);
    }

    public boolean isRunning() {
        return running;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    protected void doSimulate() {
        try {
            if (!running) return;
            Map<String, BigDecimal> metrics = generateMetrics();
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> rawData = new HashMap<>();
            metrics.forEach((k, v) -> rawData.put(k, v));

            for (Map.Entry<String, BigDecimal> entry : metrics.entrySet()) {
                MetricDataDTO dto = new MetricDataDTO();
                dto.setDeviceCode(deviceCode);
                dto.setMetricCode(entry.getKey());
                dto.setMetricValue(entry.getValue());
                dto.setTs(now);
                dto.setRawData(rawData);
                dataIngestionService.ingestSingle(dto);
                dataPointsGenerated++;
            }
        } catch (Exception e) {
            log.warn("模拟器数据生成异常: {}, {}", deviceCode, e.getMessage());
        }
    }

    public long getDataPointsGenerated() {
        return dataPointsGenerated;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    protected abstract Map<String, BigDecimal> generateMetrics();

    protected double random(double min, double max) {
        return min + Math.random() * (max - min);
    }

    protected BigDecimal bd(double value) {
        return BigDecimal.valueOf(Math.round(value * 1000.0) / 1000.0);
    }
}
