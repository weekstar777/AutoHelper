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
                int keyIndex = -1;

                // 检查是否是逗号分隔的按键列表（如 "A,B,Space"）
                if (task.keyName.contains(",")) {
                    String[] keys = task.keyName.split(",");
                    for (int i = 0; i < keys.length; i++) {
                        int taskKeyCode = KeyCodeMapper.getGlfwKeyCode(keys[i].trim());
                        if (taskKeyCode == key) {
                            keyIndex = i;
                            break;
                        }
                    }
                } else if (task.keyName.length() > 1 && task.keyName.matches("[A-Z]+")) {
                    // 连续字母序列（如 "ABC"）
                    for (int i = 0; i < task.keyName.length(); i++) {
                        int taskKeyCode = KeyCodeMapper.getGlfwKeyCode(String.valueOf(task.keyName.charAt(i)));
                        if (taskKeyCode == key) {
                            keyIndex = i;
                            break;
                        }
                    }
                } else {
                    // 单个按键匹配
                    int taskKeyCode = KeyCodeMapper.getGlfwKeyCode(task.keyName);
                    if (taskKeyCode == key) {
                        keyIndex = 0;
                    }
                }

                if (keyIndex != -1) {
                    // 匹配成功，执行对应的命令
                    executeCommandByIndex(task.command, keyIndex, client);
                    // 只触发第一个匹配的任务
                    break;
                }
            }
        }
    }

    private void executeCommandByIndex(String command, int index, MinecraftClient client) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        try {
            // 分割命令列表（用逗号分隔）
            String[] commands = command.split(",");

            // 获取对应索引的命令
            String text;
            if (index < commands.length) {
                text = commands[index].trim();
            } else {
                // 如果索引超出命令数量，使用最后一个命令
                text = commands[commands.length - 1].trim();
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

    private void executeCommand(String command, MinecraftClient client) {
        executeCommandByIndex(command, 0, client);
    }
}
