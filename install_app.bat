@echo off
echo ============================================
echo   时区设置助手 - 应用安装脚本
echo ============================================
echo.

:: 检查APK文件是否存在
if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo [错误] 未找到APK文件，请先运行 build_app.bat 构建应用
    pause
    exit /b 1
)

echo 请确保：
echo 1. 手机已通过USB连接到电脑
echo 2. 手机已开启"开发者选项"
echo 3. 手机已开启"USB调试"
echo 4. 手机已信任此电脑
echo.
echo 如果不知道如何开启开发者选项，请参考下面的步骤：
echo 1. 打开手机"设置"
echo 2. 找到"关于手机"或"关于设备"
echo 3. 连续点击"版本号"7次，直到提示开启开发者模式
echo 4. 返回设置，找到"开发者选项"
echo 5. 开启"USB调试"
echo.

pause

echo 正在检测连接的设备...
adb devices

echo.
echo 正在安装应用...
adb install -r "app\build\outputs\apk\debug\app-debug.apk"

if %errorlevel% neq 0 (
    echo.
    echo [错误] 安装失败！
    echo 可能的原因：
    echo 1. 手机未正确连接
    echo 2. 未开启USB调试
    echo 3. 未安装ADB工具
    echo.
    echo 您也可以手动安装：
    echo 1. 将 app\build\outputs\apk\debug\app-debug.apk 文件复制到手机
    echo 2. 在手机上点击APK文件进行安装
    pause
    exit /b 1
)

echo.
echo [✓] 安装成功！
echo.
echo 应用已安装到您的手机上，名称为"时区设置助手"
echo 您现在可以在手机上找到并运行该应用。
echo.
pause