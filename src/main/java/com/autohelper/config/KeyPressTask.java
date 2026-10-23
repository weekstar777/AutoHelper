package com.autohelper.config;

import java.util.ArrayList;
import java.util.List;

public class KeyPressTask {
    public String nickname;          // User-defined display name
    public List<String> keyNames;    // List of key names
    public List<String> commands;    // List of commands
    public int cycleInterval;        // Milliseconds between cycles
    public int keyInterval;          // Milliseconds between key presses
    public int holdDuration;         // Milliseconds to hold key
    public int pressCount;           // Number of times to press
    public boolean enabled;          // Whether this task is active

    // Runtime state (not saved)
    public transient long lastCycleTime = 0;
    public transient int currentPressCount = 0;
    public transient long pressStartTime = 0;
    public transient boolean isHolding = false;

    public KeyPressTask() {
        this.enabled = true;
        this.nickname = "";
        this.keyNames = new ArrayList<>();
        this.commands = new ArrayList<>();
    }

    public void reset() {
        lastCycleTime = 0;
        currentPressCount = 0;
        pressStartTime = 0;
        isHolding = false;
    }

    public String getDisplayName() {
        if (nickname != null && !nickname.trim().isEmpty()) {
            return nickname;
        }
        if (keyNames != null && !keyNames.isEmpty() && keyNames.get(0) != null && !keyNames.get(0).trim().isEmpty()) {
            return keyNames.get(0);
        }
        if (commands != null && !commands.isEmpty() && commands.get(0) != null && !commands.get(0).trim().isEmpty()) {
            String cmd = commands.get(0);
            return cmd.length() > 20 ? cmd.substring(0, 20) + "..." : cmd;
        }
        return "未命名任务";
    }

    // 辅助方法：判断是否应该执行按键
    public boolean shouldExecuteKeyPress() {
        return keyNames != null && !keyNames.isEmpty() &&
               keyNames.stream().anyMatch(k -> k != null && !k.trim().isEmpty());
    }

    // 辅助方法：判断是否应该执行命令
    public boolean shouldExecuteCommand() {
        return commands != null && !commands.isEmpty() &&
               commands.stream().anyMatch(c -> c != null && !c.trim().isEmpty());
    }
}


