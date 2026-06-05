 GoalWall 深度审计报告

  审计范围

  - Architecture.md v1.0 全部约束项
  - tasks.md 核心业务边界（进度语义、状态模型、分层规则）
  - 全部 42 个 .kt 源文件

  ---
  一、分层架构违规检查

  结论：当前实现基本满足 Architecture.md 的分层要求

  关键分层检查结果：

  检查项: ViewModel 未 import Room Entity
  结果: ✅ 通过
  备注: 所有 VM 仅依赖 Repository + Model
  ────────────────────────────────────────
  检查项: Screen 未直接访问 Repository
  结果: ✅ 通过
  备注: 全部通过 hiltViewModel() 注入
  ────────────────────────────────────────
  检查项: Widget → WidgetDataProvider → DAO
  结果: ✅ 通过
  备注: 使用 EntryPoint 模式
  ────────────────────────────────────────
  检查项: Repository 无 withContext(IO)
  结果: ✅ 通过
  备注: 无 Dispatcher 注入
  ────────────────────────────────────────
  检查项: interface + Impl + @Binds
  结果: ✅ 已消除
  备注: 上次审计后已回退为单类注入
  ────────────────────────────────────────
  检查项: UI → ViewModel → Repository → DAO
  结果: ✅ 通过
  备注: 单向依赖链路完整

  ---
  二、发现的问题清单

  【严重】×3

  严重 #1：WidgetRefreshWorker 和 DailyResetWorker 为空壳死代码

  文件：worker/WidgetRefreshWorker.kt:19、worker/DailyResetWorker.kt:19

  // WidgetRefreshWorker.kt — 注入 Repository 但从不使用
  @HiltWorker
  class WidgetRefreshWorker @AssistedInject constructor(
      @Assisted appContext: Context,
      @Assisted workerParams: WorkerParameters,
      @Suppress("UnusedPrivateProperty") private val goalRepository:
  GoalRepository,  // ← 未使用
  ) : CoroutineWorker(appContext, workerParams) {
      override suspend fun doWork(): Result = Result.success()  // ← 空壳
  }

  影响：
  - 污染 Hilt DI 依赖图
  - @Suppress("UnusedPrivateProperty") 掩盖设计缺陷，易被后续 AI 误用
  - 如果 WorkManager 调度了它们，会浪费系统电量

  修复：删除这两个文件。当前 Widget 同步已由 WidgetSyncWorker
  完成；每日重置逻辑可以在未来需要时再实现。

  ---
  严重 #2：GoalDetailScreen 重复实现进度计算（破坏单一真相源）

  文件：ui/goal/GoalDetailScreen.kt:187-192

  // ❌ 当前：在 Screen 中重新手写进度公式
  private fun GoalDetailContent(...) {
      val goal = detail.goal
      val derivedProgress =
          if (goal.targetValue > 0) {
              (goal.currentValue.toFloat() /
  goal.targetValue.toFloat()).coerceIn(0f, 1f)
          } else {
              0f
          }
      // ...
  }

  问题：Goal.progress 属性（data/model/Goal.kt:22-28）已包含完全相同的除零保护和
   clamp 逻辑。Screen 绕过 Model
  的派生属性，未来若修改公式（如改为权重进度）会漏改此处。

  修复：直接使用 goal.progress

  // ✅ 修复后
  private fun GoalDetailContent(...) {
      val goal = detail.goal
      val progressPercent = (goal.progress * 100).toInt()
      // ...
          GoalProgressBar(
              progress = goal.progress,  // 直接使用 Model 属性
              modifier = Modifier.fillMaxWidth(),
          )
  }

  ---
  严重 #3：DatabaseModule 缺少 Migration 回退策略

  文件：di/DatabaseModule.kt:29-33

  // ❌ 当前：无 migration 回退，版本号递增即崩溃
  fun provideGoalWallDatabase(@ApplicationContext context: Context):
  GoalWallDatabase =
      Room.databaseBuilder(context, GoalWallDatabase::class.java,
  "goalwall_database")
          .build()

  问题：Architecture.md §10 明确要求 addMigrations(...)。当前 schema version=1
  没有问题，但 Phase 9 测试时会频繁改 schema，一旦 version 变成 2 而无 Migration
   实现，用户设备直接崩溃。

  修复：

  // ✅ 修复后
  fun provideGoalWallDatabase(@ApplicationContext context: Context):
  GoalWallDatabase =
      Room.databaseBuilder(context, GoalWallDatabase::class.java,
  "goalwall_database")
          .fallbackToDestructiveMigration()  // 开发阶段安全回退
          .build()

  ▎ 正式发布前将 fallbackToDestructiveMigration() 替换为真实的 Migration(1, 2) {
  ▎  ... }。

  ---
  【警告】×3

  警告 #4：ReminderWorker 全量加载目标仅为了计数

  文件：worker/ReminderWorker.kt:28-30

  // ❌ 当前：加载全部 Goal 对象到内存，只为了 count
  val activeCount = goalRepository.goals.first()
      .count { goal -> goal.status == GoalStatus.ACTIVE }

  问题：随着目标数量增长（如
  1000+），每次提醒都加载全部目标到内存是无效开销。数据库层可以直接完成计数。

  修复：

  在 GoalDao 中新增计数方法，在 GoalRepository 中暴露：

  // GoalDao.kt — 新增
  @Query("SELECT COUNT(*) FROM goals WHERE status = 'ACTIVE'")
  suspend fun countActive(): Int

  // GoalRepository.kt — 新增
  suspend fun countActiveGoals(): Int = goalDao.countActive()

  // ReminderWorker.kt — 改为
  val activeCount = goalRepository.countActiveGoals()

  ---
  警告 #5：GoalEditViewModel.saveGoal() 反向字符串依赖

  文件：ui/goal/GoalEditViewModel.kt:101-106

  // ❌ 当前：Screen 将 stringResource 传给 ViewModel，由 VM 存入 UiState
  fun saveGoal(
      titleRequiredError: String,   // ← 来自 UI 层的字符串
      targetValueError: String,
      unitRequiredError: String,
      dataNotReadyError: String,
  ) {
      if (state.title.isBlank()) {
          _uiState.update { it.copy(titleError = titleRequiredError) }
          // ...

  问题：违反 Architecture.md §2.6 的"错误信息使用
  strings.xml"原则——这里的字符串虽然来自 strings.xml，但流向是 Screen →
  ViewModel → UiState → Screen，形成了闭环。ViewModel 理论上不应感知 UI
  字符串的内容。

  修复：ViewModel 使用错误类型枚举，Screen 负责映射到字符串：

  // ✅ GoalEditUiState.kt
  data class GoalEditUiState(
      // ...
      val titleError: ValidationError? = null,
  )

  enum class ValidationError { TITLE_REQUIRED, TARGET_VALUE_INVALID,
  UNIT_REQUIRED }

  // GoalEditScreen.kt — 映射
  val titleErrorText = when (uiState.titleError) {
      ValidationError.TITLE_REQUIRED ->
  stringResource(R.string.goal_edit_error_title_required)
      null -> null
      else -> null
  }

  ---
  警告 #6：DashboardViewModel 仅统计非归档目标

  文件：ui/dashboard/DashboardViewModel.kt:31-32

  // 当前使用 goals（排除 ARCHIVED），归档目标完全不可见
  goalRepository.goals
      .onEach { goals ->

  问题：当用户归档一个目标后，Dashboard 的 totalGoals
  计数会减少，可能让用户困惑。Architecture.md §5 定义了 goals（非归档）和
  allGoals（含归档），Dashboard 应显式决定使用哪个数据源。

  建议：如果产品意图是展示"活跃中的目标总数"，可在 Dashboard
  标题中明确（如"进行中目标：3"），避免静默的语义分歧。

  ---
  【优化建议】×3

  优化 #7：GoalEditScreen 预设颜色列表硬编码

  文件：ui/goal/GoalEditScreen.kt:66-73

  private val presetColors = listOf("#4F8EF7", "#F7724F", "#4FD9B3", "#F7D44F",
  "#A04FF7")

  建议：提取到 ui/theme/Color.kt 或 strings.xml 数组中，便于统一管理品牌色。

  ---
  优化 #8：SettingsUiState 为内联定义，未遵循约定

  文件：ui/settings/SettingsViewModel.kt:21-24

  // 当前在 ViewModel 文件内定义
  data class SettingsUiState(
      val reminderEnabled: Boolean = true,
      val themeMode: String = "system",
  )

  建议：按照 GoalListUiState.kt / GoalDetailUiState.kt / GoalEditUiState.kt
  的命名惯例，提取到独立的 ui/settings/SettingsUiState.kt 文件，保持一致性。

  ---
  优化 #9：GoalDetailViewModel 进度变更无并发保护

  文件：ui/goal/GoalDetailViewModel.kt:87-106

  fun setCurrentValue(newValue: Int, note: String? = null) {
      viewModelScope.launch {
          val detail = _uiState.value.detail ?: return@launch
          val oldValue = detail.goal.currentValue
          // ↑ 在 launch 内部读取，存在 TOCTOU 风险
          goalRepository.updateCurrentValue(goalId, boundedNewValue)

  问题：用户在详情页快速连点 +/- 按钮时，两个协程可能读到相同的 oldValue，导致
  delta 计算错误（少计增量）。

  修复：使用 Mutex 保护临界区：

  private val progressMutex = Mutex()

  fun setCurrentValue(newValue: Int, note: String? = null) {
      viewModelScope.launch {
          progressMutex.withLock {
              // 在锁内读取旧值、计算 delta、写入
          }
      }
  }

  ---
  三、tasks.md 核心业务边界验证

  tasks.md 约束: ProgressEntity.value = 当次增量（delta）
  当前实现: ProgressRepository.recordProgress 以 delta 写入
  状态: ✅
  ────────────────────────────────────────
  tasks.md 约束: setCurrentValue 先算 delta 再调 recordProgress
  当前实现: GoalDetailViewModel:99 delta = boundedNewValue - oldValue
  状态: ✅
  ────────────────────────────────────────
  tasks.md 约束: delta == 0 时不写历史
  当前实现: GoalDetailViewModel:101 if (delta != 0)
  状态: ✅
  ────────────────────────────────────────
  tasks.md 约束: Dashboard 基于增量聚合
  当前实现: 直接使用 Goal.progress 派生属性
  状态: ⚠️ 见警告 #6
  ────────────────────────────────────────
  tasks.md 约束: Goal.unit 字段支持"次/km/小时"等
  当前实现: Entity + Model 均含 unit: String
  状态: ✅
  ├────────────────────────────────────────────────┼───────────────────────────────────────────────────────────┼──────────────┤
  │ setCurrentValue 先算 delta 再调 recordProgress │ GoalDetailViewModel:99 delta = boundedNewValue - oldValue │ ✅           │
  ├────────────────────────────────────────────────┼───────────────────────────────────────────────────────────┼──────────────┤
  │ delta == 0 时不写历史                          │ GoalDetailViewModel:101 if (delta != 0)                   │ ✅           │
  ├────────────────────────────────────────────────┼───────────────────────────────────────────────────────────┼──────────────┤
  │ Dashboard 基于增量聚合                         │ 直接使用 Goal.progress 派生属性                           │ ⚠️ 见警告 #6 │
  ├────────────────────────────────────────────────┼───────────────────────────────────────────────────────────┼──────────────┤
  │ Goal.unit 字段支持"次/km/小时"等               │ Entity + Model 均含 unit: String                          │ ✅           │
  ├────────────────────────────────────────────────┼───────────────────────────────────────────────────────────┼──────────────┤
  │ Widget 直接 DAO（轻量）                        │ WidgetDataProvider → GoalDao.getTopByProgress             │ ✅           │
  ├────────────────────────────────────────────────┼───────────────────────────────────────────────────────────┼──────────────┤
  │ Repository 不含业务逻辑                        │ 纯 Entity↔Model 映射 + DAO 透传                           │ ✅           │
  ├────────────────────────────────────────────────┼───────────────────────────────────────────────────────────┼──────────────┤
  │ 过滤/排序在 ViewModel 层                       │ GoalListViewModel.observeGoals() 中 combine + filter      │ ✅           │
  └────────────────────────────────────────────────┴───────────────────────────────────────────────────────────┴──────────────┘

  ---
  四、安全与性能综合评估

  ┌─────────────────┬───────────────────────────────────────────────────────────────────┐
  │      维度       │                               结论                                │
  ├─────────────────┼───────────────────────────────────────────────────────────────────┤
  │ SQL 注入        │ ✅ 安全 — Room 参数化查询全覆盖                                   │
  ├─────────────────┼───────────────────────────────────────────────────────────────────┤
  │ 硬编码密钥/凭证 │ ✅ 无 — 无认证系统                                                │
  ├─────────────────┼───────────────────────────────────────────────────────────────────┤
  │ 通知权限        │ ✅ 安全 — Android 13+ 检查 POST_NOTIFICATIONS                     │
  ├─────────────────┼───────────────────────────────────────────────────────────────────┤
  │ 输入校验        │ ✅ 满足 — title/targetValue/unit 均有空白和范围校验               │
  ├─────────────────┼───────────────────────────────────────────────────────────────────┤
  │ 大列表性能      │ ⚠️ 警告 #4 — ReminderWorker 全量加载仅用于计数                    │
  ├─────────────────┼───────────────────────────────────────────────────────────────────┤
  │ Widget 刷新     │ ✅ 合理 — 使用 OneTimeWorkRequest + REPLACE 策略防抖              │
  ├─────────────────┼───────────────────────────────────────────────────────────────────┤
  │ 主线程阻塞      │ ✅ 安全 — 所有 DB 操作 suspend，UI 用 collectAsStateWithLifecycle │
  └─────────────────┴───────────────────────────────────────────────────────────────────┘

  ---
  五、总结

  严重:  3 项 | 警告:  3 项 | 优化建议:  3 项
  评分:  架构一致性 8.5/10  |  安全性 9/10  |  性能 7.5/10

  最优先处理项：
  1. 删除 WidgetRefreshWorker 和 DailyResetWorker（严重 #1）
  2. 修复 GoalDetailScreen 中的重复进度计算（严重 #2）
  3. 添加 Room .fallbackToDestructiveMigration() 防止开发期崩溃（严重 #3）
  4. 优化 ReminderWorker 的全量加载为 COUNT(*) 查询（警告 #4）
