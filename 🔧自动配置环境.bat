@echo off
echo ============================================
echo   时区设置助手 - 自动配置构建环境
echo ============================================
echo.
echo 🚀 正在为您自动配置Android构建环境...
echo 📦 这将下载并配置OpenJDK 17 (约200MB)
echo ⏱️ 预计需要5-10分钟 (取决于网络速度)
echo.

set /p confirm=确认下载并配置环境？(Y/N): 
if /i not "%confirm%"=="Y" (
    echo 操作已取消
    pause
    exit /b 0
)

echo.
echo 📥 步骤1: 下载OpenJDK 17...
if not exist "tools" mkdir tools
cd tools

echo 正在下载JDK 17 (便携版)...
powershell -Command "Invoke-WebRequest -Uri 'https://aka.ms/download-jdk/microsoft-jdk-17.0.8-windows-x64.zip' -OutFile 'jdk17.zip'"

if %errorlevel% neq 0 (
    echo [❌] JDK下载失败，请检查网络连接
    pause
    exit /b 1
)

echo.
echo 📦 步骤2: 解压JDK...
powershell -Command "Expand-Archive -Path 'jdk17.zip' -DestinationPath '.' -Force"

echo.
echo ⚙️ 步骤3: 配置环境变量...
for /d %%D in (jdk-*) do (
    set JDK_PATH=%CD%\%%D
    goto found_jdk
)

:found_jdk
echo JDK路径: %JDK_PATH%

:: 设置当前会话的环境变量
set JAVA_HOME=%JDK_PATH%
set PATH=%JDK_PATH%\bin;%PATH%

echo.
echo 🔍 步骤4: 验证Java安装...
"%JDK_PATH%\bin\java" -version

if %errorlevel% neq 0 (
    echo [❌] Java配置失败
    pause
    exit /b 1
)

echo.
echo [✅] Java环境配置完成！
echo.

cd ..

echo 🔨 步骤5: 开始构建APK...
echo.

:: 使用配置好的Java构建APK
set JAVA_HOME=%JDK_PATH%
set PATH=%JDK_PATH%\bin;%PATH%

call gradlew.bat assembleDebug

if %errorlevel% neq 0 (
    echo.
    echo [❌] APK构建失败
    echo.
    echo 💡 可能需要：
    echo 1. 检查网络连接 (下载Gradle和依赖)
    echo 2. 重新运行此脚本
    echo 3. 使用GitHub Codespaces在线构建
    echo.
    pause
    exit /b 1
)

echo.
echo [🎉] 构建成功！
echo.
echo 📱 APK文件已生成：
echo app\build\outputs\apk\debug\app-debug.apk
echo.
echo 📋 下一步：
echo 1. 将APK传输到手机
echo 2. 在手机上安装APK
echo 3. 授予必要权限后使用
echo.

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo 📂 是否打开APK所在文件夹？ (Y/N)
    set /p choice=
    if /i "%choice%"=="Y" (
        explorer app\build\outputs\apk\debug\
    )
)

pause