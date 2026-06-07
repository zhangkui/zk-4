package com.microgrid.platform.service;

import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.dto.LogQueryDTO;
import com.microgrid.platform.entity.SysLoginLog;
import com.microgrid.platform.entity.SysOperationLog;

public interface LogService {

    void saveLoginLog(Long userId, String username, String loginIp, String userAgent, Integer status, String message);

    PageResult<SysLoginLog> getLoginLogs(LogQueryDTO query);

    PageResult<SysOperationLog> getOperationLogs(LogQueryDTO query);
}
