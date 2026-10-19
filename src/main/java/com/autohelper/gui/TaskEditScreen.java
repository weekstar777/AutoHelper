package com.autohelper.gui;

import com.autohelper.config.ConfigManager;
import com.autohelper.config.KeyPressTask;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class TaskEditScreen extends Screen {
    private final Screen parent;
    private final ConfigManager configManager;
    private final KeyPressTask existingTask;
    private final int taskIndex;

    private TextFieldWidget keyNameField;
    private TextFieldWidget cycleIntervalField;
    private TextFieldWidget keyIntervalField;
    private TextFieldWidget holdDurationField;
    private TextFieldWidget pressCountField;

    public TaskEditScreen(Screen parent, ConfigManager configManager,
                         KeyPressTask existingTask, int taskIndex) {
        super(Text.literal(existingTask == null ? "Add Task" : "Edit Task"));
        this.parent = parent;
        this.configManager = configManager;
        this.existingTask = existingTask;
        this.taskIndex = taskIndex;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;

        keyNameField = createField(centerX, startY, "Key Name");
        cycleIntervalField = createField(centerX, startY + 35, "Cycle ms");
        keyIntervalField = createField(centerX, startY + 70, "Key ms");
        holdDurationField = createField(centerX, startY + 105, "Hold ms");
        pressCountField = createField(centerX, startY + 140, "Count");

        if (existingTask != null) {
            keyNameField.setText(existingTask.keyName);
            cycleIntervalField.setText(String.valueOf(existingTask.cycleInterval));
            keyIntervalField.setText(String.valueOf(existingTask.keyInterval));
            holdDurationField.setText(String.valueOf(existingTask.holdDuration));
            pressCountField.setText(String.valueOf(existingTask.pressCount));
        }

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Save"),
            button -> saveTask())
            .dimensions(centerX - 105, startY + 180, 100, 20)
            .build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Cancel"),
            button -> this.close())
            .dimensions(centerX + 5, startY + 180, 100, 20)
            .build());
    }

    private TextFieldWidget createField(int centerX, int y, String placeholder) {
        TextFieldWidget field = new TextFieldWidget(
            this.textRenderer, centerX - 100, y, 200, 20, Text.literal(placeholder)
        );
        field.setMaxLength(64);
        this.addSelectableChild(field);
        return field;
    }

    private void saveTask() {
        try {
            String keyName = keyNameField.getText();
            int cycleInterval = Integer.parseInt(cycleIntervalField.getText());
            int keyInterval = Integer.parseInt(keyIntervalField.getText());
            int holdDuration = Integer.parseInt(holdDurationField.getText());
            int pressCount = Integer.parseInt(pressCountField.getText());

            KeyPressTask task = new KeyPressTask(
                keyName, cycleInterval, keyInterval, holdDuration, pressCount
            );

            if (existingTask == null) {
                configManager.addTask(task);
            } else {
                configManager.updateTask(taskIndex, task);
            }

            this.close();
        } catch (NumberFormatException e) {
            // Invalid input, stay on screen
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        keyNameField.render(context, mouseX, mouseY, delta);
        cycleIntervalField.render(context, mouseX, mouseY, delta);
        keyIntervalField.render(context, mouseX, mouseY, delta);
        holdDurationField.render(context, mouseX, mouseY, delta);
        pressCountField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}


