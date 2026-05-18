# GoalWall Android 开发方案与实施路线

## 项目定位

GoalWall 是一个：

- Android 原生目标管理 App
- 以 Widget 为核心体验
- 强调“每日目标可视化”
- 离线优先（Offline First）
- 极简、高频使用、自律导向

本项目的核心目标不是：

- 快速商业化
- 一次性开发完整产品
- 做成复杂任务管理系统

而是：

- 做一个真正自己愿意每天使用的工具
- 通过项目驱动学习 Android 开发
- 学习现代 Android 架构
- 学习 AI 协同开发方式
- 最终完成一个完整可运行的 Android App

---

# 一、项目最终目标

最终完成一个完整的 Android 原生 GoalWall App。

功能包括：

- 今日目标管理
- 目标状态切换
- 自动打卡
- 桌面 Widget 实时同步
- 夜间提醒通知
- 静态壁纸生成
- 连续打卡统计
- 深色模式

技术目标：

- 学会 Kotlin
- 学会 Jetpack Compose
- 学会 Room
- 学会 MVVM
- 学会 StateFlow
- 学会 Widget
- 学会 WorkManager
- 学会 AI 协同开发

---

# 二、开发周期建议

推荐周期：

- 6 ~ 8 周
- 每天 1~2 小时

开发原则：

- 小步快跑
- 逐步迭代
- 不追求一步到位
- 不一次性做完整产品

错误方式：

```text
云同步
登录系统
AI 功能
复杂统计
多端同步
高级主题
```

这些功能会导致复杂度迅速爆炸。

正确方式：

先完成 MVP。

---

# 三、技术栈（最终版）

## 1. 开发语言

### Kotlin

原因：

- Android 官方主推
- Compose 必须 Kotlin
- AI 对 Kotlin 支持最好
- 现代 Android 核心语言

---

## 2. UI 框架

### Jetpack Compose

原因：

- Android 官方现代 UI
- 比 XML 更简单
- 更适合 AI 生成
- 响应式 UI

---

## 3. 本地数据库

### Room

原因：

- Android 官方 ORM
- 简化 SQLite
- 支持 Flow
- AI 非常擅长生成 Room 代码

---

## 4. 状态管理

### ViewModel + StateFlow

原因：

- 官方推荐
- 适合 Compose
- 响应式状态管理
- 有利于 Widget 同步

---

## 5. 异步处理

### Kotlin Coroutines

原因：

- Android 现代异步标准
- 与 Flow 深度集成

---

## 6. Widget

### Glance + AppWidget

原因：

- Android 官方现代 Widget 方案
- Compose 风格开发 Widget

---

## 7. 后台任务

### WorkManager

原因：

- 定时提醒
- 自动跨天任务
- 后台可靠执行

---

## 8. 依赖注入

### Hilt

原因：

- 官方推荐
- 简化依赖管理
- AI 容易生成

---

# 四、工具链推荐

## 1. Android Studio

用途：

- 真机构建
- Gradle
- Logcat
- Compose Preview
- 调试
- Profiler

这是必须安装的核心开发工具。

---

## 2. Cursor

用途：

- AI 协同开发
- 多文件修改
- Agent 自动生成代码
- 快速重构
- 自动补全

推荐作为主要 AI Coding IDE。

---

## 3. Claude

用途：

- 架构设计
- Debug 分析
- 代码解释
- Prompt 生成
- 技术问题分析

推荐作为“架构与思考助手”。

---

## 4. GitHub

用途：

- 版本管理
- 代码备份
- 提交历史
- AI 上下文管理

推荐：

- GitHub Desktop（Git 新手）
- Git CLI（熟悉 Git 后）

---

# 五、AI 协同开发工作流（核心）

错误方式：

```text
一句 Prompt 生成整个 App
```

这是当前 AI Coding 最大误区。

正确方式：

---

## Step 1：Claude 生成架构

生成：

```text
Architecture.md
```

包括：

- 项目结构
- 数据流
- MVVM
- Widget 同步

---

## Step 2：Claude 拆分任务

生成：

```text
TASKS.md
```

例如：

```text
Task 1:
搭建 Compose 项目

Task 2:
实现 Goal Entity

Task 3:
实现 Room DAO

Task 4:
实现 HomeScreen
```

---

## Step 3：Cursor 编码

逐任务开发。

不要：

```text
帮我做完整 App
```

