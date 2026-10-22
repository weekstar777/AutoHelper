package com.autohelper.gui;

import com.autohelper.config.KeyPressTask;
import com.autohelper.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class TaskEditScreen extends Screen {
    private final Screen parent;
    private final ConfigManager configManager;
    private final KeyPressTask task;
    private final int taskIndex;
    private final boolean isNewTask;

    private TextFieldWidget taskNameField;
    private List<TextFieldWidget> keyNameFields = new ArrayList<>();
    private List<TextFieldWidget> commandFields = new ArrayList<>();
    private TextFieldWidget loopIntervalField;
    private TextFieldWidget keyIntervalField;
    private TextFieldWidget holdDurationField;
    private TextFieldWidget pressCountField;

    private List<KeyCommandPair> keyCommandPairs = new ArrayList<>();
    private List<ButtonWidget> addRemoveButtons = new ArrayList<>();

    private double scrollOffset = 0;
    private double maxScrollOffset = 0;
    private static final double SCROLL_SPEED = 20.0;
    private static final int LABEL_COLOR = 0xFFFFFF;
    private static final int FIELD_WIDTH = 180;
    private static final int KEY_FIELD_WIDTH = 50;
    private static final int CMD_FIELD_WIDTH = 135;
    private static final int SMALL_FIELD_WIDTH = 80;
    private static final int BUTTON_WIDTH = 20;
    private static final int ROW_HEIGHT = 25;

    public TaskEditScreen(Screen parent, ConfigManager configManager, KeyPressTask task, int taskIndex) {
        super(Text.of(task == null ? "添加新任务" : "编辑任务"));
        this.parent = parent;
        this.configManager = configManager;
        this.task = (task != null) ? task : new KeyPressTask();
        this.taskIndex = taskIndex;
        this.isNewTask = (task == null);

        if (this.task.keyName != null && !this.task.keyName.isEmpty()) {
            String[] keys = this.task.keyName.split(",");
            String[] commands = this.task.command != null ? this.task.command.split(",") : new String[0];

            for (int i = 0; i < keys.length; i++) {
                KeyCommandPair pair = new KeyCommandPair();
                pair.keyName = keys[i].trim();
                pair.command = (i < commands.length) ? commands[i].trim() : "";
                keyCommandPairs.add(pair);
            }
        }

        if (keyCommandPairs.isEmpty()) {
            keyCommandPairs.add(new KeyCommandPair());
        }
    }

    @Override
    protected void init() {
        keyNameFields.clear();
        commandFields.clear();
        addRemoveButtons.clear();

        int centerX = this.width / 2;
        int startY = 40;
        int currentY = startY;

        taskNameField = new TextFieldWidget(
            this.textRenderer,
            centerX - 90, currentY, FIELD_WIDTH, 20,
            Text.of("任务昵称")
        );
        taskNameField.setMaxLength(50);
        taskNameField.setText(task.nickname != null ? task.nickname : "");
        taskNameField.setPlaceholder(Text.of("请输入任务昵称..."));
        this.addDrawableChild(taskNameField);
        currentY += ROW_HEIGHT + 15;

        currentY += ROW_HEIGHT;

        for (int i = 0; i < keyCommandPairs.size(); i++) {
            final int index = i;
            KeyCommandPair pair = keyCommandPairs.get(i);

            TextFieldWidget keyField = new TextFieldWidget(
                this.textRenderer,
                centerX - 90, currentY, KEY_FIELD_WIDTH, 20,
                Text.of("按键")
            );
            keyField.setMaxLength(20);
            keyField.setText(pair.keyName);
            keyField.setPlaceholder(Text.of("按键（如：R）"));
            this.addDrawableChild(keyField);
            keyNameFields.add(keyField);

            TextFieldWidget cmdField = new TextFieldWidget(
                this.textRenderer,
                centerX - 35, currentY, CMD_FIELD_WIDTH, 20,
                Text.of("命令")
            );
            cmdField.setMaxLength(100);
            cmdField.setText(pair.command);
            cmdField.setPlaceholder(Text.of("命令（如：/say hello）"));
            this.addDrawableChild(cmdField);
            commandFields.add(cmdField);

            if (i == 0) {
                ButtonWidget addBtn = ButtonWidget.builder(
                    Text.literal("+"),
                    button -> {
                        saveCurrentInputData();
                        keyCommandPairs.add(new KeyCommandPair());
                        rebuildUI();
                    })
                    .dimensions(centerX + 105, currentY, BUTTON_WIDTH, 20)
                    .build();
                this.addDrawableChild(addBtn);
                addRemoveButtons.add(addBtn);
            } else {
                ButtonWidget removeBtn = ButtonWidget.builder(
                    Text.literal("-"),
                    button -> {
                        saveCurrentInputData();
                        if (keyCommandPairs.size() > 1) {
                            keyCommandPairs.remove(index);
                            rebuildUI();
                        }
                    })
                    .dimensions(centerX + 105, currentY, BUTTON_WIDTH, 20)
                    .build();
                this.addDrawableChild(removeBtn);
                addRemoveButtons.add(removeBtn);
            }

            currentY += ROW_HEIGHT;
        }

        currentY += 10;

        loopIntervalField = new TextFieldWidget(
            this.textRenderer,
            centerX - 10, currentY, SMALL_FIELD_WIDTH, 20,
            Text.of("循环间隔")
        );
        loopIntervalField.setMaxLength(10);
        loopIntervalField.setText(String.valueOf(task.cycleInterval));
        this.addDrawableChild(loopIntervalField);
        currentY += ROW_HEIGHT;

        keyIntervalField = new TextFieldWidget(
            this.textRenderer,
            centerX - 10, currentY, SMALL_FIELD_WIDTH, 20,
            Text.of("按键间隔")
        );
        keyIntervalField.setMaxLength(10);
        keyIntervalField.setText(String.valueOf(task.keyInterval));
        this.addDrawableChild(keyIntervalField);
        currentY += ROW_HEIGHT;

        holdDurationField = new TextFieldWidget(
            this.textRenderer,
            centerX - 10, currentY, SMALL_FIELD_WIDTH, 20,
            Text.of("按住时长")
        );
        holdDurationField.setMaxLength(10);
        holdDurationField.setText(String.valueOf(task.holdDuration));
        this.addDrawableChild(holdDurationField);
        currentY += ROW_HEIGHT;

        pressCountField = new TextFieldWidget(
            this.textRenderer,
            centerX - 10, currentY, SMALL_FIELD_WIDTH, 20,
            Text.of("按键次数")
        );
        pressCountField.setMaxLength(10);
        pressCountField.setText(String.valueOf(task.pressCount));
        this.addDrawableChild(pressCountField);
        currentY += ROW_HEIGHT + 20;

        int buttonY = Math.max(currentY, this.height - 35);
        int buttonWidth = (int)(this.width * 0.2);
        int spacing = 20;
        int totalWidth = buttonWidth * 2 + spacing;
        int startX = (this.width - totalWidth) / 2;

        this.addDrawableChild(ButtonWidget.builder(
            Text.of("保存"),
            button -> {
                if (saveTask()) {
                    this.client.setScreen(parent);
                }
            })
            .dimensions(startX, buttonY, buttonWidth, 20)
            .build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.of("取消"),
            button -> this.client.setScreen(parent))
            .dimensions(startX + buttonWidth + spacing, buttonY, buttonWidth, 20)
            .build());

        maxScrollOffset = Math.max(0, currentY + 40 - this.height);
    }

    private void saveCurrentInputData() {
        for (int i = 0; i < keyCommandPairs.size(); i++) {
            if (i < keyNameFields.size()) {
                keyCommandPairs.get(i).keyName = keyNameFields.get(i).getText();
            }
            if (i < commandFields.size()) {
                keyCommandPairs.get(i).command = commandFields.get(i).getText();
            }
        }
    }

    private void rebuildUI() {
        this.clearChildren();
        this.init();
    }

    private boolean saveTask() {
        saveCurrentInputData();

        String nickname = taskNameField.getText().trim();
        if (nickname.isEmpty()) {
            return false;
        }

        List<String> validKeys = new ArrayList<>();
        List<String> validCommands = new ArrayList<>();

        for (KeyCommandPair pair : keyCommandPairs) {
            if (pair.isValid()) {
                validKeys.add(pair.keyName);
                validCommands.add(pair.command);
            }
        }

        if (validKeys.isEmpty()) {
            return false;
        }

        String keyName = String.join(",", validKeys);
        String command = String.join(",", validCommands);

        try {
            int loopInterval = Integer.parseInt(loopIntervalField.getText());
            int keyInterval = Integer.parseInt(keyIntervalField.getText());
            int holdDuration = Integer.parseInt(holdDurationField.getText());
            int pressCount = Integer.parseInt(pressCountField.getText());

            task.nickname = nickname;
            task.keyName = keyName;
            task.command = command;
            task.cycleInterval = loopInterval;
            task.keyInterval = keyInterval;
            task.holdDuration = holdDuration;
            task.pressCount = pressCount;

            if (isNewTask) {
                configManager.addTask(task);
            } else {
                configManager.save();
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        context.getMatrices().push();
        context.getMatrices().translate(0, -scrollOffset, 0);

        super.render(context, mouseX, (int)(mouseY + scrollOffset), delta);

        int centerX = this.width / 2;
        int startY = 40;
        int currentY = startY;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
            centerX, 15, LABEL_COLOR);

        context.drawTextWithShadow(this.textRenderer, "任务昵称:",
            centerX - 90, currentY - 12, LABEL_COLOR);
        currentY += ROW_HEIGHT + 15;

        context.drawTextWithShadow(this.textRenderer, "按键-命令映射:",
            centerX - 90, currentY, LABEL_COLOR);
        currentY += ROW_HEIGHT;

        currentY += (keyNameFields.size() * ROW_HEIGHT) + 10;

        context.drawTextWithShadow(this.textRenderer, "循环间隔(ms):",
            centerX - 90, currentY + 5, LABEL_COLOR);
        currentY += ROW_HEIGHT;

        context.drawTextWithShadow(this.textRenderer, "按键间隔(ms):",
            centerX - 90, currentY + 5, LABEL_COLOR);
        currentY += ROW_HEIGHT;

        context.drawTextWithShadow(this.textRenderer, "按住时长(ms):",
            centerX - 90, currentY + 5, LABEL_COLOR);
        currentY += ROW_HEIGHT;

        context.drawTextWithShadow(this.textRenderer, "按键次数:",
            centerX - 90, currentY + 5, LABEL_COLOR);

        context.getMatrices().pop();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double newOffset = scrollOffset - verticalAmount * SCROLL_SPEED;
        scrollOffset = Math.max(0, Math.min(newOffset, maxScrollOffset));
        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
