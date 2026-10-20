package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class AutoKeySystem {
    private final ConfigManager configManager;
    private final Map<String, Integer> keyCodeMap;

    public AutoKeySystem(ConfigManager configManager) {
        this.configManager = configManager;
        this.keyCodeMap = new HashMap<>();
        initializeKeyCodeMap();
    }

    private void initializeKeyCodeMap() {
        // Letters
        for (char c = 'A'; c <= 'Z'; c++) {
            keyCodeMap.put(String.valueOf(c), GLFW.GLFW_KEY_A + (c - 'A'));
        }

        // Numbers
        for (int i = 0; i <= 9; i++) {
            keyCodeMap.put(String.valueOf(i), GLFW.GLFW_KEY_0 + i);
        }

        // Numpad
        keyCodeMap.put("Num0", GLFW.GLFW_KEY_KP_0);
        keyCodeMap.put("Num1", GLFW.GLFW_KEY_KP_1);
        keyCodeMap.put("Num2", GLFW.GLFW_KEY_KP_2);
        keyCodeMap.put("Num3", GLFW.GLFW_KEY_KP_3);
        keyCodeMap.put("Num4", GLFW.GLFW_KEY_KP_4);
        keyCodeMap.put("Num5", GLFW.GLFW_KEY_KP_5);
        keyCodeMap.put("Num6", GLFW.GLFW_KEY_KP_6);
        keyCodeMap.put("Num7", GLFW.GLFW_KEY_KP_7);
        keyCodeMap.put("Num8", GLFW.GLFW_KEY_KP_8);
        keyCodeMap.put("Num9", GLFW.GLFW_KEY_KP_9);

        // Common keys
        keyCodeMap.put("Space", GLFW.GLFW_KEY_SPACE);
        keyCodeMap.put("Shift", GLFW.GLFW_KEY_LEFT_SHIFT);
        keyCodeMap.put("Ctrl", GLFW.GLFW_KEY_LEFT_CONTROL);
        keyCodeMap.put("Alt", GLFW.GLFW_KEY_LEFT_ALT);
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
     * 处理任务逻辑：
     * 1. 执行N次按键（每次按holdDuration毫秒，间隔keyInterval毫秒）
     * 2. 完成一轮后等待cycleInterval毫秒
     * 3. 开始下一轮
     */
    private void processTask(KeyPressTask task, long currentTime, MinecraftClient client) {
        // 初始化
        if (task.lastCycleTime == 0) {
            task.lastCycleTime = currentTime;
            task.currentPressCount = 0;
            task.isHolding = false;
        }

        // 检查是否完成一轮，在等待循环间隔
        if (task.currentPressCount >= task.pressCount) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                // 开始新一轮
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            }
            return;
        }

        // 处理按键
        if (task.isHolding) {
            // 正在按住
            if (currentTime - task.pressStartTime >= task.holdDuration) {
                // 按住时间到，释放
                releaseKey(task.keyName, client);
                task.isHolding = false;
                task.currentPressCount++;
            }
        } else {
            // 检查是否该按下一次
            if (task.currentPressCount == 0) {
                // 第一次按键，立即执行
                pressKey(task.keyName, client);
                task.pressStartTime = currentTime;
                task.isHolding = true;
            } else if (currentTime - task.pressStartTime >= task.holdDuration + task.keyInterval) {
                // 等待间隔后按下一次
                pressKey(task.keyName, client);
                task.pressStartTime = currentTime;
                task.isHolding = true;
            }
        }
    }

    private void pressKey(String keyName, MinecraftClient client) {
        Integer keyCode = keyCodeMap.get(keyName);
        if (keyCode == null) {
            return;
        }

        long windowHandle = client.getWindow().getHandle();

        // 使用keyboard事件模拟按键
        try {
            client.execute(() -> {
                client.keyboard.onKey(windowHandle, keyCode, 0, GLFW.GLFW_PRESS, 0);
            });
        } catch (Exception e) {
            // 忽略错误
        }
    }

    private void releaseKey(String keyName, MinecraftClient client) {
        Integer keyCode = keyCodeMap.get(keyName);
        if (keyCode == null) {
            return;
        }

        long windowHandle = client.getWindow().getHandle();

        try {
            client.execute(() -> {
                client.keyboard.onKey(windowHandle, keyCode, 0, GLFW.GLFW_RELEASE, 0);
            });
        } catch (Exception e) {
            // 忽略错误
        }
    }

    public boolean isValidKey(String keyName) {
        return keyCodeMap.containsKey(keyName);
    }
}
