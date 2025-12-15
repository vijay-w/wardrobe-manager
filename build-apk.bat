@echo off
echo 正在构建穿搭管理器APK...
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

REM 构建Debug版本APK
echo 开始构建Debug APK...
call gradlew assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ✅ APK构建成功！
    echo 📱 APK文件位置：app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 你可以将此APK文件安装到Android设备上进行测试。
    pause
) else (
    echo.
    echo ❌ APK构建失败，请检查错误信息。
    pause
    exit /b 1
)