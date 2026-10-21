package com.autohelper.mixin;

import com.autohelper.AutoHelperClient;
import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import com.autohelper.system.KeyCodeMapper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {

    /**
     * 监听玩家手动按键，执行配置的命令
     */
    @Inject(method = "onKey", at = @At("TAIL"))
    private void onManualKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // 只在按键按下时触发
        if (action != GLFW.GLFW_PRESS) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.networkHandler == null) {
            return;
        }

        ConfigManager configManager = AutoHelperClient.getConfigManager();
        if (configManager == null) {
            return;
        }

        // 检查所有启用的任务
        for (KeyPressTask task : configManager.getTasks()) {
            if (!task.enabled) {
                continue;
            }

            // 检查是否有命令需要执行
            if (!task.shouldExecuteCommand()) {
                continue;
            }

            // 检查按键是否匹配
            if (task.keyName != null && !task.keyName.isEmpty()) {
                int taskKeyCode = KeyCodeMapper.getGlfwKeyCode(task.keyName);
                if (taskKeyCode == key) {
                    // 匹配成功，执行命令
                    executeCommand(task.command, client);
                    // 只触发第一个匹配的任务
                    break;
                }
            }
        }
    }

    private void executeCommand(String command, MinecraftClient client) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        try {
            String cmd = command.trim();
            // 移除开头的斜杠（如果有）
            if (cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }

            // 执行命令
            client.player.networkHandler.sendCommand(cmd);
            System.out.println("[AutoHelper] Manual trigger: /" + cmd);
        } catch (Exception e) {
            System.err.println("[AutoHelper] Failed to execute manual command: " + e.getMessage());
        }
    }
}
