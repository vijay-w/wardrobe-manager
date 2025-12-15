@echo off
echo 穿搭管理器 - 简化构建脚本
echo ================================
echo.

echo 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 错误：未找到Java
    echo 请先安装Java JDK
    pause
    exit /b 1
)
echo ✅ Java环境正常

echo.
echo 尝试构建APK...
echo 注意：首次运行需要下载依赖，可能需要较长时间

REM 尝试使用gradlew
echo 方法1：使用Gradle Wrapper...
./gradlew assembleDebug --no-daemon --offline 2>nul
if %errorlevel% equ 0 (
    echo ✅ 构建成功！
    goto :success
)

echo 方法1失败，尝试在线模式...
./gradlew assembleDebug --no-daemon
if %errorlevel% equ 0 (
    echo ✅ 构建成功！
    goto :success
)

echo.
echo ❌ 本地构建失败
echo.
echo 🔧 解决方案：
echo 1. 检查网络连接
echo 2. 使用VPN重试
echo 3. 使用GitHub Actions自动构建（推荐）
echo.
echo 📖 详细说明请查看：github-build-guide.md
echo.
pause
exit /b 1

:success
echo.
echo 🎉 APK构建成功！
echo 📱 文件位置：app\build\outputs\apk\debug\app-debug.apk
echo.
echo 📋 下一步：
echo 1. 将APK文件传输到Android设备
echo 2. 启用"未知来源"安装
echo 3. 点击APK文件进行安装
echo.
pause