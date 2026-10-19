@echo off
echo ========================================
echo AutoHelper v1.0.0 Installation Script
echo ========================================
echo.

set MINECRAFT_DIR=%APPDATA%\.minecraft
set MODS_DIR=%MINECRAFT_DIR%\mods

echo Checking Minecraft directory...
if not exist "%MINECRAFT_DIR%" (
    echo ERROR: Minecraft directory not found!
    echo Expected location: %MINECRAFT_DIR%
    pause
    exit /b 1
)

echo.
echo Minecraft directory found: %MINECRAFT_DIR%
echo.

if not exist "%MODS_DIR%" (
    echo Creating mods folder...
    mkdir "%MODS_DIR%"
)

echo.
echo Copying AutoHelper-1.0.0.jar to mods folder...
copy /Y "AutoHelper-1.0.0.jar" "%MODS_DIR%\AutoHelper-1.0.0.jar"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo Installation successful!
    echo ========================================
    echo.
    echo The mod has been installed to:
    echo %MODS_DIR%
    echo.
    echo Make sure you have installed:
    echo - Fabric Loader 0.16.0+
    echo - Fabric API 0.102.0+1.21
    echo.
    echo Press H in game to open configuration.
    echo.
) else (
    echo.
    echo ERROR: Installation failed!
    echo Please manually copy AutoHelper-1.0.0.jar to:
    echo %MODS_DIR%
    echo.
)

pause
