package com.autohelper.gui;

import com.autohelper.config.KeyPressTask;
import com.autohelper.config.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

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
    private static final int KEY_FIELD_WIDTH = 25;
    private static final int CMD_FIELD_WIDTH = 150;
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
        int layoutY = startY;

        taskNameField = new TextFieldWidget(
            this.textRenderer,
            centerX - 90, layoutY - (int) scrollOffset, FIELD_WIDTH, 20,
            Text.of("任务昵称")
        );
        taskNameField.setMaxLength(50);
        taskNameField.setText(task.nickname != null ? task.nickname : "");
        taskNameField.setPlaceholder(Text.of("请输入任务昵称..."));
        this.addDrawableChild(taskNameField);
        layoutY += ROW_HEIGHT + 15;

        for (int i = 0; i < keyCommandPairs.size(); i++) {
            final int index = i;
            KeyCommandPair pair = keyCommandPairs.get(i);

            TextFieldWidget keyField = new TextFieldWidget(
                this.textRenderer,
                centerX - 90, layoutY - (int) scrollOffset, KEY_FIELD_WIDTH, 20,
                Text.of("按键")
            );
            keyField.setMaxLength(20);
            keyField.setText(pair.keyName);
            keyField.setPlaceholder(Text.of("按键"));
            this.addDrawableChild(keyField);
            keyNameFields.add(keyField);

            TextFieldWidget cmdField = new TextFieldWidget(
                this.textRenderer,
                centerX - 60, layoutY - (int) scrollOffset, CMD_FIELD_WIDTH, 20,
                Text.of("命令")
            );
            cmdField.setMaxLength(100);
            cmdField.setText(pair.command);
            cmdField.setPlaceholder(Text.of("命令或消息"));
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
                    .dimensions(centerX + 95, layoutY - (int) scrollOffset, BUTTON_WIDTH, 20)
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
                    .dimensions(centerX + 95, layoutY - (int) scrollOffset, BUTTON_WIDTH, 20)
                    .build();
                this.addDrawableChild(removeBtn);
                addRemoveButtons.add(removeBtn);
            }

            layoutY += ROW_HEIGHT;
        }

        layoutY += 10;

        loopIntervalField = new TextFieldWidget(
            this.textRenderer,
            centerX - 10, layoutY - (int) scrollOffset, SMALL_FIELD_WIDTH, 20,
            Text.of("循环间隔")
        );
        loopIntervalField.setMaxLength(10);
        loopIntervalField.setText(String.valueOf(task.cycleInterval));
        loopIntervalField.setTextPredicate(text -> text.isEmpty() || text.matches("\\d+"));
        this.addDrawableChild(loopIntervalField);
        layoutY += ROW_HEIGHT;

        keyIntervalField = new TextFieldWidget(
            this.textRenderer,
            centerX - 10, layoutY - (int) scrollOffset, SMALL_FIELD_WIDTH, 20,
            Text.of("按键间隔")
        );
        keyIntervalField.setMaxLength(10);
        keyIntervalField.setText(String.valueOf(task.keyInterval));
        keyIntervalField.setTextPredicate(text -> text.isEmpty() || text.matches("\\d+"));
        this.addDrawableChild(keyIntervalField);
        layoutY += ROW_HEIGHT;

        holdDurationField = new TextFieldWidget(
            this.textRenderer,
            centerX - 10, layoutY - (int) scrollOffset, SMALL_FIELD_WIDTH, 20,
            Text.of("按住时长")
        );
        holdDurationField.setMaxLength(10);
        holdDurationField.setText(String.valueOf(task.holdDuration));
        holdDurationField.setTextPredicate(text -> text.isEmpty() || text.matches("\\d+"));
        this.addDrawableChild(holdDurationField);
        layoutY += ROW_HEIGHT;

        pressCountField = new TextFieldWidget(
            this.textRenderer,
            centerX - 10, layoutY - (int) scrollOffset, SMALL_FIELD_WIDTH, 20,
            Text.of("按键次数")
        );
        pressCountField.setMaxLength(10);
        pressCountField.setText(String.valueOf(task.pressCount));
        pressCountField.setTextPredicate(text -> text.isEmpty() || text.matches("\\d+"));
        this.addDrawableChild(pressCountField);
        layoutY += ROW_HEIGHT + 20;

        int buttonY = Math.max(layoutY, this.height - 35);
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
            .dimensions(startX, buttonY - (int) scrollOffset, buttonWidth, 20)
            .build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.of("取消"),
            button -> this.client.setScreen(parent))
            .dimensions(startX + buttonWidth + spacing, buttonY - (int) scrollOffset, buttonWidth, 20)
            .build());

        int contentHeight = buttonY + 30;
        maxScrollOffset = Math.max(0, contentHeight - this.height);
    }

    private void saveCurrentInputData() {
        if (taskNameField != null) {
            task.nickname = taskNameField.getText();
        }

        for (int i = 0; i < keyCommandPairs.size(); i++) {
            if (i < keyNameFields.size()) {
                keyCommandPairs.get(i).keyName = keyNameFields.get(i).getText();
            }
            if (i < commandFields.size()) {
                keyCommandPairs.get(i).command = commandFields.get(i).getText();
            }
        }

        if (loopIntervalField != null) {
            String text = loopIntervalField.getText().trim();
            task.cycleInterval = text.isEmpty() ? 0 : Integer.parseInt(text);
        }

        if (keyIntervalField != null) {
            String text = keyIntervalField.getText().trim();
            task.keyInterval = text.isEmpty() ? 0 : Integer.parseInt(text);
        }

        if (holdDurationField != null) {
            String text = holdDurationField.getText().trim();
            task.holdDuration = text.isEmpty() ? 0 : Integer.parseInt(text);
        }

        if (pressCountField != null) {
            String text = pressCountField.getText().trim();
            task.pressCount = text.isEmpty() ? 0 : Integer.parseInt(text);
        }
    }

    private void rebuildUI() {
        saveCurrentInputData();
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
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int layoutY = 15;

        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
            centerX, layoutY - (int) scrollOffset, LABEL_COLOR);

        layoutY = 40;

        context.drawTextWithShadow(this.textRenderer, "任务昵称:",
            centerX - 90, layoutY - (int) scrollOffset - 12, LABEL_COLOR);
        layoutY += ROW_HEIGHT + 15;

        layoutY += (keyNameFields.size() * ROW_HEIGHT) + 10;

        context.drawTextWithShadow(this.textRenderer, "循环间隔(ms):",
            centerX - 90, layoutY - (int) scrollOffset + 6, LABEL_COLOR);
        layoutY += ROW_HEIGHT;

        context.drawTextWithShadow(this.textRenderer, "按键间隔(ms):",
            centerX - 90, layoutY - (int) scrollOffset + 6, LABEL_COLOR);
        layoutY += ROW_HEIGHT;

        context.drawTextWithShadow(this.textRenderer, "按住时长(ms):",
            centerX - 90, layoutY - (int) scrollOffset + 6, LABEL_COLOR);
        layoutY += ROW_HEIGHT;

        context.drawTextWithShadow(this.textRenderer, "按键次数:",
            centerX - 90, layoutY - (int) scrollOffset + 6, LABEL_COLOR);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double newOffset = scrollOffset - verticalAmount * SCROLL_SPEED;
        scrollOffset = Math.max(0, Math.min(newOffset, maxScrollOffset));
        rebuildUI();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (TextFieldWidget keyField : keyNameFields) {
            if (keyField.isFocused()) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE ||
                    keyCode == GLFW.GLFW_KEY_TAB ||
                    keyCode == GLFW.GLFW_KEY_BACKSPACE ||
                    keyCode == GLFW.GLFW_KEY_DELETE) {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }

                String keyName = convertKeyCodeToName(keyCode);
                keyField.setText(keyName);
                keyField.setFocused(false);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private String convertKeyCodeToName(int keyCode) {
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            return String.valueOf((char)keyCode);
        }

        if (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            return String.valueOf((char)keyCode);
        }

        if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9) {
            return "Num" + (keyCode - GLFW.GLFW_KEY_KP_0);
        }

        if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F12) {
            return "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE: return "Space";
            case GLFW.GLFW_KEY_ENTER: return "Enter";
            case GLFW.GLFW_KEY_LEFT_SHIFT: return "LShift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT: return "RShift";
            case GLFW.GLFW_KEY_LEFT_CONTROL: return "LCtrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCtrl";
            case GLFW.GLFW_KEY_LEFT_ALT: return "LAlt";
            case GLFW.GLFW_KEY_RIGHT_ALT: return "RAlt";
            case GLFW.GLFW_KEY_LEFT: return "Left";
            case GLFW.GLFW_KEY_RIGHT: return "Right";
            case GLFW.GLFW_KEY_UP: return "Up";
            case GLFW.GLFW_KEY_DOWN: return "Down";
            case GLFW.GLFW_KEY_HOME: return "Home";
            case GLFW.GLFW_KEY_END: return "End";
            case GLFW.GLFW_KEY_PAGE_UP: return "PgUp";
            case GLFW.GLFW_KEY_PAGE_DOWN: return "PgDn";
            case GLFW.GLFW_KEY_INSERT: return "Insert";
            case GLFW.GLFW_KEY_CAPS_LOCK: return "CapsLock";
            case GLFW.GLFW_KEY_NUM_LOCK: return "NumLock";
            case GLFW.GLFW_KEY_SCROLL_LOCK: return "ScrollLock";
            case GLFW.GLFW_KEY_PAUSE: return "Pause";
            case GLFW.GLFW_KEY_MINUS: return "-";
            case GLFW.GLFW_KEY_EQUAL: return "=";
            case GLFW.GLFW_KEY_LEFT_BRACKET: return "[";
            case GLFW.GLFW_KEY_RIGHT_BRACKET: return "]";
            case GLFW.GLFW_KEY_SEMICOLON: return ";";
            case GLFW.GLFW_KEY_APOSTROPHE: return "'";
            case GLFW.GLFW_KEY_COMMA: return ",";
            case GLFW.GLFW_KEY_PERIOD: return ".";
            case GLFW.GLFW_KEY_SLASH: return "/";
            case GLFW.GLFW_KEY_BACKSLASH: return "\\";
            case GLFW.GLFW_KEY_GRAVE_ACCENT: return "`";
            default: return "Key" + keyCode;
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    private static class KeyCommandPair {
        String keyName = "";
        String command = "";

        boolean isValid() {
            return !keyName.trim().isEmpty() && !command.trim().isEmpty();
        }
    }
}
