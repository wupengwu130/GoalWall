# GoalWall — AI 协同开发任务清单 (tasks.md)

> 基于 `docs/Architecture.md` v1.0 生成
> 本文档是 GoalWall 项目的**唯一开发执行依据**
> Cursor/Claude 必须严格按 Task 顺序执行，禁止跳跃、合并、自由发挥

---

## 目录

1. [Project Overview](#1-project-overview)
2. [Development Principles](#2-development-principles)
3. [AI Execution Rules](#3-ai-execution-rules)
4. [Development Phases](#4-development-phases)
5. [Task Definition](#5-task-definition)
6. [Definition of Done](#6-definition-of-done)
7. [AI Collaboration Workflow](#7-ai-collaboration-workflow)
8. [Git Workflow](#8-git-workflow)
9. [Risk Management](#9-risk-management)

---

## 1. Project Overview

### 项目目标

GoalWall 是一款 Android 原生目标管理应用，帮助用户设定、追踪、完成个人目标。  
核心功能：目标创建与进度追踪、里程碑管理、主屏幕 Widget、每日提醒通知。

### 技术栈

| 类别 | 技术 | 版本 |
|---|---|---|
| 语言 | Kotlin | 2.0+ |
| UI | Jetpack Compose + Material3 | BOM 2024.09.00 |
| 导航 | Navigation Compose | 2.8.0 |
| 本地数据库 | Room | 2.6.1 |
| 依赖注入 | Hilt | 2.51.1 |
| 后台任务 | WorkManager | 2.9.1 |
| 桌面组件 | Glance AppWidget | 1.1.0 |
| 用户偏好 | DataStore Preferences | 1.1.1 |
| 图片加载 | Coil | 2.7.0 |
| 状态管理 | StateFlow + Channel | Kotlin Coroutines |

### 模块结构（包结构，非 Gradle 多模块）

```
com.goalwall/
├── data/
│   ├── db/          (entity + dao)
│   ├── model/       (UI Model)
│   └── repository/  (数据聚合)
├── ui/
│   ├── navigation/
│   ├── theme/
│   ├── goal/
│   ├── dashboard/
│   └── settings/
├── worker/
├── widget/
├── notification/
└── di/
```

### 架构风格

- **单 Activity + Compose Navigation**：MainActivity 是唯一 Activity，NavHost 管理所有页面
- **MVVM**：Screen → ViewModel → Repository → DAO → Room
- **单向数据流**：UI 只读 StateFlow，写操作通过 ViewModel 函数触发
- **Widget 异步同步**：WorkManager + Glance，不直接监听 Flow

### AI 协同开发原则

1. 每个 Task 在一次 Cursor 会话内完成，不跨会话
2. 每次执行前，AI 必须阅读 `docs/Architecture.md` 确认约束
3. AI 修改代码后必须输出：修改文件列表、风险提示、验收结果
4. 遇到架构不明确处，停止执行，向开发者提问，不得擅自决策
5. 禁止在任何 Task 中引入 Architecture.md 未定义的依赖

### 开发约束

- 禁止引入 UseCase 层
- 禁止 ViewModel 直接 import Room Entity 或 DAO
- 禁止 UI 层直接访问 Repository 或 DAO
- 禁止 Gradle 多模块拆分
- 禁止提前引入 Paging3、MVI、Proto DataStore
- 所有 UiState 必须是 immutable data class

---

## 2. Development Principles

### 2.1 单向依赖原则

```
UI → ViewModel → Repository → DAO → Room
              ↑
          (不可反向)
```

- UI 层（Screen）只依赖 ViewModel，不直接持有 Repository 引用
- ViewModel 只依赖 Repository，不 import 任何 Room 类
- Repository 只依赖 DAO 和 Model，不依赖 ViewModel 或 UI
- DAO 只操作 Entity，不返回 Model

### 2.2 模块边界原则

- 每个包下的类只暴露**必要的公共接口**，内部实现细节设为 `private` 或 `internal`
- 跨包引用只允许从上层指向下层（见依赖方向）
- `data/db/entity/` 中的 Entity 类禁止在 `ui/` 包下出现
- `data/model/` 中的 Model 类是 UI 与 Repository 之间的唯一契约

### 2.3 架构风格原则（轻量 MVVM）

- 不引入 Clean Architecture 的 Domain 层
- 不创建 UseCase 类
- 业务逻辑（过滤、排序、校验）放在 ViewModel，不放在 Repository
- Repository 只做数据聚合与 Entity ↔ Model 映射

### 2.4 状态管理原则

- ViewModel 持有 `MutableStateFlow<XxxUiState>`，对外暴露 `StateFlow<XxxUiState>`
- 一次性事件（导航、Toast、Snackbar）使用 `Channel<XxxEvent>` 发出，Screen 用 `LaunchedEffect` 消费
- Screen 使用 `collectAsStateWithLifecycle()` 收集 StateFlow，不使用 `collectAsState()`
- UiState 更新只能通过 `_uiState.update { it.copy(...) }` 进行

### 2.5 Repository 原则

- Repository 是 `@Singleton`，通过 Hilt **构造器注入**（`@Inject constructor`）直接提供
- **禁止** `interface + Impl + @Binds` 双层结构（单模块单实现下属于过度设计，违反 §10 YAGNI；远端数据源扩展点在 Repository **内部**新增 source，不另起类）
- **禁止注入或持有 `CoroutineDispatcher`**（含 `@IoDispatcher` 等 Qualifier）。Room 的 `suspend fun` / `Flow` 已自动派发至内部 IO 池，DataStore 同样自带 IO 调度；Repository 内**不得**出现 `withContext(IO)` —— 线程切换由数据源内部保证
- 读操作:返回 `Flow<T>`（Room 自动感知变更）
- 写操作：`suspend fun`，在 `viewModelScope` 协程中调用
- Entity → Model 映射：在 Repository 内部通过扩展函数 `toModel()` 完成
- Repository 不接受 ViewModel 或 UI 相关参数

### 2.6 错误处理原则

- ViewModel 捕获 Repository 抛出的异常，更新 UiState 的 `errorMessage` 字段
- Repository 不捕获 DAO 异常，让其向上传播
- 不使用 `Result<T>` 包装类（项目规模不需要）
- 错误信息使用字符串资源（`strings.xml`），不硬编码

### 2.7 日志原则

- 使用 `android.util.Log`，Tag 统一为 `"GoalWall"`
- 仅在 Debug Build 下输出日志（`BuildConfig.DEBUG` 判断）
- 禁止在 Release Build 中打印用户数据

### 2.8 测试原则

- 每个 Repository 必须有对应的单元测试
- 每个 ViewModel 必须有对应的单元测试（使用 `TestCoroutineDispatcher`）
- UI 测试仅覆盖关键交互路径
- 测试使用 `in-memory Room database`，不 Mock DAO

### 2.9 CI/CD 原则

- 每次 Push 触发：编译检查 + Lint + 单元测试
- PR 合并前必须通过全部 CI 检查
- Release Build 使用 GitHub Actions 自动签名打包

---

## 3. AI Execution Rules

### 3.1 AI 允许做什么

- 在当前 Task 范围内创建新文件
- 在当前 Task 范围内修改已有文件的指定部分
- 在 Task 定义的文件列表内进行重构
- 添加单元测试
- 输出风险提示和验收检查结果
- 提问以澄清架构不明确处

### 3.2 AI 禁止做什么

| 禁止行为 | 原因 |
|---|---|
| 修改 `docs/Architecture.md` | 架构文档是唯一真相，不得在执行中修改 |
| 跨 Task 一次性实现多个功能 | 破坏原子性，难以验收和回滚 |
| 引入 Architecture.md 未定义的第三方库 | 防止依赖污染 |
| 在 ViewModel 中 import Room Entity/DAO | 违反分层约束 |
| 在 Screen 中直接调用 Repository | 违反分层约束 |
| 创建 UseCase 类 | 禁止 UseCase 层 |
| 修改公共接口签名（不在当前 Task 范围内）| 破坏下游依赖 |
| 一次修改超过 5 个文件 | 超出单次会话可验收范围 |
| 硬编码字符串、颜色、尺寸 | 违反资源管理规范 |
| 使用 `collectAsState()` 替代 `collectAsStateWithLifecycle()` | 违反生命周期规范 |
| 在 Repository 中写业务逻辑 | 违反 Repository 原则 |

### 3.3 Cursor 执行规范

执行每个 Task 前，Cursor 必须按以下顺序操作：

```
Step 1: 阅读当前 Task 的 Goal / Dependencies / Files 字段
Step 2: 确认依赖 Task 已完成（检查对应文件是否存在）
Step 3: 阅读 docs/Architecture.md 中与本 Task 相关的章节
Step 4: 按 Steps 字段逐步实现，不跳步
Step 5: 完成后执行 Acceptance Criteria 中的每一项检查
Step 6: 输出执行报告（见 3.5）
```

### 3.4 Claude 执行规范

Claude 在 Review 阶段必须检查：

```
□ 文件依赖方向是否正确（UI → VM → Repo → DAO）
□ UiState 是否为 immutable data class
□ ViewModel 是否未引入 Room Entity
□ Repository 是否无业务逻辑
□ 事件处理是否通过 Channel（非 StateFlow）
□ 是否使用 collectAsStateWithLifecycle
□ 是否有硬编码字符串
□ 新增依赖是否在 Architecture.md 已声明
```

### 3.5 每次 Task 输出格式（AI 必须输出）

```markdown
## Task X.Y 执行报告

### 修改文件
- `path/to/FileA.kt` — 新建，说明
- `path/to/FileB.kt` — 修改，说明变更内容

### 验收结果
- [x] 编译通过
- [x] 分层约束未违反
- [x] UiState immutable
- [ ] ⚠️ 未完成项（附说明）

### 风险
- 风险描述（如有）

### TODO（遗留给下一 Task）
- 待完成事项
```

### 3.6 防止架构漂移规则

- **每个 Task 开始前**：AI 重新读取 Architecture.md 的相关章节，不依赖上下文记忆
- **每个 Phase 结束时**：人工检查所有文件的 import 语句，确认无跨层引用
- **发现架构偏离时**：立即停止当前 Task，提交 `fix/arch-drift-xxx` 分支修复，不继续下一 Task
- **新文件创建时**：文件头部注释必须标注所属包层和职责（格式见附录）

---

## 4. Development Phases

```
Phase 1: Project Bootstrap        ← 工程脚手架
    │
    ▼
Phase 2: Core Infrastructure      ← DI + DB + 工具基础设施
    │
    ▼
Phase 3: Data Layer               ← Entity + DAO + Repository + Model
    │
    ▼
Phase 4: UI Foundation            ← Theme + Navigation + MainActivity
    │
    ▼
Phase 5: Goal Feature             ← 目标核心功能（列表/详情/编辑）
    │
    ▼
Phase 6: Dashboard & Settings     ← 汇总视图 + 设置页
    │
    ▼
Phase 7: Worker & Widget          ← WorkManager + Glance Widget
    │
    ▼
Phase 8: Notification             ← 通知渠道 + 提醒系统
    │
    ▼
Phase 9: Testing                  ← 单元测试 + UI 测试
    │
    ▼
Phase 10: Release                 ← CI/CD + 打包 + 发布
```

> **铁律**：禁止在前序 Phase 未完成的情况下启动后续 Phase。

---

## 5. Task Definition

---

### Phase 1 — Project Bootstrap

**目标**：建立工程骨架，确保编译通过，工具链就绪。

---

#### Task 1.1 — 初始化 Android 工程

**Goal**

使用 Android Studio 创建空白 Kotlin + Compose 工程，配置 Application ID、包名、最低 SDK。

**Modules**

根工程

**Dependencies**

无

**Input**

- 包名：`com.goalwall`
- Application ID：`com.goalwall`
- Min SDK：26（Android 8.0）
- Target SDK：35
- 语言：Kotlin
- UI：Jetpack Compose（空白 Activity 模板）

**Output**

- 可编译通过的空白工程
- 初始 `MainActivity.kt`（待后续重写）

**Files**

- `build.gradle.kts`（root）
- `app/build.gradle.kts`
- `settings.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/goalwall/MainActivity.kt`

**Steps**

1. 创建 New Project → Empty Activity（Compose 模板）
2. 配置 `applicationId = "com.goalwall"`
3. 配置 `minSdk = 26`，`targetSdk = 35`，`compileSdk = 35`
4. 确认 Kotlin 版本为 2.0+
5. 执行 `./gradlew assembleDebug`，确认编译通过

**Acceptance Criteria**

- [ ] `./gradlew assembleDebug` 编译成功，无报错
- [ ] 包名为 `com.goalwall`
- [ ] 工程结构符合 Android Studio 标准模板

**Risks**

- AGP 版本与 Kotlin 版本不兼容（确认使用 AGP 8.5+）

**Notes**

- 不要修改 MainActivity 内容，Task 4.1 负责重写

---

#### Task 1.2 — 配置 Version Catalog

**Goal**

将所有依赖版本统一迁移至 `gradle/libs.versions.toml`，确保版本管理中心化。

**Modules**

根工程构建配置

**Dependencies**

Task 1.1

**Input**

Architecture.md 附录中的依赖配置参考

**Output**

`libs.versions.toml` 文件，包含所有项目依赖版本定义

**Files**

- `gradle/libs.versions.toml`（新建）
- `build.gradle.kts`（root，修改）
- `app/build.gradle.kts`（修改，使用 `libs.xxx` 引用）

**Steps**

1. 在 `gradle/` 目录下创建 `libs.versions.toml`
2. 按以下分类填写版本：
   - `[versions]`：kotlin、agp、compose-bom、room、hilt、navigation、work、glance、datastore、coil、lifecycle
   - `[libraries]`：所有 dependencies 条目
   - `[plugins]`：kotlin-android、ksp、hilt
3. 修改 `app/build.gradle.kts`，将所有 `implementation("...")` 替换为 `implementation(libs.xxx)` 形式
4. 执行 `./gradlew assembleDebug` 确认编译通过

**Acceptance Criteria**

- [ ] `libs.versions.toml` 包含全部 Architecture.md 声明的依赖
- [ ] `app/build.gradle.kts` 无硬编码版本号字符串
- [ ] `./gradlew assembleDebug` 编译通过

**Risks**

- 版本冲突：Compose BOM 与 Navigation 版本不兼容（严格按 Architecture.md 版本填写）

**Notes**

- 不要自行升级 Architecture.md 未声明的依赖版本

---

#### Task 1.3 — 添加全部依赖声明

**Goal**

在 `app/build.gradle.kts` 中声明项目所需全部依赖（Compose、Room、Hilt、WorkManager、Glance、DataStore、Coil），启用 KSP 插件。

**Modules**

`app/build.gradle.kts`

**Dependencies**

Task 1.2

**Input**

`gradle/libs.versions.toml`（Task 1.2 产出）

**Output**

完整配置的 `app/build.gradle.kts`，`./gradlew assembleDebug` 编译通过

**Files**

- `app/build.gradle.kts`（修改）

**Steps**

1. 在 `plugins` 块中添加：`alias(libs.plugins.ksp)`、`alias(libs.plugins.hilt)`
2. 在 `dependencies` 块中添加全部依赖（分组注释）：
   - Compose 组（compose-bom、ui、material3、tooling、lifecycle-runtime-compose）
   - Navigation 组（navigation-compose）
   - Room 组（runtime、ktx、ksp compiler）
   - Hilt 组（hilt-android、hilt-compiler ksp、hilt-navigation-compose、hilt-work）
   - WorkManager 组（work-runtime-ktx）
   - Glance 组（glance-appwidget、glance-material3）
   - DataStore 组（datastore-preferences）
   - Coil 组（coil-compose）
3. 配置 `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`
4. 启用 `buildFeatures { compose = true }`
5. 执行 `./gradlew assembleDebug`

**Acceptance Criteria**

- [ ] 所有依赖正确解析，无版本冲突
- [ ] KSP 插件正常工作
- [ ] Room schema 导出目录已配置
- [ ] `./gradlew assembleDebug` 无警告（除 KSP 信息性日志）

**Risks**

- Hilt + KSP 版本不匹配导致编译失败（严格按 Architecture.md 版本）

**Notes**

- Room schema 需添加到 `.gitignore` 白名单（提交 schema 便于迁移验证）

---

#### Task 1.4 — 配置 ktlint 与 detekt

**Goal**

添加代码风格检查（ktlint）和静态分析（detekt），建立代码质量基线。

**Modules**

根工程

**Dependencies**

Task 1.3

**Input**

无

**Output**

- ktlint 可执行（`./gradlew ktlintCheck`）
- detekt 可执行（`./gradlew detekt`）
- 基础配置文件

**Files**

- `build.gradle.kts`（root，添加 plugin）
- `gradle/libs.versions.toml`（添加 ktlint、detekt 版本）
- `detekt.yml`（新建，根目录）
- `.editorconfig`（新建）

**Steps**

1. 在 `libs.versions.toml` 中添加 `ktlint = "12.x"` 和 `detekt = "1.x"`
2. 在 root `build.gradle.kts` 中添加 ktlint 和 detekt 插件
3. 创建 `detekt.yml`，配置以下规则：
   - 禁用 `TooManyFunctions`（Repository 函数较多）
   - 禁用 `LongParameterList`（ViewModel 构造器注入多个 Repository）
   - 启用 `ForbiddenImport`：禁止导入 `androidx.room.*` 在 `ui/` 包下
4. 创建 `.editorconfig`：indent_size=4，end_of_line=lf，charset=utf-8
5. 执行 `./gradlew ktlintCheck` 和 `./gradlew detekt`

**Acceptance Criteria**

- [ ] `./gradlew ktlintCheck` 通过（或仅报告 auto-fixable 问题）
- [ ] `./gradlew detekt` 通过
- [ ] detekt 规则已配置 ForbiddenImport 保护分层边界

**Risks**

- ktlint 规则与已有代码风格冲突（初期可先运行 `ktlintFormat` 自动修复）

**Notes**

- detekt 的 `ForbiddenImport` 规则是防止 ViewModel 直接引用 Room Entity 的技术保障

---

#### Task 1.5 — 配置 GitHub Actions CI

**Goal**

建立 CI 流水线，每次 Push 和 PR 自动执行编译、Lint、单元测试。

**Modules**

CI 配置

**Dependencies**

Task 1.4

**Input**

无

**Output**

`.github/workflows/ci.yml`

**Files**

- `.github/workflows/ci.yml`（新建）

**Steps**

1. 创建 `.github/workflows/ci.yml`
2. 配置触发条件：`push` 到 `main`/`develop`，`pull_request` 到 `main`
3. 配置 Job `build-and-test`：
   - 使用 `ubuntu-latest`
   - 设置 JDK 17（`actions/setup-java@v4`）
   - 缓存 Gradle（`actions/cache@v4`）
   - 执行步骤：
     1. `./gradlew ktlintCheck`
     2. `./gradlew detekt`
     3. `./gradlew assembleDebug`
     4. `./gradlew testDebugUnitTest`
4. 配置失败时上传测试报告（`actions/upload-artifact`）

**Acceptance Criteria**

- [ ] Push 到 GitHub 后 CI 自动触发
- [ ] CI 包含 lint + detekt + build + test 四步
- [ ] CI 失败时输出可读的错误报告

**Risks**

- GitHub Actions 分钟数限制（免费账号 2000 分钟/月，注意缓存 Gradle 节省时间）

**Notes**

- 暂不配置 Release 打包（Phase 10 处理）

---

### Phase 2 — Core Infrastructure

**目标**：建立项目的基础设施：Application 类、Hilt 注入框架、Room 数据库容器、通知渠道初始化。

---

#### Task 2.1 — 创建 GoalWallApp（Application 类）

**Goal**

创建 Application 入口类，完成 Hilt 注解、通知渠道初始化、WorkManager 调度入口。

**Modules**

`com.goalwall`

**Dependencies**

Task 1.3（依赖已声明）

**Input**

Architecture.md §9（通知系统）、§4（Widget 同步方案中 App 启动触发）

**Output**

`GoalWallApp.kt`，`AndroidManifest.xml` 注册

**Files**

- `app/src/main/java/com/goalwall/GoalWallApp.kt`（新建）
- `app/src/main/AndroidManifest.xml`（修改，添加 `android:name`）

**Steps**

1. 创建 `GoalWallApp.kt`：
   - 继承 `Application()`
   - 添加 `@HiltAndroidApp` 注解
   - 在 `onCreate()` 中调用 `createNotificationChannels()`（占位，Task 8.1 实现）
2. 在 `AndroidManifest.xml` 的 `<application>` 标签添加 `android:name=".GoalWallApp"`
3. 在 `AndroidManifest.xml` 中添加权限声明（占位）：
   - `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`
   - `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />`

**Acceptance Criteria**

- [ ] `@HiltAndroidApp` 注解存在
- [ ] `AndroidManifest.xml` 已注册 Application 类
- [ ] 权限已声明
- [ ] `./gradlew assembleDebug` 编译通过

**Risks**

- 忘记在 Manifest 注册 Application 类导致 Hilt 初始化失败

**Notes**

- `createNotificationChannels()` 暂时留为空函数体，Task 8.1 补充实现

---

#### Task 2.2 — 创建 DatabaseModule（Hilt DI）

**Goal**

创建 Hilt Module，提供 Room Database 和 DAO 实例的依赖注入绑定。

**Modules**

`di/`

**Dependencies**

Task 2.1

**Input**

Architecture.md §6（Room 数据结构）中的 Database 定义

**Output**

`DatabaseModule.kt`（包含 Database + DAO 提供）

**Files**

- `app/src/main/java/com/goalwall/di/DatabaseModule.kt`（新建）

**Steps**

1. 创建 `DatabaseModule.kt`：
   ```
   @Module
   @InstallIn(SingletonComponent::class)
   object DatabaseModule
   ```
2. 提供 `GoalWallDatabase`（`@Singleton`，`@Provides`）：
   - 使用 `Room.databaseBuilder`
   - 数据库名：`"goalwall_database"`
   - 后续迁移在此注册
3. 提供各 DAO（`@Singleton`，`@Provides`）：
   - `provideGoalDao(db): GoalDao`
   - `provideMilestoneDao(db): MilestoneDao`
   - `provideProgressDao(db): ProgressDao`
4. 注意：Database 和 DAO 类此时可能尚未存在，使用占位 TODO 注释，Phase 3 实现

**Acceptance Criteria**

- [ ] `@Module @InstallIn(SingletonComponent::class)` 注解正确
- [ ] 所有 `@Provides` 函数有 `@Singleton` 注解
- [ ] 文件编译通过（即使 DAO 类是空占位）

**Risks**

- Phase 3 创建 DAO 后需回来取消占位注释

**Notes**

- 此 Task 创建模块骨架，Phase 3 才填充真正的 Entity 和 DAO

---

#### Task 2.3 — 创建 RepositoryModule（Hilt DI）

**Goal**

创建 Hilt Module，绑定 Repository 的单例提供。

**Modules**

`di/`

**Dependencies**

Task 2.2

**Input**

Architecture.md §5（Repository 设计）

**Output**

`RepositoryModule.kt`

**Files**

- `app/src/main/java/com/goalwall/di/RepositoryModule.kt`（新建）

**Steps**

1. 创建 `RepositoryModule.kt`：
   ```
   @Module
   @InstallIn(SingletonComponent::class)
   object RepositoryModule
   ```
2. 提供 `GoalRepository`（`@Singleton`，`@Provides`）
3. 提供 `ProgressRepository`（`@Singleton`，`@Provides`）
4. 提供 `UserPreferences`（`@Singleton`，`@Provides`，Phase 6 实现）
5. 占位注释标记 Phase 3/6 需补充实现

**Acceptance Criteria**

- [ ] Module 结构正确
- [ ] 编译通过（Repository 类为空占位时）

**Risks**

- 无

**Notes**

- 若 Repository 使用 `@Inject constructor`，无需 `@Provides`，可将此 Module 简化为空

---

#### Task 2.4 — 创建 WorkerModule（Hilt DI）

**Goal**

配置 HiltWorkerFactory，使 WorkManager 支持 Hilt 依赖注入。

**Modules**

`di/`

**Dependencies**

Task 2.1

**Input**

Architecture.md §4（WidgetSyncWorker）、§9（ReminderWorker）

**Output**

`WorkerModule.kt`，`GoalWallApp.kt` 集成 WorkManager Hilt 配置

**Files**

- `app/src/main/java/com/goalwall/di/WorkerModule.kt`（新建）
- `app/src/main/java/com/goalwall/GoalWallApp.kt`（修改）

**Steps**

1. 创建 `WorkerModule.kt`，提供 `HiltWorkerFactory`（按 hilt-work 官方文档）
2. 修改 `GoalWallApp.kt`，实现 `Configuration.Provider` 接口：
   ```kotlin
   override val workManagerConfiguration: Configuration
       get() = Configuration.Builder()
           .setWorkerFactory(workerFactory)
           .build()
   ```
3. 在 `AndroidManifest.xml` 中为 WorkManager 添加 Provider 的 `tools:node="remove"` 配置（防止默认初始化冲突）

**Acceptance Criteria**

- [ ] `GoalWallApp` 实现 `Configuration.Provider`
- [ ] WorkManager 使用 `HiltWorkerFactory`
- [ ] Manifest 中 WorkManager 默认 Provider 已禁用
- [ ] `./gradlew assembleDebug` 编译通过

**Risks**

- 忘记在 Manifest 禁用 WorkManager 默认初始化，导致运行时崩溃

**Notes**

- 这是 Hilt + WorkManager 集成的必要配置，必须在 Worker 实现前完成

---

### Phase 3 — Data Layer

**目标**：实现完整的数据层：Entity、DAO、Database、Model、Repository。这是整个项目的数据基础，必须在 UI 开发前完成。

---

#### Task 3.1 — 创建 Room Entity（3 个）

**Goal**

创建 `GoalEntity`、`MilestoneEntity`、`ProgressEntity` 三个 Room Entity 类。

**Modules**

`data/db/entity/`

**Dependencies**

Task 2.2

**Input**

Architecture.md §6（Room 数据结构）

**Output**

三个 Entity 文件

**Files**

- `data/db/entity/GoalEntity.kt`（新建）
- `data/db/entity/MilestoneEntity.kt`（新建）
- `data/db/entity/ProgressEntity.kt`（新建）

**Steps**

1. 创建 `data/model/GoalStatus.kt`（枚举：`ACTIVE, COMPLETED, PAUSED, ARCHIVED`）
2. 创建 `data/db/converter/GoalStatusConverter.kt`（`@TypeConverter` 字符串 ↔ 枚举），并在 `GoalWallDatabase` 类上添加 `@TypeConverters(GoalStatusConverter::class)`
3. 创建 `GoalEntity.kt`（严格按 Architecture.md §6 字段定义）：
   - 字段：id、title、description、**targetValue、currentValue、unit、startDate、targetDate、status**、color、createdAt、updatedAt
   - 注解：`@Entity(tableName = "goals", indices = [Index("title")])`，`@PrimaryKey(autoGenerate = true)`
   - **Entity 不持久化 `progress` 字段**（派生属性在 Model 层计算）
4. 创建 `MilestoneEntity.kt`：
   - 字段：id、goalId、title、**targetValue、completed**、createdAt
   - 注解：`@Entity`，`@ForeignKey`（CASCADE DELETE）、`@Index("goalId")`
5. 创建 `ProgressEntity.kt`：
   - 字段：id、goalId、**value: Int**、note、**recordDate**、createdAt
   - 注解：`@Entity`，`@ForeignKey`（CASCADE DELETE）、`@Index("goalId")`
6. 验证字段类型与 Architecture.md §6 完全一致

**Acceptance Criteria**

- [ ] 三个 Entity 字段与 Architecture.md §6 完全一致（含计数 Int 模型与 GoalStatus 枚举）
- [ ] `GoalStatusConverter` 已注册到 Database
- [ ] ForeignKey CASCADE DELETE 已配置
- [ ] Index 已配置（goalId / title）
- [ ] `./gradlew assembleDebug` 编译通过

**Risks**

- ForeignKey 配置错误导致运行时数据一致性问题

**Notes**

- Entity 字段不允许超出 Architecture.md 定义，扩展字段走 Migration（Phase 9 之后）

---

#### Task 3.2 — 创建 GoalWallDatabase

**Goal**

创建 Room Database 类，注册所有 Entity 和 DAO。

**Modules**

`data/db/`

**Dependencies**

Task 3.1

**Input**

Architecture.md §6（Database 定义）

**Output**

`GoalWallDatabase.kt`

**Files**

- `data/db/GoalWallDatabase.kt`（新建）
- `di/DatabaseModule.kt`（修改，取消占位注释，连接真实 Database）

**Steps**

1. 创建 `GoalWallDatabase.kt`：
   - `@Database(entities = [GoalEntity::class, MilestoneEntity::class, ProgressEntity::class], version = 1, exportSchema = true)`
   - 继承 `RoomDatabase()`
   - 声明三个抽象 DAO 函数
2. 更新 `DatabaseModule.kt`，在 `providesDatabase` 中使用真实的 `GoalWallDatabase::class`
3. 确认 schema 导出路径配置正确（`app/schemas/`）

**Acceptance Criteria**

- [ ] Database 版本为 1
- [ ] `exportSchema = true`
- [ ] `./gradlew assembleDebug` 后 `app/schemas/` 目录有 schema JSON 文件
- [ ] 编译通过

**Risks**

- 忘记设置 `exportSchema = true` 导致迁移时无法验证

**Notes**

- 生成的 schema JSON 文件需提交到 Git（作为迁移验证依据）

---

#### Task 3.3 — 创建 GoalDao

**Goal**

实现 `GoalDao` 接口，包含 Architecture.md 定义的全部 DAO 方法。

**Modules**

`data/db/dao/`

**Dependencies**

Task 3.2

**Input**

Architecture.md §6（DAO 示例、GoalWithMilestones Relation）

**Output**

`GoalDao.kt`

**Files**

- `data/db/dao/GoalDao.kt`（新建）
- `data/db/dao/GoalWithMilestones.kt`（新建，Relation 聚合对象）

**Steps**

1. 创建 `GoalWithMilestones.kt`（`@Embedded` + `@Relation`）
2. 创建 `GoalDao.kt`，实现以下方法：
   - `observeAll(): Flow<List<GoalEntity>>`（`WHERE status != 'ARCHIVED'`，按 updatedAt 倒序）
   - `observeWithMilestones(goalId: Long): Flow<GoalWithMilestones?>`（`@Transaction`）
   - `getTopByProgress(limit: Int): List<GoalEntity>`（suspend，按 `currentValue/targetValue` 派生进度排序，**CASE WHEN 做除零保护**，Widget 用）
   - `insert(entity: GoalEntity): Long`（suspend，`OnConflict.REPLACE`）
   - `update(entity: GoalEntity)`（suspend，`@Update`）
   - `deleteById(id: Long)`（suspend，`@Query DELETE`）
   - `updateCurrentValue(id: Long, currentValue: Int, now: Long)`（suspend，`@Query UPDATE`）
   - `updateStatus(id: Long, status: GoalStatus, now: Long)`（suspend，`@Query UPDATE`）
3. 更新 `DatabaseModule.kt` 中 `provideGoalDao` 取消占位

**Acceptance Criteria**

- [ ] 所有方法签名与 Architecture.md §6 一致
- [ ] Flow 方法无 suspend 修饰符
- [ ] 写方法均有 suspend 修饰符
- [ ] `@Transaction` 用于 `observeWithMilestones`
- [ ] 编译通过

**Risks**

- `@Transaction` 遗漏导致 Relation 查询在多次 SQL 间数据不一致

**Notes**

- `getTopByProgress` 是 Widget 专用，直接返回 List，非 Flow

---

#### Task 3.4 — 创建 MilestoneDao 和 ProgressDao

**Goal**

实现 `MilestoneDao` 和 `ProgressDao`。

**Modules**

`data/db/dao/`

**Dependencies**

Task 3.3

**Input**

Architecture.md §5（Repository 中调用的 DAO 方法）

**Output**

`MilestoneDao.kt`，`ProgressDao.kt`

**Files**

- `data/db/dao/MilestoneDao.kt`（新建）
- `data/db/dao/ProgressDao.kt`（新建）

**Steps**

1. 创建 `MilestoneDao.kt`：
   - `observeByGoal(goalId: Long): Flow<List<MilestoneEntity>>`
   - `insert(entity: MilestoneEntity): Long`（suspend）
   - `setCompleted(id: Long, completed: Boolean)`（suspend，`@Query UPDATE`）
   - `deleteByGoalId(goalId: Long)`（suspend，因 CASCADE 通常不需要，但保留作备用）
2. 创建 `ProgressDao.kt`：
   - `observeByGoal(goalId: Long): Flow<List<ProgressEntity>>`（按 `recordDate` 倒序）
   - `insert(entity: ProgressEntity): Long`（suspend）
   - `sumValueByGoal(goalId: Long): Int`（suspend，可选，Dashboard 计算累计值用）
3. 更新 `DatabaseModule.kt` 中对应占位

**Acceptance Criteria**

- [ ] 两个 DAO 方法签名正确
- [ ] `@Dao` 注解存在
- [ ] 编译通过

**Risks**

- 无

**Notes**

- 无需实现超过 Repository 调用的方法，保持最小接口

---

#### Task 3.5 — 创建 UI Model 类

**Goal**

创建 `Goal`、`Milestone`、`GoalDetail`、`ProgressRecord` 四个 UI Model 类，及 Entity → Model 扩展函数。

**Modules**

`data/model/`

**Dependencies**

Task 3.1

**Input**

Architecture.md §5（Repository 中的 `toModel()` 调用）、§6（Entity 字段）

**Output**

4 个 Model 文件 + Mapper 扩展函数

**Files**

- `data/model/Goal.kt`（新建）
- `data/model/Milestone.kt`（新建）
- `data/model/GoalDetail.kt`（新建）
- `data/model/ProgressRecord.kt`（新建）
- `data/model/Mappers.kt`（新建，扩展函数）

**Steps**

1. 创建 `Goal.kt`（data class，字段与 GoalEntity 对应，去除 Room 注解）：
   - 字段：id、title、description、**targetValue、currentValue、unit、startDate、targetDate、status**、color、createdAt、updatedAt
   - **派生只读属性**：`val progress: Float get() = if (targetValue == 0) 0f else (currentValue.toFloat() / targetValue).coerceIn(0f, 1f)`
   - 添加 `GoalFilter` enum（ACTIVE、ARCHIVED、ALL；可选 COMPLETED、PAUSED）
2. 创建 `Milestone.kt`（id、goalId、title、**targetValue、completed**、createdAt）
3. 创建 `GoalDetail.kt`（包含 `goal: Goal` 和 `milestones: List<Milestone>`）
4. 创建 `ProgressRecord.kt`（id、goalId、**value: Int**、note、**recordDate**、createdAt）
5. 创建 `Mappers.kt`，实现扩展函数：
   - `GoalEntity.toModel(): Goal`
   - `MilestoneEntity.toModel(): Milestone`
   - `GoalWithMilestones.toDetail(): GoalDetail`
   - `ProgressEntity.toModel(): ProgressRecord`
   - `Goal.toEntity(): GoalEntity`（写回用）

**Acceptance Criteria**

- [ ] 所有 Model 类为 data class，无 Room 注解
- [ ] Mapper 扩展函数在 `data/model/Mappers.kt`，不在 Entity 文件中
- [ ] Model 类不 import 任何 Room 或 Entity 类（ Mappers 文件除外）
- [ ] 编译通过

**Risks**

- Model 字段与 Entity 字段不同步（字段变更时需同时更新两处）

**Notes**

- `Mappers.kt` 是唯一允许同时 import Entity 和 Model 的文件

---

#### Task 3.6 — 创建 GoalRepository

**Goal**

实现 `GoalRepository`，包含所有读写操作。

**Modules**

`data/repository/`

**Dependencies**

Task 3.3、Task 3.5

**Input**

Architecture.md §5（GoalRepository 完整设计）

**Output**

`GoalRepository.kt`

**Files**

- `data/repository/GoalRepository.kt`（新建 / 替换原 interface 占位为 `@Singleton class`）
- `data/repository/impl/GoalRepositoryImpl.kt`（**删除**）
- `di/RepositoryModule.kt`（**删除** `bindGoalRepository` 行；若 Module 中无其他 `@Binds`，整体删除该 Module 文件）

**Steps**

1. 创建 `GoalRepository.kt`（**单类实现，不抽 interface**）：
   - `@Singleton class GoalRepository @Inject constructor(goalDao: GoalDao, milestoneDao: MilestoneDao)`
   - **禁止**注入 `CoroutineDispatcher`（Room 已自带 IO 调度，违反将触发 Code Review 拒收）
   - 实现读操作：
     - `val goals: Flow<List<Goal>>`（`goalDao.observeAll().map { ... toModel() }`）
     - `fun observeGoalDetail(goalId: Long): Flow<GoalDetail?>`
   - 实现写操作（均为 suspend fun）：
     - `addGoal(title, targetValue, unit, startDate, targetDate, description, color): Long`
     - `updateGoal(goal: Goal)`
     - `deleteGoal(goalId: Long)`
     - `updateCurrentValue(goalId: Long, currentValue: Int)`
     - `setStatus(goalId: Long, status: GoalStatus)`
     - `addMilestone(goalId: Long, title: String, targetValue: Int): Long`
     - `toggleMilestone(milestoneId: Long, completed: Boolean)`
2. 确认所有写操作不在 Repository 内部启动协程（由 ViewModel 的 viewModelScope 管理）
3. 确认 Repository 内部**无** `withContext(...)` 调用、**无** `Dispatchers.IO` 引用

**Acceptance Criteria**

- [ ] Repository 为单类（无 interface + Impl 拆分）
- [ ] Repository 不 import 任何 ViewModel 或 UI 类
- [ ] Repository 不 import `kotlinx.coroutines.Dispatchers` / 不持有 `CoroutineDispatcher` 参数
- [ ] Flow 属性而非函数（`val goals: Flow<List<Goal>>`）
- [ ] 无业务逻辑判断（if/when 业务条件）
- [ ] `data/repository/impl/GoalRepositoryImpl.kt` 已删除
- [ ] `RepositoryModule` 中 `bindGoalRepository` 已删除
- [ ] 编译通过

**Risks**

- 误在 Repository 中启动协程（应由 ViewModel 的 viewModelScope 负责）

**Notes**

- Repository 不捕获异常，异常向上传播到 ViewModel

---

#### Task 3.7 — 创建 ProgressRepository

**Goal**

实现 `ProgressRepository`，包含进度历史读写操作。

**Modules**

`data/repository/`

**Dependencies**

Task 3.4、Task 3.5

**Input**

Architecture.md §5（ProgressRepository 设计）

**Output**

`ProgressRepository.kt`

**Files**

- `data/repository/ProgressRepository.kt`（新建 / 替换原 interface 占位为 `@Singleton class`）
- `data/repository/impl/ProgressRepositoryImpl.kt`（**删除**）
- `di/RepositoryModule.kt`（**删除** `bindProgressRepository` 行）

**Steps**

1. 创建 `ProgressRepository.kt`（**单类实现，不抽 interface**）：
   - `@Singleton class ProgressRepository @Inject constructor(progressDao: ProgressDao)`
   - **禁止**注入 `CoroutineDispatcher`
   - `fun observeProgressHistory(goalId: Long): Flow<List<ProgressRecord>>`
   - `suspend fun recordProgress(goalId: Long, value: Int, note: String? = null)`

**Acceptance Criteria**

- [ ] Repository 为单类（无 interface + Impl 拆分）
- [ ] 不持有 `CoroutineDispatcher`
- [ ] 与 GoalRepository 结构一致
- [ ] `data/repository/impl/ProgressRepositoryImpl.kt` 已删除
- [ ] 编译通过

**Risks**

- 无

**Notes**

- 无

---

#### Task 3.8 — 创建 WidgetDataProvider

**Goal**

实现 `WidgetDataProvider`，用于 Widget 的一次性数据查询（不走 Repository Flow）。

**Modules**

`widget/`

**Dependencies**

Task 3.3、Task 3.5

**Input**

Architecture.md §4（WidgetDataProvider 设计）

**Output**

`WidgetDataProvider.kt`

**Files**

- `widget/WidgetDataProvider.kt`（新建）

**Steps**

1. 创建 `WidgetDataProvider.kt`：
   - `@Inject constructor(private val goalDao: GoalDao)`（注意：直接依赖 DAO，非 Repository）
   - **禁止**注入 `CoroutineDispatcher`
   - `suspend fun getTopGoals(limit: Int = 3): List<Goal>`
   - 内部调用 `goalDao.getTopByProgress(limit).map { it.toModel() }`（DAO 端已用 `currentValue/targetValue` 派生进度排序并做除零保护）

**Acceptance Criteria**

- [ ] 直接依赖 GoalDao（非 GoalRepository），符合 Architecture.md §4
- [ ] 无 Flow 返回，只有 suspend fun
- [ ] 编译通过

**Risks**

- 无

**Notes**

- Widget 数据提供者绕过 Repository 是架构设计决策（轻量、无响应式需求）

---

### Phase 4 — UI Foundation

**目标**：建立 UI 基础设施：Theme、Navigation、MainActivity。

---

#### Task 4.1 — 创建 Theme（Color / Type / Theme）

**Goal**

实现 GoalWall 的 Material3 Design System，包含颜色、字体、主题配置。

**Modules**

`ui/theme/`

**Dependencies**

Task 1.3

**Input**

Material3 设计规范，Architecture.md §10（深色主题预留）

**Output**

三个 Theme 文件

**Files**

- `ui/theme/Color.kt`（新建）
- `ui/theme/Type.kt`（新建）
- `ui/theme/Theme.kt`（新建）

**Steps**

1. 创建 `Color.kt`：定义 GoalWall 品牌色系（主色、辅助色、背景色、错误色）
2. 创建 `Type.kt`：配置 Material3 `Typography`（使用系统字体，无外部字体依赖）
3. 创建 `Theme.kt`：
   - 定义 `GoalWallTheme` composable 函数
   - 支持 `darkTheme` 参数（`isSystemInDarkTheme()` 默认）
   - 使用 `MaterialTheme` 包装 content

**Acceptance Criteria**

- [ ] `GoalWallTheme` 为可组合函数
- [ ] 支持深色/浅色模式切换
- [ ] 编译通过

**Risks**

- 无

**Notes**

- 颜色值可暂时使用 Material3 默认色，后续 UI 迭代调整

---

#### Task 4.2 — 创建 Screen 路由定义

**Goal**

定义应用的所有导航路由（sealed class Screen）。

**Modules**

`ui/navigation/`

**Dependencies**

Task 4.1

**Input**

Architecture.md §1（目录结构中的页面列表）

**Output**

`Screen.kt`

**Files**

- `ui/navigation/Screen.kt`（新建）

**Steps**

1. 创建 `Screen.kt`，定义 sealed class/interface：
   ```
   sealed class Screen(val route: String) {
       object GoalList : Screen("goal_list")
       object Dashboard : Screen("dashboard")
       object Settings : Screen("settings")
       data class GoalDetail(val goalId: Long) : Screen("goal_detail/{goalId}")
       data class GoalEdit(val goalId: Long? = null) : Screen("goal_edit?goalId={goalId}")
   }
   ```
2. 为带参数的路由添加工具函数（`createRoute(goalId)`）

**Acceptance Criteria**

- [ ] 所有页面路由已定义
- [ ] 参数路由格式正确（Navigation Compose 规范）
- [ ] 编译通过

**Risks**

- 路由字符串拼写错误导致运行时导航崩溃

**Notes**

- GoalEdit 支持 goalId 为 null（新建模式）和有值（编辑模式）

---

#### Task 4.3 — 创建 AppNavHost

**Goal**

实现 NavHost 路由图，注册所有 Screen 和导航跳转逻辑。

**Modules**

`ui/navigation/`

**Dependencies**

Task 4.2

**Input**

Architecture.md §1（所有页面）

**Output**

`AppNavHost.kt`

**Files**

- `ui/navigation/AppNavHost.kt`（新建）

**Steps**

1. 创建 `AppNavHost.kt`：
   - 接受 `navController: NavHostController` 和 `modifier: Modifier` 参数
   - `startDestination = Screen.GoalList.route`
   - 注册所有 composable 路由（内容暂时为占位 `Text("TODO: xxx")`）：
     - `Screen.GoalList.route` → 占位
     - `Screen.Dashboard.route` → 占位
     - `Screen.Settings.route` → 占位
     - `Screen.GoalDetail.route` → 占位（`navBackStackEntry.arguments?.getLong("goalId")`）
     - `Screen.GoalEdit.route` → 占位
2. 为每个路由配置 `arguments`（参数类型声明）

**Acceptance Criteria**

- [ ] NavHost 正确配置 startDestination
- [ ] 所有路由已注册（内容可为占位）
- [ ] 参数路由的 `navArgument` 类型声明正确
- [ ] 编译通过

**Risks**

- 参数类型声明错误（Long vs String）导致运行时崩溃

**Notes**

- 占位内容 Phase 5/6 逐步替换为真实 Screen

---

#### Task 4.4 — 重写 MainActivity

**Goal**

将 MainActivity 改为单 Activity 架构，承载 NavHost。

**Modules**

`com.goalwall`

**Dependencies**

Task 4.3

**Input**

Architecture.md §1（单 Activity 架构）

**Output**

重写后的 `MainActivity.kt`

**Files**

- `app/src/main/java/com/goalwall/MainActivity.kt`（修改）

**Steps**

1. 重写 `MainActivity.kt`：
   - 添加 `@AndroidEntryPoint` 注解
   - `setContent { GoalWallTheme { val navController = rememberNavController(); AppNavHost(navController) } }`
2. 添加底部导航栏（BottomNavigationBar）：
   - 3 个 Tab：Goals（GoalList）、Dashboard、Settings
   - 使用 `NavigationBar` + `NavigationBarItem`（Material3）
3. 使用 `Scaffold` 包裹 NavHost 和 BottomBar

**Acceptance Criteria**

- [ ] `@AndroidEntryPoint` 注解存在
- [ ] 使用 `GoalWallTheme` 包裹
- [ ] `NavController` 由 `rememberNavController()` 创建
- [ ] 底部导航栏有 3 个 Tab
- [ ] App 可启动，显示底部导航栏和占位页面
- [ ] `./gradlew assembleDebug` 编译通过

**Risks**

- Scaffold 与 NavHost 的 padding 处理（使用 `innerPadding` 传递）

**Notes**

- 这是第一次可以在真机/模拟器上运行看到效果的 Task

---

### Phase 5 — Goal Feature

**目标**：实现目标管理核心功能：列表、详情、编辑三个页面的完整 MVVM 实现。

---

#### Task 5.1 — 创建 GoalListUiState 和 GoalListEvent

**Goal**

定义目标列表页的状态模型和事件模型。

**Modules**

`ui/goal/`

**Dependencies**

Task 3.5

**Input**

Architecture.md §8（状态管理方案）

**Output**

`GoalListUiState.kt`（含 UiState + Event）

**Files**

- `ui/goal/GoalListUiState.kt`（新建）

**Steps**

1. 创建 `GoalListUiState.kt`：
   ```kotlin
   data class GoalListUiState(
       val goals: List<Goal> = emptyList(),
       val isLoading: Boolean = true,
       val errorMessage: String? = null,
       val filterType: GoalFilter = GoalFilter.ACTIVE
   )
   sealed class GoalListEvent {
       data class ShowSnackbar(val message: String) : GoalListEvent()
       data class NavigateToDetail(val goalId: Long) : GoalListEvent()
       object NavigateToCreate : GoalListEvent()
   }
   ```

**Acceptance Criteria**

- [ ] UiState 为 immutable data class（无 var 字段）
- [ ] Event 为 sealed class
- [ ] 编译通过

**Risks**

- 无

**Notes**

- GoalFilter 已在 Task 3.5 的 `Goal.kt` 中定义

---

#### Task 5.2 — 创建 GoalListViewModel

**Goal**

实现目标列表页的 ViewModel，包含目标观察、过滤、删除、归档操作。

**Modules**

`ui/goal/`

**Dependencies**

Task 5.1、Task 3.6

**Input**

Architecture.md §7（GoalListViewModel 设计）

**Output**

`GoalListViewModel.kt`

**Files**

- `ui/goal/GoalListViewModel.kt`（新建）

**Steps**

1. 创建 `GoalListViewModel.kt`：
   - `@HiltViewModel`，`@Inject constructor(goalRepository: GoalRepository, @ApplicationContext context: Context)`
   - 声明 `_uiState` 和 `_filterState`（`MutableStateFlow<GoalFilter>`）
   - 通过 `combine(goalRepository.goals, _filterState)` 计算过滤结果（按 `goal.status` 映射：ACTIVE 对应 `GoalStatus.ACTIVE`，ARCHIVED 对应 `GoalStatus.ARCHIVED`，ALL 不过滤）
   - 使用 `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ...)` 转为 StateFlow
   - 将结果更新到 `_uiState`（通过 `onEach + launchIn`）
   - 实现函数：`deleteGoal(goalId)`、`archiveGoal(goalId)`（内部调用 `goalRepository.setStatus(id, GoalStatus.ARCHIVED)`）、`pauseGoal(goalId)`（可选，`setStatus(id, GoalStatus.PAUSED)`）、`setFilter(filter)`
   - `deleteGoal` 完成后调用 `context.enqueueWidgetSync()`（扩展函数）和 `_events.send(ShowSnackbar(...))`
2. 在同文件或 `WorkerExtensions.kt` 中定义 `fun Context.enqueueWidgetSync()` 扩展函数

**Acceptance Criteria**

- [ ] `@HiltViewModel` 注解存在
- [ ] 无 Room Entity 或 DAO 的 import
- [ ] `_events` 为 `Channel<GoalListEvent>`
- [ ] `uiState` 为 `StateFlow`（非 MutableStateFlow）对外暴露
- [ ] `deleteGoal` 后触发 Widget 同步
- [ ] 编译通过

**Risks**

- 在 init 中使用 `.launchIn(viewModelScope)` 要确保 Flow 不泄漏

**Notes**

- `enqueueWidgetSync` 扩展函数后续 Phase 7 会被 Worker 实现替换，此处为骨架

---

#### Task 5.3 — 创建 GoalCard 和 ProgressBar 组件

**Goal**

实现可复用的 `GoalCard` 和 `GoalProgressBar` Composable 组件。

**Modules**

`ui/goal/components/`

**Dependencies**

Task 4.1、Task 3.5

**Input**

Architecture.md §1（components/ 目录）

**Output**

`GoalCard.kt`，`ProgressBar.kt`

**Files**

- `ui/goal/components/GoalCard.kt`（新建）
- `ui/goal/components/ProgressBar.kt`（新建）

**Steps**

1. 创建 `GoalProgressBar.kt`：
   - 接受 `progress: Float`（0f~1f）、`color: Color`、`modifier: Modifier`
   - 使用 `LinearProgressIndicator`（Material3）
   - 显示百分比文字
2. 创建 `GoalCard.kt`：
   - 接受 `goal: Goal`、`onClick: () -> Unit`、`onDelete: () -> Unit`
   - 使用 `Card` + `Column`
   - 显示：标题、**当前值 / 目标值 单位（如 "12 / 20 次"）**、进度条（`GoalProgressBar(goal.progress, goal.color)`，progress 取自 Goal 派生属性）、截止日期（格式化）
   - 长按或滑动触发删除（简单实现：右上角删除按钮）

**Acceptance Criteria**

- [ ] 组件纯 Composable，无 ViewModel 依赖
- [ ] 参数均为 UI 所需基础类型或 Model
- [ ] 预览 `@Preview` 注解已添加
- [ ] 编译通过

**Risks**

- 无

**Notes**

- 组件不持有任何状态，完全受参数控制（无状态组件原则）

---

#### Task 5.4 — 创建 GoalListScreen

**Goal**

实现目标列表页 Composable，消费 GoalListUiState，展示目标列表和底部操作。

**Modules**

`ui/goal/`

**Dependencies**

Task 5.2、Task 5.3

**Input**

Architecture.md §7（数据流设计）

**Output**

`GoalListScreen.kt`，更新 `AppNavHost.kt`

**Files**

- `ui/goal/GoalListScreen.kt`（新建）
- `ui/navigation/AppNavHost.kt`（修改，替换占位）

**Steps**

1. 创建 `GoalListScreen.kt`：
   - 参数：`viewModel: GoalListViewModel = hiltViewModel()`、`onNavigateToDetail: (Long) -> Unit`、`onNavigateToCreate: () -> Unit`
   - 使用 `collectAsStateWithLifecycle()` 收集 `uiState`
   - 使用 `LaunchedEffect` 消费 `viewModel.events`（导航、Snackbar）
   - 布局：`Scaffold` + `LazyColumn`（GoalCard 列表）+ FAB（新建目标）
   - 显示 Loading、Empty State、Error State
   - 顶部 Filter 切换（Tab 或 Chip 组：ACTIVE / ARCHIVED / ALL）
2. 在 `AppNavHost.kt` 中替换 GoalList 占位

**Acceptance Criteria**

- [ ] 使用 `collectAsStateWithLifecycle`（不用 `collectAsState`）
- [ ] 事件通过 `LaunchedEffect` 消费
- [ ] Loading、Empty、Error 三种状态有 UI 处理
- [ ] 无直接 Repository 或 DAO 引用
- [ ] App 运行可显示目标列表（空列表状态）

**Risks**

- LazyColumn 与 Scaffold innerPadding 配合（确保列表不被 BottomBar 遮挡）

**Notes**

- 此时可以在真机测试：空列表状态、Filter 切换

---

#### Task 5.5 — 创建 GoalDetailUiState、Event、ViewModel

**Goal**

实现目标详情页的状态、事件、ViewModel。

**Modules**

`ui/goal/`

**Dependencies**

Task 3.6、Task 3.7

**Input**

Architecture.md §7（GoalDetailViewModel）、§8（GoalDetailUiState）

**Output**

`GoalDetailUiState.kt`，`GoalDetailViewModel.kt`

**Files**

- `ui/goal/GoalDetailUiState.kt`（新建）
- `ui/goal/GoalDetailViewModel.kt`（新建）

**Steps**

1. 创建 `GoalDetailUiState.kt`：
   ```kotlin
   data class GoalDetailUiState(
       val detail: GoalDetail? = null,
       val progressHistory: List<ProgressRecord> = emptyList(),
       val isLoading: Boolean = true,
       val isEditing: Boolean = false
   )
   sealed class GoalDetailEvent {
       data class ShowSnackbar(val message: String) : GoalDetailEvent()
       object NavigateBack : GoalDetailEvent()
       data class NavigateToEdit(val goalId: Long) : GoalDetailEvent()
   }
   ```
2. 创建 `GoalDetailViewModel.kt`（按 Architecture.md §7 实现）：
   - `goalId` 从 `SavedStateHandle` 获取
   - 订阅 `goalRepository.observeGoalDetail(goalId)`
   - 订阅 `progressRepository.observeProgressHistory(goalId)`
   - 实现：`setCurrentValue(value: Int, note: String? = null)`、`toggleMilestone(id, completed)`、`addMilestone(title: String, targetValue: Int)`
   - 入参做边界保护：`value.coerceIn(0, targetValue)`，避免越界

**Acceptance Criteria**

- [ ] `goalId` 从 `SavedStateHandle` 获取
- [ ] 两个 Flow 订阅均在 init 块中启动
- [ ] 无 Room Entity import
- [ ] 编译通过

**Risks**

- `SavedStateHandle` 的 Long 类型解析（确保 navArgument 声明为 NavType.LongType）

**Notes**

- 无

---

#### Task 5.6 — 创建 MilestoneItem 组件和 GoalDetailScreen

**Goal**

实现里程碑列表项组件和目标详情页。

**Modules**

`ui/goal/`

**Dependencies**

Task 5.5、Task 4.3

**Input**

Architecture.md §1（MilestoneItem 组件）

**Output**

`MilestoneItem.kt`，`GoalDetailScreen.kt`，更新 AppNavHost

**Files**

- `ui/goal/components/MilestoneItem.kt`（新建）
- `ui/goal/GoalDetailScreen.kt`（新建）
- `ui/navigation/AppNavHost.kt`（修改）

**Steps**

1. 创建 `MilestoneItem.kt`：接受 `milestone: Milestone`、`onToggle: (Boolean) -> Unit`，显示 Checkbox + 标题
2. 创建 `GoalDetailScreen.kt`：
   - 显示目标标题、单位、**`currentValue / targetValue` 数值（如 "12 / 20 次"）**、派生进度条（`GoalProgressBar(goal.progress, goal.color)`）、里程碑列表
   - **进度输入用 Stepper / NumberField**（增减整数 currentValue），**不要用 Slider** —— 避免 Float ↔ Int 反算导致的精度抖动
   - currentValue 变更触发 `viewModel.setCurrentValue(newValue)`
   - 里程碑 Checkbox 触发 `viewModel.toggleMilestone(id, completed)`
   - 顶部 TopAppBar 含返回和编辑按钮
   - 底部显示进度历史（按 `recordDate` 倒序，显示 "+N / 单位" 增量）
3. 更新 AppNavHost 替换占位

**Acceptance Criteria**

- [ ] currentValue 显示为整数，单位拼接正确
- [ ] 派生进度条范围 0f~1f（除零时为 0f）
- [ ] 里程碑完成状态实时更新
- [ ] 编译通过，可在真机测试

**Risks**

- 整数 currentValue 输入需做边界保护：`coerceIn(0, targetValue)`
- 若 targetValue 在编辑页变小，currentValue 可能 > targetValue —— 由 ViewModel `setCurrentValue` 做收敛

**Notes**

- Stepper 每次点击直接触发 `setCurrentValue`；快速点击合并由 Compose recomposition 处理，无需 onValueChangeFinished 节流
- 若需"自定义增量"输入框，校验非数字 / 负数后再调用 setCurrentValue

---

#### Task 5.7 — 创建 GoalEditUiState、Event、ViewModel

**Goal**

实现目标新建/编辑页的状态、事件、ViewModel（支持新建和编辑两种模式）。

**Modules**

`ui/goal/`

**Dependencies**

Task 3.6

**Input**

Architecture.md §8（GoalEditUiState）

**Output**

`GoalEditUiState.kt`，`GoalEditViewModel.kt`

**Files**

- `ui/goal/GoalEditUiState.kt`（新建）
- `ui/goal/GoalEditViewModel.kt`（新建）

**Steps**

1. 创建 `GoalEditUiState.kt`（按 Architecture.md §8）—— 含 title、description、**targetValue: Int、unit: String、startDate: Long、targetDate: Long?**、color、isSaving、titleError、**targetValueError、unitError**
2. 创建 `GoalEditViewModel.kt`：
   - `goalId: Long?` 从 `SavedStateHandle`（null = 新建模式）
   - 编辑模式下：在 init 中加载目标数据填充 UiState
   - 实现字段更新函数：`onTitleChange`、`onDescriptionChange`、**`onTargetValueChange(Int)`、`onUnitChange(String)`、`onStartDateChange(Long)`、`onTargetDateChange(Long?)`**、`onColorChange`
   - 实现 `saveGoal()`：
     - 校验 title 非空（更新 `titleError`）
     - 校验 **targetValue > 0**（更新 `targetValueError`）
     - 校验 **unit 非空**（更新 `unitError`）
     - 新建调用 `goalRepository.addGoal(title, targetValue, unit, startDate, targetDate, description, color)`
     - 编辑调用 `goalRepository.updateGoal(...)`
     - 成功后发送 `NavigateBack` 事件

**Acceptance Criteria**

- [ ] 支持 `goalId == null` 的新建模式
- [ ] 表单校验更新 `titleError`
- [ ] 保存成功后通过事件触发导航（不在 ViewModel 中直接调用 navController）
- [ ] 编译通过

**Risks**

- 编辑模式下的数据加载与用户输入竞争（确保只在初始化时加载一次）

**Notes**

- 无

---

#### Task 5.8 — 创建 GoalEditScreen

**Goal**

实现目标新建/编辑页 UI。

**Modules**

`ui/goal/`

**Dependencies**

Task 5.7

**Input**

GoalEditUiState

**Output**

`GoalEditScreen.kt`，更新 AppNavHost

**Files**

- `ui/goal/GoalEditScreen.kt`（新建）
- `ui/navigation/AppNavHost.kt`（修改）

**Steps**

1. 创建 `GoalEditScreen.kt`：
   - TopAppBar：标题（"新建目标" or "编辑目标"）+ 返回按钮
   - 表单字段：
     - `OutlinedTextField` — 目标标题（`titleError` 显示错误提示）
     - `OutlinedTextField` — 描述（多行）
     - **`OutlinedTextField` — 目标值 targetValue（`KeyboardType.Number`，`targetValueError` 显示错误提示）**
     - **`OutlinedTextField` — 单位 unit（如 "次"、"km"、"分钟"，`unitError` 显示错误提示）**
     - **开始日期选择器**（DatePickerDialog，默认今日）
     - 截止日期选择器（DatePickerDialog，可选 / 可清空）
     - 颜色选择器（水平 Row，5 个预设颜色圆点）
   - 底部保存按钮（`Button`），`isSaving` 状态时显示 Loading
   - 消费 NavigateBack 事件返回上一页
2. 更新 AppNavHost

**Acceptance Criteria**

- [ ] 表单校验错误在 TextField 下方显示
- [ ] 截止日期格式化显示
- [ ] 保存按钮在 `isSaving` 时禁用
- [ ] 编译通过，可在真机测试

**Risks**

- DatePickerDialog 的状态管理（remember dialog open state）

**Notes**

- 颜色选择器可简单实现，不过度设计

---

### Phase 6 — Dashboard & Settings

**目标**：实现汇总仪表盘和设置页。

---

#### Task 6.1 — 创建 DashboardUiState 和 DashboardViewModel

**Goal**

实现仪表盘页的状态和 ViewModel，展示目标汇总数据。

**Modules**

`ui/dashboard/`

**Dependencies**

Task 3.6

**Input**

Architecture.md §1（DashboardViewModel）

**Output**

`DashboardUiState.kt`，`DashboardViewModel.kt`

**Files**

- `ui/dashboard/DashboardUiState.kt`（新建）
- `ui/dashboard/DashboardViewModel.kt`（新建）

**Steps**

1. 创建 `DashboardUiState.kt`：
   ```kotlin
   data class DashboardUiState(
       val totalGoals: Int = 0,
       val completedGoals: Int = 0,
       val averageProgress: Float = 0f,
       val topGoals: List<Goal> = emptyList(),
       val isLoading: Boolean = true
   )
   ```
2. 创建 `DashboardViewModel.kt`：
   - 订阅 `goalRepository.goals`
   - 计算汇总数据（在 ViewModel 中进行，非 Repository）：
     - `totalGoals`：总数
     - `completedGoals`：`status == GoalStatus.COMPLETED` 的数量（不再用浮点比较）
     - `averageProgress`：`goals.map { it.progress }.average()`（Goal Model 的派生属性，已含除零保护）
     - `topGoals`：取前 3 个 `progress` 最高的目标（基于派生进度排序）

**Acceptance Criteria**

- [ ] 汇总计算在 ViewModel 中（非 Repository）
- [ ] 使用同一个 `goalRepository.goals` Flow（不另开查询）
- [ ] 编译通过

**Risks**

- 无

**Notes**

- Dashboard 共享 GoalRepository 的 Flow，不创建新的 DAO 查询

---

#### Task 6.2 — 创建 SummaryCard 和 DashboardScreen

**Goal**

实现汇总卡片组件和仪表盘页面 UI。

**Modules**

`ui/dashboard/`

**Dependencies**

Task 6.1、Task 4.1

**Input**

Architecture.md §1（SummaryCard 组件）

**Output**

`SummaryCard.kt`，`DashboardScreen.kt`，更新 AppNavHost

**Files**

- `ui/dashboard/components/SummaryCard.kt`（新建）
- `ui/dashboard/DashboardScreen.kt`（新建）
- `ui/navigation/AppNavHost.kt`（修改）

**Steps**

1. 创建 `SummaryCard.kt`：接受 `title: String`、`value: String`、`icon: ImageVector`，显示统计卡片
2. 创建 `DashboardScreen.kt`：
   - 顶部标题 "Goal Wall"
   - 统计区域（3 个 SummaryCard：总目标数、已完成、平均进度）
   - 热门目标区域（GoalCard 列表，最多 3 个）
   - 圆形进度图（使用 Canvas 绘制总体进度）

**Acceptance Criteria**

- [ ] 统计数据正确显示（基于真实数据库数据）
- [ ] 更新 AppNavHost 替换占位
- [ ] 编译通过，可在真机测试

**Risks**

- 无

**Notes**

- 圆形进度图是可选增强，基础版可用 LinearProgressIndicator 代替

---

#### Task 6.3 — 创建 UserPreferences（DataStore）

**Goal**

实现 DataStore Preferences 封装类，存储用户偏好设置。

**Modules**

`data/`（偏好设置作为数据层的一部分）

**Dependencies**

Task 2.3

**Input**

Architecture.md §10（DataStore 集成）

**Output**

`UserPreferences.kt`，更新 `RepositoryModule.kt`

**Files**

- `data/UserPreferences.kt`（新建，放在 `data/` 下）
- `di/RepositoryModule.kt`（修改，提供 UserPreferences）

**Steps**

1. 创建 `UserPreferences.kt`（按 Architecture.md §10 实现）：
   - `reminderEnabled: Flow<Boolean>`
   - `themeMode: Flow<String>`（"system" / "light" / "dark"）
   - `suspend fun setReminderEnabled(enabled: Boolean)`
   - `suspend fun setThemeMode(mode: String)`
2. 在 `RepositoryModule.kt` 中提供 `UserPreferences` 单例

**Acceptance Criteria**

- [ ] DataStore key 使用 `preferencesKey`（不是字符串硬编码）
- [ ] `@Singleton` 注解
- [ ] 编译通过

**Risks**

- DataStore 不能在 `Application.onCreate()` 之前访问

**Notes**

- DataStore 是异步的，不要在 Activity 中用 `runBlocking` 读取

---

#### Task 6.4 — 创建 SettingsViewModel 和 SettingsScreen

**Goal**

实现设置页，支持提醒开关和主题模式切换。

**Modules**

`ui/settings/`

**Dependencies**

Task 6.3

**Input**

Architecture.md §1（SettingsScreen）

**Output**

`SettingsViewModel.kt`，`SettingsScreen.kt`，更新 AppNavHost

**Files**

- `ui/settings/SettingsViewModel.kt`（新建）
- `ui/settings/SettingsScreen.kt`（新建）
- `ui/navigation/AppNavHost.kt`（修改）

**Steps**

1. 创建 `SettingsViewModel.kt`：
   - 订阅 `UserPreferences.reminderEnabled` 和 `themeMode`
   - 实现 `setReminderEnabled(Boolean)` 和 `setThemeMode(String)`
2. 创建 `SettingsScreen.kt`：
   - `SwitchPreferenceItem`：提醒开关
   - `ListPreferenceItem`：主题模式（跟随系统 / 浅色 / 深色）
   - 版本信息展示（`BuildConfig.VERSION_NAME`）

**Acceptance Criteria**

- [ ] 开关状态与 DataStore 双向绑定
- [ ] 主题切换立即生效（通过 `GoalWallTheme` 的 `darkTheme` 参数控制）
- [ ] 编译通过

**Risks**

- 主题切换需要在 `MainActivity` 中观察 `themeMode` 并传入 `GoalWallTheme`

**Notes**

- 主题实时切换需 MainActivity 订阅 `UserPreferences.themeMode`

---

### Phase 7 — Worker & Widget

**目标**：实现 WorkManager 后台任务和 Glance Widget。

---

#### Task 7.1 — 创建 WidgetSyncWorker

**Goal**

实现 Widget 数据同步 Worker，触发 Glance Widget 更新。

**Modules**

`worker/`

**Dependencies**

Task 2.4、Task 3.8

**Input**

Architecture.md §4（WidgetSyncWorker 设计）

**Output**

`WidgetSyncWorker.kt`

**Files**

- `worker/WidgetSyncWorker.kt`（新建）

**Steps**

1. 创建 `WidgetSyncWorker.kt`（按 Architecture.md §4）：
   - `@HiltWorker`，`@AssistedInject constructor`
   - 依赖 `WidgetDataProvider`（非 GoalRepository）
   - `doWork()`：调用 `GoalWallWidget().updateAll(applicationContext)`
   - 返回 `Result.success()`
2. 实现 `Context.enqueueWidgetSync()` 扩展函数（`WorkerExtensions.kt`）：
   - 使用 `OneTimeWorkRequestBuilder<WidgetSyncWorker>`
   - 设置 `setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)`

**Acceptance Criteria**

- [ ] `@HiltWorker` 注解正确
- [ ] 依赖 `WidgetDataProvider` 而非 `GoalRepository`
- [ ] `enqueueWidgetSync()` 扩展函数已定义
- [ ] 编译通过

**Risks**

- Expedited Work 需要在 `WorkRequest` 上设置（同时 Worker 需实现 `getForegroundInfo()`）

**Notes**

- 如果 Expedited 导致复杂度上升，可先使用普通 OneTimeWork

---

#### Task 7.2 — 实现 GoalWallWidget（Glance）

**Goal**

实现主屏幕 Widget，展示进度最高的 3 个目标。

**Modules**

`widget/`

**Dependencies**

Task 7.1、Task 3.8

**Input**

Architecture.md §4（GoalWallWidget 设计）

**Output**

`GoalWallWidget.kt`，`GoalWallWidgetReceiver.kt`，`goalwall_widget_info.xml`

**Files**

- `widget/GoalWallWidget.kt`（新建）
- `widget/GoalWallWidgetReceiver.kt`（新建）
- `res/xml/goalwall_widget_info.xml`（新建）
- `AndroidManifest.xml`（修改，注册 Receiver）

**Steps**

1. 创建 `goalwall_widget_info.xml`（Widget 元数据，minWidth、minHeight、updatePeriodMillis=0）
2. 创建 `GoalWallWidget.kt`：
   - 继承 `GlanceAppWidget()`
   - `provideGlance` 中：通过 Hilt 获取 `WidgetDataProvider`（需 `EntryPointAccessors`）
   - 调用 `getTopGoals()` 获取数据
   - `provideContent { GlanceTheme { GoalWallWidgetContent(goals) } }`
3. 创建内部 `GoalWallWidgetContent` Composable（Glance Composable）：
   - `Column`：显示 App 名称和 3 个目标行（名称 + 进度百分比）
4. 创建 `GoalWallWidgetReceiver.kt`（继承 `GlanceAppWidgetReceiver`，关联 `GoalWallWidget`）
5. 在 `AndroidManifest.xml` 注册 Receiver（`APPWIDGET_UPDATE` intent）

**Acceptance Criteria**

- [ ] Widget 可在主屏幕添加
- [ ] 显示最多 3 个目标的名称和进度
- [ ] Widget 数据通过 `WidgetDataProvider` 获取
- [ ] 编译通过

**Risks**

- Glance 中无法直接使用 Hilt 注入，需通过 `EntryPointAccessors` 手动获取

**Notes**

- Glance Composable API 与普通 Compose 有差异（不支持 Modifier 部分属性）

---

#### Task 7.3 — 创建 ReminderWorker 和定时调度

**Goal**

实现每日提醒 Worker 和 WorkManager 定时调度注册。

**Modules**

`worker/`

**Dependencies**

Task 2.4、Task 3.6、Task 8.1（通知依赖，可并行）

**Input**

Architecture.md §9（ReminderWorker、调度配置）

**Output**

`ReminderWorker.kt`，更新 `GoalWallApp.kt`

**Files**

- `worker/ReminderWorker.kt`（新建）
- `GoalWallApp.kt`（修改，添加 `scheduleReminders()`）

**Steps**

1. 创建 `ReminderWorker.kt`（按 Architecture.md §9）：
   - 依赖 `GoalRepository` 和 `NotificationHelper`
   - `doWork()`：读取活跃目标数量，调用 `notificationHelper.showDailyReminder(...)`
2. 在 `GoalWallApp.kt` 的 `onCreate()` 中调用 `scheduleReminders(this)`
3. 实现 `scheduleReminders(context)`：
   - `PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)`
   - 计算初始延迟到次日 9:00 AM
   - `enqueueUniquePeriodicWork("daily_reminder", KEEP, request)`

**Acceptance Criteria**

- [ ] `scheduleReminders` 使用 `KEEP` 策略（不重复调度）
- [ ] Worker 初始延迟计算正确（不立即触发）
- [ ] 编译通过

**Risks**

- `calculateDelayToNineAM()` 时区处理（建议使用 `java.time.ZonedDateTime`）

**Notes**

- 无

---

### Phase 8 — Notification

**目标**：实现通知基础设施：渠道创建、通知发送工具类。

---

#### Task 8.1 — 实现 NotificationChannels 和 NotificationHelper

**Goal**

实现通知渠道注册和通知发送工具类。

**Modules**

`notification/`

**Dependencies**

Task 2.1

**Input**

Architecture.md §9（通知系统完整设计）

**Output**

`NotificationChannels.kt`，`NotificationHelper.kt`，更新 `GoalWallApp.kt`

**Files**

- `notification/NotificationChannels.kt`（新建）
- `notification/NotificationHelper.kt`（新建）
- `GoalWallApp.kt`（修改，填充 `createNotificationChannels()`）

**Steps**

1. 创建 `NotificationChannels.kt`（按 Architecture.md §9）：
   - 定义 3 个渠道常量：`REMINDER`、`MILESTONE`、`DAILY_SUMMARY`
   - 实现 `Context.createNotificationChannels()` 扩展函数
2. 创建 `NotificationHelper.kt`（按 Architecture.md §9）：
   - `@Singleton`、`@Inject constructor(@ApplicationContext context: Context)`
   - `showMilestoneAchieved(goalTitle, milestoneTitle)`
   - `showDailyReminder(pendingGoalCount)`
3. 在 `GoalWallApp.onCreate()` 中补充调用 `createNotificationChannels()`
4. 在 `di/` 中将 `NotificationHelper` 添加到 Hilt 注入（或使用 `@Inject constructor` 自动注入）

**Acceptance Criteria**

- [ ] 三个通知渠道已创建（设备设置 > 应用通知中可见）
- [ ] `showDailyReminder` 可正常发送通知
- [ ] 编译通过

**Risks**

- Android 13+ 需要 POST_NOTIFICATIONS 权限申请（运行时请求）

**Notes**

- 运行时权限请求可在 MainActivity 中使用 `rememberPermissionState` 处理

---

#### Task 8.2 — 里程碑完成时触发通知

**Goal**

在 GoalDetailViewModel 中检测里程碑完成，触发 `NotificationHelper.showMilestoneAchieved()`。

**Modules**

`ui/goal/`

**Dependencies**

Task 8.1、Task 5.5

**Input**

Architecture.md §3（副作用流：里程碑达成触发通知）

**Output**

更新 `GoalDetailViewModel.kt`

**Files**

- `ui/goal/GoalDetailViewModel.kt`（修改）

**Steps**

1. 在 `GoalDetailViewModel` 中注入 `NotificationHelper`
2. 修改 `toggleMilestone` 函数：
   - 当 `done == true` 时，获取里程碑标题和目标标题
   - 调用 `notificationHelper.showMilestoneAchieved(goalTitle, milestoneTitle)`
3. 通知发送在 `viewModelScope.launch` 中执行（非阻塞）

**Acceptance Criteria**

- [ ] 里程碑勾选完成后收到通知
- [ ] 通知不在主线程发送
- [ ] 编译通过

**Risks**

- ViewModel 注入 NotificationHelper（确认 Hilt 注入链正确）

**Notes**

- 无

---

### Phase 9 — Testing

**目标**：建立测试体系，覆盖 Repository 和 ViewModel。

---

#### Task 9.1 — 配置测试依赖

**Goal**

在 `app/build.gradle.kts` 中添加测试依赖，配置测试环境。

**Modules**

测试配置

**Dependencies**

Task 1.3

**Input**

无

**Output**

更新 `app/build.gradle.kts`，更新 `libs.versions.toml`

**Files**

- `app/build.gradle.kts`（修改）
- `gradle/libs.versions.toml`（修改）

**Steps**

1. 添加测试依赖到 `libs.versions.toml`：
   - `junit = "4.13.2"`
   - `mockk = "1.13.x"`
   - `coroutines-test`（与 Kotlin Coroutines 版本匹配）
   - `room-testing`（in-memory database）
   - `turbine`（Flow 测试工具）
2. 在 `app/build.gradle.kts` 中添加：
   - `testImplementation(libs.junit)`
   - `testImplementation(libs.mockk)`
   - `testImplementation(libs.coroutines.test)`
   - `testImplementation(libs.room.testing)`
   - `testImplementation(libs.turbine)`
   - `androidTestImplementation(libs.hilt.android.testing)`

**Acceptance Criteria**

- [ ] 依赖解析成功
- [ ] `./gradlew testDebugUnitTest` 可以运行（即使无测试用例）

**Risks**

- Turbine 版本与 Coroutines 版本不兼容

**Notes**

- MockK 用于 ViewModel 测试中 Mock Repository

---

#### Task 9.2 — GoalRepository 单元测试

**Goal**

为 `GoalRepository` 编写单元测试，使用 in-memory Room Database。

**Modules**

`data/repository/` 测试

**Dependencies**

Task 9.1、Task 3.6

**Input**

GoalRepository 的所有公共方法

**Output**

`GoalRepositoryTest.kt`

**Files**

- `app/src/test/java/com/goalwall/data/repository/GoalRepositoryTest.kt`（新建）

**Steps**

1. 创建 `GoalRepositoryTest.kt`：
   - 在 `@Before` 中创建 in-memory Database 和 Repository 实例
   - 测试用例：
     - `addGoal_insertsGoalToDatabase()`
     - `deleteGoal_removesGoalAndCascades()`
     - `updateCurrentValue_updatesCurrentValueField()`
     - `goals_emitsUpdatedListOnChange()`（使用 Turbine `test{}`）
     - `observeGoalDetail_returnsGoalWithMilestones()`
2. 使用 `runTest` 包装所有协程测试

**Acceptance Criteria**

- [ ] 所有测试通过
- [ ] 使用 in-memory Database（不是 Mock DAO）
- [ ] Flow 测试使用 Turbine
- [ ] `./gradlew testDebugUnitTest` 通过

**Risks**

- in-memory Room 需要 Android 环境（JVM 测试可能需要 Robolectric）

**Notes**

- 若 JVM 测试不支持 Room，改为 `androidTest`（instrumented test）

---

#### Task 9.3 — GoalListViewModel 单元测试

**Goal**

为 `GoalListViewModel` 编写单元测试，Mock GoalRepository。

**Modules**

`ui/goal/` 测试

**Dependencies**

Task 9.1、Task 5.2

**Input**

GoalListViewModel 的所有公共函数

**Output**

`GoalListViewModelTest.kt`

**Files**

- `app/src/test/java/com/goalwall/ui/goal/GoalListViewModelTest.kt`（新建）

**Steps**

1. 创建 `GoalListViewModelTest.kt`：
   - 使用 MockK Mock `GoalRepository`
   - 配置 `Dispatchers.setMain(UnconfinedTestDispatcher())`（@Before/@After）
   - 测试用例：
     - `init_loadsGoalsFromRepository()`：验证 init 后 goals 填充 uiState
     - `deleteGoal_callsRepositoryDelete()`：验证 deleteGoal 调用正确方法
     - `setFilter_filtersGoalsCorrectly()`：验证 ACTIVE/ARCHIVED/ALL 过滤
     - `deleteGoal_emitsSnackbarEvent()`：使用 Turbine 验证 events Channel

**Acceptance Criteria**

- [ ] 所有测试通过
- [ ] 使用 MockK（不是 Mockito）
- [ ] `TestCoroutineDispatcher` 正确配置
- [ ] `./gradlew testDebugUnitTest` 通过

**Risks**

- Channel 事件消费的异步时序问题（使用 Turbine 或 `advanceUntilIdle`）

**Notes**

- 无

---

#### Task 9.4 — GoalDetailViewModel 单元测试

**Goal**

为 `GoalDetailViewModel` 编写单元测试。

**Modules**

`ui/goal/` 测试

**Dependencies**

Task 9.3

**Input**

GoalDetailViewModel 的公共函数

**Output**

`GoalDetailViewModelTest.kt`

**Files**

- `app/src/test/java/com/goalwall/ui/goal/GoalDetailViewModelTest.kt`（新建）

**Steps**

1. 测试用例：
   - `init_loadsGoalDetailAndProgressHistory()`
   - `setCurrentValue_callsRepositoryWithCorrectValues()`
   - `toggleMilestone_callsRepositoryToggle()`
   - `toggleMilestone_done_triggersNotification()`

**Acceptance Criteria**

- [ ] 所有测试通过
- [ ] 里程碑完成时通知触发验证

**Risks**

- 无

**Notes**

- 无

---

### Phase 10 — Release

**目标**：配置 Release 构建、签名、CI 打包流水线。

---

#### Task 10.1 — 配置 Build Variants 和签名

**Goal**

配置 debug/release Build Variant，配置 Release 签名（从环境变量读取）。

**Modules**

构建配置

**Dependencies**

Task 1.5

**Input**

无

**Output**

更新 `app/build.gradle.kts`，`keystore.properties`（gitignore）

**Files**

- `app/build.gradle.kts`（修改）
- `keystore.properties`（新建，添加到 `.gitignore`）

**Steps**

1. 创建 `keystore.properties`（本地，不提交 Git）：存储 keystore 路径和密码
2. 在 `app/build.gradle.kts` 中配置签名：
   ```kotlin
   signingConfigs {
       create("release") {
           storeFile = file(keystoreProperties["storeFile"] as String)
           storePassword = keystoreProperties["storePassword"] as String
           keyAlias = keystoreProperties["keyAlias"] as String
           keyPassword = keystoreProperties["keyPassword"] as String
       }
   }
   buildTypes {
       release {
           isMinifyEnabled = true
           proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
           signingConfig = signingConfigs.getByName("release")
       }
   }
   ```
3. 配置 `versionCode` 和 `versionName`

**Acceptance Criteria**

- [ ] `./gradlew assembleRelease` 编译通过
- [ ] Release APK 已签名（可用 `apksigner verify` 验证）
- [ ] `keystore.properties` 在 `.gitignore` 中

**Risks**

- keystore 文件丢失导致无法更新应用（务必备份）

**Notes**

- CI 环境使用 GitHub Secrets 存储 keystore Base64 和密码

---

#### Task 10.2 — 配置 Proguard 规则

**Goal**

配置 Proguard/R8 混淆规则，保护 Room Entity 和 Hilt 注入类。

**Modules**

构建配置

**Dependencies**

Task 10.1

**Input**

无

**Output**

`app/proguard-rules.pro`

**Files**

- `app/proguard-rules.pro`（修改）

**Steps**

1. 添加 Room Entity 保留规则（防止字段被混淆）
2. 添加 Hilt 相关保留规则
3. 添加 Kotlin Serialization 规则（如有使用）
4. 添加 WorkManager 规则
5. 执行 `./gradlew assembleRelease` 验证无混淆问题

**Acceptance Criteria**

- [ ] Release 构建无 ProGuard 警告（或警告已知可忽略）
- [ ] App 在 Release 模式下功能正常

**Risks**

- Room Entity 字段被混淆导致数据库列名错误（运行时崩溃）

**Notes**

- 使用 `@Keep` 注解备用方案

---

#### Task 10.3 — GitHub Actions Release 流水线

**Goal**

配置 CI Release 流水线，PR 合并到 main 后自动构建 Release APK 并上传 Artifact。

**Modules**

CI 配置

**Dependencies**

Task 1.5、Task 10.1

**Input**

GitHub Secrets（keystore、密码）

**Output**

`.github/workflows/release.yml`

**Files**

- `.github/workflows/release.yml`（新建）

**Steps**

1. 创建 `release.yml`：
   - 触发条件：`push` 到 `main` 分支
   - 从 `KEYSTORE_BASE64` Secret 解码 keystore 文件
   - 从 Secrets 读取签名密码
   - 执行 `./gradlew assembleRelease`
   - 上传 APK 为 GitHub Release Artifact
2. 在 GitHub 仓库 Settings > Secrets 中添加：
   - `KEYSTORE_BASE64`
   - `KEY_STORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`

**Acceptance Criteria**

- [ ] Push 到 main 触发 Release CI
- [ ] CI 产出签名 APK，可下载
- [ ] APK 文件名包含 versionName

**Risks**

- GitHub Secrets 配置错误导致 CI 失败

**Notes**

- 暂不配置 Google Play 自动发布，手动下载 APK 安装验证

---

## 6. Definition of Done

每个 Task 完成的标准（**全部满足才算完成**）：

### 代码质量

- [ ] `./gradlew assembleDebug` 编译成功，无报错
- [ ] `./gradlew ktlintCheck` 通过，无格式错误
- [ ] `./gradlew detekt` 通过，无违规
- [ ] `./gradlew testDebugUnitTest` 通过（适用于有测试的 Task）

### 架构约束

- [ ] 无跨层非法 import（UI 层无 Room Entity，ViewModel 无 DAO）
- [ ] UiState 为 immutable data class（无 var 字段）
- [ ] 事件通过 Channel 传递（非 StateFlow）
- [ ] 使用 `collectAsStateWithLifecycle()`（非 `collectAsState()`）
- [ ] Repository 无业务逻辑（无过滤/排序 if/when 判断）

### 代码规范

- [ ] 无硬编码字符串（所有显示文本在 `strings.xml`）
- [ ] 无硬编码颜色（使用 Theme 系统）
- [ ] 无未使用的 import 语句
- [ ] 无 `TODO` 注释泄漏（任务内 TODO 必须在 Task 报告中列出）
- [ ] 每个新建文件有文件头注释（包名、职责一句话说明）

### 文档

- [ ] 受影响模块的 `README.md` 已更新
- [ ] Task 执行报告已输出（修改文件、验收结果、风险、TODO）

---

## 7. AI Collaboration Workflow

### 标准协同流程

```
┌─────────────────────────────────────────────────────────┐
│                    开发者（人工）                          │
│  1. 选择下一个待执行 Task                                  │
│  2. 确认前置 Task 已完成                                   │
│  3. 将 Task 内容粘贴给 Cursor                             │
└────────────────────┬────────────────────────────────────┘
                     │ 指派 Task
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   Cursor（执行）                           │
│  1. 阅读 Architecture.md 相关章节                          │
│  2. 按 Task Steps 逐步实现                                 │
│  3. 执行 Acceptance Criteria 自检                         │
│  4. 输出 Task 执行报告                                     │
└────────────────────┬────────────────────────────────────┘
                     │ 输出代码 + 报告
                     ▼
┌─────────────────────────────────────────────────────────┐
│               Android Studio（验证）                       │
│  1. 执行 ./gradlew assembleDebug                          │
│  2. 执行 ./gradlew ktlintCheck detekt                     │
│  3. 在真机/模拟器运行验证功能                               │
└────────────────────┬────────────────────────────────────┘
                     │ 如有问题
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  Claude（Review）                          │
│  1. 检查架构约束（分层、依赖方向）                           │
│  2. 检查 UiState 不变性                                    │
│  3. 检查事件处理方式                                       │
│  4. 输出 Review 意见                                       │
└────────────────────┬────────────────────────────────────┘
                     │ Review 意见
                     ▼
┌─────────────────────────────────────────────────────────┐
│                   Cursor（修复）                           │
│  按 Review 意见修复，重新自检                               │
└────────────────────┬────────────────────────────────────┘
                     │ 修复完成
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  Git Commit（提交）                         │
│  格式：feat(phase-X): Task X.Y — 任务名称                  │
└─────────────────────────────────────────────────────────┘
```

### Cursor 上下文提示模板

每次新的 Cursor 会话，在对话开头粘贴：

```
你是 GoalWall 项目的 Android 开发者（AI）。
当前执行：[Task X.Y — 任务名称]
架构约束：
- 单 Activity + Compose Navigation
- MVVM：Screen → ViewModel → Repository → DAO → Room
- ViewModel 禁止 import Room Entity/DAO
- UiState 必须为 immutable data class
- 事件通过 Channel 传递（非 StateFlow）
- Repository 不含业务逻辑
- 禁止 UseCase 层
请严格按以下 Task 定义执行，不要超出范围：
[粘贴 Task 内容]
```

---

## 8. Git Workflow

### 分支规范

| 分支 | 用途 | 命名规范 |
|---|---|---|
| `main` | 稳定发布分支 | — |
| `develop` | 开发集成分支 | — |
| `feat/phase-X-task-Y-名称` | 功能开发分支 | `feat/phase-1-task-1-init-project` |
| `fix/描述` | Bug 修复 | `fix/arch-drift-viewmodel-imports` |
| `chore/描述` | 工程配置 | `chore/update-ci-gradle-cache` |

### Commit 规范（Conventional Commits）

```
<type>(<scope>): <subject>

type:
  feat     — 新功能
  fix      — Bug 修复
  refactor — 重构（不改变功能）
  test     — 测试
  chore    — 构建/工具配置
  docs     — 文档

scope: phase-X 或模块名（data, ui, worker, widget, di）

示例：
feat(phase-3): Task 3.1 — 创建 Room Entity
fix(ui): GoalListScreen 修复 collectAsState 为 collectAsStateWithLifecycle
chore(ci): 添加 Gradle 缓存配置
```

### PR 规范

PR 标题格式：`[Phase X] Task X.Y — 任务名称`

PR 描述模板：
```markdown
## 变更内容
- 新建/修改文件列表

## 验收结果
- [ ] 编译通过
- [ ] Lint + Detekt 通过
- [ ] 单元测试通过
- [ ] 架构约束未违反

## 测试说明
如何在真机/模拟器验证此 Task 的效果

## 风险说明
（如有）
```

### Code Review 规范

PR 合并前 Review 清单：

```
□ 无跨层非法 import
□ UiState 全为不可变字段
□ 无硬编码字符串/颜色
□ 无 ViewModel 直接 import DAO 或 Entity
□ 无 Repository 内部协程启动
□ 事件用 Channel（不是 SharedFlow/StateFlow）
□ 新依赖已在 Architecture.md 声明
□ 文件数量在 Task 定义范围内
```

---

## 9. Risk Management

### 风险清单与应对方案

#### R1 — AI 架构漂移风险

| 项目 | 内容 |
|---|---|
| 风险 | AI 在执行 Task 时引入未声明的依赖、创建 UseCase、在 ViewModel 中调用 DAO |
| 概率 | 高 |
| 影响 | 架构腐化，后续任务难以继续 |
| **应对** | 1. 每个 Task 强制要求 AI 先阅读 Architecture.md；2. detekt ForbiddenImport 规则自动拦截；3. Code Review 清单人工确认 |

#### R2 — AI 上下文丢失风险

| 项目 | 内容 |
|---|---|
| 风险 | Cursor 会话切换后忘记之前的架构约束，重复创建文件或改变公共接口 |
| 概率 | 中 |
| 影响 | 文件冲突、接口不兼容 |
| **应对** | 1. 每次新会话粘贴 Cursor 上下文提示模板；2. Task 定义明确列出修改文件；3. 完成 Task 立即 Git Commit，不积压 |

#### R3 — 依赖版本污染风险

| 项目 | 内容 |
|---|---|
| 风险 | AI 自行引入 Architecture.md 未声明的第三方库 |
| 概率 | 中 |
| 影响 | 构建复杂度增加，与其他依赖冲突 |
| **应对** | 1. AI 规则明确禁止引入未声明依赖；2. Code Review 检查 `build.gradle.kts` diff；3. 发现新依赖需求，先更新 Architecture.md，再执行 Task |

#### R4 — 模块耦合风险

| 项目 | 内容 |
|---|---|
| 风险 | UI 层直接引用 DAO，或 Repository 持有 ViewModel 引用 |
| 概率 | 中 |
| 影响 | 分层架构失效，测试困难 |
| **应对** | 1. detekt ForbiddenImport 自动检测；2. ViewModel 测试中 Mock Repository（无法 Mock 说明 VM 引用了 DAO）；3. Phase 结束时人工 import 审查 |

#### R5 — Room 迁移遗漏风险

| 项目 | 内容 |
|---|---|
| 风险 | Entity 字段变更后未配置 Migration，导致用户数据丢失 |
| 概率 | 低（初期），高（迭代期） |
| 影响 | 线上用户数据全部丢失 |
| **应对** | 1. `exportSchema = true` 强制导出 schema；2. 任何 Entity 变更必须配套 Migration Task；3. 迭代任务中增加 "Migration 检查" 步骤 |

#### R6 — Widget 与主 App 数据不一致风险

| 项目 | 内容 |
|---|---|
| 风险 | 数据写入后 Widget 未及时刷新 |
| 概率 | 中 |
| 影响 | Widget 显示过时数据 |
| **应对** | 1. 每次 Repository 写操作后调用 `enqueueWidgetSync()`；2. 6 小时定时同步兜底；3. App 启动时强制同步一次 |

#### R7 — 协程泄漏风险

| 项目 | 内容 |
|---|---|
| 风险 | 在 Repository 或 Helper 类中启动 GlobalScope 协程 |
| 概率 | 低 |
| 影响 | 内存泄漏、后台任务不可控 |
| **应对** | 1. AI 规则禁止在 ViewModel 外启动协程；2. Code Review 检查协程作用域；3. 所有协程必须在 `viewModelScope` 或 Worker 的 `coroutineScope` 中 |

---

## 附录 A — Task Dependency Graph

```
1.1 → 1.2 → 1.3 → 1.4 → 1.5
              │
              ▼
         2.1 → 2.2 → 2.3 → 2.4
                │
                ▼
    3.1 → 3.2 → 3.3 → 3.5 → 3.6 → 3.7
          │      └──→ 3.4 ──┘      │
          │                        │
          └──→ 3.8                 │
                                   │
              4.1 → 4.2 → 4.3 → 4.4
                                   │
              5.1 → 5.2 ──────────┤
              5.3 → 5.4            │
              5.5 → 5.6            │
              5.7 → 5.8            │
                                   │
              6.1 → 6.2 ──────────┤
              6.3 → 6.4            │
                                   │
         7.1 → 7.2                 │
         7.3 ──────────────────────┤
                                   │
         8.1 → 8.2                 │
                                   │
         9.1 → 9.2 → 9.3 → 9.4 ──┘
                                   │
         10.1 → 10.2 → 10.3 ──────┘
```

---

## 附录 B — 文件职责注释模板

每个新建文件，文件首行添加注释：

```kotlin
// Package: com.goalwall.data.repository
// Layer: Data — Repository
// Responsibility: 聚合目标相关数据，暴露 Flow 给 ViewModel
// Dependencies: GoalDao, MilestoneDao, data.model.*
// Forbidden imports: ui.**, worker.**, di.**
```

---

## 附录 C — 快速状态检查命令

```bash
# 编译检查
./gradlew assembleDebug

# 代码风格
./gradlew ktlintCheck

# 静态分析
./gradlew detekt

# 单元测试
./gradlew testDebugUnitTest

# 全量检查（CI 等价）
./gradlew ktlintCheck detekt assembleDebug testDebugUnitTest

# 自动修复 ktlint 格式
./gradlew ktlintFormat
```

---

*tasks.md v1.0 — 基于 GoalWall Architecture.md v1.0 生成*
*本文档随 Architecture.md 版本更新同步维护*
*禁止 AI 在执行 Task 时修改本文档*
