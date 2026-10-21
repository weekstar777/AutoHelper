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
    private int scrollOffset = 0;
    private static final int CONTENT_HEIGHT = 400;
    private static final int PAIR_HEIGHT = 30;

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

        // 任务昵称
        nicknameField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, startY, fieldWidth, fieldHeight,
            Text.literal("Nickname")
        );
        nicknameField.setMaxLength(32);
        nicknameField.setPlaceholder(Text.literal("任务昵称（可选）"));
        this.addSelectableChild(nicknameField);

        // 初始化按键-命令对
        if (existingTask != null) {
            // 解析已有任务
            String[] keys = (existingTask.keyName != null && !existingTask.keyName.isEmpty())
                ? existingTask.keyName.split("") : new String[0];
            String[] cmds = (existingTask.command != null && !existingTask.command.isEmpty())
                ? existingTask.command.split(",") : new String[0];

            int maxLen = Math.max(keys.length, cmds.length);
            if (maxLen == 0) {
                keyCommandPairs.add(new KeyCommandPair());
            } else {
                for (int i = 0; i < maxLen; i++) {
                    String key = i < keys.length ? keys[i] : "";
                    String cmd = i < cmds.length ? cmds[i].trim() : "";
                    keyCommandPairs.add(new KeyCommandPair(key, cmd));
                }
            }

            cycleIntervalField.setText(String.valueOf(existingTask.cycleInterval));
            keyIntervalField.setText(String.valueOf(existingTask.keyInterval));
            holdDurationField.setText(String.valueOf(existingTask.holdDuration));
            pressCountField.setText(String.valueOf(existingTask.pressCount));

            if (existingTask.nickname != null) {
                nicknameField.setText(existingTask.nickname);
            }
        } else {
            // 新任务，添加一个空对
            keyCommandPairs.add(new KeyCommandPair());
        }

        // 创建按键-命令对的UI
        int pairStartY = startY + spacing + 20;
        rebuildKeyCommandPairUI(pairStartY);

        // 循环间隔
        int configStartY = pairStartY + (keyCommandPairs.size() * PAIR_HEIGHT) + 20;
        cycleIntervalField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, configStartY,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.cycle_interval")
        );
        cycleIntervalField.setMaxLength(10);
        this.addSelectableChild(cycleIntervalField);

        // 按键间隔
        keyIntervalField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, configStartY + spacing,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.key_interval")
        );
        keyIntervalField.setMaxLength(10);
        this.addSelectableChild(keyIntervalField);

        // 按住时长
        holdDurationField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, configStartY + spacing * 2,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.hold_duration")
        );
        holdDurationField.setMaxLength(10);
        this.addSelectableChild(holdDurationField);

        // 按键次数
        pressCountField = new TextFieldWidget(
            this.textRenderer, centerX - fieldWidth / 2, configStartY + spacing * 3,
            fieldWidth, fieldHeight,
            Text.translatable("gui.autohelper.task.press_count")
        );
        pressCountField.setMaxLength(5);
        this.addSelectableChild(pressCountField);

        // 默认值
        if (existingTask == null) {
            cycleIntervalField.setText("5000");
            keyIntervalField.setText("0");
            holdDurationField.setText("100");
            pressCountField.setText("1");
        }

        // 保存和取消按钮
        saveButton = ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.save"),
            button -> saveTask())
            .dimensions(centerX - 105, configStartY + spacing * 4, 100, 20)
            .build();
        this.addDrawableChild(saveButton);

        cancelButton = ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.cancel"),
            button -> this.close())
            .dimensions(centerX + 5, configStartY + spacing * 4, 100, 20)
            .build();
        this.addDrawableChild(cancelButton);
    }

    private void rebuildKeyCommandPairUI(int startY) {
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

        int centerX = this.width / 2;

        for (int i = 0; i < keyCommandPairs.size(); i++) {
            final int index = i;
            KeyCommandPair pair = keyCommandPairs.get(i);
            int y = startY + (i * PAIR_HEIGHT) - scrollOffset;

            // 按键名称输入框 (60px宽)
            TextFieldWidget keyField = new TextFieldWidget(
                this.textRenderer, centerX - 150, y, 60, 20,
                Text.literal("Key")
            );
            keyField.setMaxLength(10);
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

            // 添加按钮 (+)
            if (i == keyCommandPairs.size() - 1) {
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
        keyCommandPairs.add(new KeyCommandPair());
        this.clearChildren();
        this.init();
    }

    private void removeKeyCommandPair(int index) {
        if (keyCommandPairs.size() > 1) {
            keyCommandPairs.remove(index);
            this.clearChildren();
            this.init();
        }
    }

    private void saveTask() {
        errorMessage = null;

        try {
            // 收集所有按键-命令对的数据
            StringBuilder keyNameBuilder = new StringBuilder();
            StringBuilder commandBuilder = new StringBuilder();

            for (int i = 0; i < keyCommandPairs.size(); i++) {
                String key = keyNameFields.get(i).getText().trim();
                String cmd = commandFields.get(i).getText().trim();

                if (!key.isEmpty() || !cmd.isEmpty()) {
                    if (keyNameBuilder.length() > 0) {
                        // 按键直接连接，不用分隔符
                        keyNameBuilder.append(key);
                        // 命令用逗号分隔
                        commandBuilder.append(",");
                    } else {
                        keyNameBuilder.append(key);
                    }
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
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int startY = 40 - scrollOffset;
        int spacing = 30;

        // 绘制标签
        context.drawTextWithShadow(this.textRenderer,
            Text.literal("任务昵称"),
            centerX - 100, startY - 14, 0xFFFFFF);

        // 按键-命令对区域标签
        int pairStartY = startY + spacing + 20;
        context.drawTextWithShadow(this.textRenderer,
            Text.literal("按键-命令映射"),
            centerX - 150, pairStartY - 20, 0xFFFFFF);

        // 配置参数标签
        int configStartY = pairStartY + (keyCommandPairs.size() * PAIR_HEIGHT) + 20;
        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.cycle_interval"),
            centerX - 100, configStartY - 14, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.key_interval"),
            centerX - 100, configStartY + spacing - 14, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.hold_duration"),
            centerX - 100, configStartY + spacing * 2 - 14, 0xFFFFFF);

        context.drawTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.task.press_count"),
            centerX - 100, configStartY + spacing * 3 - 14, 0xFFFFFF);

        // 渲染所有字段
        nicknameField.render(context, mouseX, mouseY, delta);
        for (TextFieldWidget field : keyNameFields) {
            field.render(context, mouseX, mouseY, delta);
        }
        for (TextFieldWidget field : commandFields) {
            field.render(context, mouseX, mouseY, delta);
        }
        cycleIntervalField.render(context, mouseX, mouseY, delta);
        keyIntervalField.render(context, mouseX, mouseY, delta);
        holdDurationField.render(context, mouseX, mouseY, delta);
        pressCountField.render(context, mouseX, mouseY, delta);

        // 绘制错误消息
        if (errorMessage != null) {
            context.drawCenteredTextWithShadow(this.textRenderer, errorMessage,
                centerX, configStartY + spacing * 4 + 25, 0xFF5555);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, CONTENT_HEIGHT - this.height + 60);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * 10));
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }
}
