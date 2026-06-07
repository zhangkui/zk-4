package com.microgrid.platform.simulator;

import com.microgrid.platform.service.DataIngestionService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WindSimulator extends AbstractDeviceSimulator {

    private final double ratedPower;
    private double lastPower;

    public WindSimulator(String deviceCode, double ratedPower,
                         DataIngestionService dataIngestionService, long intervalMs) {
        super(deviceCode, dataIngestionService, intervalMs);
        this.ratedPower = ratedPower;
        this.lastPower = ratedPower * 0.4;
    }

    @Override
    protected Map<String, BigDecimal> generateMetrics() {
        Map<String, BigDecimal> metrics = new HashMap<>();

        double windSpeed = random(3, 15);
        double cutIn = 3;
        double cutOut = 25;
        double rated = 12;

        double powerFactor;
        if (windSpeed < cutIn || windSpeed > cutOut) {
            powerFactor = 0;
        } else if (windSpeed >= rated) {
            powerFactor = 1;
        } else {
            powerFactor = Math.pow((windSpeed - cutIn) / (rated - cutIn), 3);
        }

        double targetPower = ratedPower * powerFactor;
        double delta = (targetPower - lastPower) * 0.3;
        lastPower = Math.max(0, lastPower + delta + random(-ratedPower * 0.03, ratedPower * 0.03));
        lastPower = Math.min(ratedPower, lastPower);

        metrics.put("power", bd(lastPower));
        metrics.put("wind_speed", bd(windSpeed));
        metrics.put("voltage", bd(random(380, 420)));
        metrics.put("current", bd(lastPower > 0 ? lastPower / 400.0 * 1000 : 0));
        metrics.put("energy", bd(lastPower * intervalMs / 3600000.0));
        metrics.put("status", bd(1));

        return metrics;
    }
}
