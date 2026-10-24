package com.autohelper;

import com.autohelper.config.ConfigManager;
import com.autohelper.gui.ConfigScreen;
import com.autohelper.system.AutoKeySystem;
import com.autohelper.system.ServerAutoJoin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AutoHelper主客户端类
 *
 * 功能：
 * 1. 自动按键系统 - 定时自动按键和手动触发命令
 * 2. 服务器自动连接 - 启动后自动连接服务器
 * 3. 窗口管理 - 自动最大化窗口
 *
 * @author weekstar777
 * @version 1.0.0
 */
public class AutoHelperClient implements ClientModInitializer {
    public static final String MOD_ID = "autohelper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 配置界面快捷键
    private static KeyBinding configKey;

    // 配置管理器
    private static ConfigManager configManager;

    // 自动按键系统
    private static AutoKeySystem autoKeySystem;

    // 服务器自动连接系统
    private static ServerAutoJoin serverAutoJoin;

    @Override
    public void onInitializeClient() {
        LOGGER.info("AutoHelper initializing...");

        // 加载配置文件
        configManager = new ConfigManager();
        configManager.load();

        // 初始化功能系统
        autoKeySystem = new AutoKeySystem(configManager);
        serverAutoJoin = new ServerAutoJoin(configManager);

        // 注册配置界面快捷键（默认H键）
        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autohelper.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.autohelper"
        ));

        // 注册Tick事件处理
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // 按H键打开配置界面
            if (configKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new ConfigScreen(null, configManager));
            }

            // 执行自动按键系统Tick
            autoKeySystem.tick(client);

            // 执行服务器自动连接Tick
            serverAutoJoin.tick(client);
        });

        // 监听玩家进入世界事件，重置定时任务
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info("Player joined world, resetting all task timers");
            autoKeySystem.resetAllTasks();
        });

        LOGGER.info("AutoHelper initialized successfully!");
    }

    /**
     * 获取配置管理器实例
     */
    public static ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * 获取自动按键系统实例
     */
    public static AutoKeySystem getAutoKeySystem() {
        return autoKeySystem;
    }
}

