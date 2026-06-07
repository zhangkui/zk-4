package com.microgrid.platform.simulator;

import com.microgrid.platform.service.DataIngestionService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ChargingSimulator extends AbstractDeviceSimulator {

    private final double ratedPower;
    private boolean isCharging;
    private double currentPower;
    private int counter;

    public ChargingSimulator(String deviceCode, double ratedPower,
                             DataIngestionService dataIngestionService, long intervalMs) {
        super(deviceCode, dataIngestionService, intervalMs);
        this.ratedPower = ratedPower;
        this.isCharging = Math.random() > 0.5;
        this.currentPower = isCharging ? ratedPower * 0.8 : 0;
        this.counter = 0;
    }

    @Override
    protected Map<String, BigDecimal> generateMetrics() {
        Map<String, BigDecimal> metrics = new HashMap<>();
        counter++;

        if (counter % 60 == 0) {
            LocalDateTime now = LocalDateTime.now();
            int hour = now.getHour();
            double probability;
            if (hour >= 8 && hour <= 20) {
                probability = 0.3;
            } else {
                probability = 0.1;
            }
            if (isCharging) {
                if (Math.random() < 0.15) {
                    isCharging = false;
                }
            } else {
                if (Math.random() < probability) {
                    isCharging = true;
                }
            }
        }

        if (isCharging) {
            double target = ratedPower * random(0.5, 1.0);
            currentPower = currentPower + (target - currentPower) * 0.1;
            currentPower = Math.max(0, Math.min(ratedPower, currentPower));
        } else {
            currentPower = currentPower * 0.9;
            if (currentPower < 0.1) currentPower = 0;
        }

        metrics.put("power", bd(currentPower));
        metrics.put("voltage", bd(currentPower > 0 ? random(380, 420) : random(220, 230)));
        metrics.put("current", bd(currentPower > 0 ? currentPower / 400.0 * 1000 : 0));
        metrics.put("energy", bd(currentPower * intervalMs / 3600000.0));
        metrics.put("charging_status", bd(isCharging && currentPower > 0.5 ? 2 : (isCharging ? 1 : 0)));
        metrics.put("status", bd(1));

        return metrics;
    }
}
