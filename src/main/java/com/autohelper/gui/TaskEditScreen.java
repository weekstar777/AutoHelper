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
    private TextFieldWidget commandField;  // 新增命令输入框
    private TextFieldWidget cycleIntervalField;
    private TextFieldWidget keyIntervalField;
    private TextFieldWidget holdDurationField;
    private TextFieldWidget pressCountField;

    private ButtonWidget executeKeyPressToggle;  // 执行按键开关
    private ButtonWidget executeCommandToggle;   // 执行命令开关
    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;

    private String errorMessage = null;
    private int scrollOffset = 0;
    private static final int CONTENT_HEIGHT = 380;  // 增加内容高度以容纳新字段

    private boolean tempExecuteKeyPress = true;  // 临时存储执行按键状态
    private boolean tempExecuteCommand = false;  // 临时存储执行命令状态

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
        int spacing = 38;  // 增加间距

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

        // Command field (新增)
        commandField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 2,
            fieldWidth, fieldHeight,
            Text.literal("Command")
        );
        commandField.setMaxLength(256);
        commandField.setPlaceholder(Text.literal("命令（如 /give @s apple 64）"));
        this.addSelectableChild(commandField);

        // Execute KeyPress Toggle (新增)
        updateExecuteKeyPressButton(centerX, startY + spacing * 3);

        // Execute Command Toggle (新增)
        updateExecuteCommandButton(centerX, startY + spacing * 4);

        // Cycle interval field
        cycleIntervalField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 5,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.cycle_interval")
        );
        cycleIntervalField.setMaxLength(10);
        this.addSelectableChild(cycleIntervalField);

        // Key interval field
        keyIntervalField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 6,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.key_interval")
        );
        keyIntervalField.setMaxLength(10);
        this.addSelectableChild(keyIntervalField);

        // Hold duration field
        holdDurationField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 7,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.hold_duration")
        );
        holdDurationField.setMaxLength(10);
        this.addSelectableChild(holdDurationField);

        // Press count field
        pressCountField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY + spacing * 8,
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
            if (existingTask.keyName != null) {
                keyNameField.setText(existingTask.keyName);
            }
            if (existingTask.command != null) {
                commandField.setText(existingTask.command);
            }
            tempExecuteKeyPress = existingTask.executeKeyPress;
            tempExecuteCommand = existingTask.executeCommand;
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
            tempExecuteKeyPress = true;
            tempExecuteCommand = false;
        }

        // Save and Cancel buttons - will scroll with content
        saveButton = ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.save"),
            button -> saveTask())
            .dimensions(centerX - 105, startY + spacing * 9, 100, 20)
            .build();
        this.addDrawableChild(saveButton);

        cancelButton = ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.cancel"),
            button -> this.close())
            .dimensions(centerX + 5, startY + spacing * 9, 100, 20)
            .build();
        this.addDrawableChild(cancelButton);
    }

    private void updateExecuteKeyPressButton(int centerX, int y) {
        if (executeKeyPressToggle != null) {
            this.remove(executeKeyPressToggle);
        }

        String statusText = tempExecuteKeyPress ? "§a开启" : "§c关闭";
        executeKeyPressToggle = ButtonWidget.builder(
            Text.literal("执行按键: " + statusText),
            button -> {
                tempExecuteKeyPress = !tempExecuteKeyPress;
                updateExecuteKeyPressButton(centerX, y);
            })
            .dimensions(centerX - 100, y, 200, 20)
            .build();
        this.addDrawableChild(executeKeyPressToggle);
    }

    private void updateExecuteCommandButton(int centerX, int y) {
        if (executeCommandToggle != null) {
            this.remove(executeCommandToggle);
        }

        String statusText = tempExecuteCommand ? "§a开启" : "§c关闭";
        executeCommandToggle = ButtonWidget.builder(
            Text.literal("执行命令: " + statusText),
            button -> {
                tempExecuteCommand = !tempExecuteCommand;
                updateExecuteCommandButton(centerX, y);
            })
            .dimensions(centerX - 100, y, 200, 20)
            .build();
        this.addDrawableChild(executeCommandToggle);
    }

    private void saveTask() {
        errorMessage = null;

        try {
            String nickname = nicknameField.getText().trim();
            String keyName = keyNameField.getText().trim();
            String command = commandField.getText().trim();

            // 验证：至少要有按键名或命令
            if (keyName.isEmpty() && command.isEmpty()) {
                errorMessage = "按键名和命令不能同时为空";
                return;
            }

            int cycleInterval = Integer.parseInt(cycleIntervalField.getText());
            int keyInterval = Integer.parseInt(keyIntervalField.getText());
            int holdDuration = Integer.parseInt(holdDurationField.getText());
            int pressCount = Integer.parseInt(pressCountField.getText());

            // Validation (pressCount can be 0 for infinite loop)
            if (cycleInterval < 0 || keyInterval < 0 || holdDuration < 0 || pressCount < 0) {
                errorMessage = Text.translatable("gui.autohelper.task.error_invalid_number").getString();
                return;
            }

            KeyPressTask task = new KeyPressTask(
                keyName, cycleInterval, keyInterval, holdDuration, pressCount
            );
            task.nickname = nickname;
            task.command = command;
            task.executeKeyPress = tempExecuteKeyPress;
            task.executeCommand = tempExecuteCommand;

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
        int startY = 40 - scrollOffset;  // 应用滚动偏移
        int spacing = 38;
        int labelOffset = -14;

        // Draw labels (with scroll offset)
        context.drawTextWithShadow(this.textRenderer,
            Text.literal("任务昵称 (可选)"),
            centerX - 100, startY + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.key_name"),
            centerX - 100, startY + spacing + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.literal("命令 (可选)"),
            centerX - 100, startY + spacing * 2 + labelOffset, 0xFFFFFF);

        // spacing * 3 和 * 4 是按钮，不需要标签

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.cycle_interval"),
            centerX - 100, startY + spacing * 5 + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.key_interval"),
            centerX - 100, startY + spacing * 6 + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.hold_duration"),
            centerX - 100, startY + spacing * 7 + labelOffset, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.press_count"),
            centerX - 100, startY + spacing * 8 + labelOffset, 0xFFFFFF);

        // Render fields
        nicknameField.render(context, mouseX, mouseY, delta);
        keyNameField.render(context, mouseX, mouseY, delta);
        commandField.render(context, mouseX, mouseY, delta);
        cycleIntervalField.render(context, mouseX, mouseY, delta);
        keyIntervalField.render(context, mouseX, mouseY, delta);
        holdDurationField.render(context, mouseX, mouseY, delta);
        pressCountField.render(context, mouseX, mouseY, delta);

        // Draw error message if any (with scroll offset)
        if (errorMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, errorMessage,
                centerX, startY + spacing * 9 + 25, 0xFF5555);
        }

        // Draw scroll indicator if content is scrollable
        int maxScroll = Math.max(0, CONTENT_HEIGHT - this.height + 60);
        if (maxScroll > 0) {
            // Draw scrollbar background
            int scrollbarX = this.width - 8;
            int scrollbarY = 20;
            int scrollbarHeight = this.height - 40;
            context.fill(scrollbarX, scrollbarY, scrollbarX + 6, scrollbarY + scrollbarHeight, 0x80000000);

            // Draw scrollbar thumb
            float scrollProgress = (float) scrollOffset / maxScroll;
            int thumbHeight = Math.max(20, scrollbarHeight * (this.height - 60) / CONTENT_HEIGHT);
            int thumbY = scrollbarY + (int)((scrollbarHeight - thumbHeight) * scrollProgress);
            context.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFFAAAAAA);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, CONTENT_HEIGHT - this.height + 60);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * 10));

        // Update field positions
        updateFieldPositions();

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void updateFieldPositions() {
        int centerX = this.width / 2;
        int startY = 40 - scrollOffset;
        int fieldWidth = 200;
        int fieldHeight = 20;
        int spacing = 38;

        // Update all field Y positions
        if (nicknameField != null) nicknameField.setY(startY);
        if (keyNameField != null) keyNameField.setY(startY + spacing);
        if (cycleIntervalField != null) cycleIntervalField.setY(startY + spacing * 2);
        if (keyIntervalField != null) keyIntervalField.setY(startY + spacing * 3);
        if (holdDurationField != null) holdDurationField.setY(startY + spacing * 4);
        if (pressCountField != null) pressCountField.setY(startY + spacing * 5);

        // Update button positions
        if (saveButton != null) saveButton.setY(startY + spacing * 6);
        if (cancelButton != null) cancelButton.setY(startY + spacing * 6);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}


