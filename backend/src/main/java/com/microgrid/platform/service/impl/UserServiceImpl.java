package com.microgrid.platform.service.impl;

import com.microgrid.platform.common.BusinessException;
import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.common.ResultCode;
import com.microgrid.platform.dto.RoleDTO;
import com.microgrid.platform.dto.UserCreateDTO;
import com.microgrid.platform.dto.UserDTO;
import com.microgrid.platform.dto.UserUpdateDTO;
import com.microgrid.platform.entity.SysRole;
import com.microgrid.platform.entity.SysUser;
import com.microgrid.platform.entity.SysUserRole;
import com.microgrid.platform.repository.SysRoleRepository;
import com.microgrid.platform.repository.SysUserRepository;
import com.microgrid.platform.repository.SysUserRoleRepository;
import com.microgrid.platform.service.RoleService;
import com.microgrid.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Override
    public UserDTO getById(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getByUsername(String username) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    public PageResult<UserDTO> page(Integer pageNum, Integer pageSize, String keyword) {
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<SysUser> page;

        if (StringUtils.hasText(keyword)) {
            page = userRepository.searchByKeyword(keyword).stream()
                    .skip((long) (pageNum - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.collectingAndThen(Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable,
                                    userRepository.searchByKeyword(keyword).size())));
        } else {
            page = userRepository.findAll(pageable);
        }

        List<UserDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotalElements(), pageNum, pageSize, dtoList);
    }

    @Override
    public List<UserDTO> list() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO create(UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        user = userRepository.save(user);

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            final Long userId = user.getId();
            dto.getRoleIds().forEach(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleRepository.save(ur);
            });
        }

        log.info("创建用户成功: {}", user.getUsername());
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());

        user = userRepository.save(user);

        if (dto.getRoleIds() != null) {
            userRoleRepository.deleteByUserId(id);
            final Long userId = id;
            dto.getRoleIds().forEach(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                userRoleRepository.save(ur);
            });
        }

        log.info("更新用户成功: {}", user.getUsername());
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        userRoleRepository.deleteByUserId(id);
        userRepository.deleteById(id);
        log.info("删除用户成功: id={}", id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        userRepository.updateStatus(id, status);
        log.info("更新用户状态: id={}, status={}", id, status);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        userRepository.updatePassword(id, passwordEncoder.encode(newPassword));
        log.info("重置密码成功: userId={}", id);
    }

    @Override
    @Transactional
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        userRepository.updatePassword(id, passwordEncoder.encode(newPassword));
        log.info("修改密码成功: userId={}", id);
        return true;
    }

    private UserDTO convertToDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());

        List<SysRole> roles = roleRepository.findRolesByUserId(user.getId());
        List<RoleDTO> roleDTOs = roles.stream()
                .map(role -> {
                    RoleDTO rd = new RoleDTO();
                    rd.setId(role.getId());
                    rd.setRoleCode(role.getRoleCode());
                    rd.setRoleName(role.getRoleName());
                    rd.setDescription(role.getDescription());
                    rd.setStatus(role.getStatus());
                    return rd;
                })
                .collect(Collectors.toList());
        dto.setRoles(roleDTOs);

        return dto;
    }
}
