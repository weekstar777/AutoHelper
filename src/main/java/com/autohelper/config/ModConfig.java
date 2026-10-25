package com.autohelper.config;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    public List<KeyPressTask> keyPressTasks = new ArrayList<>();
    public String serverAddress = "";
    public boolean autoJoinServer = false;
    public boolean maximizeWindow = true;
    public int retryInterval = 20000; // 20 seconds in milliseconds

    public ModConfig() {
    }
}
