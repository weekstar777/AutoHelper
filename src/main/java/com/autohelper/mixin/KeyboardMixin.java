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

import java.util.List;

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
            if (task.keyNames != null && !task.keyNames.isEmpty()) {
                int keyIndex = -1;

                // 检查按键列表
                for (int i = 0; i < task.keyNames.size(); i++) {
                    int taskKeyCode = KeyCodeMapper.getGlfwKeyCode(task.keyNames.get(i).trim());
                    if (taskKeyCode == key) {
                        keyIndex = i;
                        break;
                    }
                }

                if (keyIndex != -1) {
                    // 匹配成功，执行对应的命令
                    executeCommandByIndex(task.commands, keyIndex, client);
                }
            }
        }
    }

    private void executeCommandByIndex(List<String> commands, int index, MinecraftClient client) {
        if (commands == null || commands.isEmpty()) {
            return;
        }

        try {
            // 获取对应索引的命令
            String text;
            if (index < commands.size()) {
                text = commands.get(index).trim();
            } else {
                // 如果索引超出命令数量，使用最后一个命令
                text = commands.get(commands.size() - 1).trim();
            }

            if (text.startsWith("/")) {
                // 命令：移除/然后用sendCommand
                String cmd = text.substring(1);
                client.player.networkHandler.sendCommand(cmd);
                System.out.println("[AutoHelper] Manual command[" + index + "]: /" + cmd);
            } else {
                // 消息：直接用sendChatMessage
                client.player.networkHandler.sendChatMessage(text);
                System.out.println("[AutoHelper] Manual message[" + index + "]: " + text);
            }
        } catch (Exception e) {
            System.err.println("[AutoHelper] Failed to execute manual command: " + e.getMessage());
        }
    }
}
