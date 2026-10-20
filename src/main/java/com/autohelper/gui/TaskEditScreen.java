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

    private TextFieldWidget nicknameField;
    private TextFieldWidget keyNameField;
    private TextFieldWidget cycleIntervalField;
    private TextFieldWidget keyIntervalField;
    private TextFieldWidget holdDurationField;
    private TextFieldWidget pressCountField;

    private String errorMessage = null;

    public TaskEditScreen(Screen parent, ConfigManager configManager,
                         KeyPressTask existingTask, int taskIndex) {
        super(Text.translatable(existingTask == null ?
            "gui.autohelper.task.add_title" : "gui.autohelper.task.edit_title"));
        this.parent = parent;
        this.configManager = configManager;
        this.existingTask = existingTask;
        this.taskIndex = taskIndex;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 40;
        int fieldWidth = 200;
        int fieldHeight = 20;
        int spacing = 30;

        // Nickname field (optional)
        nicknameField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY, fieldWidth, fieldHeight,
            Text.literal("Nickname")
        );
        nicknameField.setMaxLength(32);
        nicknameField.setPlaceholder(Text.literal("任务昵称（可选）"));
        this.addSelectableChild(nicknameField);

        // Key name field
        keyNameField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing, fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.key_name")
        );
        keyNameField.setMaxLength(64);
        keyNameField.setPlaceholder(Text.translatable("gui.autohelper.task.key_name_hint"));
        this.addSelectableChild(keyNameField);

        // Cycle interval field
        cycleIntervalField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 2,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.cycle_interval")
        );
        cycleIntervalField.setMaxLength(10);
        this.addSelectableChild(cycleIntervalField);

        // Key interval field
        keyIntervalField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 3,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.key_interval")
        );
        keyIntervalField.setMaxLength(10);
        this.addSelectableChild(keyIntervalField);

        // Hold duration field
        holdDurationField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 4,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.hold_duration")
        );
        holdDurationField.setMaxLength(10);
        this.addSelectableChild(holdDurationField);

        // Press count field
        pressCountField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 5,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.press_count")
        );
        pressCountField.setMaxLength(5);
        this.addSelectableChild(pressCountField);

        // Fill existing values
        if (existingTask != null) {
            if (existingTask.nickname != null) {
                nicknameField.setText(existingTask.nickname);
            }
            keyNameField.setText(existingTask.keyName);
            cycleIntervalField.setText(String.valueOf(existingTask.cycleInterval));
            keyIntervalField.setText(String.valueOf(existingTask.keyInterval));
            holdDurationField.setText(String.valueOf(existingTask.holdDuration));
            pressCountField.setText(String.valueOf(existingTask.pressCount));
        } else {
            // Default values
            cycleIntervalField.setText("5000");
            keyIntervalField.setText("0");
            holdDurationField.setText("100");
            pressCountField.setText("1");
        }

        // Save button
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.save"),
            button -> saveTask())
            .dimensions(centerX - 105, startY + spacing * 6, 100, 20)
            .build());

        // Cancel button
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.cancel"),
            button -> this.close())
            .dimensions(centerX + 5, startY + spacing * 6, 100, 20)
            .build());
    }

    private void saveTask() {
        errorMessage = null;

        try {
            String nickname = nicknameField.getText().trim();
            String keyName = keyNameField.getText().trim();
            if (keyName.isEmpty()) {
                errorMessage = Text.translatable("gui.autohelper.task.error_empty_key").getString();
                return;
            }

            int cycleInterval = Integer.parseInt(cycleIntervalField.getText());
            int keyInterval = Integer.parseInt(keyIntervalField.getText());
            int holdDuration = Integer.parseInt(holdDurationField.getText());
            int pressCount = Integer.parseInt(pressCountField.getText());

            // Validation
            if (cycleInterval < 0 || keyInterval < 0 || holdDuration < 0 || pressCount < 1) {
                errorMessage = Text.translatable("gui.autohelper.task.error_invalid_number").getString();
                return;
            }

            KeyPressTask task = new KeyPressTask(
                keyName, cycleInterval, keyInterval, holdDuration, pressCount
            );
            task.nickname = nickname;  // Save nickname

            if (existingTask == null) {
                configManager.addTask(task);
            } else {
                configManager.updateTask(taskIndex, task);
            }

            this.close();
        } catch (NumberFormatException e) {
            errorMessage = Text.translatable("gui.autohelper.task.error_invalid_number").getString();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        this.renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int startY = 40;
        int spacing = 30;
        int labelOffset = -12;

        // Draw labels
        context.drawTextWithShadow(this.textRenderer,
            Text.literal("任务昵称 (可选)"),
            centerX - 100, startY + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.key_name"),
            centerX - 100, startY + spacing + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.cycle_interval"),
            centerX - 100, startY + spacing * 2 + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.key_interval"),
            centerX - 100, startY + spacing * 3 + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.hold_duration"),
            centerX - 100, startY + spacing * 4 + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.press_count"),
            centerX - 100, startY + spacing * 5 + labelOffset, 0xFFFFFF);

        // Render fields
        nicknameField.render(context, mouseX, mouseY, delta);
        keyNameField.render(context, mouseX, mouseY, delta);
        cycleIntervalField.render(context, mouseX, mouseY, delta);
        keyIntervalField.render(context, mouseX, mouseY, delta);
        holdDurationField.render(context, mouseX, mouseY, delta);
        pressCountField.render(context, mouseX, mouseY, delta);

        // Draw error message if any
        if (errorMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, errorMessage,
                centerX, startY + spacing * 6 + 25, 0xFF5555);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}


