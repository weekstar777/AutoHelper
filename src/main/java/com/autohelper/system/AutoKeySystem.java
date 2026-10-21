package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import net.minecraft.client.MinecraftClient;

import java.awt.Robot;
import java.awt.event.KeyEvent;

public class AutoKeySystem {
    private final ConfigManager configManager;
    private Robot robot;

    public AutoKeySystem(ConfigManager configManager) {
        this.configManager = configManager;
        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(0);
        } catch (Exception e) {
            System.err.println("[AutoHelper] Failed to create Robot: " + e.getMessage());
        }
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

        if (task.pressCount > 0 && task.currentPressCount >= task.pressCount) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            }
            return;
        }

        if (task.pressCount == 0 && task.currentPressCount > 0) {
            if (currentTime - task.lastCycleTime >= task.cycleInterval) {
                task.currentPressCount = 0;
                task.lastCycleTime = currentTime;
                task.isHolding = false;
            } else {
                if (task.isHolding && currentTime - task.pressStartTime >= task.holdDuration) {
                    releaseKeyWithCommand(task.keyName, task.command, client, task.shouldExecuteKeyPress(), task.shouldExecuteCommand());
                    task.isHolding = false;
                }
                return;
            }
        }

        if (task.isHolding) {
            if (currentTime - task.pressStartTime >= task.holdDuration) {
                releaseKeyWithCommand(task.keyName, task.command, client, task.shouldExecuteKeyPress(), task.shouldExecuteCommand());
                task.isHolding = false;
                task.currentPressCount++;
            }
        } else {
            if (task.currentPressCount == 0) {
                pressKey(task.keyName, client, task.shouldExecuteKeyPress());
                task.pressStartTime = currentTime;
                task.isHolding = true;
            } else if (currentTime - task.pressStartTime >= task.holdDuration + task.keyInterval) {
                pressKey(task.keyName, client, task.shouldExecuteKeyPress());
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
            String cmd = command.trim();
            // 移除开头的斜杠（如果有）
            if (cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }

            // 执行命令
            client.player.networkHandler.sendCommand(cmd);
            System.out.println("[AutoHelper] Executed command: /" + cmd);
        } catch (Exception e) {
            System.err.println("[AutoHelper] Failed to execute command: " + e.getMessage());
        }
    }

    private int getKeyCode(String keyName) {
        if (keyName.length() == 1 && keyName.matches("[A-Z]")) {
            return KeyEvent.VK_A + (keyName.charAt(0) - 'A');
        }
        if (keyName.matches("[1-9]")) {
            return KeyEvent.VK_1 + (keyName.charAt(0) - '1');
        }
        if (keyName.equals("0")) {
            return KeyEvent.VK_0;
        }
        if (keyName.startsWith("Num") && keyName.length() == 4) {
            char digit = keyName.charAt(3);
            if (digit >= '0' && digit <= '9') {
                return KeyEvent.VK_NUMPAD0 + (digit - '0');
            }
        }

        switch (keyName) {
            // 基本控制键
            case "Space": return KeyEvent.VK_SPACE;
            case "Enter": return KeyEvent.VK_ENTER;
            case "Tab": return KeyEvent.VK_TAB;
            case "Backspace": return KeyEvent.VK_BACK_SPACE;
            case "Delete": return KeyEvent.VK_DELETE;
            case "Insert": return KeyEvent.VK_INSERT;
            case "Escape": return KeyEvent.VK_ESCAPE;

            // 修饰键
            case "Shift": return KeyEvent.VK_SHIFT;
            case "Ctrl": return KeyEvent.VK_CONTROL;
            case "Alt": return KeyEvent.VK_ALT;
            case "CapsLock": return KeyEvent.VK_CAPS_LOCK;

            // 方向键
            case "Up": return KeyEvent.VK_UP;
            case "Down": return KeyEvent.VK_DOWN;
            case "Left": return KeyEvent.VK_LEFT;
            case "Right": return KeyEvent.VK_RIGHT;

            // 导航键
            case "Home": return KeyEvent.VK_HOME;
            case "End": return KeyEvent.VK_END;
            case "PageUp": return KeyEvent.VK_PAGE_UP;
            case "PageDown": return KeyEvent.VK_PAGE_DOWN;

            // 功能键
            case "F1": return KeyEvent.VK_F1;
            case "F2": return KeyEvent.VK_F2;
            case "F3": return KeyEvent.VK_F3;
            case "F4": return KeyEvent.VK_F4;
            case "F5": return KeyEvent.VK_F5;
            case "F6": return KeyEvent.VK_F6;
            case "F7": return KeyEvent.VK_F7;
            case "F8": return KeyEvent.VK_F8;
            case "F9": return KeyEvent.VK_F9;
            case "F10": return KeyEvent.VK_F10;
            case "F11": return KeyEvent.VK_F11;
            case "F12": return KeyEvent.VK_F12;

            // 标点符号
            case ",": case "Comma": return KeyEvent.VK_COMMA;
            case ".": case "Period": return KeyEvent.VK_PERIOD;
            case "/": case "Slash": return KeyEvent.VK_SLASH;
            case ";": case "Semicolon": return KeyEvent.VK_SEMICOLON;
            case "'": case "Quote": return KeyEvent.VK_QUOTE;
            case "[": case "LeftBracket": return KeyEvent.VK_OPEN_BRACKET;
            case "]": case "RightBracket": return KeyEvent.VK_CLOSE_BRACKET;
            case "\\": case "Backslash": return KeyEvent.VK_BACK_SLASH;
            case "-": case "Minus": return KeyEvent.VK_MINUS;
            case "=": case "Equal": return KeyEvent.VK_EQUALS;
            case "`": case "GraveAccent": return KeyEvent.VK_BACK_QUOTE;

            default: return -1;
        }
    }

    private void pressKey(String keyName, MinecraftClient client, boolean execute) {
        if (!execute || robot == null || keyName == null || keyName.isEmpty()) return;

        // 检查是否是逗号分隔的按键列表（如 "A,B,Space"）
        if (keyName.contains(",")) {
            String[] keys = keyName.split(",");
            for (String key : keys) {
                pressSingleKey(key.trim());
                try {
                    Thread.sleep(50); // 每个按键间隔50ms
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else if (keyName.length() > 1 && keyName.matches("[A-Z]+")) {
            // 连续字母序列（如 "ABC"）
            for (char c : keyName.toCharArray()) {
                pressSingleKey(String.valueOf(c));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            // 单个按键
            pressSingleKey(keyName);
        }
    }

    private void pressSingleKey(String keyName) {
        if (robot == null) return;

        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) return;

        try {
            robot.keyPress(keyCode);
        } catch (Exception e) {
            System.err.println("[AutoHelper] Failed to press key: " + e.getMessage());
        }
    }

    private void releaseKey(String keyName, MinecraftClient client, boolean execute) {
        releaseKeyWithCommand(keyName, null, client, execute, false);
    }

    private void releaseKeyWithCommand(String keyName, String command, MinecraftClient client, boolean executeKey, boolean executeCmd) {
        if (!executeKey || robot == null || keyName == null || keyName.isEmpty()) return;

        // 分割命令列表（用逗号分隔）
        String[] commands = null;
        if (executeCmd && command != null && !command.trim().isEmpty()) {
            commands = command.split(",");
        }

        // 检查是否是逗号分隔的按键列表（如 "A,B,Space"）
        if (keyName.contains(",")) {
            String[] keys = keyName.split(",");
            for (int i = keys.length - 1; i >= 0; i--) {
                releaseSingleKey(keys[i].trim());

                // 执行对应的命令（如果有）
                if (commands != null && i < commands.length) {
                    executeCommand(commands[i].trim(), client);
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else if (keyName.length() > 1 && keyName.matches("[A-Z]+")) {
            // 连续字母序列（如 "ABC"）
            for (int i = keyName.length() - 1; i >= 0; i--) {
                releaseSingleKey(String.valueOf(keyName.charAt(i)));

                // 执行对应的命令（如果有）
                if (commands != null && i < commands.length) {
                    executeCommand(commands[i].trim(), client);
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            // 单个按键
            releaseSingleKey(keyName);

            // 执行命令（单个命令或命令列表的第一个）
            if (commands != null && commands.length > 0) {
                executeCommand(commands[0].trim(), client);
            }
        }
    }

    private void releaseSingleKey(String keyName) {
        if (robot == null) return;

        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) return;

        try {
            robot.keyRelease(keyCode);
        } catch (Exception e) {
            System.err.println("[AutoHelper] Failed to release key: " + e.getMessage());
        }
    }

    public boolean isValidKey(String keyName) {
        return getKeyCode(keyName) != -1;
    }
}

