package com.microgrid.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String realName;
    private String email;
    private String phone;
    private Integer status = 1;
    private java.util.List<Long> roleIds;
}
