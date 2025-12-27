package cn.cinian.screen;

import cn.cinian.YOKONEXLink;
import cn.cinian.config.ModConfig;
import cn.cinian.websocket.ModWebSocketClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private EditBox urlField;
    private EditBox uidField;
    private EditBox tokenField;

    // 主控制按钮
    private Button connectButton;
    private Button saveButton;
    private Button testButton;
    private Button enabledButton;

    // 事件控制按钮
    private final Map<String, Button> eventButtons = new HashMap<>();

    // 当前页面：0=连接设置，1=事件设置
    private int currentPage = 0;
    private Button pageButton;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("yokonex_link.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (currentPage == 0) {
            initConnectionPage();
        } else {
            initEventsPage();
        }

        // 页面切换按钮（底部）
        pageButton = Button.builder(
            Component.literal(currentPage == 0 ? "事件设置 →" : "← 连接设置"),
            button -> switchPage()
        ).bounds(this.width / 2 - 100, this.height - 27, 200, 20).build();
        addRenderableWidget(pageButton);
    }

    private void initConnectionPage() {
        ModConfig config = ModConfig.getInstance();

        int centerX = this.width / 2;
        int startY = 30;

        // WebSocket URL
        urlField = new EditBox(this.font, centerX - 150, startY, 300, 20, Component.literal("WebSocket URL"));
        urlField.setValue(config.getWebsocketUrl());
        urlField.setMaxLength(256);

        // UID
        uidField = new EditBox(this.font, centerX - 150, startY + 40, 300, 20, Component.literal("UID"));
        uidField.setValue(config.getUid());
        uidField.setMaxLength(128);

        // Token
        tokenField = new EditBox(this.font, centerX - 150, startY + 80, 300, 20, Component.literal("Token"));
        tokenField.setValue(config.getToken());
        tokenField.setMaxLength(512);

        // 连接按钮
        connectButton = Button.builder(
            Component.translatable("yokonex_link.config.connect"),
            button -> onConnectClick()
        ).bounds(centerX - 150, startY + 120, 145, 20).build();

        // 保存按钮
        saveButton = Button.builder(
            Component.translatable("yokonex_link.config.save"),
            button -> onSaveClick()
        ).bounds(centerX + 5, startY + 120, 145, 20).build();

        // 测试按钮
        testButton = Button.builder(
            Component.translatable("yokonex_link.config.test"),
            button -> onTestClick()
        ).bounds(centerX - 150, startY + 145, 300, 20).build();

        // 启用/禁用按钮
        enabledButton = Button.builder(
            Component.translatable(config.isEnabled() ? "yokonex_link.config.enabled" : "yokonex_link.config.disabled"),
            button -> onEnabledClick()
        ).bounds(centerX - 150, startY + 170, 300, 20).build();

        addRenderableWidget(urlField);
        addRenderableWidget(uidField);
        addRenderableWidget(tokenField);
        addRenderableWidget(connectButton);
        addRenderableWidget(saveButton);
        addRenderableWidget(testButton);
        addRenderableWidget(enabledButton);

        setInitialFocus(urlField);
    }

    private void initEventsPage() {
        ModConfig config = ModConfig.getInstance();
        ModConfig.EventConfig events = config.getEvents();

        int centerX = this.width / 2;
        int startY = 30;
        int buttonWidth = 300;
        int buttonHeight = 20;
        int spacing = 22;

        // 创建事件开关按钮
        createEventButton("playerJoin", "玩家加入", events.playerJoin, centerX, startY, buttonWidth, buttonHeight);
        createEventButton("playerLeave", "玩家离开", events.playerLeave, centerX, startY + spacing, buttonWidth, buttonHeight);
        createEventButton("playerChat", "玩家聊天", events.playerChat, centerX, startY + spacing * 2, buttonWidth, buttonHeight);
        createEventButton("blockBreak", "方块破坏", events.blockBreak, centerX, startY + spacing * 3, buttonWidth, buttonHeight);
        createEventButton("blockPlace", "方块放置", events.blockPlace, centerX, startY + spacing * 4, buttonWidth, buttonHeight);
        createEventButton("blockAttack", "方块攻击", events.blockAttack, centerX, startY + spacing * 5, buttonWidth, buttonHeight);
        createEventButton("playerDeath", "玩家死亡", events.playerDeath, centerX, startY + spacing * 6, buttonWidth, buttonHeight);
        createEventButton("playerDamage", "玩家受伤", events.playerDamage, centerX, startY + spacing * 7, buttonWidth, buttonHeight);
        createEventButton("entityKilled", "击杀实体", events.entityKilled, centerX, startY + spacing * 8, buttonWidth, buttonHeight);
        createEventButton("itemUse", "使用物品", events.itemUse, centerX, startY + spacing * 9, buttonWidth, buttonHeight);

        // 保存按钮
        Button saveEventsButton = Button.builder(
            Component.literal("保存事件设置"),
            button -> {
                onSaveClick();
                minecraft.setScreen(new ConfigScreen(parent));
            }
        ).bounds(centerX - 100, startY + spacing * 10, 200, 20).build();
        addRenderableWidget(saveEventsButton);
    }

    private void createEventButton(String eventKey, String displayName, boolean currentState, int centerX, int y, int width, int height) {
        Button button = Button.builder(
            Component.literal(displayName + ": " + (currentState ? "§a启用" : "§c禁用")),
            btn -> toggleEvent(eventKey)
        ).bounds(centerX - width / 2, y, width, height).build();

        eventButtons.put(eventKey, button);
        addRenderableWidget(button);
    }

    private void toggleEvent(String eventKey) {
        ModConfig config = ModConfig.getInstance();
        ModConfig.EventConfig events = config.getEvents();

        // 使用switch来切换状态
        switch (eventKey) {
            case "playerJoin" -> events.playerJoin = !events.playerJoin;
            case "playerLeave" -> events.playerLeave = !events.playerLeave;
            case "playerChat" -> events.playerChat = !events.playerChat;
            case "blockBreak" -> events.blockBreak = !events.blockBreak;
            case "blockPlace" -> events.blockPlace = !events.blockPlace;
            case "blockAttack" -> events.blockAttack = !events.blockAttack;
            case "playerDeath" -> events.playerDeath = !events.playerDeath;
            case "playerDamage" -> events.playerDamage = !events.playerDamage;
            case "entityKilled" -> events.entityKilled = !events.entityKilled;
            case "itemUse" -> events.itemUse = !events.itemUse;
        }

        // 刷新界面
        clearWidgets();
        init();
    }

    private void switchPage() {
        currentPage = 1 - currentPage;
        clearWidgets();
        init();
    }

    private void onConnectClick() {
        try {
            ModWebSocketClient.reconnectToServer();
        } catch (Throwable e) {
            YOKONEXLink.LOGGER.error("无法连接 WebSocket 服务器", e);
        }
    }

    private void onSaveClick() {
        if (currentPage == 0) {
            ModConfig config = ModConfig.getInstance();
            config.setWebsocketUrl(urlField.getValue());
            config.setUid(uidField.getValue());
            config.setToken(tokenField.getValue());
        }
        ModConfig.getInstance().save();
        YOKONEXLink.LOGGER.info("配置已保存");
    }

    private void onTestClick() {
        try {
            ModWebSocketClient client = ModWebSocketClient.getInstance();
            if (client != null && client.isOpen()) {
                client.sendCommand("test_command");
            } else {
                YOKONEXLink.LOGGER.warn("WebSocket 未连接，无法测试");
            }
        } catch (Throwable e) {
            YOKONEXLink.LOGGER.error("测试 WebSocket 时出错", e);
        }
    }

    private void onEnabledClick() {
        ModConfig config = ModConfig.getInstance();
        config.setEnabled(!config.isEnabled());
        config.save();
        enabledButton.setMessage(Component.translatable(config.isEnabled() ? "yokonex_link.config.enabled" : "yokonex_link.config.disabled"));
        YOKONEXLink.LOGGER.info("Mod 已{}", config.isEnabled() ? "启用" : "禁用");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        String title = currentPage == 0 ? "连接设置" : "事件设置";
        graphics.drawCenteredString(this.font, Component.literal(title), this.width / 2, 15, 0xFFFFFF);

        if (currentPage == 0) {
            renderConnectionPage(graphics, mouseX, mouseY, partialTick);
        } else {
            renderEventsPage(graphics);
        }

        // 底部提示
        graphics.drawCenteredString(this.font, Component.literal("§7按 ESC 关闭"), this.width / 2, this.height - 12, 0x808080);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderConnectionPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int startY = 30;

        graphics.drawString(this.font, Component.translatable("yokonex_link.config.url"), centerX - 150, startY - 12, 0xA0A0A0, false);
        urlField.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(this.font, Component.translatable("yokonex_link.config.uid"), centerX - 150, startY + 28, 0xA0A0A0, false);
        uidField.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(this.font, Component.translatable("yokonex_link.config.token"), centerX - 150, startY + 68, 0xA0A0A0, false);
        tokenField.render(graphics, mouseX, mouseY, partialTick);

        // 连接状态 - 添加异常处理以避免类加载失败
        String statusText;
        int statusColor;
        try {
            ModWebSocketClient client = ModWebSocketClient.getInstance();
            if (client == null || !client.isOpen()) {
                statusText = Component.translatable("yokonex_link.config.status.disconnected").getString();
                statusColor = 0xFF5555;
            } else if (client.isLoggedIn()) {
                statusText = Component.translatable("yokonex_link.config.status.logged_in").getString();
                statusColor = 0x55FF55;
            } else {
                statusText = Component.translatable("yokonex_link.config.status.connected").getString();
                statusColor = 0xFFFF55;
            }
        } catch (Throwable e) {
            // WebSocket 类加载失败，显示不可用状态
            statusText = "WebSocket 不可用";
            statusColor = 0xFF5555;
            YOKONEXLink.LOGGER.error("无法加载 WebSocket 客户端", e);
        }

        graphics.drawString(this.font, Component.translatable("yokonex_link.config.status").getString() + ": " + statusText,
                         centerX - 150, startY + 195, statusColor, false);
    }

    private void renderEventsPage(GuiGraphics graphics) {
        int centerX = this.width / 2;
        int startY = 30;

        graphics.drawCenteredString(this.font, Component.literal("§7启用/禁用各项游戏事件"), centerX, startY - 10, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
