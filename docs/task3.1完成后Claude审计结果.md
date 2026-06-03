 请对当前 GoalWall 工程进行只读审计（Read Only Audit）。

● Explore(GoalWall project read-only audit)
  ⎿  Initializing…
  ⎿  PowerShell(Get-ChildItem -Path "E:/programming/GoalWall" -Force |
               Select-Object Name, PSIsContainer)
  ⎿  Interrupted · What should Claude do instead?

> 审计依据：

> * docs/Architecture.md

> * docs/tasks.md

> 要求：

> 1. 不修改任何文件

> 2. 不生成任何代码

> 3. 不执行自动修复

> 4. 仅读取项目并输出审计报告

> 重点检查：

> # 1. Architecture Alignment

> 检查当前实现是否符合：

> * 单模块架构

> * 包结构

> * 分层职责

> * 依赖方向

> 识别：

> * 架构漂移

> * 包路径错误

> * 模块职责污染

> # 2. Phase Progress

> 根据 tasks.md：

> 列出：

> * 已完成 Task

> * 部分完成 Task

> * 未完成 Task

> 并说明依据。

> # 3. Dependency Review

> 检查：

> * Hilt

> * Room

> * WorkManager

> * Compose

> * Navigation

> * DataStore

> 是否符合 Architecture.md。

> # 4. Room Review

> 检查：

> * Entity

> * ForeignKey

> * Index

> * TypeConverter

> * GoalWallDatabase

> 是否存在：

> * Migration 风险

> * Schema 风险

> * Room 最佳实践问题

> # 5. Hilt Review

> 检查：

> * Module

> * Repository Binding

> * Worker 注入

> * Qualifier

> 是否存在问题。

> # 6. WorkManager Review

> 检查：

> * HiltWorkerFactory

> * Configuration.Provider

> * Manifest

> 是否正确。

> # 7. Static Analysis Review

> 检查：

> * detekt

> * ktlint

> * ForbiddenImport

> 是否覆盖主要架构风险。

> # 8. Risk Assessment

> 输出：

> High Risk

> Medium Risk

> Low Risk

> # 输出格式

> Current Progress Audit

> ## 已完成 Tasks

> ## 部分完成 Tasks

> ## 未完成 Tasks

> ## 架构问题

> ## Room 问题

> ## Hilt 问题

> ## WorkManager 问题

> ## 风险

> ## 下一推荐 Task

> 不要修改代码。

> 不要生成代码。

> 不要自动修复。

> 仅输出审计结果。

  Searched for 12 patterns, read 44 files (ctrl+o to expand)

● Current Progress Audit

  ▎ 审计依据：docs/Architecture.md v1.0、docs/tasks.md v1.0
  ▎ 审计范围：E:/programming/GoalWall 当前 main 分支静态读取
  ▎ 模式：只读，不修改、不生成代码、不自动修复

---
  一、已完成 Tasks

  依据：源码存在 + 语义匹配 Acceptance Criteria。

  Task: 1.1 初始化 Android 工程
  依据: app/build.gradle.kts:11-58 namespace/applicationId com.goalwall、Compose

    模板齐备
  ────────────────────────────────────────
  Task: 1.2 Version Catalog
  依据: gradle/libs.versions.toml 完整，app/build.gradle.kts 全部走 libs.xxx
  引用
  ────────────────────────────────────────
  Task: 1.3 全部依赖声明
  依据: app/build.gradle.kts:84-140，KSP+Hilt 插件已启用，ksp {
    arg("room.schemaLocation", ...) } 已配置
  ────────────────────────────────────────
  Task: 1.4 ktlint + detekt
  依据: detekt.yml 含 ForbiddenImport、.editorconfig
    已建，app/build.gradle.kts:60-82 ktlint/detekt 配置就绪
  ────────────────────────────────────────
  Task: 1.5 GitHub Actions CI
  依据: .github/workflows/ci.yml 含 ktlint → detekt → assembleDebug →
    testDebugUnitTest 四步 + 失败上传 reports
  ────────────────────────────────────────
  Task: 2.1 GoalWallApp
  依据: GoalWallApp.kt:10-25 @HiltAndroidApp +
    createNotificationChannels()；Manifest:9
    android:name=".GoalWallApp"；权限已声明 (Manifest:5-6)
  ────────────────────────────────────────
  Task: 2.2 DatabaseModule
  依据: di/DatabaseModule.kt 提供 Database + 三
    DAO（已脱离占位状态，绑定真实类型）
  ────────────────────────────────────────
  Task: 3.2 GoalWallDatabase
  依据: data/db/GoalWallDatabase.kt version=1、exportSchema=true、三 Entity 注册
  ────────────────────────────────────────
  Task: 4.1 Theme
  依据: ui/theme/Color.kt、Type.kt、Theme.kt 完整、支持 dark/light

