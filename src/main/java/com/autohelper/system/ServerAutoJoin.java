package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ServerAutoJoin {
    private final ConfigManager configManager;
    private long lastAttemptTime = 0;

    public ServerAutoJoin(ConfigManager configManager) {
        this.configManager = configManager;
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

        // 检测是否在服务器内：只有client.world != null才表示在游戏世界中
        if (client.world == null) {
            // 不在服务器内，无论什么界面都尝试连接
            if (currentTime - lastAttemptTime >= retryInterval) {
                attemptConnection(client, serverAddress);
                lastAttemptTime = currentTime;
            }
        } else {
            // 在服务器内，重置计时器以便下次退出时立即触发
            lastAttemptTime = currentTime - retryInterval;
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
