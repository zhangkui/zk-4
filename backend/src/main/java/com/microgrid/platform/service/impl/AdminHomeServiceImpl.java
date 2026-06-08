package com.microgrid.platform.service.impl;

import com.microgrid.platform.entity.Device;
import com.microgrid.platform.entity.Park;
import com.microgrid.platform.entity.SysLoginLog;
import com.microgrid.platform.repository.DeviceRepository;
import com.microgrid.platform.repository.ParkRepository;
import com.microgrid.platform.repository.SysLoginLogRepository;
import com.microgrid.platform.repository.SysRoleRepository;
import com.microgrid.platform.repository.SysUserRepository;
import com.microgrid.platform.service.AdminHomeService;
import com.microgrid.platform.vo.AdminHomeStatsVO;
import com.microgrid.platform.vo.ParkDeviceOnlineRateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminHomeServiceImpl implements AdminHomeService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final ParkRepository parkRepository;
    private final DeviceRepository deviceRepository;
    private final SysLoginLogRepository loginLogRepository;

    @Override
    public AdminHomeStatsVO getStats() {
        AdminHomeStatsVO stats = new AdminHomeStatsVO();

        stats.setUserCount(userRepository.count());
        stats.setRoleCount(roleRepository.count());
        stats.setParkCount(parkRepository.count());

        long totalDevices = deviceRepository.count();
        stats.setDeviceTotalCount(totalDevices);

        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long onlineDevices = 0;
        for (Device d : deviceRepository.findAll()) {
            if (d.getLastReportAt() != null && d.getLastReportAt().isAfter(fiveMinutesAgo)) {
                onlineDevices++;
            }
        }
        stats.setDeviceOnlineCount(onlineDevices);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        stats.setTodayLoginCount(loginLogRepository.countByLoginAtAfter(todayStart));

        stats.setDisabledUserCount((long) userRepository.findByStatus(0).size());
        stats.setDisabledDeviceCount((long) deviceRepository.findByStatus(0).size());

        long parksWithNoDevice = 0;
        List<Park> allParks = parkRepository.findAll();
        for (Park p : allParks) {
            long count = deviceRepository.countByParkId(p.getId());
            if (count == 0) {
                parksWithNoDevice++;
            }
        }
        stats.setParkWithNoDeviceCount(parksWithNoDevice);

        List<ParkDeviceOnlineRateVO> parkRates = new ArrayList<>();
        for (Park p : allParks) {
            ParkDeviceOnlineRateVO vo = new ParkDeviceOnlineRateVO();
            vo.setParkId(p.getId());
            vo.setParkName(p.getParkName());
            long parkTotal = deviceRepository.countByParkId(p.getId());
            vo.setTotalDeviceCount(parkTotal);
            long parkOnline = 0;
            for (Device d : deviceRepository.findByParkId(p.getId())) {
                if (d.getLastReportAt() != null && d.getLastReportAt().isAfter(fiveMinutesAgo)) {
                    parkOnline++;
                }
            }
            vo.setOnlineDeviceCount(parkOnline);
            if (parkTotal > 0) {
                BigDecimal rate = BigDecimal.valueOf(parkOnline)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(parkTotal), 2, RoundingMode.HALF_UP);
                vo.setOnlineRate(rate);
            } else {
                vo.setOnlineRate(BigDecimal.ZERO);
            }
            vo.setAlarmCount(0L);
            parkRates.add(vo);
        }
        stats.setParkOnlineRates(parkRates);

        Pageable top10Pageable = PageRequest.of(0, 10);
        List<SysLoginLog> recentLogs = loginLogRepository.findAllByOrderByLoginAtDesc(top10Pageable).getContent();
        stats.setRecentLoginLogs(recentLogs);

        return stats;
    }
}
