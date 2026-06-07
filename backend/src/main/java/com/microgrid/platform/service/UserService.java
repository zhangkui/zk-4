package com.microgrid.platform.service;

import com.microgrid.platform.common.PageResult;
import com.microgrid.platform.dto.UserCreateDTO;
import com.microgrid.platform.dto.UserDTO;
import com.microgrid.platform.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {

    UserDTO getById(Long id);

    UserDTO getByUsername(String username);

    PageResult<UserDTO> page(Integer pageNum, Integer pageSize, String keyword);

    List<UserDTO> list();

    UserDTO create(UserCreateDTO dto);

    UserDTO update(Long id, UserUpdateDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    void resetPassword(Long id, String newPassword);

    boolean updatePassword(Long id, String oldPassword, String newPassword);
}
