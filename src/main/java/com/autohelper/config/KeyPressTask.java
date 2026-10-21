package com.autohelper.config;

public class KeyPressTask {
    public String nickname;          // User-defined display name
    public String keyName;           // Key name like "Q", "Num8", "S"
    public String command;           // Command to execute (e.g., "/give @s apple 64")
    public boolean executeKeyPress;  // Whether to simulate key press
    public boolean executeCommand;   // Whether to execute command
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
        this.command = "";
        this.executeKeyPress = true;
        this.executeCommand = false;
    }

    public KeyPressTask(String keyName, int cycleInterval, int keyInterval,
                       int holdDuration, int pressCount) {
        this.keyName = keyName;
        this.cycleInterval = cycleInterval;
        this.keyInterval = keyInterval;
        this.holdDuration = holdDuration;
        this.pressCount = pressCount;
        this.enabled = true;
        this.nickname = "";
        this.command = "";
        this.executeKeyPress = true;
        this.executeCommand = false;
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
        if (keyName != null && !keyName.trim().isEmpty()) {
            return keyName;
        }
        if (command != null && !command.trim().isEmpty()) {
            // 截取命令的前20个字符作为显示名
            return command.length() > 20 ? command.substring(0, 20) + "..." : command;
        }
        return "未命名任务";
    }
}


