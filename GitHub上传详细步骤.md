# 📤 GitHub上传详细步骤指南

## 🎯 目标
将穿搭管理器项目上传到GitHub，自动构建APK文件

## 📋 准备工作

### 需要的文件清单
确保你有以下所有文件：
- ✅ `app/` 文件夹（包含所有源代码）
- ✅ `gradle/` 文件夹
- ✅ `.github/workflows/build-apk.yml`
- ✅ `build.gradle.kts`
- ✅ `settings.gradle.kts`
- ✅ `gradle.properties`
- ✅ `gradlew.bat`
- ✅ `README.md`
- ✅ 其他所有项目文件

---

## 🚀 第一步：创建GitHub账户

1. **访问GitHub官网**
   - 打开浏览器，访问：https://github.com

2. **注册新账户**
   - 点击右上角"Sign up"
   - 输入邮箱地址
   - 创建密码（建议使用强密码）
   - 输入用户名（建议：你的名字+数字）
   - 完成验证
   - 点击"Create account"

3. **验证邮箱**
   - 检查邮箱收件箱
   - 点击验证链接

---

## 📁 第二步：创建新仓库

1. **登录GitHub后**
   - 点击右上角"+"号
   - 选择"New repository"

2. **填写仓库信息**
   ```
   Repository name: wardrobe-manager
   Description: 穿搭管理器 - Android应用
   Public: ✅ 选择（免费用户必须选择Public）
   Add a README file: ❌ 不勾选（我们已经有了）
   Add .gitignore: ❌ 不勾选
   Choose a license: ❌ 暂时不选择
   ```

3. **创建仓库**
   - 点击"Create repository"
   - 记住仓库地址：`https://github.com/你的用户名/wardrobe-manager`

---

## 📤 第三步：上传项目文件

### 方法A：网页上传（推荐，简单易用）

1. **准备文件**
   - 选择项目根目录下的所有文件和文件夹
   - 可以分批上传，但建议一次性上传

2. **上传文件**
   - 在仓库页面点击"uploading an existing file"
   - 将所有文件拖拽到上传区域
   - 或点击"choose your files"选择文件

3. **重要文件确认**
   确保以下关键文件已上传：
   ```
   ✅ .github/workflows/build-apk.yml  （自动构建配置）
   ✅ app/build.gradle.kts             （应用构建配置）
   ✅ build.gradle.kts                 （项目构建配置）
   ✅ settings.gradle.kts              （项目设置）
   ✅ gradlew.bat                      （Gradle包装器）
   ✅ gradle/wrapper/                  （Gradle包装器文件）
   ```

4. **提交更改**
   ```
   Commit message: 初始提交 - 穿搭管理器应用
   Description: 包含完整的Android应用源代码和自动构建配置
   ```
   - 点击"Commit changes"

### 方法B：使用Git命令（如果你熟悉Git）

```bash
# 在项目根目录打开命令行
git init
git add .
git commit -m "初始提交 - 穿搭管理器应用"
git branch -M main
git remote add origin https://github.com/你的用户名/wardrobe-manager.git
git push -u origin main
```

---

## 🔨 第四步：触发自动构建

1. **检查Actions是否启用**
   - 上传完成后，点击仓库的"Actions"标签
   - 如果看到"Get started with GitHub Actions"，说明需要启用
   - 如果看到工作流列表，说明已启用

2. **查看构建状态**
   - 文件上传后，GitHub会自动开始构建
   - 在"Actions"页面可以看到构建进度
   - 🟡 黄色圆点：正在构建
   - ✅ 绿色勾号：构建成功
   - ❌ 红色叉号：构建失败

3. **构建时间**
   - 首次构建通常需要5-15分钟
   - 后续构建会更快（3-8分钟）

---

## 📱 第五步：下载APK文件

### 构建成功后：

1. **进入Actions页面**
   - 点击仓库的"Actions"标签
   - 找到最新的成功构建（绿色✅）

2. **下载APK**
   - 点击构建任务名称
   - 滚动到页面底部"Artifacts"部分
   - 下载以下文件：
     ```
     📱 wardrobe-manager-debug.zip    （测试版本，推荐）
     📱 wardrobe-manager-release.zip  （发布版本）
     ```

3. **解压文件**
   - 解压下载的zip文件
   - 得到`.apk`文件

---

## 📲 第六步：安装到手机

### Android手机设置：

1. **启用开发者选项**
   - 设置 → 关于手机 → 连续点击"版本号"7次

2. **允许安装未知应用**
   - 设置 → 安全 → 未知来源 → 开启
   - 或：设置 → 应用管理 → 特殊访问权限 → 安装未知应用

### 安装APK：

1. **传输文件**
   - 通过USB、蓝牙、或云盘将APK传到手机

2. **安装应用**
   - 使用文件管理器找到APK文件
   - 点击APK文件
   - 点击"安装"
   - 等待安装完成

3. **授予权限**
   - 首次打开应用时授予相机权限
   - 授予存储权限

---

## 🎉 完成！

安装成功后，你就可以使用穿搭管理器了！

### 应用功能：
- 📷 拍照添加衣服
- 👔 创建穿搭组合
- 🔍 搜索筛选衣服
- ⭐ 评分管理
- 📊 统计分析
- 💾 数据备份

---

## 🔧 常见问题解决

### 构建失败怎么办？
1. 检查所有文件是否完整上传
2. 查看Actions页面的错误日志
3. 确保`.github/workflows/build-apk.yml`文件存在

### 无法下载APK？
1. 确保构建显示绿色✅
2. 刷新页面重试
3. 检查网络连接

### 安装失败？
1. 确保Android版本7.0+
2. 检查存储空间
3. 重新下载APK文件

### 应用闪退？
1. 检查权限是否授予
2. 重启手机后重试
3. 卸载重新安装

---

## 📞 需要帮助？

如果遇到任何问题：
1. 仔细检查每个步骤
2. 确保网络连接稳定
3. 可以重新上传文件重试

**祝你使用愉快！** 🎊