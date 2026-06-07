package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.BusinessException;
import com.microgrid.platform.common.Constants;
import com.microgrid.platform.common.ResultCode;
import com.microgrid.platform.dto.LoginRequest;
import com.microgrid.platform.dto.LoginResponse;
import com.microgrid.platform.entity.SysLoginLog;
import com.microgrid.platform.entity.SysPermission;
import com.microgrid.platform.entity.SysRole;
import com.microgrid.platform.entity.SysUser;
import com.microgrid.platform.repository.SysLoginLogRepository;
import com.microgrid.platform.repository.SysPermissionRepository;
import com.microgrid.platform.repository.SysRoleRepository;
import com.microgrid.platform.repository.SysUserRepository;
import com.microgrid.platform.security.JwtTokenProvider;
import com.microgrid.platform.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysLoginLogRepository loginLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String loginIp, String userAgent) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            com.microgrid.platform.security.CustomUserDetails userDetails =
                    (com.microgrid.platform.security.CustomUserDetails) authentication.getPrincipal();
            SysUser user = userDetails.getUser();

            if (user.getStatus() != null && user.getStatus() == 0) {
                saveLoginLog(user.getId(), user.getUsername(), loginIp, userAgent, 0, "用户已被禁用");
                throw new BusinessException(ResultCode.USER_DISABLED);
            }

            String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            String redisKey = Constants.REDIS_TOKEN_PREFIX + user.getId();
            redisTemplate.opsForValue().set(redisKey, token, jwtTokenProvider.getExpiration(), TimeUnit.MILLISECONDS);

            List<SysRole> roles = roleRepository.findRolesByUserId(user.getId());
            List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
            List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());

            saveLoginLog(user.getId(), user.getUsername(), loginIp, userAgent, 1, "登录成功");

            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenProvider.getExpiration() / 1000);
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setRealName(user.getRealName());
            response.setRoles(roleCodes);
            response.setPermissions(permissionCodes);
            response.setLoginTime(LocalDateTime.now());

            log.info("用户登录成功: {}", user.getUsername());
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("用户登录失败: {}, {}", request.getUsername(), e.getMessage());
            saveLoginLog(null, request.getUsername(), loginIp, userAgent, 0, "用户名或密码错误");
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId != null) {
                redisTemplate.delete(Constants.REDIS_TOKEN_PREFIX + userId);
            }
        } catch (Exception e) {
            log.warn("登出时解析token失败", e);
        }
    }

    @Override
    public LoginResponse refreshToken(String token) {
        if (token != null && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.substring(Constants.TOKEN_PREFIX.length());
        }
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        String newToken = jwtTokenProvider.generateToken(userId, username);
        redisTemplate.opsForValue().set(Constants.REDIS_TOKEN_PREFIX + userId, newToken,
                jwtTokenProvider.getExpiration(), TimeUnit.MILLISECONDS);

        List<SysRole> roles = roleRepository.findRolesByUserId(userId);
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(userId);

        LoginResponse response = new LoginResponse();
        response.setToken(newToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.getExpiration() / 1000);
        response.setUserId(userId);
        response.setUsername(username);
        response.setRealName(user.getRealName());
        response.setRoles(roleCodes);
        response.setPermissions(permissionCodes);
        response.setLoginTime(LocalDateTime.now());

        return response;
    }

    @Override
    public LoginResponse getCurrentUserInfo(Long userId) {
        SysUser user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        List<SysRole> roles = roleRepository.findRolesByUserId(userId);
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(userId);

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRoles(roleCodes);
        response.setPermissions(permissionCodes);
        response.setTokenType("Bearer");

        return response;
    }

    private void saveLoginLog(Long userId, String username, String loginIp, String userAgent, Integer status, String message) {
        try {
            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginIp(loginIp);
            loginLog.setUserAgent(userAgent != null && userAgent.length() > 500 ? userAgent.substring(0, 500) : userAgent);
            loginLog.setStatus(status);
            loginLog.setMessage(message);
            loginLog.setLoginAt(LocalDateTime.now());
            loginLogRepository.save(loginLog);
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }
}
