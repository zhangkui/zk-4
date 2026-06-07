package com.microgrid.platform.mqtt;

import com.microgrid.platform.config.MqttConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mqtt", name = "enabled", havingValue = "true")
public class MqttPublisher {

    private final MqttConfig mqttConfig;

    private MqttClient client;

    @PostConstruct
    public void init() {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            String clientId = mqttConfig.getClientId() + "-pub-" + System.currentTimeMillis();
            client = new MqttClient(mqttConfig.getBrokerUrl(), clientId, persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(30);
            options.setKeepAliveInterval(60);

            if (mqttConfig.getUsername() != null && !mqttConfig.getUsername().isEmpty()) {
                options.setUserName(mqttConfig.getUsername());
                options.setPassword(mqttConfig.getPassword().toCharArray());
            }

            client.connect(options);
            log.info("MQTT Publisher连接成功: {}", mqttConfig.getBrokerUrl());
        } catch (MqttException e) {
            log.error("MQTT Publisher初始化失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
            }
        } catch (MqttException e) {
            log.error("关闭MQTT Publisher失败: {}", e.getMessage());
        }
    }

    public void publish(String topic, String payload) {
        publish(topic, payload, mqttConfig.getQos());
    }

    public void publish(String topic, String payload, int qos) {
        try {
            if (client != null && client.isConnected()) {
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(qos);
                client.publish(topic, message);
                log.debug("MQTT发布消息 - 主题: {}, QoS: {}, 内容: {}", topic, qos, payload);
            } else {
                log.warn("MQTT Publisher未连接，无法发布消息");
            }
        } catch (MqttException e) {
            log.error("MQTT发布消息失败 - 主题: {}, 错误: {}", topic, e.getMessage());
        }
    }

    public void publishDeviceCommand(String deviceCode, String command) {
        String topic = "microgrid/device/" + deviceCode + "/command";
        publish(topic, command);
    }
}
