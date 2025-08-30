@echo off
echo ============================================
echo   GitHub自动上传和构建脚本
echo ============================================
echo.

echo 本脚本将帮助您：
echo 1. 初始化Git仓库
echo 2. 上传代码到GitHub
echo 3. 触发自动APK构建
echo.

:: 检查是否安装了Git
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Git，请先安装Git：
    echo 下载地址：https://git-scm.com/download/win
    echo.
    echo 或者使用网页上传方式（见GitHub构建指南.md）
    pause
    exit /b 1
)

echo [✓] Git环境检测通过
echo.

:: 获取用户GitHub信息
set /p GITHUB_USERNAME="请输入您的GitHub用户名: "
set /p REPO_NAME="请输入仓库名称 (默认: TimezoneApp): "
if "%REPO_NAME%"=="" set REPO_NAME=TimezoneApp

echo.
echo 请确保您已经在GitHub上创建了仓库：
echo https://github.com/%GITHUB_USERNAME%/%REPO_NAME%
echo.
echo 如果还没有创建，请：
echo 1. 访问 https://github.com
echo 2. 点击右上角 + 号
echo 3. 选择 New repository
echo 4. 仓库名称输入：%REPO_NAME%
echo 5. 选择 Public
echo 6. 点击 Create repository
echo.

set /p CONFIRM="已创建仓库？(y/n): "
if /i not "%CONFIRM%"=="y" (
    echo 请先创建GitHub仓库后再运行此脚本
    pause
    exit /b 1
)

echo.
echo 正在初始化Git仓库...

:: 初始化Git仓库
git init
if %errorlevel% neq 0 (
    echo [错误] Git初始化失败
    pause
    exit /b 1
)

:: 添加所有文件
echo 正在添加项目文件...
git add .

:: 创建初始提交
echo 正在创建提交...
git commit -m "初始版本 - 时区设置助手Android应用"

:: 设置主分支
git branch -M main

:: 添加远程仓库
echo 正在连接GitHub仓库...
git remote add origin https://github.com/%GITHUB_USERNAME%/%REPO_NAME%.git

:: 推送到GitHub
echo 正在上传代码到GitHub...
echo 可能需要输入GitHub用户名和密码（或Personal Access Token）
git push -u origin main

if %errorlevel% neq 0 (
    echo [错误] 上传失败，可能的原因：
    echo 1. GitHub用户名或仓库名错误
    echo 2. 没有权限访问仓库
    echo 3. 网络连接问题
    echo 4. 需要配置GitHub认证
    echo.
    echo 解决方案：
    echo 1. 检查仓库是否存在且可访问
    echo 2. 配置GitHub Personal Access Token
    echo 3. 或使用网页上传方式
    pause
    exit /b 1
)

echo.
echo [✓] 代码上传成功！
echo.
echo 接下来：
echo 1. 访问：https://github.com/%GITHUB_USERNAME%/%REPO_NAME%
echo 2. 点击"Actions"标签查看构建进度
echo 3. 等待构建完成（约5-10分钟）
echo 4. 在"Releases"中下载APK文件
echo.
echo 构建完成后APK下载地址：
echo https://github.com/%GITHUB_USERNAME%/%REPO_NAME%/releases
echo.

pause