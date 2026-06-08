package com.microgrid.platform.vo;

import com.microgrid.platform.entity.SysLoginLog;
import lombok.Data;

import java.util.List;

@Data
public class AdminHomeStatsVO {

    private Long userCount;
    private Long roleCount;
    private Long parkCount;
    private Long deviceTotalCount;
    private Long deviceOnlineCount;
    private Long todayLoginCount;

    private Long disabledUserCount;
    private Long disabledDeviceCount;
    private Long parkWithNoDeviceCount;

    private List<ParkDeviceOnlineRateVO> parkOnlineRates;

    private List<SysLoginLog> recentLoginLogs;
}
