# 穿搭管理器 (Wardrobe Manager)

一个现代化的Android穿搭管理应用，帮助用户管理衣橱、创建穿搭组合，并跟踪相关信息。

## ✨ 功能特性

- 📷 **衣服管理**: 拍照或从图库添加衣服，支持分类和评分
- 👔 **穿搭创建**: 组合多件衣服创建穿搭，保存喜爱的搭配
- 🔍 **智能搜索**: 按名称、分类、评分等条件搜索和筛选
- 📊 **统计分析**: 查看衣橱统计信息和使用频率
- 🛒 **购买管理**: 记录价格和购买链接信息
- 💾 **数据备份**: 完整的数据备份和恢复功能
- 🎨 **现代UI**: 基于Jetpack Compose的现代化用户界面

## 🏗️ 技术架构

- **架构模式**: MVVM (Model-View-ViewModel)
- **UI框架**: Jetpack Compose
- **数据库**: Room (SQLite)
- **图片处理**: Glide + Coil
- **相机**: CameraX
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow
- **导航**: Navigation Compose
- **测试**: Kotest (属性测试) + JUnit + MockK

## 📱 构建APK

### 方法1: 本地构建

#### 前置要求
- Java JDK 8或更高版本
- Android SDK (可选，推荐安装Android Studio)

#### 安装Java
1. 下载并安装Java JDK: https://adoptium.net/
2. 设置环境变量:
   ```bash
   # Windows
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-8.0.XXX-hotspot
   set PATH=%PATH%;%JAVA_HOME%\bin
   
   # macOS/Linux
   export JAVA_HOME=/path/to/jdk
   export PATH=$PATH:$JAVA_HOME/bin
   ```

#### 构建命令
```bash
# 构建Debug版本（用于测试）
./gradlew assembleDebug

# 构建Release版本（用于发布）
./gradlew assembleRelease

# 或者使用提供的批处理文件（Windows）
build-apk.bat          # Debug版本
build-release-apk.bat  # Release版本
```

#### APK文件位置
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`

### 方法2: GitHub Actions自动构建

1. 将代码推送到GitHub仓库
2. GitHub Actions会自动构建APK
3. 在Actions页面下载构建好的APK文件

## 📲 安装APK

### Android设备安装
1. 在设备上启用"未知来源"安装
   - 设置 → 安全 → 未知来源 (Android 7及以下)
   - 设置 → 应用和通知 → 特殊应用访问 → 安装未知应用 (Android 8+)

2. 将APK文件传输到设备
3. 点击APK文件进行安装

### 模拟器安装
```bash
# 使用adb安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🧪 运行测试

```bash
# 运行所有单元测试
./gradlew test

# 运行属性测试
./gradlew testDebugUnitTest

# 生成测试覆盖率报告
./gradlew testDebugUnitTestCoverage
```

## 📋 权限说明

应用需要以下权限：
- **相机权限**: 拍摄衣服照片
- **存储权限**: 保存和读取图片文件
- **网络权限**: 打开购买链接（可选）

## 🔧 开发环境设置

1. 安装Android Studio
2. 克隆项目: `git clone <repository-url>`
3. 在Android Studio中打开项目
4. 同步Gradle依赖
5. 运行应用或测试

## 📖 项目结构

```
app/src/main/java/com/wardrobemanager/
├── data/                 # 数据层
│   ├── database/        # Room数据库
│   ├── model/           # 数据模型
│   ├── repository/      # 数据仓库
│   └── backup/          # 备份管理
├── ui/                  # UI层
│   ├── wardrobe/        # 衣橱界面
│   ├── outfit/          # 穿搭界面
│   ├── addclothing/     # 添加衣服界面
│   ├── camera/          # 相机功能
│   ├── navigation/      # 导航组件
│   └── components/      # 通用UI组件
└── di/                  # 依赖注入模块
```

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支: `git checkout -b feature/new-feature`
3. 提交更改: `git commit -am 'Add new feature'`
4. 推送分支: `git push origin feature/new-feature`
5. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情。

## 🐛 问题反馈

如果遇到问题或有功能建议，请在GitHub Issues中提交。

## 📞 联系方式

- 项目地址: [GitHub Repository]
- 问题反馈: [GitHub Issues]

---

**注意**: 这是一个开源项目，仅供学习和个人使用。