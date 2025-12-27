package cn.cinian;

import cn.cinian.config.ModConfig;
import cn.cinian.event.GameEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("yokonex_link")
public class YOKONEXLink {
	public static final String MOD_ID = "yokonex_link";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public YOKONEXLink() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("YOKONEX-Link 正在初始化...");

		try {
			// 加载配置
			ModConfig.getInstance();

			// 注册游戏事件处理器
			GameEventHandler.register();

			LOGGER.info("YOKONEX-Link 初始化完成");
		} catch (Exception e) {
			LOGGER.error("YOKONEX-Link 初始化失败，但不影响游戏运行", e);
		}
	}
}
