# 穿搭管理器 - 安装指南

## 🚀 快速开始

### 选项1: 直接下载APK（推荐）

如果你只想使用应用，不需要修改代码：

1. **下载Java JDK**
   - 访问: https://adoptium.net/
   - 下载并安装Java 8或更高版本
   - 重启命令行/PowerShell

2. **构建APK**
   ```powershell
   # 在项目根目录运行
   build-apk.bat
   ```

3. **安装到手机**
   - APK文件位置: `app\build\outputs\apk\debug\app-debug.apk`
   - 传输到手机并安装

### 选项2: 使用GitHub Actions（无需本地Java）

1. **上传到GitHub**
   - 创建GitHub仓库
   - 上传所有项目文件
   - 推送到main分支

2. **自动构建**
   - GitHub会自动构建APK
   - 在Actions页面下载APK文件

## 📱 手机安装步骤

### Android手机设置
1. 打开"设置" → "安全"
2. 启用"未知来源"或"安装未知应用"
3. 允许从文件管理器安装应用

### 安装APK
1. 将APK文件传输到手机
2. 使用文件管理器找到APK文件
3. 点击安装
4. 授予必要权限（相机、存储）

## 🔧 故障排除

### Java相关问题
```powershell
# 检查Java是否安装
java -version

# 如果显示错误，需要安装Java JDK
# 下载地址: https://adoptium.net/
```

### 构建失败
```powershell
# 清理并重新构建
./gradlew clean
./gradlew assembleDebug
```

### 权限问题
- 确保授予相机权限（拍照功能）
- 确保授予存储权限（保存图片）

## 📞 需要帮助？

如果遇到问题：
1. 检查Java是否正确安装
2. 确保网络连接正常
3. 尝试重新下载项目文件
4. 查看错误信息并搜索解决方案

## 🎉 安装成功！

安装完成后，你可以：
- 📷 拍照添加衣服到衣橱
- 👔 创建穿搭组合
- 🔍 搜索和筛选衣服
- 📊 查看统计信息
- 💾 备份和恢复数据