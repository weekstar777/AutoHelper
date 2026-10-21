# AutoHelper

Minecraft 1.21 Fabric 自动化模组，支持自动按键、命令执行和服务器自动连接。

## ✨ 功能特性

### 🎮 自动按键系统
- **定时自动执行**：按设定的循环间隔自动模拟按键
- **手动触发执行**：玩家按键时自动执行配置的命令
- 支持多任务配置
- 每个任务可独立启用/禁用
- 可自定义任务昵称

### 💬 命令执行系统
- 配置按键对应的Minecraft命令
- 支持纯命令执行（无需按键）
- 支持按键+命令组合执行
- 手动按键触发命令（类似MacroKeybinds）

### 🖥️ 窗口管理
- 游戏启动时自动最大化窗口（非全屏）

### 🌐 服务器自动连接
- 指定服务器IP地址
- 连接失败后每5秒自动重试
- 直到成功连接为止

## 📦 安装要求

- Minecraft 1.21
- Fabric Loader 0.16.0+
- Fabric API

## 🚀 安装步骤

1. 下载最新的`AutoHelper-1.0.0.jar`
2. 将jar文件放入`.minecraft/mods`文件夹
3. 启动游戏

## 📖 使用方法

### 打开配置界面
- 默认快捷键：**H键**
- 在游戏主界面或游戏中按H键打开配置

### 配置自动任务

#### 场景1：定时自动按键
```
任务昵称：自动跳跃
按键名称：Space
命令：（留空）
循环间隔：2000ms
→ 每2秒自动按空格键
```

#### 场景2：手动触发命令
```
任务昵称：快速获得苹果
按键名称：Z
命令：/give @s apple 64
循环间隔：10000ms
→ 手动按Z键立即获得苹果
→ 或每10秒自动执行一次
```

#### 场景3：纯命令定时执行
```
任务昵称：定时报时
按键名称：（留空）
命令：/say 当前时间已过去5秒
循环间隔：5000ms
→ 每5秒自动发送消息
```

#### 场景4：按键+命令组合
```
任务昵称：跳跃并喊话
按键名称：Space
命令：/say 起跳！
循环间隔：3000ms
→ 每3秒自动按空格并发送消息
→ 手动按空格也会发送消息
```

### 任务参数说明

| 参数 | 说明 | 示例 |
|------|------|------|
| 任务昵称 | 任务显示名称（可选） | "自动攻击" |
| 按键名称 | 要模拟的按键（可选） | "Z", "Space", "Num8" |
| 命令 | 要执行的命令（可选） | "/give @s diamond 10" |
| 循环间隔 | 自动执行间隔（毫秒） | 3000 = 3秒 |
| 按键间隔 | 多次按键的间隔（毫秒） | 100 |
| 按住时长 | 按键按住时间（毫秒） | 50 |
| 按键次数 | 每轮按几次（0=无限） | 1 |

### 配置服务器自动连接

1. 在配置界面顶部输入服务器地址
2. 点击"自动连接"按钮启用
3. 游戏将在标题界面自动连接服务器
4. 连接失败后每5秒重试

## ⌨️ 支持的按键

### 字母键
A-Z

### 数字键
0-9, Num0-Num9

### 功能键
F1-F12

### 特殊键
Space, Enter, Tab, Shift, Ctrl, Alt, Escape, Backspace

## 📁 配置文件

配置文件位置：`.minecraft/config/autohelper.json`

示例配置：
```json
{
  "keyPressTasks": [
    {
      "nickname": "获得苹果",
      "keyName": "Z",
      "command": "/give @s apple 64",
      "cycleInterval": 3000,
      "keyInterval": 100,
      "holdDuration": 50,
      "pressCount": 0,
      "enabled": true
    }
  ],
  "serverAddress": "play.example.com",
  "autoJoinServer": true,
  "maximizeWindow": true,
  "retryInterval": 5000
}
```

## 🛠️ 开发

### 构建项目
```bash
gradlew build
```

输出：`build/libs/AutoHelper-1.0.0.jar`

### 运行客户端
```bash
gradlew runClient
```

### 生成源码
```bash
gradlew genSources
```

## 🔧 技术实现

- **按键模拟**：使用`java.awt.Robot`类实现OS级按键模拟
- **命令执行**：通过`ClientPlayerNetworkHandler.sendCommand()`执行
- **按键监听**：使用Mixin注入`Keyboard.onKey()`方法
- **配置管理**：使用Gson进行JSON序列化

## 📄 许可证

MIT License

## 👤 作者

weekstar777

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📝 更新日志

### v1.0.0
- ✅ 自动按键系统
- ✅ 命令执行系统
- ✅ 手动触发功能
- ✅ 服务器自动连接
- ✅ 窗口自动最大化
- ✅ 游戏内GUI配置
