# 🕐 时区设置助手 (TimeZone Helper)

[![Build Android APK](https://github.com/YOUR_USERNAME/TimezoneApp/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/TimezoneApp/actions/workflows/build.yml)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)

一个专为三星Galaxy S8设计的Android应用，提供便捷的时区设置和相册管理功能。

## 📱 快速下载

### 🚀 自动构建版本
**[📲 点击下载最新APK](https://github.com/YOUR_USERNAME/TimezoneApp/releases/latest)**

- 🔄 每次代码更新后自动构建
- ✅ 经过完整测试验证
- 🛡️ 已签名，可直接安装

## ✨ 功能特性

### ⏰ 时区管理
- 🌍 支持全球主要时区选择
- 📶 智能WiFi连接检测
- 🌐 网络时间自动同步
- 💾 时区设置持久化保存
- 🔄 **一次设置，永久自动化** - 保存用户设置后每次启动自动应用
- ⚡ **WiFi优先策略** - 优先检测WiFi连接，适应各种手机网络环境

### 🗂️ 相册清理
- 🧹 一键清理所有照片和视频
- 🔒 双重确认安全机制
- 📋 支持主流媒体格式
- ⚠️ **注意：操作不可逆，请谨慎使用**

### 🎨 界面设计
- 📐 Material Design 3 设计风格
- 🎴 卡片式布局，功能分区清晰
- 🎯 直观的用户交互体验
- 🚨 重要操作橙色警告提示

## 📋 系统要求

- 🤖 Android 7.0 (API 24) 或更高版本
- 📱 推荐设备：三星Galaxy S8
- 💾 至少20MB可用存储空间
- 🌐 网络连接（时间同步功能）

## 🔑 权限说明

应用需要以下权限来正常工作：

| 权限 | 用途 |
|------|------|
| `INTERNET` | 获取网络时间 |
| `ACCESS_NETWORK_STATE` | 检测网络连接状态 |
| `ACCESS_WIFI_STATE` | 检测WiFi连接状态 |
| `WRITE_SETTINGS` | 修改系统时区设置 |
| `SET_TIME_ZONE` | 设置时区 |
| `READ_EXTERNAL_STORAGE` | 读取相册文件 |
| `WRITE_EXTERNAL_STORAGE` | 删除相册文件 |
| `MANAGE_EXTERNAL_STORAGE` | 管理所有文件 (Android 11+) |
| `READ_MEDIA_IMAGES` | 读取媒体图片 (Android 13+) |
| `READ_MEDIA_VIDEO` | 读取媒体视频 (Android 13+) |

## 📲 安装方法

### 方法一：直接下载
1. 点击上方的 **[📲 下载最新APK]** 链接
2. 在手机上下载APK文件
3. 点击安装（可能需要允许未知来源应用）

### 方法二：GitHub Releases
1. 访问 [Releases 页面](https://github.com/YOUR_USERNAME/TimezoneApp/releases)
2. 下载最新版本的APK文件
3. 传输到手机并安装

## 🔧 使用指南

### 首次使用
1. 📝 授予必要的系统权限
2. 🔌 确保手机连接WiFi网络
3. 🎯 开始使用各项功能

### 时区设置
1. 🌍 在下拉菜单中选择目标时区
2. ⚡ 应用启动后自动检测WiFi连接状态
3. 🔄 WiFi连接成功后自动获取网络时间并设置时区
4. 🔁 WiFi未连接时，连接WiFi后点击"重试连接"按钮
5. ⚙️ 或点击"前往WiFi设置"一键跳转到WiFi设置页面
6. 💾 **一次设置，永久生效** - 设置后下次打开APP自动应用保存的时区

💡 **智能化体验**: 选择好时区后，应用会自动完成所有设置步骤！
🎆 **永久记忆**: 设置一次后，以后每次打开APP都会自动应用您的时区偏好！

### 相册清理
1. 🗂️ 点击"清理相册"按钮
2. 🔑 授予存储访问权限
3. ⚠️ 在确认对话框中点击"确认删除"
4. ⏳ 等待清理过程完成

## 🏗️ 技术架构

### 开发技术栈
- 🏷️ **语言**: Kotlin
- 🎨 **UI**: Material Design 3 + ViewBinding
- ⚙️ **异步处理**: Kotlin Coroutines
- 💾 **数据存储**: SharedPreferences
- 🔧 **构建工具**: Gradle + Android Gradle Plugin
- 🧪 **测试框架**: JUnit + Android Test

### 核心组件
- `MainActivity`: 主界面控制器
- `TimezoneUtils`: 时区工具类
- `GalleryCleanup`: 相册清理功能
- Material Design卡片式界面

### 技术实现亮点
- **WiFi优先策略**: 优先检测WiFi连接，适应不同手机网络环境
- **智能降级**: 获取网络时间失败时自动使用本地时间
- **延迟处理**: 增加超时时间，应对网络延迟问题
- **权限管理**: 使用`ActivityResultContracts`处理复杂权限请求
- **异步处理**: Kotlin协程确保主线程安全

## 🧪 测试

项目包含完整的单元测试：
```bash
# 运行所有测试
./gradlew test

# 运行Android仪器测试
./gradlew connectedAndroidTest
```

## 🔄 自动构建

本项目使用GitHub Actions进行自动构建：
- ✅ 每次代码提交自动触发构建
- 📦 自动生成APK文件
- 🏷️ 自动创建GitHub Release
- 📊 构建状态实时反馈

## ⚠️ 重要提醒

1. **相册清理功能**会永久删除所有照片和视频，**无法恢复**
2. 首次使用需要授予"修改系统设置"权限
3. 建议在WiFi环境下使用时间同步功能
4. 应用专为三星S8优化，其他设备使用前请测试

## 📞 问题反馈

如果您在使用过程中遇到问题：
1. 🐛 [提交Issue](https://github.com/YOUR_USERNAME/TimezoneApp/issues)
2. 📧 发送邮件到：[您的邮箱]
3. 📱 联系开发者QQ/微信

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

---

**⭐ 如果这个项目对您有帮助，请给个Star支持一下！**