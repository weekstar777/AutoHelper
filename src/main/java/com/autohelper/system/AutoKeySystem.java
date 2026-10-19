package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
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

    private void processTask(KeyPressTask task, long currentTime, MinecraftClient client) {
        // Check if cycle should start
        if (task.lastCycleTime == 0 ||
            currentTime - task.lastCycleTime >= task.cycleInterval) {

            if (task.currentPressCount == 0) {
                task.lastCycleTime = currentTime;
            }
        }

        // Process key holding
        if (task.isHolding) {
            if (currentTime - task.pressStartTime >= task.holdDuration) {
                releaseKey(task.keyName, client);
                task.isHolding = false;
                task.currentPressCount++;

                if (task.currentPressCount >= task.pressCount) {
                    task.currentPressCount = 0;
                }
            }
        } else if (task.currentPressCount < task.pressCount) {
            if (task.currentPressCount == 0 ||
                currentTime - task.pressStartTime >= task.keyInterval) {
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

        long handle = client.getWindow().getHandle();
        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {});
        KeyBinding.setKeyPressed(InputUtil.fromKeyCode(keyCode, 0), true);
    }

    private void releaseKey(String keyName, MinecraftClient client) {
        Integer keyCode = keyCodeMap.get(keyName);
        if (keyCode == null) {
            return;
        }

        KeyBinding.setKeyPressed(InputUtil.fromKeyCode(keyCode, 0), false);
    }

    public boolean isValidKey(String keyName) {
        return keyCodeMap.containsKey(keyName);
    }
}



