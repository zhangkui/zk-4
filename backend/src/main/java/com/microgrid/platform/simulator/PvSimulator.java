package com.microgrid.platform.simulator;

import com.microgrid.platform.service.DataIngestionService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PvSimulator extends AbstractDeviceSimulator {

    private final double ratedPower;

    public PvSimulator(String deviceCode, double ratedPower,
                       DataIngestionService dataIngestionService, long intervalMs) {
        super(deviceCode, dataIngestionService, intervalMs);
        this.ratedPower = ratedPower;
    }

    @Override
    protected Map<String, BigDecimal> generateMetrics() {
        Map<String, BigDecimal> metrics = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();

        double timeOfDay = hour + minute / 60.0;
        double solarFactor = 0;

        if (timeOfDay >= 6 && timeOfDay <= 18) {
            double normalized = (timeOfDay - 6) / 12.0;
            solarFactor = Math.sin(normalized * Math.PI);
            solarFactor = Math.max(0, solarFactor);
        }

        double basePower = ratedPower * solarFactor;
        double fluctuation = 1 + random(-0.08, 0.08);
        double power = Math.max(0, basePower * fluctuation);

        metrics.put("power", bd(power));
        metrics.put("voltage", bd(random(600, 800)));
        metrics.put("current", bd(power > 0 ? power / 700.0 * 1000 : 0));
        metrics.put("energy", bd(power * intervalMs / 3600000.0));
        metrics.put("status", bd(1));

        return metrics;
    }
}
