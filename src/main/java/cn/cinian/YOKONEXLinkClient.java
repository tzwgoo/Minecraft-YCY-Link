package cn.cinian;

import cn.cinian.client.ModKeyBindings;
import cn.cinian.screen.ConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.Minecraft;

@Mod.EventBusSubscriber(modid = YOKONEXLink.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class YOKONEXLinkClient {

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		YOKONEXLink.LOGGER.info("YOKONEX-Link 客户端初始化完成");
	}

	@SubscribeEvent
	public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		ModKeyBindings.register(event);
	}
}

@Mod.EventBusSubscriber(modid = YOKONEXLink.MOD_ID, value = Dist.CLIENT)
class ClientEventHandler {
	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event) {
		Minecraft mc = Minecraft.getInstance();
		if (ModKeyBindings.OPEN_CONFIG_KEY.consumeClick() && mc.screen == null) {
			mc.setScreen(new ConfigScreen(null));
		}
	}
}
