@echo off
echo 正在构建穿搭管理器发布版APK...
echo.

REM 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到Java。请先安装Java JDK 8或更高版本。
    echo 下载地址：https://adoptium.net/
    pause
    exit /b 1
)

echo Java环境检查通过
echo.

REM 清理之前的构建
echo 清理之前的构建文件...
call gradlew clean

REM 构建Release版本APK
echo 开始构建Release APK...
call gradlew assembleRelease

if %errorlevel% equ 0 (
    echo.
    echo ✅ 发布版APK构建成功！
    echo 📱 APK文件位置：app\build\outputs\apk\release\app-release-unsigned.apk
    echo.
    echo ⚠️  注意：这是未签名的APK，如需发布到应用商店需要进行签名。
    echo 📖 签名指南：https://developer.android.com/studio/publish/app-signing
    pause
) else (
    echo.
    echo ❌ APK构建失败，请检查错误信息。
    pause
    exit /b 1
)