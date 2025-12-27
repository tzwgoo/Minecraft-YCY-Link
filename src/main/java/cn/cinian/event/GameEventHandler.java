package cn.cinian.event;

import cn.cinian.YOKONEXLink;
import cn.cinian.config.ModConfig;
import cn.cinian.websocket.ModWebSocketClient;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = YOKONEXLink.MOD_ID)
public class GameEventHandler {

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new GameEventHandler());
        YOKONEXLink.LOGGER.info("已注册游戏事件处理器");
    }

    // 服务器生命周期事件
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        YOKONEXLink.LOGGER.info("服务器已启动");
        ModConfig config = ModConfig.getInstance();
        if (config.isEnabled() && config.isAutoConnect()) {
            connectWebSocket();
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        YOKONEXLink.LOGGER.info("服务器正在停止");
        disconnectWebSocket();
    }

    // 玩家连接事件
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
        if (events.playerJoin && event.getEntity() instanceof ServerPlayer player) {
            sendCommand("player_join");
            YOKONEXLink.LOGGER.info("玩家 {} 加入游戏", player.getName().getString());
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
        if (events.playerLeave && event.getEntity() instanceof ServerPlayer player) {
            sendCommand("player_leave");
            YOKONEXLink.LOGGER.info("玩家 {} 离开游戏", player.getName().getString());
        }
    }

    // 玩家聊天事件
    @SubscribeEvent
    public void onPlayerChat(ServerChatEvent event) {
        ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
        if (events.playerChat) {
            String playerName = event.getPlayer().getName().getString();
            String chatMessage = event.getRawText();
            sendCommand("player_chat");
            YOKONEXLink.LOGGER.info("玩家 {} 发送消息: {}", playerName, chatMessage);
        }
    }

    // 方块破坏事件
    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
        if (events.blockBreak && event.getPlayer() instanceof ServerPlayer player) {
            String blockName = event.getState().getBlock().getName().getString();
            BlockPos pos = event.getPos();
            sendCommand("block_break");
            YOKONEXLink.LOGGER.info("玩家 {} 破坏了方块: {} 位置: {}",
                    player.getName().getString(), blockName, formatPos(pos));
        }
    }

    // 方块放置事件
    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
        if (events.blockPlace && event.getEntity() instanceof ServerPlayer player) {
            String blockName = event.getPlacedBlock().getBlock().getName().getString();
            BlockPos pos = event.getPos();
            sendCommand("block_place");
            YOKONEXLink.LOGGER.info("玩家 {} 放置了方块: {} 位置: {}",
                    player.getName().getString(), blockName, formatPos(pos));
        }
    }

    // 左键点击方块事件（攻击方块）
    @SubscribeEvent
    public void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
        if (events.blockAttack && !event.getLevel().isClientSide()) {
            Player player = event.getEntity();
            BlockPos pos = event.getPos();
            String blockName = event.getLevel().getBlockState(pos).getBlock().getName().getString();
            sendCommand("block_attack");
            YOKONEXLink.LOGGER.info("玩家 {} 开始挖掘方块: {} 位置: {}",
                    player.getName().getString(), blockName, formatPos(pos));
        }
    }

    // 玩家死亡事件
    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
            if (events.playerDeath) {
                String deathMessage = event.getSource().getLocalizedDeathMessage(player).getString();
                sendCommand("player_death");
                YOKONEXLink.LOGGER.info("玩家 {} 死亡: {}", player.getName().getString(), deathMessage);
            }
        }
    }

    // 玩家受伤事件
    @SubscribeEvent
    public void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
            if (events.playerDamage) {
                float damage = event.getAmount();
                String source = event.getSource().getMsgId();
                sendCommand("player_damage");
                YOKONEXLink.LOGGER.info("玩家 {} 受到 {} 伤害来自: {}",
                        player.getName().getString(), damage, source);
            }
        }
    }

    // 击杀实体事件
    @SubscribeEvent
    public void onEntityKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
            if (events.entityKilled) {
                String entityType = event.getEntity().getType().getDescription().getString();
                boolean isHostile = event.getEntity() instanceof Enemy;
                sendCommand("entity_killed");
                YOKONEXLink.LOGGER.info("玩家 {} 击杀了实体: {} ({})",
                        player.getName().getString(), entityType, isHostile ? "敌对" : "和平");
            }
        }
    }

    // 使用物品事件
    @SubscribeEvent
    public void onItemUse(PlayerInteractEvent.RightClickItem event) {
        ModConfig.EventConfig events = ModConfig.getInstance().getEvents();
        if (events.itemUse && !event.getLevel().isClientSide()) {
            Player player = event.getEntity();
            ItemStack stack = event.getItemStack();
            if (!stack.isEmpty()) {
                String itemName = stack.getHoverName().getString();
                sendCommand("item_use");
                YOKONEXLink.LOGGER.info("玩家 {} 使用了物品: {}", player.getName().getString(), itemName);
            }
        }
    }

    // 工具方法：格式化坐标
    private String formatPos(BlockPos pos) {
        return String.format("%d,%d,%d", pos.getX(), pos.getY(), pos.getZ());
    }

    // 连接 WebSocket
    private void connectWebSocket() {
        try {
            ModWebSocketClient client = ModWebSocketClient.getInstance();
            if (!client.isOpen()) {
                client.connect();
                YOKONEXLink.LOGGER.info("正在连接 WebSocket 服务器...");
            }
        } catch (Exception e) {
            YOKONEXLink.LOGGER.error("连接 WebSocket 服务器失败", e);
        }
    }

    // 断开 WebSocket
    private void disconnectWebSocket() {
        try {
            ModWebSocketClient client = ModWebSocketClient.getInstance();
            if (client != null) {
                client.shutdown();
            }
        } catch (Throwable e) {
            YOKONEXLink.LOGGER.error("断开 WebSocket 连接失败", e);
        }
    }

    // 发送命令到 WebSocket
    private void sendCommand(String eventType) {
        ModConfig config = ModConfig.getInstance();
        if (!config.isEnabled()) {
            return;
        }

        try {
            ModWebSocketClient client = ModWebSocketClient.getInstance();
            if (client != null && client.isOpen() && client.isLoggedIn()) {
                client.sendCommand(eventType);
            } else {
                YOKONEXLink.LOGGER.warn("WebSocket 未连接或未登录，无法发送指令: {}", eventType);
            }
        } catch (Throwable e) {
            YOKONEXLink.LOGGER.error("发送 WebSocket 指令失败: {}", eventType, e);
        }
    }

    public static void reconnect() {
        try {
            ModWebSocketClient.reconnectToServer();
        } catch (Throwable e) {
            YOKONEXLink.LOGGER.error("重新连接 WebSocket 服务器失败", e);
        }
    }
}
