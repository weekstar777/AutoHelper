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

public class TaskEditScreen extends Screen {
    private final Screen parent;
    private final ConfigManager configManager;
    private final KeyPressTask existingTask;
    private final int taskIndex;

    private TextFieldWidget nicknameField;
    private TextFieldWidget cycleIntervalField;
    private TextFieldWidget keyIntervalField;
    private TextFieldWidget holdDurationField;
    private TextFieldWidget pressCountField;

    private ButtonWidget saveButton;
    private ButtonWidget cancelButton;

    // 按键-命令对列表
    private List<KeyCommandPair> keyCommandPairs = new ArrayList<>();
    private List<TextFieldWidget> keyNameFields = new ArrayList<>();
    private List<TextFieldWidget> commandFields = new ArrayList<>();
    private List<ButtonWidget> addButtons = new ArrayList<>();
    private List<ButtonWidget> removeButtons = new ArrayList<>();

    private String errorMessage = null;
    private static final int MAX_VISIBLE_PAIRS = 5; // 最多显示5对

    public TaskEditScreen(Screen parent, ConfigManager configManager,
                         KeyPressTask existingTask, int taskIndex) {
        super(Text.translatable(existingTask == null ?
            "gui.autohelper.task.add_title" : "gui.autohelper.task.edit_title"));
        this.parent = parent;
        this.configManager = configManager;
        this.existingTask = existingTask;
        this.taskIndex = taskIndex;

        // 初始化按键-命令对数据
        if (existingTask != null) {
            // 解析已有任务（按键用逗号分隔，命令也用逗号分隔）
            String[] keys = (existingTask.keyName != null && !existingTask.keyName.isEmpty())
                ? existingTask.keyName.split(",") : new String[0];
            String[] cmds = (existingTask.command != null && !existingTask.command.isEmpty())
                ? existingTask.command.split(",") : new String[0];

            int maxLen = Math.max(keys.length, cmds.length);
            if (maxLen == 0) {
                keyCommandPairs.add(new KeyCommandPair());
            } else {
                for (int i = 0; i < maxLen; i++) {
                    String key = i < keys.length ? keys[i].trim() : "";
                    String cmd = i < cmds.length ? cmds[i].trim() : "";
                    keyCommandPairs.add(new KeyCommandPair(key, cmd));
                }
            }
        } else {
            // 新任务，添加一个空对
            keyCommandPairs.add(new KeyCommandPair());
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;
        int spacing = 25;
        int currentY = startY;

        // 任务昵称
        nicknameField = new TextFieldWidget(
            this.textRenderer, centerX - 100, currentY, 200, 20,
            Text.literal("Nickname")
        );
        nicknameField.setMaxLength(32);
        nicknameField.setPlaceholder(Text.literal("任务昵称（可选）"));
        if (existingTask != null && existingTask.nickname != null) {
            nicknameField.setText(existingTask.nickname);
        }
        this.addSelectableChild(nicknameField);

        currentY += spacing + 10;

        // 创建按键-命令对的UI
        rebuildKeyCommandPairUI(centerX, currentY);

        // 更新currentY
        currentY += Math.min(keyCommandPairs.size(), MAX_VISIBLE_PAIRS) * 25 + 10;

        // 配置参数区域
        int configY = currentY;

        // 循环间隔
        cycleIntervalField = new TextFieldWidget(
            this.textRenderer, centerX - 100, configY, 200, 20,
            Text.translatable("gui.autohelper.task.cycle_interval")
        );
        cycleIntervalField.setMaxLength(10);
        if (existingTask != null) {
            cycleIntervalField.setText(String.valueOf(existingTask.cycleInterval));
        } else {
            cycleIntervalField.setText("5000");
        }
        this.addSelectableChild(cycleIntervalField);

        // 按键间隔
        keyIntervalField = new TextFieldWidget(
            this.textRenderer, centerX - 100, configY + spacing, 200, 20,
            Text.translatable("gui.autohelper.task.key_interval")
        );
        keyIntervalField.setMaxLength(10);
        if (existingTask != null) {
            keyIntervalField.setText(String.valueOf(existingTask.keyInterval));
        } else {
            keyIntervalField.setText("0");
        }
        this.addSelectableChild(keyIntervalField);

        // 按住时长
        holdDurationField = new TextFieldWidget(
            this.textRenderer, centerX - 100, configY + spacing * 2, 200, 20,
            Text.translatable("gui.autohelper.task.hold_duration")
        );
        holdDurationField.setMaxLength(10);
        if (existingTask != null) {
            holdDurationField.setText(String.valueOf(existingTask.holdDuration));
        } else {
            holdDurationField.setText("100");
        }
        this.addSelectableChild(holdDurationField);

        // 按键次数
        pressCountField = new TextFieldWidget(
            this.textRenderer, centerX - 100, configY + spacing * 3, 200, 20,
            Text.translatable("gui.autohelper.task.press_count")
        );
        pressCountField.setMaxLength(5);
        if (existingTask != null) {
            pressCountField.setText(String.valueOf(existingTask.pressCount));
        } else {
            pressCountField.setText("1");
        }
        this.addSelectableChild(pressCountField);

        // 保存和取消按钮
        int buttonY = configY + spacing * 4;
        saveButton = ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.save"),
            button -> saveTask())
            .dimensions(centerX - 105, buttonY, 100, 20)
            .build();
        this.addDrawableChild(saveButton);

        cancelButton = ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.cancel"),
            button -> this.close())
            .dimensions(centerX + 5, buttonY, 100, 20)
            .build();
        this.addDrawableChild(cancelButton);
    }

    private void rebuildKeyCommandPairUI(int centerX, int startY) {
        // 清空旧的UI组件
        for (TextFieldWidget field : keyNameFields) {
            this.remove(field);
        }
        for (TextFieldWidget field : commandFields) {
            this.remove(field);
        }
        for (ButtonWidget button : addButtons) {
            this.remove(button);
        }
        for (ButtonWidget button : removeButtons) {
            this.remove(button);
        }

        keyNameFields.clear();
        commandFields.clear();
        addButtons.clear();
        removeButtons.clear();

        // 限制显示数量
        int visibleCount = Math.min(keyCommandPairs.size(), MAX_VISIBLE_PAIRS);

        for (int i = 0; i < visibleCount; i++) {
            final int index = i;
            KeyCommandPair pair = keyCommandPairs.get(i);
            int y = startY + (i * 25);

            // 按键名称输入框 (60px宽)
            TextFieldWidget keyField = new TextFieldWidget(
                this.textRenderer, centerX - 150, y, 60, 20,
                Text.literal("Key")
            );
            keyField.setMaxLength(20);
            keyField.setText(pair.keyName);
            keyField.setPlaceholder(Text.literal("按键"));
            this.addSelectableChild(keyField);
            keyNameFields.add(keyField);

            // 命令输入框 (200px宽)
            TextFieldWidget cmdField = new TextFieldWidget(
                this.textRenderer, centerX - 80, y, 200, 20,
                Text.literal("Command")
            );
            cmdField.setMaxLength(256);
            cmdField.setText(pair.command);
            cmdField.setPlaceholder(Text.literal("命令"));
            this.addSelectableChild(cmdField);
            commandFields.add(cmdField);

            // 添加按钮 (+) - 只在最后一行且未超过最大数量
            if (i == visibleCount - 1 && keyCommandPairs.size() < MAX_VISIBLE_PAIRS) {
                ButtonWidget addBtn = ButtonWidget.builder(
                    Text.literal("+"),
                    button -> addKeyCommandPair())
                    .dimensions(centerX + 125, y, 20, 20)
                    .build();
                this.addDrawableChild(addBtn);
                addButtons.add(addBtn);
            }

            // 删除按钮 (-)
            if (keyCommandPairs.size() > 1) {
                ButtonWidget removeBtn = ButtonWidget.builder(
                    Text.literal("-"),
                    button -> removeKeyCommandPair(index))
                    .dimensions(centerX + 150, y, 20, 20)
                    .build();
                this.addDrawableChild(removeBtn);
                removeButtons.add(removeBtn);
            }
        }
    }

    private void addKeyCommandPair() {
        if (keyCommandPairs.size() >= MAX_VISIBLE_PAIRS) {
            errorMessage = "最多只能添加" + MAX_VISIBLE_PAIRS + "个按键-命令对";
            return;
        }

        // 保存当前输入的数据
        saveCurrentInputData();
        // 添加新对
        keyCommandPairs.add(new KeyCommandPair());
        // 重建UI
        this.clearChildren();
        this.init();
    }

    private void removeKeyCommandPair(int index) {
        if (keyCommandPairs.size() > 1) {
            // 保存当前输入的数据
            saveCurrentInputData();
            // 删除指定对
            keyCommandPairs.remove(index);
            // 重建UI
            this.clearChildren();
            this.init();
        }
    }

    private void saveCurrentInputData() {
        // 保存当前所有输入框的数据到keyCommandPairs
        for (int i = 0; i < keyCommandPairs.size() && i < keyNameFields.size(); i++) {
            keyCommandPairs.get(i).keyName = keyNameFields.get(i).getText();
            keyCommandPairs.get(i).command = commandFields.get(i).getText();
        }
    }

    private void saveTask() {
        errorMessage = null;

        try {
            // 保存当前输入数据
            saveCurrentInputData();

            // 收集所有按键-命令对的数据
            StringBuilder keyNameBuilder = new StringBuilder();
            StringBuilder commandBuilder = new StringBuilder();

            for (int i = 0; i < keyCommandPairs.size(); i++) {
                String key = keyCommandPairs.get(i).keyName.trim();
                String cmd = keyCommandPairs.get(i).command.trim();

                if (!key.isEmpty() || !cmd.isEmpty()) {
                    if (keyNameBuilder.length() > 0) {
                        keyNameBuilder.append(",");
                        commandBuilder.append(",");
                    }
                    keyNameBuilder.append(key);
                    commandBuilder.append(cmd);
                }
            }

            String keyName = keyNameBuilder.toString();
            String command = commandBuilder.toString();
            String nickname = nicknameField.getText().trim();

            // 验证：至少要有按键名或命令
            if (keyName.isEmpty() && command.isEmpty()) {
                errorMessage = "按键名和命令不能同时为空";
                return;
            }

            int cycleInterval = Integer.parseInt(cycleIntervalField.getText());
            int keyInterval = Integer.parseInt(keyIntervalField.getText());
            int holdDuration = Integer.parseInt(holdDurationField.getText());
            int pressCount = Integer.parseInt(pressCountField.getText());

            // 验证数值
            if (cycleInterval < 0 || keyInterval < 0 || holdDuration < 0 || pressCount < 0) {
                errorMessage = Text.translatable("gui.autohelper.task.error_invalid_number").getString();
                return;
            }

            KeyPressTask task = new KeyPressTask(
                keyName, cycleInterval, keyInterval, holdDuration, pressCount
            );
            task.nickname = nickname;
            task.command = command;

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
        this.renderBackground(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int startY = 30;
        int spacing = 25;
        int currentY = startY;

        // 绘制标题
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
            centerX, 10, 0xFFFFFF);

        // 绘制任务昵称标签
        context.drawTextWithShadow(this.textRenderer,
            Text.literal("任务昵称"),
            centerX - 150, currentY + 5, 0xFFFFFF);

        currentY += spacing + 10;

        // 按键-命令对区域标签
        context.drawTextWithShadow(this.textRenderer,
            Text.literal("按键-命令映射 (最多" + MAX_VISIBLE_PAIRS + "对)"),
            centerX - 150, currentY - 5, 0xFFFFFF);

        currentY += Math.min(keyCommandPairs.size(), MAX_VISIBLE_PAIRS) * 25 + 10;

        // 配置参数标签
        int configY = currentY;
        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.cycle_interval"),
            centerX - 150, configY + 5, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.key_interval"),
            centerX - 150, configY + spacing + 5, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.hold_duration"),
            centerX - 150, configY + spacing * 2 + 5, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.press_count"),
            centerX - 150, configY + spacing * 3 + 5, 0xFFFFFF);

        // 渲染所有组件
        super.render(context, mouseX, mouseY, delta);

        // 绘制错误消息
        if (errorMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, errorMessage,
                centerX, configY + spacing * 4 + 25, 0xFF5555);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
