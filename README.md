# AutoHelper

Minecraft 1.21 Fabric 自动化模组 - 自动按键、命令执行、服务器自动连接

## 功能特性

### 1. 自动按键系统
- 定时自动按键模拟（A方案）
- 手动触发按键宏（B方案）
- 支持字母、数字、小键盘、功能键、特殊键
- 可配置循环间隔、按键间隔、按住时长、次数

### 2. 命令执行系统
- 配置按键对应的Minecraft命令
- 支持纯命令执行（无按键）
- 支持按键+命令组合
- 手动按键触发命令（类似MacroKeybinds）

### 3. 服务器自动连接
- 游戏启动后自动连接服务器
- 连接失败每5秒自动重试

### 4. 窗口管理
- 游戏启动时自动最大化窗口（非全屏）

### 5. GUI配置界面
- 按H键打开配置
- 游戏内可视化配置所有功能

## 安装

### 前置要求
- Minecraft 1.21
- Fabric Loader 0.16.0+
- Fabric API

### 安装步骤

**Windows快速安装：**
```batch
cd releases\v1.0.0
install.bat
```

**手动安装：**
1. 下载`releases/v1.0.0/AutoHelper-1.0.0.jar`
2. 复制到`.minecraft/mods`文件夹
3. 启动游戏

## 使用方法

### 快速开始
1. 按`H`键打开配置界面
2. 点击"添加新任务"
3. 配置任务参数并保存

### 配置示例

**场景1：自动跳跃（防踢）**
```
按键名称：Space
命令：（留空）
循环间隔：10000ms
按键次数：1
```

**场景2：手动触发快捷命令**
```
按键名称：Z
命令：/give @s apple 64
循环间隔：60000ms
效果：手动按Z获得苹果，每60秒也自动执行
```

**场景3：纯命令定时执行**
```
按键名称：（留空）
命令：/say 定时消息
循环间隔：5000ms
```

**场景4：按键+命令组合**
```
按键名称：E
命令：/say 攻击！
循环间隔：1000ms
按键次数：3
```

## 支持的按键

- 字母：A-Z
- 数字：0-9
- 小键盘：Num0-Num9
- 功能键：F1-F12
- 特殊键：Space, Enter, Tab, Shift, Ctrl, Alt, Escape, Backspace

## 配置文件

位置：`.minecraft/config/autohelper.json`

示例：
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

## 开发

详见 [BUILD.md](BUILD.md)

```bash
# 构建
gradlew.bat build

# 运行测试客户端
gradlew.bat runClient
```

## 版本历史

- **v1.0.0** (2024-10-21) - 首次发布
  - 自动按键系统（定时+手动）
  - 命令执行系统
  - 服务器自动连接
  - GUI配置界面

## 许可证

MIT License

## 作者

weekstar777
