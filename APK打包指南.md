# Android APK打包详细指南

## 方案一：GitHub Actions自动打包（推荐，最简单）

这是最简单的方法，不需要安装任何开发环境，只需使用GitHub网页即可完成。

### 步骤1：创建GitHub仓库

1. 访问 https://github.com/new
2. 仓库名称填写：`android-word-learning-app`
3. 选择公开或私有仓库
4. 点击"Create repository"按钮

### 步骤2：推送代码到GitHub

在项目目录下执行以下命令：

```powershell
# 进入项目目录
cd E:\stock\android-word-learning-app

# 添加所有文件到git
git add .

# 提交代码
git commit -m "Initial commit"

# 添加远程仓库（替换YOUR_USERNAME为你的GitHub用户名）
git remote add origin https://github.com/YOUR_USERNAME/android-word-learning-app.git

# 推送代码到GitHub
git push -u origin master
```

### 步骤3：触发自动打包

1. 代码推送到GitHub后，GitHub Actions会自动开始打包
2. 访问仓库的"Actions"标签页
3. 等待打包完成（大约5-10分钟）
4. 打包完成后，在Actions页面下载APK文件

### 步骤4：下载APK文件

1. 在Actions页面找到最新的构建记录
2. 点击构建记录
3. 在页面底部找到"Artifacts"部分
4. 下载以下文件：
   - `app-debug.apk`（调试版本，用于测试）
   - `app-release-unsigned.apk`（发布版本，未签名）

### 步骤5：安装APK到手机

1. 将APK文件传输到手机
2. 在手机上打开APK文件
3. 允许安装未知来源的应用
4. 点击安装按钮

---

## 方案二：使用在线打包平台

如果不想使用GitHub，可以使用在线打包平台。

### 1. AppVeyor

1. 访问 https://www.appveyor.com/
2. 注册账号并登录
3. 点击"New Project"
4. 选择GitHub并授权
5. 选择你的仓库
6. 配置构建脚本（自动检测Gradle项目）
7. 点击"Save"开始构建
8. 构建完成后下载APK文件

### 2. CircleCI

1. 访问 https://circleci.com/
2. 注册账号并登录
3. 点击"Add Projects"
4. 选择你的GitHub仓库
5. 配置构建脚本（自动检测Gradle项目）
6. 点击"Start Building"开始构建
7. 构建完成后下载APK文件

---

## 方案三：使用Android Studio（需要安装开发环境）

如果您愿意安装开发环境，可以使用Android Studio打包。

### 步骤1：安装Android Studio

1. 下载Android Studio：https://developer.android.com/studio
2. 安装Android Studio（需要约5GB空间）
3. 首次启动会下载SDK和工具（需要约2GB）

### 步骤2：打开项目

1. 启动Android Studio
2. 选择"Open an Existing Project"
3. 选择项目目录：`E:\stock\android-word-learning-app`
4. 等待Gradle同步完成

### 步骤3：构建Debug APK

1. 点击菜单：Build → Build Bundle(s) / APK(s) → Build APK(s)
2. 等待构建完成
3. 点击通知中的"locate"链接
4. 在`app/build/outputs/apk/debug/`目录找到APK文件

### 步骤4：构建Release APK

1. 点击菜单：Build → Generate Signed Bundle / APK
2. 选择"APK"，点击"Next"
3. 创建或选择密钥库文件
4. 填写密钥信息
5. 选择"release"构建类型
6. 点击"Finish"开始构建
7. 在`app/build/outputs/apk/release/`目录找到APK文件

---

## 方案四：使用命令行打包（需要JDK和Android SDK）

如果您有JDK和Android SDK，可以使用命令行打包。

### 步骤1：安装JDK

1. 下载JDK 11：https://adoptium.net/
2. 安装JDK
3. 配置JAVA_HOME环境变量

### 步骤2：安装Android SDK

1. 下载Android命令行工具：https://developer.android.com/studio#command-tools
2. 解压到指定目录
3. 配置ANDROID_HOME环境变量

### 步骤3：下载Gradle Wrapper

```powershell
# 在项目目录执行
cd E:\stock\android-word-learning-app

# 下载Gradle Wrapper（如果不存在）
# gradlew.bat文件已经创建，可以直接使用
```

### 步骤4：构建APK

```powershell
# 构建Debug版本
.\gradlew.bat assembleDebug

# 构建Release版本
.\gradlew.bat assembleRelease
```

### 步骤5：查找APK文件

- Debug版本：`app/build/outputs/apk/debug/app-debug.apk`
- Release版本：`app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 常见问题

### 1. GitHub Actions构建失败

**问题**：构建失败，显示错误信息

**解决方案**：
- 检查代码是否有语法错误
- 查看Actions日志，定位具体错误
- 确保所有依赖库版本正确

### 2. APK无法安装

**问题**：安装时提示"解析包时出现问题"

**解决方案**：
- 确保APK文件完整下载
- 尝试使用Debug版本
- 检查手机Android版本是否满足要求（最低Android 7.0）

### 3. 应用无法连接网络

**问题**：应用无法访问智谱AI和Gmail API

**解决方案**：
- 确保手机有网络连接
- 检查应用权限设置
- 确认API密钥配置正确

### 4. 语音功能不工作

**问题**：录音或语音合成功能不工作

**解决方案**：
- 检查应用是否获得录音权限
- 确认手机支持语音识别
- 检查TTS引擎是否安装

---

## 推荐方案

对于没有开发环境的用户，**强烈推荐使用方案一（GitHub Actions）**，原因如下：

1. **无需安装任何软件**：只需要浏览器和Git命令
2. **完全免费**：GitHub Actions提供免费额度
3. **自动化**：每次代码更新都会自动打包
4. **稳定可靠**：GitHub提供稳定的构建环境
5. **易于使用**：操作简单，只需几个步骤

---

## 下一步

1. 按照方案一的步骤创建GitHub仓库
2. 推送代码到GitHub
3. 等待自动打包完成
4. 下载APK文件并安装到手机
5. 配置智谱AI API密钥
6. 开始使用应用

---

## 技术支持

如果在打包过程中遇到问题，可以：

1. 查看GitHub Actions的详细日志
2. 检查项目的README文件
3. 参考Android官方文档：https://developer.android.com/
4. 寻求开发者社区的帮助

---

## 注意事项

1. **API密钥配置**：安装应用后，需要在代码中配置智谱AI API密钥
2. **Gmail API配置**：需要配置Gmail API OAuth认证
3. **权限授予**：首次使用需要授予应用相关权限
4. **网络环境**：建议在WiFi环境下使用
5. **定期更新**：定期检查更新，获取最新功能

---

## 文件清单

打包完成后，您将获得以下APK文件：

- `app-debug.apk`：调试版本，用于测试
- `app-release-unsigned.apk`：发布版本，未签名

**注意**：未签名的APK可能无法在某些设备上安装。如果需要正式发布，需要对APK进行签名。

---

## APK签名（可选）

如果您需要正式发布应用，需要对APK进行签名：

### 使用Android Studio签名

1. Build → Generate Signed Bundle / APK
2. 选择"APK"，点击"Next"
3. 创建新的密钥库或选择现有密钥库
4. 填写密钥信息：
   - 密钥库密码
   - 密钥别名
   - 密钥密码
   - 有效期（至少25年）
   - 姓名和组织信息
5. 选择"release"构建类型
6. 点击"Finish"

### 使用命令行签名

```powershell
# 生成密钥库
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias

# 签名APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.jks app-release-unsigned.apk my-alias

# 对齐APK
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

---

祝您打包成功！如有任何问题，请随时提问。