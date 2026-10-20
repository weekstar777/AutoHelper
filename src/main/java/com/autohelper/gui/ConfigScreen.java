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
    private List<ButtonWidget> taskButtons;

    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 30;
    private static final int TASKS_START_Y = 120;

    public ConfigScreen(Screen parent, ConfigManager configManager) {
        super(Text.translatable("gui.autohelper.config.title"));
        this.parent = parent;
        this.configManager = configManager;
        this.taskEntries = new ArrayList<>();
        this.taskButtons = new ArrayList<>();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Back button
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.autohelper.config.back"),
            button -> this.close())
            .dimensions(centerX - 100, this.height - 30, 200, 20)
            .build());

        // Server address field
        serverAddressField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, 35, 200, 20,
            Text.translatable("gui.autohelper.config.server_address")
        );
        serverAddressField.setMaxLength(128);
        serverAddressField.setText(configManager.getConfig().serverAddress);
        this.addSelectableChild(serverAddressField);

        // Auto join toggle
        updateAutoJoinButton(centerX);

        // Add task button
        addTaskButton = ButtonWidget.builder(
            Text.translatable("gui.autohelper.config.add_task"),
            button -> this.client.setScreen(
                new TaskEditScreen(this, configManager, null, -1)))
            .dimensions(centerX - 100, 90, 200, 20)
            .build();
        this.addDrawableChild(addTaskButton);

        refreshTaskList();
    }

    private void updateAutoJoinButton(int centerX) {
        if (autoJoinToggle != null) {
            this.remove(autoJoinToggle);
        }

        boolean enabled = configManager.getConfig().autoJoinServer;
        String statusKey = enabled ? "gui.autohelper.config.on" : "gui.autohelper.config.off";

        autoJoinToggle = ButtonWidget.builder(
            Text.translatable("gui.autohelper.config.auto_join",
                Text.translatable(statusKey).getString()),
            button -> {
                configManager.getConfig().autoJoinServer =
                    !configManager.getConfig().autoJoinServer;
                configManager.save();
                updateAutoJoinButton(centerX);
            })
            .dimensions(centerX - 100, 60, 200, 20)
            .build();
        this.addDrawableChild(autoJoinToggle);
    }

    private void refreshTaskList() {
        // Clear old buttons
        for (ButtonWidget button : taskButtons) {
            this.remove(button);
        }
        taskButtons.clear();
        taskEntries.clear();

        List<KeyPressTask> tasks = configManager.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            taskEntries.add(new TaskEntry(tasks.get(i), i));
            createTaskButtons(i, tasks.get(i));
        }
    }

    private void createTaskButtons(int index, KeyPressTask task) {
        int y = TASKS_START_Y + (index * ENTRY_HEIGHT);
        int centerX = this.width / 2;

        if (y + ENTRY_HEIGHT > this.height - 40) {
            return; // Out of screen
        }

        // Toggle button
        ButtonWidget toggleBtn = ButtonWidget.builder(
            Text.translatable(task.enabled ?
                "gui.autohelper.task.enabled" : "gui.autohelper.task.disabled"),
            button -> {
                task.enabled = !task.enabled;
                configManager.save();
                refreshTaskList();
            })
            .dimensions(centerX - 150, y, 50, 20)
            .build();
        this.addDrawableChild(toggleBtn);
        taskButtons.add(toggleBtn);

        // Edit button
        ButtonWidget editBtn = ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.edit"),
            button -> this.client.setScreen(
                new TaskEditScreen(this, configManager, task, index)))
            .dimensions(centerX + 55, y, 45, 20)
            .build();
        this.addDrawableChild(editBtn);
        taskButtons.add(editBtn);

        // Delete button
        ButtonWidget deleteBtn = ButtonWidget.builder(
            Text.literal("×"),
            button -> {
                configManager.removeTask(index);
                refreshTaskList();
            })
            .dimensions(centerX + 105, y, 20, 20)
            .build();
        this.addDrawableChild(deleteBtn);
        taskButtons.add(deleteBtn);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        this.renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        // Server address label
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.config.server_address"),
            this.width / 2, 24, 0xFFFFFF);

        serverAddressField.render(context, mouseX, mouseY, delta);

        // Tasks title
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.config.tasks_title"),
            this.width / 2, TASKS_START_Y - 10, 0xFFFFFF);

        // Render task entries
        int y = TASKS_START_Y;
        for (TaskEntry entry : taskEntries) {
            if (y + ENTRY_HEIGHT > this.height - 40) break;
            renderTaskEntry(context, entry, y);
            y += ENTRY_HEIGHT;
        }

        // No tasks message
        if (taskEntries.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.autohelper.config.no_tasks"),
                this.width / 2, TASKS_START_Y + 10, 0x888888);
        }
    }

    private void renderTaskEntry(DrawContext context, TaskEntry entry, int y) {
        KeyPressTask task = entry.task;
        int centerX = this.width / 2;

        String info = Text.translatable("gui.autohelper.task.info",
            task.keyName, task.cycleInterval, task.keyInterval,
            task.holdDuration, task.pressCount).getString();

        int color = task.enabled ? 0x00FF00 : 0xFF0000;
        context.drawTextWithShadow(this.textRenderer, info,
            centerX - 95, y + 6, color);
    }

    @Override
    public void close() {
        configManager.getConfig().serverAddress = serverAddressField.getText();
        configManager.save();
        this.client.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset = (int) Math.max(0, scrollOffset - verticalAmount * 10);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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




