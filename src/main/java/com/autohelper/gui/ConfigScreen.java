package com.autohelper.gui;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ConfigManager configManager;

    private TextFieldWidget serverAddressField;
    private ButtonWidget autoJoinToggle;
    private ButtonWidget addTaskButton;
    private List<TaskEntry> taskEntries;

    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 60;

    public ConfigScreen(Screen parent, ConfigManager configManager) {
        super(Text.literal("AutoHelper Configuration"));
        this.parent = parent;
        this.configManager = configManager;
        this.taskEntries = new ArrayList<>();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Back"),
            button -> this.close())
            .dimensions(centerX - 100, this.height - 30, 200, 20)
            .build());

        serverAddressField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, 30, 200, 20,
            Text.literal("Server Address")
        );
        serverAddressField.setMaxLength(128);
        serverAddressField.setText(configManager.getConfig().serverAddress);
        this.addSelectableChild(serverAddressField);

        autoJoinToggle = ButtonWidget.builder(
            Text.literal("Auto Join: " +
                (configManager.getConfig().autoJoinServer ? "ON" : "OFF")),
            button -> {
                configManager.getConfig().autoJoinServer =
                    !configManager.getConfig().autoJoinServer;
                button.setMessage(Text.literal("Auto Join: " +
                    (configManager.getConfig().autoJoinServer ? "ON" : "OFF")));
                configManager.save();
            })
            .dimensions(centerX - 100, 55, 200, 20)
            .build();
        this.addDrawableChild(autoJoinToggle);

        addTaskButton = ButtonWidget.builder(
            Text.literal("Add New Task"),
            button -> this.client.setScreen(
                new TaskEditScreen(this, configManager, null, -1)))
            .dimensions(centerX - 100, 85, 200, 20)
            .build();
        this.addDrawableChild(addTaskButton);

        refreshTaskList();
    }

    private void refreshTaskList() {
        taskEntries.clear();
        List<KeyPressTask> tasks = configManager.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            taskEntries.add(new TaskEntry(tasks.get(i), i));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
            "Server Address:", this.width / 2, 18, 0xFFFFFF);

        serverAddressField.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
            "Auto Key Tasks:", this.width / 2, 115, 0xFFFFFF);

        int y = 130;
        for (TaskEntry entry : taskEntries) {
            if (y > this.height - 50) break;
            renderTaskEntry(context, entry, y, mouseX, mouseY);
            y += ENTRY_HEIGHT;
        }
    }

    private void renderTaskEntry(DrawContext context, TaskEntry entry,
                                  int y, int mouseX, int mouseY) {
        KeyPressTask task = entry.task;
        int centerX = this.width / 2;

        String info = String.format("%s | Cycle:%dms Hold:%dms Count:%d",
            task.keyName, task.cycleInterval, task.holdDuration, task.pressCount);

        context.drawTextWithShadow(this.textRenderer, info,
            centerX - 150, y + 5, task.enabled ? 0x00FF00 : 0xFF0000);
    }

    @Override
    public void close() {
        configManager.getConfig().serverAddress = serverAddressField.getText();
        configManager.save();
        this.client.setScreen(parent);
    }

    private static class TaskEntry {
        final KeyPressTask task;
        final int index;

        TaskEntry(KeyPressTask task, int index) {
            this.task = task;
            this.index = index;
        }
    }
}