而是：

```text
帮我实现 Goal DAO
```

---

## Step 4：Android Studio 调试

负责：

- 真机运行
- Logcat
- 崩溃定位
- Widget 调试

---

## Step 5：Claude 分析问题

例如：

```text
为什么 Widget 不刷新？
```

让 Claude：

- 分析生命周期
- 分析 Flow
- 分析状态同步

---

# 六、项目开发阶段（重点）

---

# Phase 0：环境搭建（1~2天）

## 目标

完成：

- Android 环境安装
- Kotlin 基础
- Compose 基础认知

---

## 需要学习的 Kotlin

重点：

- data class
- class
- fun
- val / var
- null
- when
- coroutine 基础

不需要一次学太深。

---

## Compose 基础

只需要学习：

- Column
- Row
- LazyColumn
- Button
- Text
- remember
- mutableStateOf
- Scaffold

即可。

---

## 本阶段工具

### Android Studio

负责：

- 创建项目
- Compose Preview
- 运行 Demo

### Claude

用于解释：

```text
remember 是什么？
State 是什么？
Compose 为什么自动刷新？
```

---

## 本阶段产出

能够：

- 跑通 Compose Demo
- 理解 Compose UI 基础结构

---

# Phase 1：MVP 核心（第1周）

## 目标

实现：

# 今日目标列表

这是整个项目最核心部分。

---

## 功能

必须完成：

- 添加目标
- 删除目标
- 修改状态
- Compose UI

目标状态：

```text
0 = 未开始
1 = 进行中
2 = 已完成
```

---

## 技术栈

- Compose
- ViewModel
- mutableStateListOf

注意：

暂时不要接 Room。

原因：

新手同时学习：

- Compose
- Room
- Flow

复杂度会过高。

---

## 本阶段 AI 使用方式

### Cursor

生成：

```text
Compose Todo List 页面
```

### Claude

解释：

```text
mutableStateListOf 为什么会自动刷新 UI？
```

---

## 本阶段结束目标

实现：

```text
今日目标
[ ] 学 Kotlin
[进行中] 写 GoalWall
[✔] 俯卧撑
```

---

# Phase 2：数据库（第2周）

## 目标

接入：

# Room

---

## 功能

实现：

- Goal Entity
- DAO
- Repository
- Flow 查询

---

## 数据流

理解：

```text
Entity
→ DAO
→ Repository
→ ViewModel
→ UI
```

这是现代 Android 核心架构。

---

## 本阶段工具

### Cursor

生成：

- Entity
- DAO
- Repository

### Claude

解释：

```text
为什么 Room 返回 Flow？
```

---

## 本阶段结束目标

实现：

- App 重启后数据不丢失
- Goal 数据持久化

---

# Phase 3：架构升级（第3周）

## 目标

引入：

- MVVM
- StateFlow
- Hilt

---

## 功能

实现：

### ViewModel

管理：

- UIState
- Goal List
- 更新逻辑

---

## 学习重点

理解：

```text
UI
→ ViewModel
→ Repository
→ Room
→ Flow
→ UI
```

即：

# 单向数据流

这是未来最重要的 Android 架构思想。

---

## 本阶段结束目标

真正理解：

# 现代 Android 架构

---

# Phase 4：Widget（第4~5周）

# 这是整个项目最重要阶段

同时也是最难阶段。

---

## 目标

实现：

# 桌面 Widget

---

## Widget 功能

- 显示今日目标
- 显示目标状态
- 显示完成进度
- 显示日期

---

## 技术栈

- Glance
- AppWidget
- updateAll()

---

## 本阶段难点

你会遇到：

- Widget 不刷新
- 数据不同步
- 生命周期问题
- Widget 更新限制

这是正常现象。

---

## 本阶段 AI 使用方式（非常重要）

错误方式：

```text
帮我实现 Widget
```

正确方式：

```text
1. 创建 Glance Widget
2. 显示静态文本
3. 接入 Room
4. updateAll()
5. Widget 刷新
```

即：

# 拆小任务

---

## 本阶段结束目标

实现：

- 修改 App 中目标状态
- Widget 自动刷新同步

这是整个项目最大的里程碑。

---

# Phase 5：通知系统（第6周）

## 目标

实现：

- 每晚提醒
- 点击通知跳转

---

## 技术栈

- WorkManager
- NotificationManager
- PendingIntent

