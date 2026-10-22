package com.autohelper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("AutoHelper");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File configFile;

    private ModConfig config;

    public ConfigManager() {
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        this.configFile = new File(configDir, "autohelper.json");
        this.config = new ModConfig();
    }

    public void load() {
        if (!configFile.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            config = GSON.fromJson(reader, ModConfig.class);
            if (config == null) {
                config = new ModConfig();
            }

            // 为旧数据初始化commandTypes（默认为消息类型）
            boolean needsSave = false;
            for (KeyPressTask task : config.keyPressTasks) {
                if (task.commandTypes == null || task.commandTypes.isEmpty()) {
                    if (task.command != null && !task.command.isEmpty()) {
                        String[] commands = task.command.split(",");
                        StringBuilder types = new StringBuilder();
                        for (int i = 0; i < commands.length; i++) {
                            if (i > 0) types.append(",");
                            // 只有明确以/开头的才是命令，其他都是消息
                            types.append(commands[i].trim().startsWith("/") ? "cmd" : "msg");
                        }
                        task.commandTypes = types.toString();
                        needsSave = true;
                    } else {
                        task.commandTypes = "";
                    }
                }
            }

            if (needsSave) {
                save();
            }

            LOGGER.info("Configuration loaded successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to load config, using defaults", e);
            config = new ModConfig();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
            LOGGER.info("Configuration saved successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public ModConfig getConfig() {
        return config;
    }

    public List<KeyPressTask> getTasks() {
        return config.keyPressTasks;
    }

    public void addTask(KeyPressTask task) {
        config.keyPressTasks.add(task);
        save();
    }

    public void removeTask(int index) {
        if (index >= 0 && index < config.keyPressTasks.size()) {
            config.keyPressTasks.remove(index);
            save();
        }
    }

    public void updateTask(int index, KeyPressTask task) {
        if (index >= 0 && index < config.keyPressTasks.size()) {
            config.keyPressTasks.set(index, task);
            save();
        }
    }
}


