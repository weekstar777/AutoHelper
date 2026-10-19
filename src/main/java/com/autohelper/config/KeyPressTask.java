package com.autohelper.config;

public class KeyPressTask {
    public String keyName;           // Key name like "Q", "Num8", "S"
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
    }

    public KeyPressTask(String keyName, int cycleInterval, int keyInterval,
                       int holdDuration, int pressCount) {
        this.keyName = keyName;
        this.cycleInterval = cycleInterval;
        this.keyInterval = keyInterval;
        this.holdDuration = holdDuration;
        this.pressCount = pressCount;
        this.enabled = true;
    }

    public void reset() {
        lastCycleTime = 0;
        currentPressCount = 0;
        pressStartTime = 0;
        isHolding = false;
    }
}