---
  二、部分完成 Tasks

  Task: 2.3 RepositoryModule
  完成度: 结构上完成
  缺失项: 采用 interface + Impl + @Binds 模式（RepositoryModule.kt:17-29），偏离

    Architecture.md §5 的"@Singleton class XxxRepository @Inject
    constructor(...)"直接注入设计（详见架构问题 #1）
  ────────────────────────────────────────
  Task: 2.4 WorkerModule
  完成度: 关键集成已完成
  缺失项: GoalWallApp 实现 Configuration.Provider ✓、Manifest:21-24 已
    tools:node="remove" ✓；但 di/WorkerModule.kt:7-19 只是常量集合（Tags /
    UniqueWorkNames），不是 @Module，命名误导
  ────────────────────────────────────────
  Task: 3.1 Room Entity
  完成度: 三个 Entity 已建并配置 ForeignKey/Index
  缺失项: 字段语义与 Architecture.md §6 严重不一致（详见 Room 问题 #1）
  ────────────────────────────────────────
  Task: 4.2 Screen 路由
  完成度: sealed interface 已建 (ui/navigation/Screen.kt)
  缺失项: 只有 Home 一个路由，缺 GoalList / Dashboard / Settings / GoalDetail /
    GoalEdit 全部 5 个
  ────────────────────────────────────────
  Task: 4.3 AppNavHost
  完成度: 框架存在 (GoalWallNavHost.kt)
  缺失项: 仅注册 Home 单路由 + 占位 Text("GoalWall")；命名 GoalWallNavHost 而非
    spec 的 AppNavHost
  ────────────────────────────────────────
  Task: 4.4 MainActivity 重写
  完成度: @AndroidEntryPoint ✓、GoalWallTheme 包裹 ✓、enableEdgeToEdge ✓
  缺失项: 未实现 BottomNavigationBar / Scaffold / 3 Tab（Task 4.4 Steps 2-3
    明确要求）

---
  三、未完成 Tasks

  Phase 3 余下任务（全部未实现 / 仅占位）

  Task: 3.3 GoalDao
  当前状态: data/db/dao/GoalDao.kt:10-15 仅 @Insert 占位；缺
    observeAll、observeWithMilestones、getTopByProgress、update、deleteById、upd
  ateProgress
  ────────────────────────────────────────
  Task: 3.4 MilestoneDao / ProgressDao
  当前状态: 两个 DAO 均为 @Insert
    占位（MilestoneDao.kt:10-15、ProgressDao.kt:10-15），缺
    setDone、observeByGoal 等
  ────────────────────────────────────────
  Task: 3.5 UI Model + Mappers
  当前状态: data/model/ 下仅有 GoalStatus 枚举；Architecture 要求的 Goal.kt /
    Milestone.kt / GoalDetail.kt / ProgressRecord.kt / Mappers.kt
    全部缺失；Architecture 设计的 GoalWithMilestones Relation 类也缺失
  ────────────────────────────────────────
  Task: 3.6 GoalRepository
  当前状态: GoalRepository.kt 是接口，方法签名与 Architecture.md §5
    不一致：addGoal()、updateGoal() 无参（Arch 要求带
    title/deadline/description/Goal），缺 goals:
    Flow<List<Goal>>、observeGoalDetail；GoalRepositoryImpl 所有方法均 TODO()
  ────────────────────────────────────────
  Task: 3.7 ProgressRepository
  当前状态: 接口仅含 recordProgress，缺 observeProgressHistory:
    Flow<List<ProgressRecord>>；Impl 全部 TODO()
  ────────────────────────────────────────
  Task: 3.8 WidgetDataProvider
  当前状态: widget/ 包不存在

  Phase 5–10 全部未启动

  - Phase 5 (Goal Feature 5.1-5.8) — 0/8
  - Phase 6 (Dashboard & Settings 6.1-6.4) — 0/4（注：6.3
  UserPreferencesRepository 已存在接口骨架，但实现为 TODO()）
  - Phase 7 (Worker & Widget 7.1-7.3) — Worker 类骨架已建（DailyResetWorker /
  ReminderWorker / WidgetRefreshWorker），doWork() 全部返回 Result.success()
    空实现；Widget 完全未启动；调度逻辑未实现
  - Phase 8 (Notification 8.1-8.2) — 8.1 NotificationChannels.kt
  已部分实现；NotificationHelper.kt 缺失
  - Phase 9 (Testing 9.1-9.4) — 测试依赖在 app/build.gradle.kts:130-139
  已声明，但 app/src/test/、app/src/androidTest/ 目录均为空
  - Phase 10 (Release 10.1-10.3) — 0/3，且 app/build.gradle.kts:32
  isMinifyEnabled = false（待 Task 10.2）

