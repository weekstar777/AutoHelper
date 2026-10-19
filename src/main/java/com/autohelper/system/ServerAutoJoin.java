package com.autohelper.system;

import com.autohelper.config.ConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class ServerAutoJoin {
    private final ConfigManager configManager;
    private long lastAttemptTime = 0;
    private boolean hasAttempted = false;

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

        // Check if on title screen or disconnected
        if (client.world == null && client.currentScreen instanceof TitleScreen) {
            if (!hasAttempted || (currentTime - lastAttemptTime >= retryInterval)) {
                attemptConnection(client, serverAddress);
                lastAttemptTime = currentTime;
                hasAttempted = true;
            }
        } else if (client.world != null) {
            // Reset when successfully connected
            hasAttempted = false;
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
        hasAttempted = false;
        lastAttemptTime = 0;
    }
}

