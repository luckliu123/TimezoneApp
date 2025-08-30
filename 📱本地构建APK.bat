@echo off
echo ============================================
echo   时区设置助手 - 本地构建APK
echo ============================================
echo.
echo 🎯 这个方案绕过GitHub Actions，直接在本地构建APK
echo 📱 构建完成后，您可以直接使用或手动上传到GitHub
echo.

echo 📋 检查构建环境...
if not exist "gradlew.bat" (
    echo [错误] 找不到gradlew.bat文件
    echo 请确保在项目根目录下运行此脚本
    pause
    exit /b 1
)

echo.
echo 🔧 开始构建APK...
echo 这可能需要几分钟，请耐心等待...
echo.

:: 构建Debug版本APK
call gradlew.bat assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo [❌] 构建失败！
    echo.
    echo 💡 可能的解决方案：
    echo 1. 安装Android Studio: https://developer.android.com/studio
    echo 2. 配置JAVA_HOME环境变量
    echo 3. 检查网络连接（下载依赖需要网络）
    echo.
    pause
    exit /b 1
)

echo.
echo [✅] 构建成功！
echo.
echo 📱 APK文件位置：
echo app\build\outputs\apk\debug\app-debug.apk
echo.
echo 📋 文件信息：
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    for %%F in ("app\build\outputs\apk\debug\app-debug.apk") do (
        echo - 文件大小: %%~zF 字节
        echo - 修改时间: %%~tF
    )
    echo.
    echo 🎉 您现在可以：
    echo 1. 直接安装到手机（传输APK文件到手机）
    echo 2. 手动上传到GitHub Releases页面
    echo 3. 分享给其他用户
    echo.
    echo 📂 是否打开APK所在文件夹？ (Y/N)
    set /p choice=
    if /i "%choice%"=="Y" (
        explorer app\build\outputs\apk\debug\
    )
) else (
    echo [⚠️] APK文件未找到，请检查构建日志
)

echo.
pause