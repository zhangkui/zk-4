package com.microgrid.platform.simulator;

import com.microgrid.platform.service.DataIngestionService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EssSimulator extends AbstractDeviceSimulator {

    private final double ratedPower;
    private double soc;
    private double lastPower;

    public EssSimulator(String deviceCode, double ratedPower,
                        DataIngestionService dataIngestionService, long intervalMs) {
        super(deviceCode, dataIngestionService, intervalMs);
        this.ratedPower = ratedPower;
        this.soc = 60;
        this.lastPower = 0;
    }

    @Override
    protected Map<String, BigDecimal> generateMetrics() {
        Map<String, BigDecimal> metrics = new HashMap<>();

        double chargeFactor;
        if (soc < 20) {
            chargeFactor = -0.8;
        } else if (soc > 90) {
            chargeFactor = 0.5;
        } else {
            chargeFactor = random(-0.6, 0.6);
        }

        double targetPower = ratedPower * chargeFactor;
        double delta = (targetPower - lastPower) * 0.2;
        lastPower = lastPower + delta + random(-ratedPower * 0.02, ratedPower * 0.02);
        lastPower = Math.max(-ratedPower, Math.min(ratedPower, lastPower));

        double energyChange = lastPower * intervalMs / 3600000.0;
        double capacityKWh = ratedPower * 2;
        soc = soc + (energyChange / capacityKWh * 100);
        soc = Math.max(5, Math.min(95, soc));

        metrics.put("power", bd(lastPower));
        metrics.put("soc", bd(soc));
        metrics.put("voltage", bd(lastPower > 0 ? random(700, 760) : random(650, 700)));
        metrics.put("current", bd(Math.abs(lastPower) / 700.0 * 1000));
        metrics.put("temperature", bd(random(25, 35)));
        metrics.put("status", bd(1));

        return metrics;
    }
}
