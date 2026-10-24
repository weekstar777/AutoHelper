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

    /**
     * 重置所有任务的定时状态
     * 在玩家进入世界时调用，确保所有模组加载完成后才开始定时
     */
    public void resetAllTasks() {
        for (KeyPressTask task : configManager.getTasks()) {
            task.reset();
        }
    }

    private void processTask(KeyPressTask task, long currentTime, MinecraftClient client) {
        // 如果所有定时参数都为0，则不启用定时触发（仅手动触发）
        if (task.cycleInterval == 0 && task.keyInterval == 0 &&
            task.holdDuration == 0 && task.pressCount == 0) {
            return;
        }

        if (task.lastCycleTime == 0) {
            task.lastCycleTime = currentTime;
            task.currentPressCount = 0;
            task.isHolding = false;
            task.pressStartTime = 0;
        }

        // 检查是否需要重置循环
        int commandCount = (task.commands != null) ? task.commands.size() : 0;
        if (commandCount == 0) {
            return;
        }

        // pressCount > 0: 执行指定次数后停止
        // pressCount = 0: 执行所有命令后重置循环
        int maxCount = (task.pressCount > 0) ? task.pressCount : commandCount;

        if (task.currentPressCount >= maxCount) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            }
            return;
        }

        // 每个周期开始时，必须等待 cycleInterval 后才能开始执行
        if (task.currentPressCount == 0 && currentTime - task.lastCycleTime < task.cycleInterval) {
            return;
        }

        // 执行命令
        if (task.isHolding) {
            if (currentTime - task.pressStartTime >= task.holdDuration) {
                // 执行命令
                if (task.shouldExecuteCommand()) {
                    int index = Math.min(task.currentPressCount, commandCount - 1);
                    executeCommand(task.commands.get(index).trim(), client);
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
