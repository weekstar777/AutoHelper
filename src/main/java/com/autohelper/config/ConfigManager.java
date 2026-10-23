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

            // 迁移旧配置格式（使用|||分隔符的）到新格式（数组）
            for (KeyPressTask task : config.keyPressTasks) {
                if ((task.keyNames == null || task.keyNames.isEmpty()) && task.keyName != null) {
                    task.keyNames = new ArrayList<>();
                    for (String key : task.keyName.split("\\|\\|\\|")) {
                        task.keyNames.add(key.trim());
                    }
                }
                if ((task.commands == null || task.commands.isEmpty()) && task.command != null) {
                    task.commands = new ArrayList<>();
                    for (String cmd : task.command.split("\\|\\|\\|")) {
                        task.commands.add(cmd.trim());
                    }
                }
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


