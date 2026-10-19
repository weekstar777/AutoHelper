# AutoHelper

Minecraft 1.21 Fabric自动化模组，支持自动按键和服务器自动连接。

## 功能特性

### 1. 自动按键系统
- 支持多个按键配置（Num8, Q, S等）
- 可自定义：
  - 循环间隔：每个循环之间的等待时间
  - 按键间隔：每次按键之间的间隔
  - 按住时长：每次按键按住的时间
  - 按住次数：每个循环按键的次数
- 游戏内GUI配置界面
- 每个任务可独立启用/禁用

### 2. 窗口自动最大化
- 游戏启动时自动最大化窗口（非全屏）

### 3. 服务器自动连接
- 指定服务器IP地址
- 连接失败后每5秒自动重试
- 直到成功连接为止

## 安装

1. 确保已安装Fabric Loader 0.16.0或更高版本
2. 确保已安装Fabric API
3. 将本模组.jar文件放入`.minecraft/mods`文件夹
4. 启动游戏

## 使用方法

### 打开配置界面
- 默认快捷键：`H`键
- 在游戏主界面或游戏中按H键打开配置

### 配置服务器自动连接
1. 在配置界面顶部输入服务器地址
2. 点击"Auto Join"按钮切换开关
3. 启用后游戏会在标题界面自动连接到指定服务器
4. 连接失败后每5秒重试一次

### 配置自动按键
1. 点击"Add New Task"添加新任务
2. 填写配置：
   - Key Name: 按键名称（如Q, W, Num8）
   - Cycle Interval: 循环间隔（毫秒）
   - Key Interval: 按键间隔（毫秒）
   - Hold Duration: 按住时长（毫秒）
   - Press Count: 每个循环按几次
3. 点击"Save"保存
4. 绿色=已启用，红色=已禁用

## 支持的按键
- 字母：A-Z
- 数字：0-9
- 小键盘：Num0-Num9
- 特殊键：Space, Shift, Ctrl, Alt

## 开发

### 构建
```bash
./gradlew build
```

输出文件：`build/libs/autohelper-1.0.0.jar`

### 开发环境
```bash
./gradlew genSources
./gradlew runClient
```

## 许可证
MIT License

## 作者
weekstar777

