package com.microgrid.platform.service;

import com.microgrid.platform.dto.LoginRequest;
import com.microgrid.platform.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request, String loginIp, String userAgent);

    void logout(String token);

    LoginResponse refreshToken(String token);

    LoginResponse getCurrentUserInfo(Long userId);
}
