package com.microgrid.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PermissionDTO {

    private Long id;
    private String permissionCode;
    private String permissionName;
    private Integer permissionType;
    private Long parentId;
    private Integer sortOrder;
    private String path;
    private String icon;
    private String component;
    private String apiMethod;
    private String apiPath;
    private LocalDateTime createdAt;
    private List<PermissionDTO> children;
}
