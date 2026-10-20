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
    private Map<String, KeyBinding> keyBindingMap;
    private Map<String, Integer> keyCodeMap;
    private boolean initialized = false;

    public AutoKeySystem(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private void initializeKeyMaps(MinecraftClient client) {
        if (initialized || client.options == null) {
            return;
        }

        keyBindingMap = new HashMap<>();
        keyCodeMap = new HashMap<>();

        // 映射常用键到它们的KeyBinding
        keyBindingMap.put("Space", client.options.jumpKey);
        keyBindingMap.put("W", client.options.forwardKey);
        keyBindingMap.put("A", client.options.leftKey);
        keyBindingMap.put("S", client.options.backKey);
        keyBindingMap.put("D", client.options.rightKey);
        keyBindingMap.put("Q", client.options.dropKey);
        keyBindingMap.put("E", client.options.inventoryKey);
        keyBindingMap.put("Shift", client.options.sneakKey);
        keyBindingMap.put("Ctrl", client.options.sprintKey);

        // 所有字母键
        for (char c = 'A'; c <= 'Z'; c++) {
            String key = String.valueOf(c);
            if (!keyBindingMap.containsKey(key)) {
                keyCodeMap.put(key, GLFW.GLFW_KEY_A + (c - 'A'));
            }
        }

        // 数字键 0-9
        for (int i = 0; i <= 9; i++) {
            keyCodeMap.put(String.valueOf(i), GLFW.GLFW_KEY_0 + i);
        }

        // 小键盘 Num0-9
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

        // 其他常用键
        keyCodeMap.put("Alt", GLFW.GLFW_KEY_LEFT_ALT);

        initialized = true;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        // 延迟初始化
        if (!initialized) {
            initializeKeyMaps(client);
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
        // 初始化
        if (task.lastCycleTime == 0) {
            task.lastCycleTime = currentTime;
            task.currentPressCount = 0;
            task.isHolding = false;
        }

        // 检查是否完成一轮
        if (task.pressCount > 0 && task.currentPressCount >= task.pressCount) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            }
            return;
        }

        // pressCount = 0 时的处理
        if (task.pressCount == 0 && task.currentPressCount > 0) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            } else {
                return;
            }
        }

        // 处理按键
        if (task.isHolding) {
            if (currentTime - task.pressStartTime >= task.holdDuration) {
                releaseKey(task.keyName, client);
                task.isHolding = false;
                task.currentPressCount++;
            }
        } else {
            if (task.currentPressCount == 0) {
                pressKey(task.keyName, client);
                task.pressStartTime = currentTime;
                task.isHolding = true;
            } else if (currentTime - task.pressStartTime >= task.holdDuration + task.keyInterval) {
                pressKey(task.keyName, client);
                task.pressStartTime = currentTime;
                task.isHolding = true;
            }
        }
    }

    private void pressKey(String keyName, MinecraftClient client) {
        if (!initialized) {
            return;
        }

        // 优先使用KeyBinding
        KeyBinding keyBinding = keyBindingMap.get(keyName);
        if (keyBinding != null) {
            keyBinding.setPressed(true);
            return;
        }

        // 否则使用keyCode
        Integer keyCode = keyCodeMap.get(keyName);
        if (keyCode != null) {
            InputUtil.Key key = InputUtil.fromKeyCode(keyCode, 0);
            KeyBinding.setKeyPressed(key, true);
            KeyBinding.onKeyPressed(key);
        }
    }

    private void releaseKey(String keyName, MinecraftClient client) {
        if (!initialized) {
            return;
        }

        // 优先使用KeyBinding
        KeyBinding keyBinding = keyBindingMap.get(keyName);
        if (keyBinding != null) {
            keyBinding.setPressed(false);
            return;
        }

        // 否则使用keyCode
        Integer keyCode = keyCodeMap.get(keyName);
        if (keyCode != null) {
            InputUtil.Key key = InputUtil.fromKeyCode(keyCode, 0);
            KeyBinding.setKeyPressed(key, false);
        }
    }

    public boolean isValidKey(String keyName) {
        if (!initialized) {
            return true; // 未初始化时假定有效
        }
        return keyBindingMap.containsKey(keyName) || keyCodeMap.containsKey(keyName);
    }
}