---

## 学习重点

理解：

# Android 后台任务限制

---

## 功能

通知内容：

```text
✍️ 编写明天的目标，让每一天更高效！
```

点击后：

跳转到：

```text
TomorrowGoalEditScreen
```

---

# Phase 6：自动打卡（第6周）

## 目标

实现：

```text
全部完成
→ 自动打卡
```

---

## 技术栈

- Repository
- Room Transaction

---

## 重点

理解：

# 业务逻辑层

不要把业务逻辑写进 UI。

---

# Phase 7：壁纸生成（第7周）

## 目标

实现：

# 静态壁纸生成

---

## 技术栈

- Canvas
- Paint
- Bitmap
- MediaStore

---

## 功能

用户点击：

```text
生成壁纸
```

App 自动：

- 将今日目标绘制成 Bitmap
- 保存到相册

---

## 学习重点

学习：

# Android Canvas 绘制

---

# Phase 8：优化阶段（第8周）

## UI 优化

- 动画
- 深色模式
- Widget 美化
- 圆角
- 间距

---

## 稳定性优化

考虑：

- 跨天
- 系统重启
- 时区变化
- Widget 多实例
- Android 13 通知权限

---

## 体验优化

- 一键复制昨日目标
- 完成动画
- 空状态页面

---

# 七、项目目录结构（推荐）

```text
app/
 ├── data/
 │    ├── local/
 │    ├── repository/
 │
 ├── domain/
 │
 ├── ui/
 │    ├── home/
 │    ├── stats/
 │    ├── settings/
 │
 ├── widget/
 │
 ├── notification/
 │
 ├── wallpaper/
 │
 ├── worker/
 │
 ├── di/
 │
 ├── utils/
```

---

# 八、数据模型（初版）

## Goal

字段：

```text
id
title
status
orderIndex
date
createdAt
updatedAt
```

---

## CheckinRecord

字段：

```text
id
date
allCompletedFlag
completedAt
```

---

# 九、状态同步链路（核心）

```text
UI
→ ViewModel
→ Repository
→ Room
→ Flow
→ ViewModel
→ WidgetSyncManager
→ updateAll()
→ Widget 刷新
```

这是整个项目最核心的数据流。

---

# 十、每阶段真正需要做的事

## 你负责：

- 理解逻辑
- 做决策
- Debug
- 控制复杂度
- 学习 Android 思维

---

## AI 负责：

- 样板代码
- Boilerplate
- 基础 UI
- Repository
- DAO
- 重构
- Kotlin 模板代码

---

# 十一、开发原则（非常重要）

## 1. 永远小步提交

每天：

```bash
git commit
```

---

## 2. 一次只解决一个问题

不要同时处理：

```text
Widget 不刷新
+ Room 报错
+ Compose 崩溃
```

---

## 3. 先能跑，再优化

不要一开始：

- Clean Architecture 极致化
- 多模块
- 复杂设计模式
- 极限性能优化

---

## 4. Widget 是整个项目核心

GoalWall 最大价值：

# Widget 持续可视化

因此：

优先保证：

- Widget 稳定
- Widget 美观
- Widget 刷新及时

---

# 十二、重要里程碑（Milestone）

## Milestone 1

Compose Todo 可运行

---

## Milestone 2

Room 数据持久化成功

---

## Milestone 3

MVVM + StateFlow 跑通

---

## Milestone 4（核心）

Widget 同步成功

---

## Milestone 5

通知 + 自动打卡完成

---

## Milestone 6

壁纸生成完成

---

# 十三、未来扩展方向

暂时不要做，但未来可以扩展：

## 1. 云同步

例如：

- Firebase
- Supabase

---

## 2. 多设备同步

- 平板
- Web
- Desktop

---

## 3. 锁屏 Widget

未来 Android 版本可能增强锁屏 Widget。

---

## 4. WearOS

手表显示今日目标。

---

## 5. AI 辅助目标规划

例如：

```text
根据昨日完成情况推荐今日目标
```

但这些都不是 MVP 必须内容。

---

# 十四、最终建议（最重要）

不要把它想成：

```text
我要开发一个完整商业 App
```

而要想：

```text
我要做一个自己每天都会看到的小工具
```

这样：

- 压力更小
- 更容易坚持
- 更容易真正做完

而：

# 真正完整做完一个 Android App

本身就已经是非常有价值的成长。

