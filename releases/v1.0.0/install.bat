@echo off
chcp 65001 >nul
echo ================================
echo AutoHelper v1.0.0 安装程序
echo ================================
echo.

REM 检测.minecraft文件夹
set "MINECRAFT_DIR=%APPDATA%\.minecraft"
if not exist "%MINECRAFT_DIR%" (
    echo [错误] 未找到.minecraft文件夹
    echo 请确保已安装Minecraft Java版
    pause
    exit /b 1
)

REM 检测mods文件夹
set "MODS_DIR=%MINECRAFT_DIR%\mods"
if not exist "%MODS_DIR%" (
    echo [信息] 创建mods文件夹...
    mkdir "%MODS_DIR%"
)

REM 复制jar文件
echo [信息] 正在安装AutoHelper...
copy /Y "AutoHelper-1.0.0.jar" "%MODS_DIR%\" >nul
if %errorlevel% equ 0 (
    echo [成功] AutoHelper已安装到: %MODS_DIR%
    echo.
    echo ================================
    echo 安装完成！
    echo ================================
    echo.
    echo 使用说明：
    echo 1. 确保已安装Fabric Loader和Fabric API
    echo 2. 启动Minecraft 1.21
    echo 3. 按H键打开配置界面
    echo.
) else (
    echo [错误] 安装失败
)

pause
