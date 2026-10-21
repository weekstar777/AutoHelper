package com.autohelper.system;

import org.lwjgl.glfw.GLFW;

public class KeyCodeMapper {

    /**
     * 将按键名称转换为GLFW按键码
     * @param keyName 按键名称（如 "A", "Space", "F1"）
     * @return GLFW按键码，未找到返回-1
     */
    public static int getGlfwKeyCode(String keyName) {
        if (keyName == null || keyName.isEmpty()) {
            return -1;
        }

        // 字母键 A-Z
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

        // 特殊按键
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
}
