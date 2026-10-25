package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ServerAutoJoin {
    private final ConfigManager configManager;
    private long lastAttemptTime;
    private boolean wasInServer = false;

    public ServerAutoJoin(ConfigManager configManager) {
        this.configManager = configManager;
        // 初始化为当前时间，确保第一次连接前等待完整的retryInterval
        this.lastAttemptTime = System.currentTimeMillis();
    }

    public void tick(MinecraftClient client) {
        if (!configManager.getConfig().autoJoinServer) {
            return;
        }

        String serverAddress = configManager.getConfig().serverAddress;
        if (serverAddress == null || serverAddress.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        int retryInterval = configManager.getConfig().retryInterval;

        // 检查是否在服务器内
        boolean isInServer = client.world != null && client.player != null;

        if (isInServer) {
            // 在服务器内，标记状态并返回
            wasInServer = true;
            return;
        }

        // 不在服务器内
        if (wasInServer) {
            // 刚从服务器退出，重置计时器，需要等待后才能重连
            lastAttemptTime = currentTime;
            wasInServer = false;
        }

        // 检查是否到达连接时间
        if (currentTime - lastAttemptTime >= retryInterval) {
            attemptConnection(client, serverAddress);
            lastAttemptTime = currentTime;
        }
    }

    private void attemptConnection(MinecraftClient client, String address) {
        try {
            ServerInfo serverInfo = new ServerInfo("AutoHelper", address, ServerInfo.ServerType.OTHER);
            ConnectScreen.connect(client.currentScreen, client,
                ServerAddress.parse(address), serverInfo, false, null);
        } catch (Exception e) {
            // Will retry after interval
        }
    }

    public void reset() {
        lastAttemptTime = 0;
    }
}
