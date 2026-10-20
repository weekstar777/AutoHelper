package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class AutoKeySystem {
    private final ConfigManager configManager;
    private Map<String, KeyBinding> keyBindingMap;
    private boolean initialized = false;

    public AutoKeySystem(ConfigManager configManager) {
        this.configManager = configManager;
    }

    private void initializeKeyMaps(MinecraftClient client) {
        if (initialized || client.options == null) {
            return;
        }

        keyBindingMap = new HashMap<>();

        // 游戏控制键
        keyBindingMap.put("Space", client.options.jumpKey);
        keyBindingMap.put("W", client.options.forwardKey);
        keyBindingMap.put("A", client.options.leftKey);
        keyBindingMap.put("S", client.options.backKey);
        keyBindingMap.put("D", client.options.rightKey);
        keyBindingMap.put("Q", client.options.dropKey);
        keyBindingMap.put("E", client.options.inventoryKey);
        keyBindingMap.put("Shift", client.options.sneakKey);
        keyBindingMap.put("Ctrl", client.options.sprintKey);
        keyBindingMap.put("F", client.options.swapHandsKey);

        // 战斗键
        keyBindingMap.put("R", client.options.attackKey);
        keyBindingMap.put("T", client.options.useKey);
        keyBindingMap.put("G", client.options.pickItemKey);

        // 快捷栏 1-9
        for (int i = 0; i < 9; i++) {
            keyBindingMap.put(String.valueOf(i + 1), client.options.hotbarKeys[i]);
        }

        // 其他常用键（如果有对应的KeyBinding）
        try {
            // 尝试添加其他可能的键
            keyBindingMap.put("C", findKeyBindingByKey(client, GLFW.GLFW_KEY_C));
            keyBindingMap.put("V", findKeyBindingByKey(client, GLFW.GLFW_KEY_V));
            keyBindingMap.put("B", findKeyBindingByKey(client, GLFW.GLFW_KEY_B));
            keyBindingMap.put("N", findKeyBindingByKey(client, GLFW.GLFW_KEY_N));
            keyBindingMap.put("M", findKeyBindingByKey(client, GLFW.GLFW_KEY_M));

            // 小键盘尝试映射到数字键
            keyBindingMap.put("Num1", client.options.hotbarKeys[0]);
            keyBindingMap.put("Num2", client.options.hotbarKeys[1]);
            keyBindingMap.put("Num3", client.options.hotbarKeys[2]);
            keyBindingMap.put("Num4", client.options.hotbarKeys[3]);
            keyBindingMap.put("Num5", client.options.hotbarKeys[4]);
            keyBindingMap.put("Num6", client.options.hotbarKeys[5]);
            keyBindingMap.put("Num7", client.options.hotbarKeys[6]);
            keyBindingMap.put("Num8", client.options.hotbarKeys[7]);
            keyBindingMap.put("Num9", client.options.hotbarKeys[8]);
        } catch (Exception e) {
            // 忽略找不到的KeyBinding
        }

        initialized = true;
    }

    private KeyBinding findKeyBindingByKey(MinecraftClient client, int keyCode) {
        // 遍历所有KeyBinding，找到对应keyCode的
        for (KeyBinding kb : client.options.allKeys) {
            if (kb.matchesKey(keyCode, 0)) {
                return kb;
            }
        }
        return null;
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

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

        // 处理按键 - 持续保持按下状态直到时间到
        if (task.isHolding) {
            // 持续按住
            pressKey(task.keyName);

            if (currentTime - task.pressStartTime >= task.holdDuration) {
                // 时间到，释放
                releaseKey(task.keyName);
                task.isHolding = false;
                task.currentPressCount++;
            }
        } else {
            // 检查是否该开始新的按键
            if (task.currentPressCount == 0) {
                // 第一次按键
                pressKey(task.keyName);
                task.pressStartTime = currentTime;
                task.isHolding = true;
            } else if (currentTime - task.pressStartTime >= task.holdDuration + task.keyInterval) {
                // 间隔时间到，开始下一次按键
                pressKey(task.keyName);
                task.pressStartTime = currentTime;
                task.isHolding = true;
            }
        }
    }

    private void pressKey(String keyName) {
        if (!initialized || keyBindingMap == null) {
            return;
        }

        KeyBinding keyBinding = keyBindingMap.get(keyName);
        if (keyBinding != null) {
            keyBinding.setPressed(true);
        }
    }

    private void releaseKey(String keyName) {
        if (!initialized || keyBindingMap == null) {
            return;
        }

        KeyBinding keyBinding = keyBindingMap.get(keyName);
        if (keyBinding != null) {
            keyBinding.setPressed(false);
        }
    }

    public boolean isValidKey(String keyName) {
        if (!initialized) {
            return true;
        }
        return keyBindingMap != null && keyBindingMap.containsKey(keyName);
    }
}
