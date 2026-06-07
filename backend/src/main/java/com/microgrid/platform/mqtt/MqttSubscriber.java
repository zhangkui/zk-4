package com.microgrid.platform.mqtt;

import com.microgrid.platform.config.MqttConfig;
import com.microgrid.platform.service.DataIngestionService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
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
public class MqttSubscriber implements MqttCallback {

    private final MqttConfig mqttConfig;
    private final DataIngestionService dataIngestionService;

    private MqttClient client;

    @PostConstruct
    public void init() {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            String clientId = mqttConfig.getClientId() + "-" + System.currentTimeMillis();
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

            client.setCallback(this);
            log.info("正在连接MQTT Broker: {}", mqttConfig.getBrokerUrl());
            client.connect(options);

            if (client.isConnected()) {
                log.info("MQTT连接成功，订阅主题: {}", mqttConfig.getTopic());
                client.subscribe(mqttConfig.getTopic(), mqttConfig.getQos());
            } else {
                log.warn("MQTT连接失败");
            }
        } catch (MqttException e) {
            log.error("MQTT初始化失败: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                client.close();
                log.info("MQTT连接已关闭");
            }
        } catch (MqttException e) {
            log.error("关闭MQTT连接失败: {}", e.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT连接丢失: {}", cause != null ? cause.getMessage() : "unknown");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.debug("收到MQTT消息 - 主题: {}, QoS: {}, 内容: {}", topic, message.getQos(), payload);
            dataIngestionService.ingestFromMqtt(topic, payload);
        } catch (Exception e) {
            log.error("处理MQTT消息失败 - 主题: {}, 错误: {}", topic, e.getMessage(), e);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("MQTT消息发送完成");
    }
}
