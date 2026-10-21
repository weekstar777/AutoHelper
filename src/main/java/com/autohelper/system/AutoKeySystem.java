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
                    releaseKey(task.keyName, client);
                    task.isHolding = false;
                }
                return;
            }
        }

        if (task.isHolding) {
            if (currentTime - task.pressStartTime >= task.holdDuration) {
                releaseKey(task.keyName, client);
                task.isHolding = false;
                task.currentPressCount++;

                // 执行命令（在按键释放时执行一次）
                if (task.executeCommand && task.command != null && !task.command.trim().isEmpty()) {
                    executeCommand(task.command, client);
                }
            }
        } else {
            if (task.currentPressCount == 0) {
                pressKey(task.keyName, client, task.executeKeyPress);
                task.pressStartTime = currentTime;
                task.isHolding = true;
            } else if (currentTime - task.pressStartTime >= task.holdDuration + task.keyInterval) {
                pressKey(task.keyName, client, task.executeKeyPress);
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
            case "Space": return KeyEvent.VK_SPACE;
            case "Enter": return KeyEvent.VK_ENTER;
            case "Tab": return KeyEvent.VK_TAB;
            case "Shift": return KeyEvent.VK_SHIFT;
            case "Ctrl": return KeyEvent.VK_CONTROL;
            case "Alt": return KeyEvent.VK_ALT;
            case "Escape": return KeyEvent.VK_ESCAPE;
            case "Backspace": return KeyEvent.VK_BACK_SPACE;
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
            case "W": return KeyEvent.VK_W;
            case "A": return KeyEvent.VK_A;
            case "S": return KeyEvent.VK_S;
            case "D": return KeyEvent.VK_D;
            case "Q": return KeyEvent.VK_Q;
            case "E": return KeyEvent.VK_E;
            case "R": return KeyEvent.VK_R;
            case "T": return KeyEvent.VK_T;
            case "Y": return KeyEvent.VK_Y;
            case "U": return KeyEvent.VK_U;
            case "I": return KeyEvent.VK_I;
            case "O": return KeyEvent.VK_O;
            case "P": return KeyEvent.VK_P;
            case "F": return KeyEvent.VK_F;
            case "G": return KeyEvent.VK_G;
            case "H": return KeyEvent.VK_H;
            case "J": return KeyEvent.VK_J;
            case "K": return KeyEvent.VK_K;
            case "L": return KeyEvent.VK_L;
            case "Z": return KeyEvent.VK_Z;
            case "X": return KeyEvent.VK_X;
            case "C": return KeyEvent.VK_C;
            case "V": return KeyEvent.VK_V;
            case "B": return KeyEvent.VK_B;
            case "N": return KeyEvent.VK_N;
            case "M": return KeyEvent.VK_M;
            default: return -1;
        }
    }

    private void pressKey(String keyName, MinecraftClient client, boolean execute) {
        if (!execute || robot == null) return;

        int keyCode = getKeyCode(keyName);
        if (keyCode == -1) return;

        try {
            robot.keyPress(keyCode);
        } catch (Exception e) {
            System.err.println("[AutoHelper] Failed to press key: " + e.getMessage());
        }
    }

    private void releaseKey(String keyName, MinecraftClient client) {
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

