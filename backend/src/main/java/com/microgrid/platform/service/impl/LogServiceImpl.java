package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.dto.LogQueryDTO;
import com.microgrid.platform.entity.SysLoginLog;
import com.microgrid.platform.entity.SysOperationLog;
import com.microgrid.platform.repository.SysLoginLogRepository;
import com.microgrid.platform.repository.SysOperationLogRepository;
import com.microgrid.platform.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final SysLoginLogRepository loginLogRepository;
    private final SysOperationLogRepository operationLogRepository;

    @Override
    @Transactional
    public void saveLoginLog(Long userId, String username, String loginIp, String userAgent, Integer status, String message) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginIp(loginIp);
            if (userAgent != null && userAgent.length() > 500) {
                userAgent = userAgent.substring(0, 500);
            }
            loginLog.setUserAgent(userAgent);
            loginLog.setStatus(status);
            loginLog.setMessage(message);
            loginLogRepository.save(loginLog);
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }

    @Override
    public PageResult<SysLoginLog> getLoginLogs(LogQueryDTO query) {
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 20;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<SysLoginLog> page = loginLogRepository.searchLogs(
                query.getUsername(),
                query.getStartTime(),
                query.getEndTime(),
                pageable
        );

        return PageResult.of(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }

    @Override
    public PageResult<SysOperationLog> getOperationLogs(LogQueryDTO query) {
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 20;
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);

        Page<SysOperationLog> page = operationLogRepository.searchLogs(
                query.getUsername(),
                query.getOperationType(),
                query.getOperationModule(),
                query.getStartTime(),
                query.getEndTime(),
                pageable
        );

        return PageResult.of(page.getTotalElements(), pageNum, pageSize, page.getContent());
    }
}
