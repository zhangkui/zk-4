package com.microgrid.platform.config;

import com.microgrid.platform.entity.SysUser;
import com.microgrid.platform.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "admin123";
    private static final List<String> DEFAULT_USERS = Arrays.asList("admin", "operator", "viewer");

    @Override
    public void run(String... args) {
        log.info("开始初始化默认用户密码...");
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
        int count = 0;

        for (String username : DEFAULT_USERS) {
            SysUser user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                if (!passwordEncoder.matches(DEFAULT_PASSWORD, user.getPassword())) {
                    user.setPassword(encodedPassword);
                    userRepository.save(user);
                    log.info("用户 [{}] 密码已重置为默认密码", username);
                    count++;
                } else {
                    log.info("用户 [{}] 密码正确，无需修改", username);
                }
            } else {
                log.warn("用户 [{}] 不存在，跳过密码初始化", username);
            }
        }

        if (count > 0) {
            log.info("共重置了 {} 个用户的密码", count);
        } else {
            log.info("默认用户密码均已正确，无需修改");
        }
    }
}
