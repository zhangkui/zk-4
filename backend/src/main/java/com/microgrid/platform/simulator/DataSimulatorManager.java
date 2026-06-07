package com.microgrid.platform.simulator;

import com.microgrid.platform.common.DeviceTypeEnum;
import com.microgrid.platform.config.SimulatorConfig;
import com.microgrid.platform.entity.Device;
import com.microgrid.platform.repository.DeviceRepository;
import com.microgrid.platform.service.DataIngestionService;
import com.microgrid.platform.service.SimulatorService;
import com.microgrid.platform.vo.SimulatorOverviewVO;
import com.microgrid.platform.vo.SimulatorStatusVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSimulatorManager implements SimulatorService {

    private final DataIngestionService dataIngestionService;
    private final DeviceRepository deviceRepository;
    private final SimulatorConfig simulatorConfig;

    private final Map<String, AbstractDeviceSimulator> simulators = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (simulatorConfig.isEnabled()) {
            log.info("自动启动所有设备模拟器...");
            startAll(null);
        }
    }

    @PreDestroy
    public void destroy() {
        stopAll(null);
    }

    @Override
    public boolean start(String deviceCode) {
        if (simulators.containsKey(deviceCode) && simulators.get(deviceCode).isRunning()) {
            return true;
        }

        Device device = deviceRepository.findByDeviceCode(deviceCode).orElse(null);
        if (device == null) {
            log.warn("设备不存在: {}", deviceCode);
            return false;
        }

        AbstractDeviceSimulator simulator = createSimulator(device);
        if (simulator != null) {
            simulators.put(deviceCode, simulator);
            simulator.start();
            return true;
        }
        return false;
    }

    @Override
    public boolean startAll(Long parkId) {
        List<Device> devices = parkId != null ?
                deviceRepository.findByParkId(parkId) :
                deviceRepository.findAll();

        int started = 0;
        for (Device device : devices) {
            try {
                if (start(device.getDeviceCode())) {
                    started++;
                }
            } catch (Exception e) {
                log.warn("启动模拟器失败: {}, {}", device.getDeviceCode(), e.getMessage());
            }
        }
        log.info("启动模拟器完成: 成功 {}, 总数 {}", started, devices.size());
        return true;
    }

    @Override
    public boolean stop(String deviceCode) {
        AbstractDeviceSimulator simulator = simulators.get(deviceCode);
        if (simulator != null) {
            simulator.stop();
            simulators.remove(deviceCode);
            return true;
        }
        return false;
    }

    @Override
    public boolean stopAll(Long parkId) {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, AbstractDeviceSimulator> entry : simulators.entrySet()) {
            if (parkId == null || isDeviceInPark(entry.getKey(), parkId)) {
                entry.getValue().stop();
                toRemove.add(entry.getKey());
            }
        }
        toRemove.forEach(simulators::remove);
        log.info("停止模拟器完成: 数量 {}", toRemove.size());
        return true;
    }

    @Override
    public boolean isRunning(String deviceCode) {
        AbstractDeviceSimulator simulator = simulators.get(deviceCode);
        return simulator != null && simulator.isRunning();
    }

    @Override
    public List<SimulatorStatusVO> listStatus(Long parkId) {
        List<Device> devices = parkId != null ?
                deviceRepository.findByParkId(parkId) :
                deviceRepository.findAll();

        List<SimulatorStatusVO> result = new ArrayList<>();
        for (Device device : devices) {
            SimulatorStatusVO vo = new SimulatorStatusVO();
            vo.setDeviceCode(device.getDeviceCode());
            vo.setDeviceName(device.getDeviceName());
            vo.setDeviceType(device.getDeviceType());
            vo.setRunning(isRunning(device.getDeviceCode()));
            result.add(vo);
        }
        return result;
    }

    private AbstractDeviceSimulator createSimulator(Device device) {
        String type = device.getDeviceType();
        String code = device.getDeviceCode();
        double ratedPower = device.getRatedPower() != null ? device.getRatedPower().doubleValue() : 100;
        long interval = simulatorConfig.getIntervalMs();

        DeviceTypeEnum typeEnum = DeviceTypeEnum.fromCode(type);
        if (typeEnum == null) {
            return null;
        }

        switch (typeEnum) {
            case PV:
                return new PvSimulator(code, ratedPower, dataIngestionService, interval);
            case WIND:
                return new WindSimulator(code, ratedPower, dataIngestionService, interval);
            case ESS:
                return new EssSimulator(code, ratedPower, dataIngestionService, interval);
            case LOAD:
            case METER:
                return new LoadSimulator(code, ratedPower, dataIngestionService, interval);
            case CHARGING:
                return new ChargingSimulator(code, ratedPower, dataIngestionService, interval);
            case PCS:
                return new LoadSimulator(code, ratedPower, dataIngestionService, interval);
            default:
                return null;
        }
    }

    private boolean isDeviceInPark(String deviceCode, Long parkId) {
        return deviceRepository.findByDeviceCode(deviceCode)
                .map(d -> d.getParkId().equals(parkId))
                .orElse(false);
    }

    @Override
    public SimulatorOverviewVO getOverview() {
        SimulatorOverviewVO vo = new SimulatorOverviewVO();

        List<Device> allDevices = deviceRepository.findAll();
        int totalDeviceCount = allDevices.size();
        int runningCount = 0;
        long totalDataPoints = 0;
        LocalDateTime earliestStartTime = null;

        for (Map.Entry<String, AbstractDeviceSimulator> entry : simulators.entrySet()) {
            AbstractDeviceSimulator simulator = entry.getValue();
            if (simulator.isRunning()) {
                runningCount++;
            }
            totalDataPoints += simulator.getDataPointsGenerated();
            if (simulator.getStartTime() != null) {
                if (earliestStartTime == null || simulator.getStartTime().isBefore(earliestStartTime)) {
                    earliestStartTime = simulator.getStartTime();
                }
            }
        }

        vo.setRunning(runningCount > 0);
        vo.setStartTime(earliestStartTime);
        vo.setSpeed(1);
        vo.setDeviceCount(totalDeviceCount);
        vo.setRunningDeviceCount(runningCount);
        vo.setDataPointsGenerated(totalDataPoints);

        return vo;
    }
}