---
  四、架构问题（Architecture Drift）

  A1【HIGH】Repository 引入 Interface + Impl 双层结构（未授权）

  - 位置：data/repository/GoalRepository.kt（interface）+
  data/repository/impl/GoalRepositoryImpl.kt（class）+ di/RepositoryModule.kt 用
     @Binds 绑定
  - Architecture.md §5 明确示例为：@Singleton class GoalRepository @Inject
  constructor(...)，由 Hilt 构造器注入直接提供
  - tasks.md Task 2.3 Notes 也写明："若 Repository 使用 @Inject
  constructor，无需 @Provides，可将此 Module 简化为空"
  - 影响：违反 §2.3 "禁止过度设计"原则，且令后续 Task 3.6/3.7
  实现路径偏离设计文档；@Binds 模板需在每次新增 Repository 时维护

  A2【MEDIUM】引入未声明包 common/ 与 data/repository/impl/

  - app/src/main/java/com/goalwall/common/GoalWallDispatchers.kt —
  Architecture.md §1 目录结构未列出 common/ 包
  - data/repository/impl/ 子包同样不在 Arch §1 / tasks.md §1 模块结构中
  - detekt.yml:73-80 ForbiddenImport.excludes 中已临时增加 common/、domain/ 例外
   — 暗示绕过了既定边界
  - 影响：违反 tasks.md §3.6 "防止架构漂移规则"

  A3【MEDIUM】引入未声明的 Dispatcher 注入框架

  - di/DispatchersModule.kt、di/IoDispatcher.kt、common/GoalWallDispatchers.kt —
   Architecture.md 完全未提及 @IoDispatcher Qualifier 或自定义 Dispatcher
  - Repository 设计原则 §5 强调 Repository 只做数据聚合，不涉及线程切换决策（由
  Room/Flow 自身管理）
  - 在 GoalRepositoryImpl.kt:18 / ProgressRepositoryImpl.kt:16 / 三个 Worker
  中都注入了 @IoDispatcher 但都标注
    @Suppress("UnusedPrivateProperty")，目前完全未使用
  - 影响：YAGNI 违反，增加复杂度且阻塞 Phase 3 后续 Task

  A4【MEDIUM】Domain 包占位预留

  - detekt.yml:79 ForbiddenImport.excludes 包含 **/domain/**
  - Architecture.md §10 "扩展性禁忌" 明确："❌ 不要提前引入 UseCaseInteractor
  层"
  - 影响：为禁止的 UseCase 层留下了通道

  A5【MEDIUM】命名偏离 spec

  Spec: AppNavHost.kt (Task 4.3)
  实际: GoalWallNavHost.kt
  影响: 文件名约定不一致
  ────────────────────────────────────────
  Spec: WidgetSyncWorker (Task 7.1)
  实际: WidgetRefreshWorker
  影响: 后续工作链需重命名或文档同步
  ────────────────────────────────────────
  Spec: UserPreferences (Arch §10)
  实际: UserPreferencesRepository
  影响: 仓库语义混淆（DataStore 不是 Repository）
  ────────────────────────────────────────
  Spec: 新增 DailyResetWorker.kt
  实际: 无对应 Task / Arch 章节
  影响: 越界产物

  A6【LOW】MainActivity 缺底部导航

  - MainActivity.kt:18-23 只调用 GoalWallNavHost(modifier =
  Modifier.fillMaxSize())，缺 Scaffold + NavigationBar（Task 4.4 Step 2-3
    强制要求）

---
  五、Room 问题

  R1【HIGH】Entity 字段语义与 Architecture.md §6 严重不一致

  Architecture.md §6（设计）: progress: Float (0.0~1.0)
  当前实现 GoalEntity.kt: ❌ 替换为 targetValue: Int + currentValue: Int + unit:

    String
  ────────────────────────────────────────
  Architecture.md §6（设计）: deadline: Long?
  当前实现 GoalEntity.kt: ❌ 重命名为 targetDate: Long?，并新增 startDate: Long
  ────────────────────────────────────────
  Architecture.md §6（设计）: color: String = "#4F8EF7"
  当前实现 GoalEntity.kt: ❌ 缺失
  ────────────────────────────────────────
  Architecture.md §6（设计）: isArchived: Boolean = false
  当前实现 GoalEntity.kt: ❌ 替换为 status:
    GoalStatus（枚举：ACTIVE/COMPLETED/PAUSED/ARCHIVED）
  ────────────────────────────────────────
  Architecture.md §6（设计）: createdAt = System.currentTimeMillis() 默认
  当前实现 GoalEntity.kt: 改为强制参数（createdAt: Long）

  类似地：
  - MilestoneEntity.kt:24-25 新增 targetValue: Int，isDone → completed
  - ProgressEntity.kt:23-25 value: Float → value: Int，新增 recordDate: Long

  影响：
  1. 后续 §5 Repository 接口签名（如 updateProgress(goalId, progress: Float)）与
   Entity 不再兼容
  2. UI 端 progress: Float 一路贯穿（如 GoalEdit/Slider 0~1）实现路径阻塞
  3. 后续 DAO 查询 SELECT * FROM goals WHERE isArchived = 0
  直接编不过（字段不存在）
  4. tasks.md Task 3.1 Acceptance "三个 Entity 字段与 Architecture.md §6
  完全一致" 未达成
  5. Architecture §10 已声明 priority
  字段是迁移示例，但此处已自行扩展字段属于架构未授权改动

  R2【HIGH】导出 Schema 与当前 Entity 不一致

  - app/schemas/com.goalwall.data.db.GoalWallDatabase/1.json:9 的 goals 表
  CREATE SQL 仅有 id + createdAt 两列
  - 但当前 GoalEntity.kt 共 11 列
  - 推断：Task 2.2 时（占位 Entity 仅 id + createdAt）执行过 KSP 生成；Task 3.1
  扩展字段后未重新生成 schema（或源已提交，但本地未重新编译并 commit）
  - 影响：CI 编译会重写 schema，差异引发后续 Migration 验证错配；如果误将旧
  schema 投放至发行版，未来增加 Migration 时回放测试会失败

  R3【MEDIUM】DAO 集合空，违背 Task 3.2 Acceptance

  - GoalWallDatabase.kt:25-29 三个 DAO 抽象方法已声明，但 DAO 体均仅 @Insert
  占位
  - Task 3.2 Acceptance 不要求 DAO 内部满足，但 Task 3.3/3.4 显然未启动

  R4【LOW】引入 TypeConverter（超出 Arch 范围）

  - GoalStatusConverter.kt + @TypeConverters(GoalStatusConverter::class)
  (Database.kt:14)
  - Architecture.md §6 未提及 TypeConverter；其存在是 R1（用枚举替换 isArchived:
   Boolean）的连锁产物
  - 良性写法上 OK，但属于架构外引入

  R5【LOW】缺少 GoalWithMilestones Relation

  - Architecture.md §6 明确定义；当前 data/db/dao/ 下不存在该文件 — 阻塞 Task
  3.3 observeWithMilestones

---
  六、Hilt 问题

  H1【HIGH — 同 A1】Binds 模式与 Architecture 直注入冲突

  - RepositoryModule.kt:17-29 用 abstract class + 三个 @Binds
  - 与 Arch §5 设计模式（构造器注入 + 无需 Module）相悖
  - 但技术上正确：@Binds + Impl 也能完成依赖图

  H2【MEDIUM】WorkerModule 名不符实

  - di/WorkerModule.kt:7-19 内容只是 object 常量（Tags、UniqueWorkNames），既无
  @Module 也无 @Provides
  - Task 2.4 期望此处提供 HiltWorkerFactory — 实际 factory 是 hilt-work
  自动生成、在 GoalWallApp.kt:13 @Inject lateinit var workerFactory:
    HiltWorkerFactory 直接拿到，故功能不缺，但文件命名误导（不像 Module）

  H3【LOW】DI 多出 DispatchersModule

  - 与 A3 同源 — DispatchersModule 是为 Dispatcher
  注入而设，但目前下游全部未用，纯死代码

  H4【LOW】Qualifier 数量

  - 仅 @IoDispatcher；常规模式还应有 @DefaultDispatcher /
  @MainDispatcher。当前未使用，问题暂不显化

---
  七、WorkManager 问题

  W1【OK】HiltWorkerFactory 注入正确

  - GoalWallApp.kt:11-24 实现 Configuration.Provider，注入
  HiltWorkerFactory，覆盖 workManagerConfiguration getter ✓
  - Manifest:16-25 已 tools:node="remove" androidx.work WorkManagerInitializer ✓

  W2【MEDIUM】Worker 实现为空骨架

  - ReminderWorker.kt:22、WidgetRefreshWorker.kt:22、DailyResetWorker.kt:22 三者
   doWork() 直接 Result.success()
  - goalRepository / progressRepository / ioDispatcher 全部
  @Suppress("UnusedPrivateProperty") — 注入但未使用
  - 未启动 Task 7.1/7.3 实质逻辑

  W3【MEDIUM】调度链缺失

  - GoalWallApp.onCreate() 未调用 scheduleReminders() / enqueueWidgetSync()
  任何调度入口
  - Architecture §4 / §9 / Task 7.3 强制要求

  W4【LOW】DailyResetWorker 越界

  - Architecture.md / tasks.md 均无 "Daily Reset"
  概念，建议确认其是否计划内（否则属架构漂移产物）

---
  八、Static Analysis 覆盖审查

  检查项: ktlint
  状态: ✓ 已启用（build.gradle.kts:60-63，ignoreFailures=false）
  备注: OK
  ────────────────────────────────────────
  检查项: detekt
  状态: ✓ 已启用，buildUponDefaultConfig=true
  备注: OK
  ────────────────────────────────────────
  检查项: ForbiddenImport (detekt.yml:66-79)
  状态: 部分有效
  备注: 禁 androidx.room / data.db / data.repository 在非
    data/di/worker/widget/notification/common/domain 下；有效防 UI 层泄漏；但
    common/、domain/ 例外是 A2/A4 后门
  ────────────────────────────────────────
  检查项: 防 collectAsState() 滥用
  状态: ❌ 未配置规则
  备注: tasks.md §3.4 强制要求 collectAsStateWithLifecycle，未加 detekt 规则保护
  ────────────────────────────────────────
  检查项: 防 runBlocking、GlobalScope
  状态: ❌ 未配置
  备注: R7 协程泄漏风险无静态保护
  ────────────────────────────────────────
  检查项: 防 var 字段 in UiState
  状态: ❌ 未配置
  备注: "UiState immutable" 规则无静态保护
  ────────────────────────────────────────
  检查项: MagicNumber
  状态: ✓ 已配置白名单
  备注: OK
  ────────────────────────────────────────
  检查项: WildcardImport
  状态: ✓ 启用
  备注: OK
  ────────────────────────────────────────
  检查项: 主要架构风险覆盖度评估
  状态: 约 40%
  备注: 仅覆盖 ForbiddenImport，未覆盖三大约束（生命周期收集、不可变
    UiState、协程作用域）

---
  九、风险汇总

  High Risk

  ID: H-1
  风险: GoalEntity / MilestoneEntity / ProgressEntity 字段全面背离 §6
    设计，后续所有 Repository / ViewModel / UI 都将无法按 Architecture.md
    拼装；Arch 中 progress: Float、isArchived: Boolean、color: String
    被替换为完全不同的语义模型
  来源: R1
  ────────────────────────────────────────
  ID: H-2
  风险: Repository 强行多了一层 interface + Impl，且 RepositoryModule 已落地
    @Binds 模板，后续每个仓库都被迫复制此模板；与 Architecture §5 "Repository
    设计原则" 直接冲突，AI 协作模板会持续放大
  来源: A1
  ────────────────────────────────────────
  ID: H-3
  风险: 已导出 schema (app/schemas/.../1.json) 与当前 Entity
    严重不一致，下一次构建会重写 1.json，git diff 将变得巨大；若旧版 schema
    已经投放过任何位置，Migration 测试会破产
  来源: R2
  ────────────────────────────────────────
  ID: H-4
  风险: Phase 3 主体未做完即开启了 Phase 7 Worker 骨架（DailyResetWorker /
    WidgetRefreshWorker / ReminderWorker），违反 tasks.md §4 "禁止在前序 Phase
    未完成的情况下启动后续 Phase" 铁律
  来源: W2

  Medium Risk

  ID: M-1
  风险: common/、domain/ 例外路径在 detekt 中已被打开，AI 后续可能借此把不属于
    data 的代码塞入 common/，形成长期腐化通道
  来源: A2 / A4
  ────────────────────────────────────────
  ID: M-2
  风险: DispatchersModule + @IoDispatcher 注入了一整套未使用的依赖，且
  Repository
    注入时全部 @Suppress("UnusedPrivateProperty") — 后续 Task 3.6
    实现时极易演变成"Repository 内启动协程"（违反 §2.5 / R7）
  来源: A3
  ────────────────────────────────────────
  ID: M-3
  风险: WorkerModule.kt 不是真正 @Module，名称误导；后续 AI 会困惑此处是否需要补

    @Provides
  来源: H2
  ────────────────────────────────────────
  ID: M-4
  风险: Screen 路由 + NavHost 仅含 Home 一项，但 commits 表明 Phase 1/2
    已完结、Task 3.1 已完成 — 与 Phase 4 整体的进度同步不一致
  来源: 三、Phase 4
  ────────────────────────────────────────
  ID: M-5
  风险: detekt 未覆盖 "UiState immutable / collectAsStateWithLifecycle /
    GlobalScope" 三大架构铁律，护栏只剩 ForbiddenImport 一层
  来源: 八

  Low Risk

  ID: L-1
  风险: app/build.gradle.kts:32 isMinifyEnabled = false，符合当前阶段，Phase 10
    需切换
  来源: —
  ────────────────────────────────────────
  ID: L-2
  风险: applicationId = "com.goalwall" 与开发者预期一致，但未配置 debug
    applicationIdSuffix，未来 debug/release 共安装会冲突
  来源: —
  ────────────────────────────────────────
  ID: L-3
  风险: 通知渠道名 "目标提醒" / "里程碑达成" / "每日汇总"
    硬编码中文（NotificationChannels.kt:22,27,32），违反 §2.6
    "错误信息使用字符串资源" 精神（不止错误信息，all 用户可见文案均应资源化）
  来源: —
  ────────────────────────────────────────
  ID: L-4
  风险: app/src/test、app/src/androidTest 目录均不存在，但测试依赖已声明（Phase
  9
    预备，符合阶段）
  来源: —
  ────────────────────────────────────────
  ID: L-5
  风险: Architecture.md 要求 minSdk=26（Android 8.0），实际 libs.versions.toml:7

    minSdk = "29"（Android 10）— 与 Architecture
    不一致但更安全；建议确认是否有意调整
  来源: —
  ────────────────────────────────────────
  ID: L-6
  风险: App 图标使用 ic_launcher_foreground（drawable）作为
    android:icon（Manifest:11），未走 mipmap-anydpi-v26 adaptive icon — 不阻断
  来源: —

---
  十、下一推荐 Task

  主线推荐：先回退架构漂移 → 再继续 Task 3.3

  执行顺序建议（不立即写代码，先开会对齐）：

  Step 0 — 架构决策对齐（人工，需在写代码前）

  需要开发者裁决以下 3 个偏离点，二选一：

  1. Entity 字段语义：是按 Architecture.md §6 回退（progress: Float /
  isArchived: Boolean / color），还是更新 Architecture.md 接受当前
    targetValue/currentValue/unit/status 模型？两者必须同步，否则 Phase 3
    后续任务全部阻塞。
  2. Repository interface + Impl 模式：是回退为 Arch §5 直注入，还是更新
  Architecture.md 明确允许此模式？影响 Task 3.6 / 3.7 / 6.3 实现路径。
  3. @IoDispatcher / DispatchersModule / common/ 包：保留还是删除？影响 R7
  十、下一推荐 Task

  主线推荐：先回退架构漂移 → 再继续 Task 3.3

  执行顺序建议（不立即写代码，先开会对齐）：

  Step 0 — 架构决策对齐（人工，需在写代码前）

  需要开发者裁决以下 3 个偏离点，二选一：

  1. Entity 字段语义：是按 Architecture.md §6 回退（progress: Float / isArchived: Boolean / color），还是更新 Architecture.md 接受当前 targetValue/currentValue/unit/status 模型？两者必须同步，否则 Phase 3
  后续任务全部阻塞。
  2. Repository interface + Impl 模式：是回退为 Arch §5 直注入，还是更新 Architecture.md 明确允许此模式？影响 Task 3.6 / 3.7 / 6.3 实现路径。
  3. @IoDispatcher / DispatchersModule / common/ 包：保留还是删除？影响 R7 协程作用域约束如何落地。

  Step 1 — 修复架构漂移（fix/arch-drift 分支）

  按 tasks.md §3.6 规定："发现架构偏离时，立即停止当前 Task，提交 fix/arch-drift-xxx 分支修复，不继续下一 Task"，建议拆分为：

  - fix/arch-drift-entity-fields — 同步 Entity 与 Arch §6
  - fix/arch-drift-repository-pattern — 决定 interface vs 直注入并统一
  - fix/arch-drift-remove-unused-dispatchers — 移除或落地 @IoDispatcher

  Step 2 — 补 Task 3.3 GoalDao（架构对齐后）

  依据 tasks.md Task 3.3 严格实现 observeAll、observeWithMilestones、getTopByProgress、update、deleteById、updateProgress 全部方法，同步引入 GoalWithMilestones Relation 类。

  Step 3 — 补强 detekt 护栏（建议作为 chore）

  新增规则覆盖：
  - 禁 collectAsState( 直接调用（强制 collectAsStateWithLifecycle）
  - 禁 GlobalScope / runBlocking 在生产代码中出现
  - 禁 UiState data class 含 var 字段（手写规则或借 detekt 自定义）

  Step 4 — 回归 Phase 4 未完成项

  补 Task 4.2 / 4.3 / 4.4 缺失的路由（GoalList/Dashboard/Settings/GoalDetail/GoalEdit）、BottomNavigationBar、Scaffold；以及 Task 3.5 UI Model + Mappers（被 Phase 4/5 重度依赖）。

  Step 5 — 暂停 Worker 进一步开发

  ReminderWorker / WidgetRefreshWorker / DailyResetWorker 的实质实现应回到 Phase 7（依赖 Task 3.6 GoalRepository / Task 3.8 WidgetDataProvider），当前阶段保持骨架即可，避免在 Repository 未就绪前注入空壳依赖加深
  A3 风险。

---
  总评

  - 声明的进度（commit 提示 Phase 1/2 + Task 3.1 完成）与实际状态偏差较大：Phase 2 完成但引入了未授权架构、Task 3.1 字段语义未对齐设计、Phase 4 / 7 / 8 已经"提前开工"但都停在空骨架阶段。
  - 真实状态：Phase 1 完成、Phase 2 完成（带漂移）、Phase 3 仅 3.1 / 3.2 完成（带漂移）、Phase 4 部分（含漂移）、Phase 7/8 越界开工。
  - 最大风险来自 R1 + A1 的复合：Entity 模型一旦带漂移走到 Repository，整个数据流契约都会跑偏；建议立刻进入 fix/arch-drift 通道，不要直接执行 Task 3.3。

  ▎ 本审计仅基于源码静态读取，未执行任何构建命令、未触发 KSP 代码生成，未对工程做出任何修改。

✻ Crunched for 4m 51s