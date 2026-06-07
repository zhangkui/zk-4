package com.microgrid.platform.common;

public final class Constants {

    private Constants() {
    }

    public static final String DEFAULT_PASSWORD = "admin123";

    public static final Long ADMIN_ROLE_ID = 1L;

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String TOKEN_HEADER = "Authorization";

    public static final String REDIS_TOKEN_PREFIX = "microgrid:token:";

    public static final String REDIS_DEVICE_REALTIME_PREFIX = "microgrid:device:realtime:";

    public static final Integer STATUS_ENABLED = 1;

    public static final Integer STATUS_DISABLED = 0;

    public static final Integer STATUS_OFFLINE = 0;

    public static final Integer STATUS_ONLINE = 1;

    public static final Integer STATUS_FAULT = 2;

    public static final Integer DEVICE_TIMEOUT_SECONDS = 300;

    public static final String METRIC_POWER = "power";

    public static final String METRIC_SOC = "soc";

    public static final String METRIC_VOLTAGE = "voltage";

    public static final String METRIC_CURRENT = "current";

    public static final String METRIC_TEMPERATURE = "temperature";

    public static final String METRIC_ENERGY = "energy";

    public static final String METRIC_STATUS = "status";

    public static final String METRIC_CHARGING_STATUS = "charging_status";
}
