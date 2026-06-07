package com.microgrid.platform.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microgrid.platform.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeWebSocketHandler extends TextWebSocketHandler {

    private final MonitorService monitorService;
    private final ObjectMapper objectMapper;

    private static final Map<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledFuture<?>> TASKS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(4);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        SESSIONS.put(sessionId, session);
        log.info("WebSocket连接建立: sessionId={}", sessionId);

        Long parkId = extractParkId(session);
        ScheduledFuture<?> future = SCHEDULER.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    Object data = parkId != null ? monitorService.getOverview(parkId) : monitorService.getOverview(1L);
                    String json = objectMapper.writeValueAsString(data);
                    session.sendMessage(new TextMessage(json));
                }
            } catch (Exception e) {
                log.warn("WebSocket发送数据失败: {}", e.getMessage());
            }
        }, 0, 2, TimeUnit.SECONDS);

        TASKS.put(sessionId, future);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.debug("收到WebSocket消息: sessionId={}, message={}", session.getId(), message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        SESSIONS.remove(sessionId);

        ScheduledFuture<?> future = TASKS.remove(sessionId);
        if (future != null) {
            future.cancel(false);
        }

        log.info("WebSocket连接关闭: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误: sessionId={}, error={}", session.getId(), exception.getMessage());
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            log.warn("关闭WebSocket连接失败", e);
        }
    }

    public void broadcast(String message) {
        for (Map.Entry<String, WebSocketSession> entry : SESSIONS.entrySet()) {
            try {
                if (entry.getValue().isOpen()) {
                    entry.getValue().sendMessage(new TextMessage(message));
                }
            } catch (Exception e) {
                log.warn("WebSocket广播消息失败: sessionId={}", entry.getKey());
            }
        }
    }

    private Long extractParkId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2 && "parkId".equals(kv[0])) {
                    try {
                        return Long.parseLong(kv[1]);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
