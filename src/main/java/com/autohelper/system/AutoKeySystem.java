package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

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
                // 仍在等待下一循环，但需要检查是否需要释放当前按键
                if (task.isHolding && currentTime - task.pressStartTime >= task.holdDuration) {
                    releaseKey(task.keyName, client);
                    task.isHolding = false;
                }
                return;
            }
        }

        // 处理按键 - 只在状态变化时触发事件
        if (task.isHolding) {
            // 检查是否需要释放
            if (currentTime - task.pressStartTime >= task.holdDuration) {
                // 时间到，释放
                releaseKey(task.keyName, client);
                task.isHolding = false;
                task.currentPressCount++;
            }
            // 按住期间不重复触发事件
        } else {
            // 检查是否该开始新的按键
            if (task.currentPressCount == 0) {
                // 第一次按键
                pressKey(task.keyName, client);
                task.pressStartTime = currentTime;
                task.isHolding = true;
            } else if (currentTime - task.pressStartTime >= task.holdDuration + task.keyInterval) {
                // 间隔时间到，开始下一次按键
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
            case "CapsLock": return GLFW.GLFW_KEY_CAPS_LOCK;

            // F1-F12
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

            // 符号键
            case "-": case "Minus": return GLFW.GLFW_KEY_MINUS;
            case "=": case "Equal": return GLFW.GLFW_KEY_EQUAL;
            case "[": return GLFW.GLFW_KEY_LEFT_BRACKET;
            case "]": return GLFW.GLFW_KEY_RIGHT_BRACKET;
            case ";": case "Semicolon": return GLFW.GLFW_KEY_SEMICOLON;
            case "'": case "Apostrophe": return GLFW.GLFW_KEY_APOSTROPHE;
            case ",": case "Comma": return GLFW.GLFW_KEY_COMMA;
            case ".": case "Period": return GLFW.GLFW_KEY_PERIOD;
            case "/": case "Slash": return GLFW.GLFW_KEY_SLASH;
            case "\\": case "Backslash": return GLFW.GLFW_KEY_BACKSLASH;
            case "`": case "GraveAccent": return GLFW.GLFW_KEY_GRAVE_ACCENT;

            default: return -1;
        }
    }

    private void pressKey(String keyName, MinecraftClient client) {
        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) {
            return;
        }

        long window = client.getWindow().getHandle();
        int scancode = GLFW.glfwGetKeyScancode(keyCode);

        // 调用InputUtil来正确触发按键
        InputUtil.Key key = InputUtil.fromKeyCode(keyCode, scancode);

        // 通过KeyBinding系统触发
        client.keyboard.onKey(window, keyCode, scancode, GLFW.GLFW_PRESS, 0);

        // 同时更新InputUtil的按键状态
        try {
            // 调用InputUtil.Key的内部方法标记按键按下
            key.getClass().getMethod("setPressed", boolean.class).invoke(key, true);
        } catch (Exception ignored) {
            // 如果反射失败，至少onKey已经调用了
        }
    }

    private void releaseKey(String keyName, MinecraftClient client) {
        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) {
            return;
        }

        long window = client.getWindow().getHandle();
        int scancode = GLFW.glfwGetKeyScancode(keyCode);

        InputUtil.Key key = InputUtil.fromKeyCode(keyCode, scancode);

        // 释放按键
        client.keyboard.onKey(window, keyCode, scancode, GLFW.GLFW_RELEASE, 0);

        try {
            key.getClass().getMethod("setPressed", boolean.class).invoke(key, false);
        } catch (Exception ignored) {
        }
    }

    public boolean isValidKey(String keyName) {
        return getKeyCode(keyName) != -1;
    }
}

