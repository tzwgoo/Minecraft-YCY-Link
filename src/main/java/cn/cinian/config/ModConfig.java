package cn.cinian.config;

import cn.cinian.YOKONEXLink;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("yokonex-link.json");

    private static ModConfig instance;

    // WebSocket 连接配置
    private String websocketUrl = "ws://localhost:3001";
    private String uid = "";
    private String token = "";
    private boolean enabled = true;
    private boolean autoConnect = true;

    // 事件开关配置
    private EventConfig events = new EventConfig();

    public static class EventConfig {
        // 玩家连接事件
        public boolean playerJoin = true;
        public boolean playerLeave = true;

        // 玩家聊天事件
        public boolean playerChat = true;

        // 方块事件
        public boolean blockBreak = true;
        public boolean blockPlace = true;
        public boolean blockAttack = true;

        // 战斗事件
        public boolean playerDeath = true;
        public boolean playerDamage = true;
        public boolean entityKilled = true;

        // 物品事件
        public boolean itemUse = true;
    }

    public ModConfig() {
    }

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static ModConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(reader, ModConfig.class);
                // 确保 events 不为 null
                if (instance.events == null) {
                    instance.events = new EventConfig();
                }
                YOKONEXLink.LOGGER.info("配置文件加载成功");
            } catch (IOException e) {
                YOKONEXLink.LOGGER.error("加载配置文件失败", e);
                instance = new ModConfig();
            }
        } else {
            instance = new ModConfig();
            instance.save();
            YOKONEXLink.LOGGER.info("创建默认配置文件");
        }
        return instance;
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(this, writer);
            YOKONEXLink.LOGGER.info("配置文件保存成功");
        } catch (IOException e) {
            YOKONEXLink.LOGGER.error("保存配置文件失败", e);
        }
    }

    // WebSocket 配置的 getter 和 setter
    public String getWebsocketUrl() {
        return websocketUrl;
    }

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    // 事件配置的 getter
    public EventConfig getEvents() {
        if (events == null) {
            events = new EventConfig();
        }
        return events;
    }
}
