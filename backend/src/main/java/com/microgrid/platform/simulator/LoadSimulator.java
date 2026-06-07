package com.microgrid.platform.simulator;

import com.microgrid.platform.service.DataIngestionService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LoadSimulator extends AbstractDeviceSimulator {

    private final double ratedPower;
    private double lastPower;

    public LoadSimulator(String deviceCode, double ratedPower,
                         DataIngestionService dataIngestionService, long intervalMs) {
        super(deviceCode, dataIngestionService, intervalMs);
        this.ratedPower = ratedPower;
        this.lastPower = ratedPower * 0.5;
    }

    @Override
    protected Map<String, BigDecimal> generateMetrics() {
        Map<String, BigDecimal> metrics = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int dayOfWeek = now.getDayOfWeek().getValue();

        double loadFactor;

        boolean isWeekend = dayOfWeek >= 6;

        if (isWeekend) {
            if (hour >= 8 && hour <= 22) {
                loadFactor = 0.5 + 0.1 * Math.sin((hour - 8) / 14.0 * Math.PI);
            } else {
                loadFactor = 0.25;
            }
        } else {
            if (hour >= 8 && hour <= 11) {
                loadFactor = 0.7 + 0.15 * Math.sin((hour - 8) / 3.0 * Math.PI);
            } else if (hour >= 14 && hour <= 17) {
                loadFactor = 0.7 + 0.15 * Math.sin((hour - 14) / 3.0 * Math.PI);
            } else if (hour >= 18 && hour <= 21) {
                loadFactor = 0.85 + 0.1 * Math.sin((hour - 18) / 3.0 * Math.PI);
            } else if (hour >= 22 || hour < 6) {
                loadFactor = 0.2;
            } else {
                loadFactor = 0.45;
            }
        }

        double targetPower = ratedPower * loadFactor;
        double delta = (targetPower - lastPower) * 0.2;
        lastPower = Math.max(ratedPower * 0.1, lastPower + delta + random(-ratedPower * 0.03, ratedPower * 0.03));
        lastPower = Math.min(ratedPower * 1.1, lastPower);

        metrics.put("power", bd(lastPower));
        metrics.put("voltage", bd(random(220, 240)));
        metrics.put("current", bd(lastPower / 230.0 * 1000));
        metrics.put("energy", bd(lastPower * intervalMs / 3600000.0));
        metrics.put("status", bd(1));

        return metrics;
    }
}
