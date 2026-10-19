package com.autohelper;

import com.autohelper.config.ConfigManager;
import com.autohelper.gui.ConfigScreen;
import com.autohelper.system.AutoKeySystem;
import com.autohelper.system.ServerAutoJoin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoHelperClient implements ClientModInitializer {
    public static final String MOD_ID = "autohelper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyBinding configKey;
    private static ConfigManager configManager;
    private static AutoKeySystem autoKeySystem;
    private static ServerAutoJoin serverAutoJoin;

    @Override
    public void onInitializeClient() {
        LOGGER.info("AutoHelper initializing...");

        // Load configuration
        configManager = new ConfigManager();
        configManager.load();

        // Initialize systems
        autoKeySystem = new AutoKeySystem(configManager);
        serverAutoJoin = new ServerAutoJoin(configManager);

        // Register keybinding
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autohelper.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.autohelper"
        ));

        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new ConfigScreen(null, configManager));
            }
            autoKeySystem.tick(client);
            serverAutoJoin.tick(client);
        });

        LOGGER.info("AutoHelper initialized successfully!");
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static AutoKeySystem getAutoKeySystem() {
        return autoKeySystem;
    }
}

