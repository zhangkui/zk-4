package com.microgrid.platform.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgrid.platform.annotation.OperationLog;
import com.microgrid.platform.entity.SysOperationLog;
import com.microgrid.platform.entity.SysUser;
import com.microgrid.platform.repository.SysOperationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.microgrid.platform.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLogAnnotation = method.getAnnotation(OperationLog.class);

        SysOperationLog operationLog = new SysOperationLog();
        operationLog.setOperationType(operationLogAnnotation.operationType());
        operationLog.setOperationModule(operationLogAnnotation.operationModule());
        operationLog.setDescription(operationLogAnnotation.description());

        try {
            fillRequestInfo(operationLog);
            fillUserInfo(operationLog);
            fillParams(joinPoint, signature, operationLog);
        } catch (Exception e) {
            log.warn("填充操作日志信息失败", e);
        }

        Object result;
        try {
            result = joinPoint.proceed();
            operationLog.setResult(1);
        } catch (Throwable e) {
            operationLog.setResult(0);
            operationLog.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            try {
                operationLogRepository.save(operationLog);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
            }
            log.info("操作日志: {} - {} - 耗时: {}ms",
                    operationLog.getOperationModule(),
                    operationLog.getDescription(),
                    System.currentTimeMillis() - startTime);
        }

        return result;
    }

    private void fillRequestInfo(SysOperationLog operationLog) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setRequestPath(request.getRequestURI());
            operationLog.setOperationIp(getClientIp(request));
        }
    }

    private void fillUserInfo(SysOperationLog operationLog) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof SysUser user) {
                    operationLog.setUserId(user.getId());
                    operationLog.setUsername(user.getUsername());
                } else if (principal instanceof org.springframework.security.core.userdetails.User user) {
                    operationLog.setUsername(user.getUsername());
                }
            }
        } catch (Exception e) {
            log.debug("获取用户信息失败", e);
        }
    }

    private void fillParams(ProceedingJoinPoint joinPoint, MethodSignature signature, SysOperationLog operationLog) {
        try {
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            Map<String, Object> params = new HashMap<>();
            if (paramNames != null && args != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    Object arg = args[i];
                    if (arg != null && !isSensitiveParam(paramNames[i])) {
                        try {
                            if (arg instanceof HttpServletRequest || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                                continue;
                            }
                            params.put(paramNames[i], arg);
                        } catch (Exception e) {
                            params.put(paramNames[i], arg.toString());
                        }
                    }
                }
            }
            if (!params.isEmpty()) {
                String json = objectMapper.writeValueAsString(params);
                if (json.length() > 4000) {
                    json = json.substring(0, 4000) + "...";
                }
                operationLog.setRequestParams(json);
            }
        } catch (Exception e) {
            log.debug("序列化请求参数失败", e);
        }
    }

    private boolean isSensitiveParam(String paramName) {
        String lower = paramName.toLowerCase();
        return lower.contains("password") || lower.contains("token") || lower.contains("secret");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
