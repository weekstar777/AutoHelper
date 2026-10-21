# AutoHelper v1.0.0 Release Notes

发布日期：2024-10-21

## ✨ 新功能

### 1. 自动按键系统
- 支持定时自动按键模拟
- 支持字母键（A-Z）、数字键（0-9）、小键盘（Num0-9）
- 支持功能键（F1-F12）和特殊键（Space, Enter, Tab等）
- 可配置循环间隔、按键间隔、按住时长、按键次数
- 每个任务可独立启用/禁用

### 2. 命令执行系统
- 配置按键对应的Minecraft命令
- 支持纯命令执行（无需按键）
- 支持按键+命令组合执行
- 自动处理命令格式

### 3. 手动触发功能
- 玩家手动按键时自动执行配置的命令
- 类似MacroKeybinds的按键宏功能
- 与自动触发互不干扰

### 4. GUI配置界面
- 按H键打开配置界面
- 游戏内可视化配置
- 添加/编辑/删除任务
- 实时启用/禁用切换

### 5. 服务器自动连接
- 配置服务器地址
- 游戏启动后自动连接
- 连接失败每5秒自动重试

### 6. 窗口管理
- 游戏启动时自动最大化窗口（非全屏）

## 📦 安装要求

- Minecraft 1.21
- Fabric Loader 0.16.0+
- Fabric API

## 🚀 安装方法

### 方法1：使用安装脚本（Windows）
```batch
install.bat
```

### 方法2：手动安装
1. 将`AutoHelper-1.0.0.jar`复制到`.minecraft/mods`文件夹
2. 启动游戏

## 📖 使用说明

### 快速开始
1. 启动游戏
2. 按`H`键打开配置界面
3. 点击"添加新任务"
4. 配置任务参数并保存

### 配置示例

**自动跳跃（防踢）：**
```
按键名称：Space
命令：（留空）
循环间隔：10000ms
按键次数：1
```

**手动触发命令：**
```
按键名称：Z
命令：/give @s apple 64
循环间隔：60000ms
```

**纯命令定时执行：**
```
按键名称：（留空）
命令：/say 自动报时
循环间隔：5000ms
```

## 🔧 技术实现

- 使用`java.awt.Robot`进行OS级按键模拟
- 使用Mixin注入`Keyboard.onKey()`监听玩家按键
- 通过`ClientPlayerNetworkHandler.sendCommand()`执行命令
- 配置文件：`.minecraft/config/autohelper.json`

## 📄 文件说明

- `AutoHelper-1.0.0.jar` - Mod文件（31KB）
- `RELEASE_NOTES.md` - 本文档
- `install.bat` - Windows安装脚本

## 🐛 已知问题

无

## 📞 支持

如有问题或建议，请通过GitHub Issues反馈。

## 📜 许可证

MIT License

---

**版本**：1.0.0  
**构建日期**：2024-10-21  
**开发者**：weekstar777
