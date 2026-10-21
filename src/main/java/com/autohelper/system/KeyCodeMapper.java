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
            // 基本控制键
            case "Space": return GLFW.GLFW_KEY_SPACE;
            case "Enter": return GLFW.GLFW_KEY_ENTER;
            case "Escape": return GLFW.GLFW_KEY_ESCAPE;
            case "Tab": return GLFW.GLFW_KEY_TAB;
            case "Backspace": return GLFW.GLFW_KEY_BACKSPACE;
            case "Delete": return GLFW.GLFW_KEY_DELETE;
            case "Insert": return GLFW.GLFW_KEY_INSERT;

            // 修饰键
            case "Shift": return GLFW.GLFW_KEY_LEFT_SHIFT;
            case "Ctrl": return GLFW.GLFW_KEY_LEFT_CONTROL;
            case "Alt": return GLFW.GLFW_KEY_LEFT_ALT;
            case "CapsLock": return GLFW.GLFW_KEY_CAPS_LOCK;

            // 方向键
            case "Up": return GLFW.GLFW_KEY_UP;
            case "Down": return GLFW.GLFW_KEY_DOWN;
            case "Left": return GLFW.GLFW_KEY_LEFT;
            case "Right": return GLFW.GLFW_KEY_RIGHT;

            // 导航键
            case "Home": return GLFW.GLFW_KEY_HOME;
            case "End": return GLFW.GLFW_KEY_END;
            case "PageUp": return GLFW.GLFW_KEY_PAGE_UP;
            case "PageDown": return GLFW.GLFW_KEY_PAGE_DOWN;

            // 功能键
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

            // 标点符号和特殊字符
            case ",": case "Comma": return GLFW.GLFW_KEY_COMMA;
            case ".": case "Period": return GLFW.GLFW_KEY_PERIOD;
            case "/": case "Slash": return GLFW.GLFW_KEY_SLASH;
            case ";": case "Semicolon": return GLFW.GLFW_KEY_SEMICOLON;
            case "'": case "Quote": return GLFW.GLFW_KEY_APOSTROPHE;
            case "[": case "LeftBracket": return GLFW.GLFW_KEY_LEFT_BRACKET;
            case "]": case "RightBracket": return GLFW.GLFW_KEY_RIGHT_BRACKET;
            case "\\": case "Backslash": return GLFW.GLFW_KEY_BACKSLASH;
            case "-": case "Minus": return GLFW.GLFW_KEY_MINUS;
            case "=": case "Equal": return GLFW.GLFW_KEY_EQUAL;
            case "`": case "GraveAccent": return GLFW.GLFW_KEY_GRAVE_ACCENT;

            default: return -1;
        }
    }
}
