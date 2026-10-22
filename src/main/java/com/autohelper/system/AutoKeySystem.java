package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import net.minecraft.client.MinecraftClient;

public class AutoKeySystem {
    private final ConfigManager configManager;

    public AutoKeySystem(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (KeyPressTask task : configManager.getTasks()) {
            if (!task.enabled) {
                continue;
            }

            processTask(task, currentTime, client);
        }
    }

    private void processTask(KeyPressTask task, long currentTime, MinecraftClient client) {
        if (task.lastCycleTime == 0) {
            task.lastCycleTime = currentTime;
            task.currentPressCount = 0;
            task.isHolding = false;
            task.pressStartTime = 0;
        }

        if (task.pressCount > 0 && task.currentPressCount >= task.pressCount) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            }
            return;
        }

        if (task.pressCount == 0 && task.currentPressCount > 0) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            } else {
                if (task.isHolding && currentTime - task.pressStartTime >= task.holdDuration) {
                    // 执行命令
                    if (task.shouldExecuteCommand()) {
                        String[] commands = task.command.split(",");
                        int index = Math.min(task.currentPressCount, commands.length - 1);
                        executeCommand(commands[index].trim(), client);
                    }
                    task.isHolding = false;
                }
                return;
            }
        }

        // 执行命令
        if (task.isHolding) {
            if (currentTime - task.pressStartTime >= task.holdDuration) {
                // 执行命令
                if (task.shouldExecuteCommand()) {
                    String[] commands = task.command.split(",");
                    int index = Math.min(task.currentPressCount, commands.length - 1);
                    executeCommand(commands[index].trim(), client);
                }
                task.isHolding = false;
                task.currentPressCount++;
            }
        } else {
            if (task.currentPressCount == 0) {
                task.pressStartTime = currentTime;
                task.isHolding = true;
            } else if (currentTime - task.pressStartTime >= task.holdDuration + task.keyInterval) {
                task.pressStartTime = currentTime;
                task.isHolding = true;
            }
        }
    }

    private void executeCommand(String command, MinecraftClient client) {
        if (client.player == null || client.player.networkHandler == null) {
            return;
        }

        try {
            String text = command.trim();
            if (text.isEmpty()) {
                return;
            }

            if (text.startsWith("/")) {
                // 命令：移除/然后用sendCommand
                String cmd = text.substring(1);
                client.player.networkHandler.sendCommand(cmd);
                System.out.println("[AutoHelper] Command: /" + cmd);
            } else {
                // 消息：直接用sendChatMessage
                client.player.networkHandler.sendChatMessage(text);
                System.out.println("[AutoHelper] Message: " + text);
            }
        } catch (Exception e) {
            System.err.println("[AutoHelper] Failed to execute: " + e.getMessage());
        }
    }
}
