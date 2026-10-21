package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import com.autohelper.mixin.KeyboardMixin;
import net.minecraft.client.MinecraftClient;
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

        // 处理已注入的按键
        ((KeyboardMixin)(Object)client.keyboard).processInjectedKeys();

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
        if (keyName.length() == 1 && keyName.matches("[A-Z]")) {
            return GLFW.GLFW_KEY_A + (keyName.charAt(0) - 'A');
        }
        if (keyName.matches("[1-9]")) {
            return GLFW.GLFW_KEY_1 + (keyName.charAt(0) - '1');
        }
        if (keyName.equals("0")) {
            return GLFW.GLFW_KEY_0;
        }
        if (keyName.startsWith("Num") && keyName.length() == 4) {
            char digit = keyName.charAt(3);
            if (digit >= '0' && digit <= '9') {
                return GLFW.GLFW_KEY_KP_0 + (digit - '0');
            }
        }

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
            case "-": case "Minus": return GLFW.GLFW_KEY_MINUS;
            case "=": case "Equal": return GLFW.GLFW_KEY_EQUAL;
            case "[": return GLFW.GLFW_KEY_LEFT_BRACKET;
            case "]": return GLFW.GLFW_KEY_RIGHT_BRACKET;
            case ";": return GLFW.GLFW_KEY_SEMICOLON;
            case "'": return GLFW.GLFW_KEY_APOSTROPHE;
            case ",": return GLFW.GLFW_KEY_COMMA;
            case ".": return GLFW.GLFW_KEY_PERIOD;
            case "/": return GLFW.GLFW_KEY_SLASH;
            case "\\": return GLFW.GLFW_KEY_BACKSLASH;
            case "`": return GLFW.GLFW_KEY_GRAVE_ACCENT;
            default: return -1;
        }
    }

    private void pressKey(String keyName, MinecraftClient client) {
        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) return;

        long window = client.getWindow().getHandle();
        int scancode = GLFW.glfwGetKeyScancode(keyCode);

        // 通过Mixin注入到Keyboard的onKey方法
        KeyboardMixin.injectKey(window, keyCode, scancode, GLFW.GLFW_PRESS, 0);
    }

    private void releaseKey(String keyName, MinecraftClient client) {
        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) return;

        long window = client.getWindow().getHandle();
        int scancode = GLFW.glfwGetKeyScancode(keyCode);

        // 通过Mixin注入到Keyboard的onKey方法
        KeyboardMixin.injectKey(window, keyCode, scancode, GLFW.GLFW_RELEASE, 0);
    }

    public boolean isValidKey(String keyName) {
        return getKeyCode(keyName) != -1;
    }
}

