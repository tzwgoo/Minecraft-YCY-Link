package cn.cinian.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static KeyMapping OPEN_CONFIG_KEY;

    public static void register(RegisterKeyMappingsEvent event) {
        OPEN_CONFIG_KEY = new KeyMapping(
            "key.yokonex_link.open_config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "category.yokonex_link"
        );
        event.register(OPEN_CONFIG_KEY);
    }
}
