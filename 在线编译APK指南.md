# 在线编译APK详细指南

## 概述

本项目已经配置了GitHub Actions自动打包功能，可以在线编译成APK文件，无需安装任何开发环境。

## 快速开始

### 步骤1：访问GitHub仓库
打开浏览器访问：https://github.com/zuiweng518/android-word-learning-app

### 步骤2：查看自动打包状态
1. 点击仓库页面顶部的"Actions"标签
2. 查看最新的构建记录
3. 如果有正在运行的构建，等待其完成
4. 如果没有构建记录，代码会自动触发构建

### 步骤3：下载APK文件
1. 在Actions页面找到最新的构建记录
2. 点击构建记录查看详情
3. 滚动到页面底部
4. 在"Artifacts"部分找到以下文件：
   - `app-debug.apk`（调试版本，用于测试）
   - `app-release-unsigned.apk`（发布版本，未签名）
5. 点击文件名下载APK

### 步骤4：安装APK到手机
1. 将下载的APK文件传输到手机
2. 在手机上打开APK文件
3. 允许安装未知来源的应用
4. 点击安装按钮
5. 等待安装完成

## 详细说明

### GitHub Actions自动打包

项目已经配置了`.github/workflows/build-apk.yml`文件，会在以下情况自动触发：
- 代码推送到master分支
- 创建Pull Request到master分支
- 手动触发（通过Actions页面）

### 构建过程

GitHub Actions会自动执行以下步骤：
1. **检出代码**：从GitHub仓库获取最新代码
2. **设置JDK**：安装Java 11
3. **构建Debug APK**：使用Gradle构建调试版本
4. **构建Release APK**：使用Gradle构建发布版本
5. **上传APK**：将构建好的APK文件上传到Artifacts

### 构建时间

- **首次构建**：约10-15分钟（需要下载依赖）
- **后续构建**：约5-10分钟（使用缓存）

### APK文件说明

#### Debug APK (`app-debug.apk`)
- **用途**：用于测试和调试
- **特点**：
  - 包含调试信息
  - 未签名
  - 可以直接安装
  - 适合日常测试使用

#### Release APK (`app-release-unsigned.apk`)
- **用途**：用于正式发布
- **特点**：
  - 优化过的代码
  - 未签名（需要签名后才能发布到应用商店）
  - 体积更小
  - 性能更好

### APK签名（可选）

如果需要正式发布应用，需要对APK进行签名：

#### 方法1：使用Android Studio签名
1. 打开Android Studio
2. 打开项目
3. 选择Build → Generate Signed Bundle / APK
4. 选择APK，点击Next
5. 创建新的密钥库或选择现有密钥库
6. 填写密钥信息：
   - 密钥库密码
   - 密钥别名
   - 密钥密码
   - 有效期（至少25年）
   - 姓名和组织信息
7. 选择release构建类型
8. 点击Finish开始构建

#### 方法2：使用命令行签名
```bash
# 生成密钥库
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias

# 签名APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore my-release-key.jks app-release-unsigned.apk my-alias

# 对齐APK
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

## 常见问题

### 1. 构建失败怎么办？

**问题**：Actions页面显示构建失败

**解决方案**：
1. 点击失败的构建记录查看详细日志
2. 检查错误信息
3. 常见错误：
   - 依赖下载失败：等待几分钟后重试
   - 编译错误：检查代码语法
   - 超时错误：重试构建

### 2. APK无法安装怎么办？

**问题**：安装时提示"解析包时出现问题"

**解决方案**：
1. 确保APK文件完整下载
2. 尝试使用Debug版本
3. 检查手机Android版本（最低要求：Android 7.0）
4. 清空手机缓存后重试

### 3. 应用无法连接网络怎么办？

**问题**：应用无法访问智谱AI和邮箱服务

**解决方案**：
1. 确保手机有网络连接
2. 检查应用权限设置
3. 确认API密钥配置正确
4. 尝试在WiFi环境下使用

### 4. 如何查看构建日志？

**步骤**：
1. 打开失败的构建记录
2. 点击左侧的构建步骤
3. 查看每个步骤的详细日志
4. 找到错误信息并分析

### 5. 如何手动触发构建？

**步骤**：
1. 访问仓库的Actions页面
2. 点击"Run workflow"按钮
3. 选择分支（master）
4. 点击"Run workflow"开始构建

## 其他在线打包平台

如果GitHub Actions不可用，可以使用其他在线打包平台：

### 1. AppVeyor
- 网址：https://www.appveyor.com/
- 特点：
  - 支持多种构建配置
  - 提供详细的构建日志
  - 免费额度充足

### 2. CircleCI
- 网址：https://circleci.com/
- 特点：
  - 界面友好
  - 支持Docker
  - 集成GitHub

### 3. Travis CI
- 网址：https://travis-ci.org/
- 特点：
  - 开源项目免费
  - 配置简单
  - 支持多种语言

### 4. GitLab CI
- 网址：https://about.gitlab.com/
- 特点：
  - 集成GitLab
  - 支持Docker
  - 提供详细的构建日志

## 注意事项

1. **网络环境**：建议在稳定的网络环境下访问GitHub
2. **构建时间**：首次构建需要较长时间，请耐心等待
3. **APK大小**：Debug版本较大，Release版本较小
4. **签名问题**：未签名的APK可能无法在某些设备上安装
5. **版本兼容**：确保手机Android版本满足要求（最低7.0）
6. **存储空间**：确保手机有足够的存储空间安装应用

## 总结

使用GitHub Actions在线编译APK是最简单的方法：
- ✅ 无需安装任何开发环境
- ✅ 完全免费
- ✅ 自动化构建
- ✅ 稳定可靠
- ✅ 易于使用

按照本指南的步骤操作，您就可以轻松获得APK文件并安装到手机上使用。

## 技术支持

如果在打包过程中遇到问题，可以：
1. 查看GitHub Actions的详细日志
2. 检查项目的README文件
3. 参考Android官方文档：https://developer.android.com/
4. 寻求开发者社区的帮助

## 更新日志

### 最新更新
- 添加设置功能，支持用户自定义智谱AI API密钥
- 增强邮箱导入功能，支持自定义SMTP/POP3配置
- 更新README文档，添加在线编译指南

### 历史版本
- v1.0：初始版本，包含基本学习功能
- v1.1：添加智谱AI集成
- v1.2：添加错词本功能
- v1.3：添加复习功能
- v1.4：添加设置功能，支持自定义配置