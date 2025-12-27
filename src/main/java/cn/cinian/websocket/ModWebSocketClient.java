package cn.cinian.websocket;

import cn.cinian.YOKONEXLink;
import cn.cinian.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ModWebSocketClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private static ModWebSocketClient instance;
    private final ScheduledExecutorService heartbeatExecutor;
    private boolean isConnected = false;
    private boolean isLoggedIn = false;
    
    private ModWebSocketClient(URI serverUri) {
        super(serverUri);
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    
    public static ModWebSocketClient getInstance() {
        if (instance == null) {
            ModConfig config = ModConfig.getInstance();
            try {
                instance = new ModWebSocketClient(new URI(config.getWebsocketUrl()));
            } catch (Exception e) {
                YOKONEXLink.LOGGER.error("创建 WebSocket 客户端失败", e);
            }
        }
        return instance;
    }
    
    public static void reconnectToServer() {
        if (instance != null) {
            instance.close();
        }
        instance = null;
        getInstance().connect();
    }
    
    @Override
    public void onOpen(ServerHandshake handshake) {
        isConnected = true;
        YOKONEXLink.LOGGER.info("WebSocket 连接已建立");
        
        ModConfig config = ModConfig.getInstance();
        if (!config.getUid().isEmpty() && !config.getToken().isEmpty()) {
            login(config.getUid(), config.getToken());
        }
        
        startHeartbeat();
    }
    
    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = GSON.fromJson(message, JsonObject.class);
            String type = json.has("type") ? json.get("type").getAsString() : "";
            
            switch (type) {
                case "connected":
                    YOKONEXLink.LOGGER.info("WebSocket 连接成功: {}", message);
                    break;
                case "pong":
                    break;
                case "status":
                    handleStatus(json);
                    break;
                case "commandResult":
                    handleCommandResult(json);
                    break;
                case "reinitResult":
                    YOKONEXLink.LOGGER.info("重新初始化结果: {}", message);
                    break;
                case "message":
                    YOKONEXLink.LOGGER.info("收到 IM 消息: {}", message);
                    break;
                case "heartbeat":
                    break;
                case "error":
                    YOKONEXLink.LOGGER.error("WebSocket 错误: {}", json.get("message").getAsString());
                    break;
                default:
                    YOKONEXLink.LOGGER.info("收到未知消息类型: {}", type);
            }
        } catch (Exception e) {
            YOKONEXLink.LOGGER.error("处理 WebSocket 消息失败: {}", message, e);
        }
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        isConnected = false;
        isLoggedIn = false;
        stopHeartbeat();
        YOKONEXLink.LOGGER.info("WebSocket 连接已关闭: {} - {}", code, reason);
    }
    
    @Override
    public void onError(Exception ex) {
        YOKONEXLink.LOGGER.error("WebSocket 错误", ex);
    }
    
    private void handleStatus(JsonObject json) {
        if (json.has("data")) {
            JsonObject data = json.getAsJsonObject("data");
            if (data.has("isReady") && data.get("isReady").getAsBoolean()) {
                isLoggedIn = true;
                YOKONEXLink.LOGGER.info("IM 登录成功");
            }
        }
    }
    
    private void handleCommandResult(JsonObject json) {
        boolean success = json.has("success") && json.get("success").getAsBoolean();
        String message = json.has("message") ? json.get("message").getAsString() : "";
        if (success) {
            YOKONEXLink.LOGGER.info("指令发送成功: {}", message);
        } else {
            YOKONEXLink.LOGGER.error("指令发送失败: {}", message);
        }
    }
    
    public void sendPing() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "ping");
        send(json.toString());
    }
    
    public void getStatus() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "getStatus");
        send(json.toString());
    }
    
    public void login(String uid, String token) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "login");
        json.addProperty("uid", uid);
        json.addProperty("token", token);
        send(json.toString());
        YOKONEXLink.LOGGER.info("发送登录请求");
    }
    
    public void sendCommand(String commandId) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "sendCommand");
        json.addProperty("commandId", commandId);
        send(json.toString());
    }
    
    public void reinit() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "reinit");
        send(json.toString());
    }
    
    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (isConnected && isOpen()) {
                sendPing();
            }
        }, 30, 30, TimeUnit.SECONDS);
    }
    
    private void stopHeartbeat() {
        heartbeatExecutor.shutdownNow();
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public void shutdown() {
        stopHeartbeat();
        close();
    }
}