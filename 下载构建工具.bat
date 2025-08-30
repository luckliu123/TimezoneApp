@echo off
echo ============================================
echo   自动下载Gradle构建工具
echo ============================================
echo.

:: 创建gradle wrapper目录
if not exist "gradle\wrapper" mkdir gradle\wrapper

:: 设置下载URL
set GRADLE_JAR_URL=https://github.com/gradle/gradle/raw/v8.0.0/gradle/wrapper/gradle-wrapper.jar

echo 正在下载Gradle Wrapper...
echo 这可能需要几分钟，请耐心等待...

:: 使用PowerShell下载文件
powershell -Command "Invoke-WebRequest -Uri '%GRADLE_JAR_URL%' -OutFile 'gradle\wrapper\gradle-wrapper.jar'"

if %errorlevel% neq 0 (
    echo [错误] 下载失败，请检查网络连接
    echo 您也可以手动下载Android Studio来构建项目
    pause
    exit /b 1
)

echo [✓] Gradle Wrapper下载完成
echo.
echo 现在可以运行 build_app.bat 来构建应用了
pause