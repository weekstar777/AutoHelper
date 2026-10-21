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
    private int scrollOffset = 0;
    private static final int SCROLL_SPEED = 15;

    private static final int HEADER_HEIGHT = 50;
    private static final int FOOTER_HEIGHT = 40;
    private int contentHeight = 0;

    public TaskEditScreen(Screen parent, ConfigManager configManager, KeyPressTask task, int taskIndex) {
        super(Text.of(task == null ? "添加新任务" : "编辑任务"));
        this.parent = parent;
        this.configManager = configManager;
        this.task = (task != null) ? task : new KeyPressTask();
        this.taskIndex = taskIndex;
        this.isNewTask = (task == null);

        // 初始化数据
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
        int yPos = HEADER_HEIGHT;

        // 任务昵称
        yPos += 5;
        taskNameField = new TextFieldWidget(this.textRenderer, centerX - 100, yPos - scrollOffset, 200, 20, Text.of("任务昵称"));
        taskNameField.setMaxLength(50);
        taskNameField.setText(task.nickname);
        this.addDrawableChild(taskNameField);

        yPos += 40;

        // 按键-命令对区域
        for (int i = 0; i < keyCommandPairs.size(); i++) {
            KeyCommandPair pair = keyCommandPairs.get(i);

            // 按键名称输入框
            TextFieldWidget keyField = new TextFieldWidget(this.textRenderer, centerX - 150, yPos - scrollOffset, 80, 20, Text.of("按键"));
            keyField.setMaxLength(30);
            keyField.setText(pair.keyName);
            keyNameFields.add(keyField);
            this.addDrawableChild(keyField);

            // 命令输入框
            TextFieldWidget cmdField = new TextFieldWidget(this.textRenderer, centerX - 60, yPos - scrollOffset, 150, 20, Text.of("命令"));
            cmdField.setMaxLength(100);
            cmdField.setText(pair.command);
            commandFields.add(cmdField);
            this.addDrawableChild(cmdField);

            // 只有第1个对旁边有+按钮
            if (i == 0) {
                this.addDrawableChild(ButtonWidget.builder(Text.of("+"), button -> {
                    saveCurrentInputData();
                    keyCommandPairs.add(new KeyCommandPair());
                    rebuildUI();
                }).dimensions(centerX + 100, yPos - scrollOffset, 40, 20).build());
            } else {
                final int index = i;
                this.addDrawableChild(ButtonWidget.builder(Text.of("-"), button -> {
                    saveCurrentInputData();
                    keyCommandPairs.remove(index);
                    rebuildUI();
                }).dimensions(centerX + 100, yPos - scrollOffset, 40, 20).build());
            }

            yPos += 30;
        }

        yPos += 10;

        // 配置参数
        loopIntervalField = new TextFieldWidget(this.textRenderer, centerX - 100, yPos - scrollOffset, 200, 20, Text.of("循环间隔(ms)"));
        loopIntervalField.setMaxLength(10);
        loopIntervalField.setText(String.valueOf(task.cycleInterval));
        this.addDrawableChild(loopIntervalField);

        yPos += 30;

        keyIntervalField = new TextFieldWidget(this.textRenderer, centerX - 100, yPos - scrollOffset, 200, 20, Text.of("按键间隔(ms)"));
        keyIntervalField.setMaxLength(10);
        keyIntervalField.setText(String.valueOf(task.keyInterval));
        this.addDrawableChild(keyIntervalField);

        yPos += 30;

        holdDurationField = new TextFieldWidget(this.textRenderer, centerX - 100, yPos - scrollOffset, 200, 20, Text.of("按住时长(ms)"));
        holdDurationField.setMaxLength(10);
        holdDurationField.setText(String.valueOf(task.holdDuration));
        this.addDrawableChild(holdDurationField);

        yPos += 30;

        pressCountField = new TextFieldWidget(this.textRenderer, centerX - 100, yPos - scrollOffset, 200, 20, Text.of("按键次数"));
        pressCountField.setMaxLength(10);
        pressCountField.setText(String.valueOf(task.pressCount));
        this.addDrawableChild(pressCountField);

        yPos += 30;

        // 记录内容总高度
        contentHeight = yPos;

        // 底部按钮 - 固定位置，不受滚动影响
        int bottomY = this.height - 30;
        this.addDrawableChild(ButtonWidget.builder(Text.of("保存"), button -> {
            if (saveTask()) {
                this.client.setScreen(parent);
            }
        }).dimensions(centerX - 105, bottomY, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("取消"), button -> {
            this.client.setScreen(parent);
        }).dimensions(centerX + 5, bottomY, 100, 20).build());
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
        // 渲染背景
        this.renderBackground(context, mouseX, mouseY, delta);

        // 渲染固定的标题
        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer,
            isNewTask ? "添加新任务" : "编辑任务",
            centerX, 20, 0xFFFFFF);

        // 渲染可滚动内容区域的标签
        int yPos = HEADER_HEIGHT + 5;

        // 任务昵称标签
        context.drawTextWithShadow(this.textRenderer, "任务昵称:", centerX - 150, yPos + 5 - scrollOffset, 0xFFFFFF);
        yPos += 40;

        // 按键-命令标签
        context.drawTextWithShadow(this.textRenderer, "按键-命令映射:", centerX - 150, yPos - 10 - scrollOffset, 0xFFFFFF);

        for (int i = 0; i < keyCommandPairs.size(); i++) {
            int labelY = yPos + 5 - scrollOffset;
            if (labelY > HEADER_HEIGHT && labelY < this.height - FOOTER_HEIGHT) {
                context.drawTextWithShadow(this.textRenderer, "按键:", centerX - 180, labelY, 0xA0A0A0);
                context.drawTextWithShadow(this.textRenderer, "命令:", centerX - 90, labelY, 0xA0A0A0);
            }
            yPos += 30;
        }

        yPos += 10;

        // 配置参数标签
        String[] labels = {"循环间隔(ms):", "按键间隔(ms):", "按住时长(ms):", "按键次数:"};

        for (int i = 0; i < 4; i++) {
            int labelY = yPos + 5 - scrollOffset;
            if (labelY > HEADER_HEIGHT && labelY < this.height - FOOTER_HEIGHT) {
                context.drawTextWithShadow(this.textRenderer, labels[i], centerX - 150, labelY, 0xFFFFFF);
            }
            yPos += 30;
        }

        // 渲染所有子组件（widget）
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int oldOffset = scrollOffset;
        scrollOffset -= (int)(verticalAmount * SCROLL_SPEED);

        // 计算最大滚动量
        int maxScroll = Math.max(0, contentHeight - (this.height - HEADER_HEIGHT - FOOTER_HEIGHT));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        // 如果滚动量改变，重建UI以更新widget位置
        if (scrollOffset != oldOffset) {
            rebuildUI();
        }

        return true;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
