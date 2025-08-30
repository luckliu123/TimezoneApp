@echo off
echo ============================================
echo   时区设置助手 - 一键启动开发环境
echo ============================================
echo.

:: 检查常见的Android Studio安装路径
set STUDIO_PATH=""

if exist "C:\Program Files\Android\Android Studio\bin\studio64.exe" (
    set STUDIO_PATH="C:\Program Files\Android\Android Studio\bin\studio64.exe"
    goto found
)

if exist "C:\Program Files (x86)\Android\Android Studio\bin\studio64.exe" (
    set STUDIO_PATH="C:\Program Files (x86)\Android\Android Studio\bin\studio64.exe"
    goto found
)

if exist "%LOCALAPPDATA%\Programs\Android Studio\bin\studio64.exe" (
    set STUDIO_PATH="%LOCALAPPDATA%\Programs\Android Studio\bin\studio64.exe"
    goto found
)

echo [错误] 未找到Android Studio安装路径
echo.
echo 请先安装Android Studio：
echo 1. 访问：https://developer.android.com/studio
echo 2. 下载Android Studio
echo 3. 安装完成后重新运行此脚本
echo.
echo 或者手动启动Android Studio，然后打开此项目文件夹
pause
exit /b 1

:found
echo [✓] 找到Android Studio: %STUDIO_PATH%
echo.
echo 正在启动Android Studio...
echo 请在Android Studio中：
echo 1. 选择 "Open an existing project"
echo 2. 选择此文件夹 (%~dp0)
echo 3. 等待Gradle同步完成
echo 4. 点击绿色播放按钮运行应用
echo.

start "" %STUDIO_PATH% "%~dp0"

echo Android Studio已启动！
echo 如果这是第一次使用，可能需要下载一些组件，请耐心等待。
echo.
pause