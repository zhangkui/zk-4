package com.microgrid.platform.security;

import com.microgrid.platform.entity.SysRole;
import com.microgrid.platform.entity.SysUser;
import com.microgrid.platform.repository.SysPermissionRepository;
import com.microgrid.platform.repository.SysRoleRepository;
import com.microgrid.platform.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        List<SysRole> roles = roleRepository.findRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
        List<String> permissionCodes = permissionRepository.findPermissionCodesByUserId(user.getId());

        return new CustomUserDetails(user, roleCodes, permissionCodes);
    }
}
