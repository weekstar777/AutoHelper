package com.autohelper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
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

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            config = GSON.fromJson(reader, ModConfig.class);
            if (config == null) {
                config = new ModConfig();
            }

            LOGGER.info("Configuration loaded successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to load config, using defaults", e);
            config = new ModConfig();
        }
    }

    public void save() {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
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


