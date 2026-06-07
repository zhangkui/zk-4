package com.microgrid.platform.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {

    private String realName;
    private String email;
    private String phone;
    private Integer status;
    private java.util.List<Long> roleIds;
}
