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
        keyNameFields.clear();
        commandFields.clear();

        int centerX = this.width / 2;
        int startY = 30;

        // 任务昵称
        taskNameField = new TextFieldWidget(this.textRenderer, centerX - 100, startY, 200, 20, Text.of("Task Name"));
        taskNameField.setMaxLength(50);
        taskNameField.setText(task.nickname);
        this.addDrawableChild(taskNameField);

        int currentY = startY + 35;

        // 按键-命令对区域
        for (int i = 0; i < keyCommandPairs.size(); i++) {
            KeyCommandPair pair = keyCommandPairs.get(i);

            // 按键名称输入框
            TextFieldWidget keyField = new TextFieldWidget(this.textRenderer, centerX - 150, currentY, 80, 20, Text.of("Key"));
            keyField.setMaxLength(30);
            keyField.setText(pair.keyName);
            keyNameFields.add(keyField);
            this.addDrawableChild(keyField);

            // 命令输入框
            TextFieldWidget cmdField = new TextFieldWidget(this.textRenderer, centerX - 60, currentY, 150, 20, Text.of("Command"));
            cmdField.setMaxLength(100);
            cmdField.setText(pair.command);
            commandFields.add(cmdField);
            this.addDrawableChild(cmdField);

            // 只有第1个对旁边有+按钮
            if (i == 0) {
                // 第1个对：只有+按钮
                this.addDrawableChild(ButtonWidget.builder(Text.of("+"), button -> {
                    saveCurrentInputData();
                    keyCommandPairs.add(new KeyCommandPair());
                    rebuildUI();
                }).dimensions(centerX + 100, currentY, 40, 20).build());
            } else {
                // 其他对：只有-按钮
                final int index = i;
                this.addDrawableChild(ButtonWidget.builder(Text.of("-"), button -> {
                    saveCurrentInputData();
                    keyCommandPairs.remove(index);
                    rebuildUI();
                }).dimensions(centerX + 100, currentY, 40, 20).build());
            }

            currentY += 25;
        }

        currentY += 10;

        // 配置参数
        loopIntervalField = new TextFieldWidget(this.textRenderer, centerX - 100, currentY, 200, 20, Text.of("Loop Interval"));
        loopIntervalField.setMaxLength(10);
        loopIntervalField.setText(String.valueOf(task.cycleInterval));
        this.addDrawableChild(loopIntervalField);

        currentY += 25;

        keyIntervalField = new TextFieldWidget(this.textRenderer, centerX - 100, currentY, 200, 20, Text.of("Key Interval"));
        keyIntervalField.setMaxLength(10);
        keyIntervalField.setText(String.valueOf(task.keyInterval));
        this.addDrawableChild(keyIntervalField);

        currentY += 25;

        holdDurationField = new TextFieldWidget(this.textRenderer, centerX - 100, currentY, 200, 20, Text.of("Hold Duration"));
        holdDurationField.setMaxLength(10);
        holdDurationField.setText(String.valueOf(task.holdDuration));
        this.addDrawableChild(holdDurationField);

        currentY += 25;

        pressCountField = new TextFieldWidget(this.textRenderer, centerX - 100, currentY, 200, 20, Text.of("Press Count"));
        pressCountField.setMaxLength(10);
        pressCountField.setText(String.valueOf(task.pressCount));
        this.addDrawableChild(pressCountField);

        currentY += 35;

        // 保存和取消按钮
        this.addDrawableChild(ButtonWidget.builder(Text.of("保存"), button -> {
            if (saveTask()) {
                this.client.setScreen(parent);
            }
        }).dimensions(centerX - 105, currentY, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("取消"), button -> {
            this.client.setScreen(parent);
        }).dimensions(centerX + 5, currentY, 100, 20).build());
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

        int centerX = this.width / 2;
        int startY = 30;

        // 标题固定在顶部，不受滚动影响
        context.drawCenteredTextWithShadow(this.textRenderer,
            isNewTask ? "添加新任务" : "编辑任务",
            centerX, 10, 0xFFFFFF);

        // 应用滚动偏移到其他内容
        context.getMatrices().push();
        context.getMatrices().translate(0, -scrollOffset, 0);

        // 任务昵称标签
        context.drawTextWithShadow(this.textRenderer, "任务昵称:", centerX - 150, startY + 5, 0xFFFFFF);

        int currentY = startY + 35;

        // 按键-命令标签
        context.drawTextWithShadow(this.textRenderer, "按键-命令映射:", centerX - 150, currentY - 15, 0xFFFFFF);

        for (int i = 0; i < keyCommandPairs.size(); i++) {
            context.drawTextWithShadow(this.textRenderer, "按键:", centerX - 150, currentY + 5, 0xA0A0A0);
            context.drawTextWithShadow(this.textRenderer, "命令:", centerX - 60, currentY + 5, 0xA0A0A0);
            currentY += 25;
        }

        currentY += 10;

        // 配置参数标签
        context.drawTextWithShadow(this.textRenderer, "循环间隔(ms):", centerX - 150, currentY + 5, 0xFFFFFF);
        currentY += 25;

        context.drawTextWithShadow(this.textRenderer, "按键间隔(ms):", centerX - 150, currentY + 5, 0xFFFFFF);
        currentY += 25;

        context.drawTextWithShadow(this.textRenderer, "按住时长(ms):", centerX - 150, currentY + 5, 0xFFFFFF);
        currentY += 25;

        context.drawTextWithShadow(this.textRenderer, "按键次数:", centerX - 150, currentY + 5, 0xFFFFFF);

        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= verticalAmount * SCROLL_SPEED;

        // 计算内容总高度
        int contentHeight = 30 + 35 + (keyCommandPairs.size() * 25) + 10 + (4 * 25) + 35 + 20;
        double maxScroll = Math.max(0, contentHeight - this.height + 50);

        // 限制滚动范围
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
