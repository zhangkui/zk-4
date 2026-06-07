package com.microgrid.platform.service;

import com.microgrid.platform.vo.SimulatorOverviewVO;
import com.microgrid.platform.vo.SimulatorStatusVO;

import java.util.List;

public interface SimulatorService {

    boolean start(String deviceCode);

    boolean startAll(Long parkId);

    boolean stop(String deviceCode);

    boolean stopAll(Long parkId);

    boolean isRunning(String deviceCode);

    List<SimulatorStatusVO> listStatus(Long parkId);

    SimulatorOverviewVO getOverview();
}
