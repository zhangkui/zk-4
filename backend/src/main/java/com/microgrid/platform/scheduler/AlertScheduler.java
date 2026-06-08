package com.microgrid.platform.scheduler;

import com.microgrid.platform.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private final AlertService alertService;

    @Scheduled(fixedDelayString = "${alert.scan.interval:30000}")
    public void scanAlertRules() {
        try {
            alertService.processAlertRules();
        } catch (Exception e) {
            log.error("告警规则扫描异常", e);
        }
    }
}
