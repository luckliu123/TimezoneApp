@echo off
echo ============================================
echo   时区设置助手 - 自动构建脚本
echo ============================================
echo.
echo 正在检查环境...

:: 检查是否安装了Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请先安装Java JDK 8或更高版本
    echo 下载地址：https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo [✓] Java环境检测通过

:: 检查Gradle Wrapper
if not exist "gradlew.bat" (
    echo [信息] 正在下载Gradle Wrapper...
    echo 请稍等，这可能需要几分钟...
)

echo.
echo 正在构建应用...
echo 这可能需要几分钟时间，请耐心等待...
echo.

:: 构建应用
call gradlew.bat assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo [错误] 构建失败！
    echo 请检查以下可能的问题：
    echo 1. 网络连接是否正常
    echo 2. 是否安装了Android SDK
    echo 3. 环境变量是否配置正确
    pause
    exit /b 1
)

echo.
echo [✓] 构建成功！
echo.
echo APK文件位置：app\build\outputs\apk\debug\app-debug.apk
echo.
echo 接下来您可以：
echo 1. 将APK文件传输到手机上安装
echo 2. 或者连接手机后运行 install_app.bat 直接安装
echo.
pause