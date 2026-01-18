# 英语单词学习Android应用

这是一个功能完整的Android英语单词学习应用，使用H2数据库存储数据，集成智谱AI大模型提供智能学习功能。

## 功能特性

### 1. 单词导入
- 从邮箱读取Chrome插件发送的单词邮件
- 支持OAuth认证和密码认证两种方式
- 支持自定义SMTP/POP3服务器和端口
- 自动提取邮件中的单词并导入到本地数据库
- 支持批量导入和去重处理

### 2. 每日学习
- 每天自动从数据库中随机选择10个单词进行学习
- 提供单词发音、意思和例句（通过智谱AI获取）
- 支持语音合成播放单词和例句
- 支持录音练习发音
- 标记单词为已学习状态

### 3. 智能复习
- 每三天自动提醒复习
- 对学习过的单词进行全面检验
- 支持三种检验方式：
  - 发音检验：通过语音识别接受用户发音，AI判断准确性
  - 意思检验：手动输入单词意思，AI验证正确性
  - 造句检验：用单词造句，AI检查语法和用法

### 4. 错词本管理
- 自动记录检验错误的单词
- 显示错误类型（发音、意思、造句）
- 支持测试和移除功能
- 测试通过后自动从错词本移除

### 5. 智谱AI集成
- 支持用户自定义API密钥
- 获取单词的音标、中文意思和英文例句
- 智能判断用户发音准确性
- 验证单词意思的正确性
- 检查造句的语法和用法

### 6. 设置功能
- 智谱AI API密钥配置
- Gmail邮箱账户配置
- 支持OAuth认证和密码认证
- 自定义SMTP/POP3服务器和端口
- 邮箱密码安全存储

## 技术架构

### 数据库
- 使用H2嵌入式数据库
- 包含两个主要表：
  - words表：存储单词信息（单词、发音、意思、例句等）
  - mistake_words表：存储错误单词记录

### 服务层
- GmailService：处理Gmail API集成和邮件读取
- ZhipuAIService：与智谱AI API交互
- WordLearningService：管理每日学习功能和语音合成
- ReviewService：管理复习功能和检验逻辑
- MistakeBookService：管理错词本功能
- SpeechRecognitionService：处理语音识别

### 用户界面
- MainActivity：主界面，显示单词统计和功能入口
- WordLearningActivity：学习界面，显示单词详情和学习功能
- ReviewActivity：复习界面，提供三种检验方式
- MistakeBookActivity：错词本界面，管理和测试错误单词
- GmailImportActivity：Gmail导入界面，处理单词导入流程

## 使用说明

### 1. 配置智谱AI API
在`ZhipuAIService.java`中替换API密钥：
```java
private static final String API_KEY = "YOUR_API_KEY_HERE";
```

### 2. 配置Gmail API
- 在Google Cloud Console创建项目
- 启用Gmail API
- 创建OAuth 2.0客户端ID
- 在`AndroidManifest.xml`中配置必要的权限

### 3. 导入单词
- 点击主界面的"从Gmail导入单词"按钮
- 授权应用访问Gmail
- 应用会自动读取主题为"Word Learning List"的邮件
- 提取邮件中的单词并导入到数据库

### 4. 每日学习
- 点击"开始今日学习"按钮
- 查看单词详情（发音、意思、例句）
- 点击"播放发音"和"播放例句"听取语音
- 点击"录音练习发音"进行发音练习
- 学习完成后点击"标记为已学习"

### 5. 复习功能
- 每三天后点击"开始复习"按钮
- 对每个单词进行三种检验：
  - 发音检验：点击录音或输入发音，点击"检查发音"
  - 意思检验：输入单词意思，点击"检查意思"
  - 造句检验：用单词造句，点击"检查造句"
- 错误的单词会自动添加到错词本

### 6. 错词本管理
- 点击"查看错词本"按钮
- 选择一个单词进行测试
- 测试通过后单词会自动从错词本移除
- 可以点击"清空错词本"清空所有错误单词

## 权限说明

应用需要以下权限：
- INTERNET：访问网络（智谱AI API、Gmail API）
- RECORD_AUDIO：录音功能（发音练习）
- READ_EXTERNAL_STORAGE：存储权限（H2数据库）
- GET_ACCOUNTS：访问Google账户（Gmail API）

## 开发环境

- Android Studio
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Java 8

## 依赖库

- H2数据库：com.h2database:h2:2.2.224
- Gmail API：com.google.apis:google-api-services-gmail:v1-rev20240123-2.0.0
- OkHttp：com.squareup.okhttp3:okhttp:4.12.0
- Gson：com.google.code.gson:gson:2.10.1
- Material Components：com.google.android.material:material:1.11.0

## 注意事项

1. 首次使用需要授予应用相关权限
2. 需要有效的智谱AI API密钥
3. 需要配置Gmail API OAuth认证
4. 建议在WiFi环境下使用，避免消耗移动数据
5. 定期复习错词本中的单词，提高学习效果

## 项目结构

```
android-word-learning-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/wordlearning/app/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── WordLearningActivity.java
│   │   │   │   ├── ReviewActivity.java
│   │   │   │   ├── MistakeBookActivity.java
│   │   │   │   ├── GmailImportActivity.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── Word.java
│   │   │   │   │   └── MistakeWord.java
│   │   │   │   ├── database/
│   │   │   │   │   └── DatabaseHelper.java
│   │   │   │   └── service/
│   │   │   │       ├── GmailService.java
│   │   │   │       ├── ZhipuAIService.java
│   │   │   │       ├── WordLearningService.java
│   │   │   │       ├── ReviewService.java
│   │   │   │       ├── MistakeBookService.java
│   │   │   │       └── SpeechRecognitionService.java
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── activity_word_learning.xml
│   │   │   │   │   ├── activity_review.xml
│   │   │   │   │   ├── activity_mistake_book.xml
│   │   │   │   │   ├── activity_gmail_import.xml
│   │   │   │   │   └── item_mistake.xml
│   │   │   │   └── values/
│   │   │   │       ├── strings.xml
│   │   │   │       ├── colors.xml
│   │   │   │       └── themes.xml
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，请联系开发者。