package com.microgrid.platform.common;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILURE(500, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_DISABLED(1003, "用户已被禁用"),
    USER_ALREADY_EXISTS(1004, "用户名已存在"),

    TOKEN_INVALID(1101, "无效的Token"),
    TOKEN_EXPIRED(1102, "Token已过期"),
    TOKEN_NOT_FOUND(1103, "Token不存在"),

    DEVICE_NOT_FOUND(1201, "设备不存在"),
    DEVICE_CODE_EXISTS(1202, "设备编码已存在"),
    DEVICE_OFFLINE(1203, "设备离线"),

    PARK_NOT_FOUND(1301, "园区不存在"),
    PARK_CODE_EXISTS(1302, "园区编码已存在"),

    ROLE_NOT_FOUND(1401, "角色不存在"),
    ROLE_CODE_EXISTS(1402, "角色编码已存在"),

    PERMISSION_DENIED(1501, "权限不足"),

    DATA_INGEST_ERROR(1601, "数据接入错误"),
    INVALID_METRIC_CODE(1602, "无效的指标编码");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
