
# 项目简介

`metersphere-ai-engine` 是一个用于实现多种AI聊天模型的引擎，提供了与不同AI聊天服务的集成，支持从多种聊天服务获取响应。它采用模块化设计，便于扩展和集成不同的AI聊天模型。

该项目的目的是提供一个可扩展的框架，支持不同聊天模型的接入，利用不同的AI接口实现聊天功能，并在过程中提供各种实用工具（如文本清理、关键词去重、日志记录等）。


## 项目结构

```
metersphere
└── ai
    └── engine
        ├── ChatToolEngine.java  # 主要的聊天工具引擎，处理聊天请求和响应
        ├── advisor
        │   └── LoggingAdvisor.java  # 日志记录顾问，用于记录和跟踪日志信息
        ├── common
        │   ├── AIChatClient.java  # AI聊天客户端的基础类，用于发送请求和接收响应
        │   ├── AIChatOptions.java  # 存储AI聊天选项的配置类
        │   ├── AIModelType.java  # 定义不同的AI模型类型
        │   └── AIRegister.java  # AI客户端的注册和管理类
        ├── holder
        │   └── ChatClientHolder.java  # 存储当前使用的聊天客户端实例
        ├── models
        │   ├── AIDeepSeekChatClient.java  # DeepSeek AI聊天客户端实现
        │   ├── AIOpenAIChatClient.java  # OpenAI聊天客户端实现
        │   ├── AIQianFanChatClient.java  # QianFan AI聊天客户端实现
        │   └── AIZhiPuAiChatClient.java  # ZhiPu AI聊天客户端实现
        ├── tools
        │   ├── DateTimeTool.java  # 日期和时间相关工具类
        │   ├── FileReaderTool.java  # 文件读取工具类
        │   ├── IntegrateTool.java  # 集成工具类，用于集成其他功能
        │   └── JvmTool.java  # JVM相关的工具类，提供JVM相关的信息
        └── utils
            ├── KeywordDeduplication.java  # 关键词去重工具类
            ├── TextCleaner.java  # 文本清理工具类，用于去除无关信息
            └── TextRankSummarizer.java  # 基于TextRank的文本摘要工具
```
## 项目结构说明

### 1. `ChatToolEngine.java`

* 作为项目的核心引擎，`ChatToolEngine` 负责处理所有的聊天请求、响应的发送与接收，并进行必要的错误处理、重试等操作。

### 2. `advisor/LoggingAdvisor.java`

* 该文件提供了一个日志顾问，能够记录聊天过程中的信息，包括请求、响应、错误信息等，帮助开发人员在调试和监控过程中更好地跟踪和分析数据。

### 3. `common/`

* **`AIChatClient.java`**: 定义了所有AI聊天客户端的基本接口，提供了请求发送、响应接收的功能。
* **`AIChatOptions.java`**: 用于存储和管理聊天过程中的各种配置选项，如接口地址、超时设置等。
* **`AIModelType.java`**: 该类定义了项目中支持的AI模型类型，便于根据不同的模型类型选择合适的客户端。
* **`AIRegister.java`**: 负责注册并管理各个AI聊天客户端实例，以便在不同场景中调用。

### 4. `holder/`

* **`ChatClientHolder.java`**: 该文件负责保存和管理当前正在使用的聊天客户端实例，确保在多个请求中复用相同的客户端，减少重复创建实例的开销。

### 5. `models/`

* **`AIDeepSeekChatClient.java`**: DeepSeek AI聊天客户端的实现，使用DeepSeek API进行对话。
* **`AIOpenAIChatClient.java`**: OpenAI聊天客户端的实现，使用OpenAI的API进行对话。
* **`AIQianFanChatClient.java`**: QianFan AI聊天客户端的实现。
* **`AIZhiPuAiChatClient.java`**: ZhiPu AI聊天客户端的实现。

### 6. `tools/`

* **`DateTimeTool.java`**: 提供与日期和时间相关的功能，如获取当前时间、格式化日期等。
* **`FileReaderTool.java`**: 用于读取文件内容，支持不同格式的文件读取。
* **`IntegrateTool.java`**: 用于将不同的功能和模块进行集成，提供统一的接口。
* **`JvmTool.java`**: 提供与JVM相关的工具和信息，如获取JVM的堆内存、线程信息等。

### 7. `utils/`

* **`KeywordDeduplication.java`**: 该工具负责在处理文本时去除重复的关键词，以确保最终结果更加简洁且无冗余信息。
* **`TextCleaner.java`**: 用于清理文本中的无关字符和信息（如HTML标签、特殊符号等）。
* **`TextRankSummarizer.java`**: 使用TextRank算法为输入文本生成摘要。

## 使用说明

   ```java
   ChatResponse response = ChatToolEngine.builder(AIModelType.OPEN_AI,
             AiChatOptions.builder()
                 .modelType("deepseek-chat")
                 .apiKey("sk-xxx")
                 .baseUrl("url")
                 .build())
                 .addPrompt("明天时间是多少")
                 .tools(new DateTimeTool())
                 .executeChatResponse();
   ``` 

---

## 提示词工程

提示词思路链（CoT）
思路链提示鼓励模型逐步推理问题，从而提高复杂推理任务的准确性。通过明确要求模型展示其工作成果或以逻辑步骤思考问题，您可以显著提高需要多步骤推理的任务的性能。

CoT 的工作原理是鼓励模型在得出最终答案之前生成中间推理步骤，类似于人类解决复杂问题的方式。这使得模型的思维过程更加清晰，并有助于其得出更准确的结论。
比如：
### 比如：
让我们一步步解决这个问题：

1. **确定过去年龄**：  
   当我3岁时，我的伙伴的年龄是我的三倍，即 3 * 3 = 9岁。

2. **计算年龄差**：  
   当时我的伙伴比我大 9 - 3 = 6岁。这个年龄差是固定的，因为我们都以相同的速度变老。

3. **当前年龄**：  
   现在我20岁。因为年龄差保持不变，我的伙伴的年龄是 20 + 6 = 26岁。

4. **验证**：  
   从我3岁到20岁，过去了 20 - 3 = 17年。  
   我的伙伴当时的年龄是9岁，17年后，9 + 17 = 26岁，与计算一致。

**答案**：我的伙伴现在26岁。

--- 