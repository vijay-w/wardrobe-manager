@echo off
echo 手动设置Gradle环境...
echo.

REM 创建临时目录
mkdir temp_gradle 2>nul

echo 请手动下载以下文件：
echo 1. 访问: https://services.gradle.org/distributions/gradle-8.4-bin.zip
echo 2. 下载gradle-8.4-bin.zip到temp_gradle文件夹
echo 3. 下载完成后按任意键继续...
pause

REM 检查文件是否存在
if not exist "temp_gradle\gradle-8.4-bin.zip" (
    echo 错误：未找到gradle-8.4-bin.zip文件
    echo 请确保文件已下载到temp_gradle文件夹中
    pause
    exit /b 1
)

echo 正在解压Gradle...
powershell -command "Expand-Archive -Path 'temp_gradle\gradle-8.4-bin.zip' -DestinationPath 'temp_gradle' -Force"

echo 正在设置Gradle环境...
set GRADLE_HOME=%CD%\temp_gradle\gradle-8.4
set PATH=%GRADLE_HOME%\bin;%PATH%

echo 测试Gradle安装...
temp_gradle\gradle-8.4\bin\gradle --version

if %errorlevel% equ 0 (
    echo Gradle安装成功！
    echo 现在可以构建APK了...
    temp_gradle\gradle-8.4\bin\gradle assembleDebug
) else (
    echo Gradle安装失败
    pause
    exit /b 1
)

pause