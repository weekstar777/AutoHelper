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
    private Map<String, KeyBinding> customKeyBindings;
    private boolean initialized = false;

    public AutoKeySystem(ConfigManager configManager) {
        this.configManager = configManager;
        this.customKeyBindings = new HashMap<>();
    }

    private void initializeKeyBindings(MinecraftClient client) {
        if (initialized) {
            return;
        }

        // 扫描所有已注册的KeyBinding，通过keyCode匹配
        for (int keyCode = GLFW.GLFW_KEY_SPACE; keyCode <= GLFW.GLFW_KEY_LAST; keyCode++) {
            String keyName = getKeyNameFromCode(keyCode);
            if (keyName != null) {
                // 找到绑定到这个keyCode的KeyBinding（包括MacroKeybinds的）
                for (KeyBinding kb : client.options.allKeys) {
                    if (kb.matchesKey(keyCode, 0) || kb.matchesKey(keyCode, -1)) {
                        if (!customKeyBindings.containsKey(keyName)) {
                            customKeyBindings.put(keyName, kb);
                        }
                        break;
                    }
                }
            }
        }

        initialized = true;
    }

    private String getKeyNameFromCode(int code) {
        // 字母 A-Z
        if (code >= GLFW.GLFW_KEY_A && code <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char)('A' + (code - GLFW.GLFW_KEY_A)));
        }

        // 数字 0-9
        if (code >= GLFW.GLFW_KEY_0 && code <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char)('0' + (code - GLFW.GLFW_KEY_0)));
        }

        // 小键盘
        if (code >= GLFW.GLFW_KEY_KP_0 && code <= GLFW.GLFW_KEY_KP_9) {
            return "Num" + (code - GLFW.GLFW_KEY_KP_0);
        }

        // 特殊键
        switch (code) {
            case GLFW.GLFW_KEY_SPACE: return "Space";
            case GLFW.GLFW_KEY_ENTER: return "Enter";
            case GLFW.GLFW_KEY_F1: return "F1";
            case GLFW.GLFW_KEY_F2: return "F2";
            case GLFW.GLFW_KEY_F3: return "F3";
            case GLFW.GLFW_KEY_F4: return "F4";
            case GLFW.GLFW_KEY_F5: return "F5";
            case GLFW.GLFW_KEY_F6: return "F6";
            case GLFW.GLFW_KEY_F7: return "F7";
            case GLFW.GLFW_KEY_F8: return "F8";
            case GLFW.GLFW_KEY_F9: return "F9";
            case GLFW.GLFW_KEY_F10: return "F10";
            case GLFW.GLFW_KEY_F11: return "F11";
            case GLFW.GLFW_KEY_F12: return "F12";
            default: return null;
        }
    }

    public void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return;
        }

        if (!initialized) {
            initializeKeyBindings(client);
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

        // 检查是否完成一轮
        if (task.pressCount > 0 && task.currentPressCount >= task.pressCount) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            }
            return;
        }

        // pressCount = 0 时的无限循环处理
        if (task.pressCount == 0 && task.currentPressCount > 0) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            } else {
                if (task.isHolding && currentTime - task.pressStartTime >= task.holdDuration) {
                    releaseKey(task.keyName, client);
                    task.isHolding = false;
                }
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

    private int getKeyCode(String keyName) {
        // 字母键
        if (keyName.length() == 1 && keyName.matches("[A-Z]")) {
            return GLFW.GLFW_KEY_A + (keyName.charAt(0) - 'A');
        }

        // 数字键 1-9
        if (keyName.matches("[1-9]")) {
            return GLFW.GLFW_KEY_1 + (keyName.charAt(0) - '1');
        }

        // 数字键 0
        if (keyName.equals("0")) {
            return GLFW.GLFW_KEY_0;
        }

        // 小键盘数字 Num0-9
        if (keyName.startsWith("Num") && keyName.length() == 4) {
            char digit = keyName.charAt(3);
            if (digit >= '0' && digit <= '9') {
                return GLFW.GLFW_KEY_KP_0 + (digit - '0');
            }
        }

        // 特殊键映射
        switch (keyName) {
            case "Space": return GLFW.GLFW_KEY_SPACE;
            case "Enter": return GLFW.GLFW_KEY_ENTER;
            case "Escape": return GLFW.GLFW_KEY_ESCAPE;
            case "Tab": return GLFW.GLFW_KEY_TAB;
            case "Backspace": return GLFW.GLFW_KEY_BACKSPACE;
            case "Shift": return GLFW.GLFW_KEY_LEFT_SHIFT;
            case "Ctrl": return GLFW.GLFW_KEY_LEFT_CONTROL;
            case "Alt": return GLFW.GLFW_KEY_LEFT_ALT;
            case "F1": return GLFW.GLFW_KEY_F1;
            case "F2": return GLFW.GLFW_KEY_F2;
            case "F3": return GLFW.GLFW_KEY_F3;
            case "F4": return GLFW.GLFW_KEY_F4;
            case "F5": return GLFW.GLFW_KEY_F5;
            case "F6": return GLFW.GLFW_KEY_F6;
            case "F7": return GLFW.GLFW_KEY_F7;
            case "F8": return GLFW.GLFW_KEY_F8;
            case "F9": return GLFW.GLFW_KEY_F9;
            case "F10": return GLFW.GLFW_KEY_F10;
            case "F11": return GLFW.GLFW_KEY_F11;
            case "F12": return GLFW.GLFW_KEY_F12;
            default: return -1;
        }
    }

    private void pressKey(String keyName, MinecraftClient client) {
        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) {
            return;
        }

        long window = client.getWindow().getHandle();

        // 方法1: 触发所有匹配keyCode的KeyBinding（包括MacroKeybinds的）
        for (KeyBinding kb : client.options.allKeys) {
            if (kb.matchesKey(keyCode, 0) || kb.matchesKey(keyCode, -1)) {
                kb.setPressed(true);
            }
        }

        // 方法2: 发送底层键盘事件
        client.keyboard.onKey(window, keyCode, 0, GLFW.GLFW_PRESS, 0);
        client.keyboard.onKey(window, keyCode, 0, GLFW.GLFW_REPEAT, 0);
    }

    private void releaseKey(String keyName, MinecraftClient client) {
        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) {
            return;
        }

        long window = client.getWindow().getHandle();

        // 方法1: 释放所有KeyBinding
        for (KeyBinding kb : client.options.allKeys) {
            if (kb.matchesKey(keyCode, 0) || kb.matchesKey(keyCode, -1)) {
                kb.setPressed(false);
            }
        }

        // 方法2: 发送释放事件
        client.keyboard.onKey(window, keyCode, 0, GLFW.GLFW_RELEASE, 0);
    }

    public boolean isValidKey(String keyName) {
        return getKeyCode(keyName) != -1;
    }
}

