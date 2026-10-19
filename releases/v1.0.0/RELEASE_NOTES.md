# AutoHelper v1.0.0 发布说明

**发布日期：** 2024-10-20

**Minecraft版本：** 1.21  
**Fabric Loader：** 0.16.0+  
**Fabric API：** 0.102.0+1.21

---

## 核心功能

### ✅ 自动按键系统
支持配置多个按键任务，每个任务可设置：
- 循环间隔（Cycle Interval）
- 按键间隔（Key Interval）
- 按住时长（Hold Duration）
- 按键次数（Press Count）

支持的按键类型：
- 字母键：A-Z
- 数字键：0-9  
- 小键盘：Num0-Num9
- 特殊键：Space, Shift, Ctrl, Alt

### ✅ 服务器自动连接
- 配置服务器IP地址
- 游戏启动后自动连接
- 连接失败每5秒自动重试
- 直到成功连接为止

### ✅ 窗口自动最大化
- 游戏启动时自动最大化窗口
- 非全屏模式，保留任务栏

### ✅ 游戏内GUI配置
- 按H键打开配置界面
- 可视化任务管理
- 配置实时保存到JSON文件

---

## 安装说明

**前置依赖：**
1. Minecraft Java版 1.21
2. Fabric Loader 0.16.0或更高版本
3. Fabric API 0.102.0+1.21或更高版本

**安装步骤：**
1. 下载 `AutoHelper-1.0.0.jar`
2. 放入 `.minecraft/mods` 文件夹
3. 启动游戏

---

## 使用指南

详细使用说明请参考：[USER_GUIDE.md](USER_GUIDE.md)

**快速开始：**
- 按 **H键** 打开配置
- 配置服务器地址并启用自动连接
- 添加自动按键任务
- 开始游戏！

---

## 已知限制

- 暂不支持鼠标按键（左键/右键）
- 暂不支持组合键（Ctrl+C等）
- 不支持任务优先级设置
- 不支持全局快捷键开关

---

## 配置文件

配置保存位置：`.minecraft/config/autohelper.json`

示例配置：
```json
{
  "keyPressTasks": [
    {
      "keyName": "Space",
      "cycleInterval": 10000,
      "keyInterval": 0,
      "holdDuration": 100,
      "pressCount": 1,
      "enabled": true
    }
  ],
  "serverAddress": "play.hypixel.net",
  "autoJoinServer": true,
  "maximizeWindow": true,
  "retryInterval": 5000
}
```

---

## 技术细节

**架构设计：**
- 基于Fabric Mod Loader
- 使用Mixin进行窗口管理
- 事件驱动的按键系统
- Gson进行配置持久化

**性能优化：**
- 轻量级设计，构建产物仅21KB
- 按键系统基于游戏Tick事件
- 无额外线程开销

**安全性：**
- 仅客户端模组（Client-side only）
- 不修改游戏核心逻辑
- 所有配置保存在本地

---

## 开发者信息

- **作者：** weekstar777
- **许可证：** MIT License
- **仓库：** [GitHub](https://github.com/weekstar777/AutoHelper)

---

## 更新日志

### v1.0.0 (2024-10-20)
- 🎉 首次发布
- ✨ 实现自动按键系统
- ✨ 实现服务器自动连接
- ✨ 实现窗口自动最大化
- ✨ 实现游戏内GUI配置界面
- 📝 完整的用户文档

---

## 未来计划

v1.1.0 考虑的功能：
- [ ] 鼠标按键支持
- [ ] 组合键支持
- [ ] 任务启用/禁用快捷键
- [ ] 任务优先级系统
- [ ] 任务执行统计
- [ ] 导入/导出配置

---

感谢使用 AutoHelper！如有问题请提交Issue。
