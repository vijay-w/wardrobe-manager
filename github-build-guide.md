# GitHub自动构建APK指南

## 🚀 步骤1：上传代码到GitHub

1. **创建GitHub账户**（如果没有）
   - 访问：https://github.com
   - 注册新账户

2. **创建新仓库**
   - 点击"New repository"
   - 仓库名：`wardrobe-manager`
   - 设为Public（免费用户）
   - 点击"Create repository"

## 📤 步骤2：上传项目文件

### 方法A：使用GitHub网页界面
1. 在仓库页面点击"uploading an existing file"
2. 将所有项目文件拖拽到页面上
3. 写提交信息："Initial commit - Wardrobe Manager App"
4. 点击"Commit changes"

### 方法B：使用Git命令（如果安装了Git）
```bash
git init
git add .
git commit -m "Initial commit - Wardrobe Manager App"
git branch -M main
git remote add origin https://github.com/你的用户名/wardrobe-manager.git
git push -u origin main
```

## 🔨 步骤3：自动构建APK

1. **触发构建**
   - 代码上传后，GitHub Actions会自动开始构建
   - 在仓库页面点击"Actions"标签查看进度

2. **等待构建完成**
   - 构建通常需要5-10分钟
   - 绿色✅表示成功，红色❌表示失败

3. **下载APK**
   - 构建成功后，在Actions页面找到最新的构建
   - 点击构建任务
   - 在"Artifacts"部分下载APK文件

## 📱 步骤4：安装APK

1. **下载的文件**
   - `wardrobe-manager-debug.zip` - 测试版本
   - `wardrobe-manager-release.zip` - 发布版本

2. **解压并安装**
   - 解压zip文件得到.apk文件
   - 传输到Android设备
   - 启用"未知来源"安装
   - 点击APK文件安装

## 🎉 完成！

安装成功后，你就可以使用穿搭管理器应用了！

## 🔧 故障排除

### 构建失败
- 检查Actions页面的错误日志
- 确保所有文件都已正确上传
- 检查文件结构是否完整

### 无法下载APK
- 确保构建已成功完成（绿色✅）
- 刷新页面重试
- 检查网络连接

### 安装失败
- 确保Android版本兼容（Android 7.0+）
- 检查存储空间是否充足
- 重新下载APK文件