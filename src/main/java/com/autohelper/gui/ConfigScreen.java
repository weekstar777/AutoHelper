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

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ConfigManager configManager;

    private TextFieldWidget serverAddressField;
    private ButtonWidget autoJoinToggle;
    private ButtonWidget addTaskButton;
    private List<TaskEntry> taskEntries;
    private List<ButtonWidget> taskButtons;

    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 30;
    private static final int TASKS_START_Y = 120;

    public ConfigScreen(Screen parent, ConfigManager configManager) {
        super(Text.translatable("gui.autohelper.config.title"));
        this.parent = parent;
        this.configManager = configManager;
        this.taskEntries = new ArrayList<>();
        this.taskButtons = new ArrayList<>();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // Back button
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.autohelper.config.back"),
            button -> this.close())
            .dimensions(centerX - 100, this.height - 30, 200, 20)
            .build());

        // Server address field
        serverAddressField = new TextFieldWidget(
            this.textRenderer,
            centerX - 100, 35, 200, 20,
            Text.translatable("gui.autohelper.config.server_address")
        );
        serverAddressField.setMaxLength(128);
        serverAddressField.setText(configManager.getConfig().serverAddress);
        this.addSelectableChild(serverAddressField);

        // Auto join toggle
        updateAutoJoinButton(centerX);

        // Add task button
        addTaskButton = ButtonWidget.builder(
            Text.translatable("gui.autohelper.config.add_task"),
            button -> this.client.setScreen(
                new TaskEditScreen(this, configManager, null, -1)))
            .dimensions(centerX - 100, 90, 200, 20)
            .build();
        this.addDrawableChild(addTaskButton);

        refreshTaskList();
    }

    private void updateAutoJoinButton(int centerX) {
        if (autoJoinToggle != null) {
            this.remove(autoJoinToggle);
        }

        boolean enabled = configManager.getConfig().autoJoinServer;
        String statusKey = enabled ? "gui.autohelper.config.on" : "gui.autohelper.config.off";

        autoJoinToggle = ButtonWidget.builder(
            Text.translatable("gui.autohelper.config.auto_join",
                Text.translatable(statusKey).getString()),
            button -> {
                configManager.getConfig().autoJoinServer =
                    !configManager.getConfig().autoJoinServer;
                configManager.save();
                updateAutoJoinButton(centerX);
            })
            .dimensions(centerX - 100, 60, 200, 20)
            .build();
        this.addDrawableChild(autoJoinToggle);
    }

    private void refreshTaskList() {
        // Clear old buttons
        for (ButtonWidget button : taskButtons) {
            this.remove(button);
        }
        taskButtons.clear();
        taskEntries.clear();

        List<KeyPressTask> tasks = configManager.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            taskEntries.add(new TaskEntry(tasks.get(i), i));
            createTaskButtons(i, tasks.get(i));
        }
    }

    private void createTaskButtons(int index, KeyPressTask task) {
        int y = TASKS_START_Y + (index * ENTRY_HEIGHT) - scrollOffset;
        int centerX = this.width / 2;

        // 创建按钮，只添加到交互列表，不自动渲染（手动在scissor内渲染）
        // Toggle button
        ButtonWidget toggleBtn = ButtonWidget.builder(
            Text.translatable(task.enabled ?
                "gui.autohelper.task.enabled" : "gui.autohelper.task.disabled"),
            button -> {
                task.enabled = !task.enabled;
                configManager.save();
                refreshTaskList();
            })
            .dimensions(centerX - 150, y, 50, 20)
            .build();
        this.addSelectableChild(toggleBtn);  // 只添加交互，不自动渲染
        taskButtons.add(toggleBtn);

        // Edit button
        ButtonWidget editBtn = ButtonWidget.builder(
            Text.translatable("gui.autohelper.task.edit"),
            button -> this.client.setScreen(
                new TaskEditScreen(this, configManager, task, index)))
            .dimensions(centerX + 55, y, 45, 20)
            .build();
        this.addSelectableChild(editBtn);
        taskButtons.add(editBtn);

        // Delete button
        ButtonWidget deleteBtn = ButtonWidget.builder(
            Text.literal("×"),
            button -> {
                configManager.removeTask(index);
                refreshTaskList();
            })
            .dimensions(centerX + 105, y, 20, 20)
            .build();
        this.addSelectableChild(deleteBtn);
        taskButtons.add(deleteBtn);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render background
        this.renderBackground(context, mouseX, mouseY, delta);

        // 渲染固定元素（服务器地址、固定按钮等）
        super.render(context, mouseX, mouseY, delta);

        // Server address label
        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.translatable("gui.autohelper.config.server_address"),
            this.width / 2, 24, 0xFFFFFF);

        serverAddressField.render(context, mouseX, mouseY, delta);

        // 启用裁剪区域，限制任务列表渲染范围
        int listBottom = this.height - 40;
        context.enableScissor(0, TASKS_START_Y, this.width, listBottom);

        // 手动渲染任务按钮（在scissor内，会被裁剪）
        for (ButtonWidget button : taskButtons) {
            button.render(context, mouseX, mouseY, delta);
        }

        // Render task entries
        int y = TASKS_START_Y;
        for (TaskEntry entry : taskEntries) {
            int renderY = y - scrollOffset;
            renderTaskEntry(context, entry, renderY);
            y += ENTRY_HEIGHT;
        }

        // No tasks message
        if (taskEntries.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("gui.autohelper.config.no_tasks"),
                this.width / 2, TASKS_START_Y + 10, 0x888888);
        }

        // 禁用裁剪
        context.disableScissor();
    }

    private void renderTaskEntry(DrawContext context, TaskEntry entry, int y) {
        KeyPressTask task = entry.task;
        int centerX = this.width / 2;

        // 获取显示名称（昵称或按键名）
        String displayText = task.getDisplayName();

        // 计算文字宽度
        int textWidth = this.textRenderer.getWidth(displayText);

        // 文字框的位置和尺寸
        int boxX = centerX - 95;
        int boxY = y + 2;
        int boxWidth = textWidth + 10;
        int boxHeight = 16;

        // 绘制背景框
        int bgColor = task.enabled ? 0x80404040 : 0x80202020;
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, bgColor);

        // 绘制边框
        int borderColor = task.enabled ? 0xFF00AA00 : 0xFF555555;
        context.fill(boxX, boxY, boxX + boxWidth, boxY + 1, borderColor);  // 上
        context.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, borderColor);  // 下
        context.fill(boxX, boxY, boxX + 1, boxY + boxHeight, borderColor);  // 左
        context.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, borderColor);  // 右

        // 绘制文字
        int textColor = task.enabled ? 0xFFFFFF : 0x888888;
        context.drawTextWithShadow(this.textRenderer, displayText,
            boxX + 5, boxY + 4, textColor);
    }

    @Override
    public void close() {
        configManager.getConfig().serverAddress = serverAddressField.getText();
        configManager.save();
        this.client.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, (taskEntries.size() * ENTRY_HEIGHT) - (this.height - TASKS_START_Y - 50));
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - verticalAmount * 15));
        refreshTaskList();
        return true;
    }

    private static class TaskEntry {
        final KeyPressTask task;
        final int index;

        TaskEntry(KeyPressTask task, int index) {
            this.task = task;
            this.index = index;
        }
    }
}




