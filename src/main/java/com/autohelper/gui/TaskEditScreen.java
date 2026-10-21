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
    private double scrollOffset = 0;
    private static final double SCROLL_SPEED = 20.0;

    public TaskEditScreen(Screen parent, ConfigManager configManager, KeyPressTask task, int taskIndex) {
        super(Text.of(task == null ? "添加新任务" : "编辑任务"));
        this.parent = parent;
        this.configManager = configManager;
        this.task = (task != null) ? task : new KeyPressTask();
        this.taskIndex = taskIndex;
        this.isNewTask = (task == null);

        // 初始化数据 - 使用this.task确保访问已初始化的实例
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

        // 如果为空，添加一个默认对
        if (keyCommandPairs.isEmpty()) {
            keyCommandPairs.add(new KeyCommandPair());
        }
    }

    @Override
    protected void init() {
        // TODO: 实现新的GUI布局
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

        // 构建按键和命令字符串
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
        // TODO: 实现新的渲染逻辑
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // TODO: 实现新的滚动逻辑
        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
