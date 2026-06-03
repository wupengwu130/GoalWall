# GoalWall — Android 工程架构设计文档

> 版本：1.0 | 技术栈：Kotlin · Jetpack Compose · Room · MVVM · StateFlow · Hilt · WorkManager · Glance Widget
> 定位：个人开发 · AI 协同友好 · 轻量可维护

---

## 目录

1. [项目目录结构](#1-项目目录结构)
2. [模块划分](#2-模块划分)
3. [数据流设计](#3-数据流设计)
4. [Widget 同步方案](#4-widget-同步方案)
5. [Repository 设计](#5-repository-设计)
6. [Room 数据结构](#6-room-数据结构)
7. [ViewModel 设计](#7-viewmodel-设计)
8. [状态管理方案](#8-状态管理方案)
9. [通知系统设计](#9-通知系统设计)
10. [后续扩展性设计](#10-后续扩展性设计)

---

## 1. 项目目录结构

```
GoalWall/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/goalwall/
│   │       │
│   │       ├── GoalWallApp.kt              # Application 入口，Hilt 注入点
│   │       ├── MainActivity.kt             # 唯一 Activity，承载 NavHost
│   │       │
│   │       ├── data/                       # 数据层
│   │       │   ├── db/
│   │       │   │   ├── GoalWallDatabase.kt
│   │       │   │   ├── dao/
│   │       │   │   │   ├── GoalDao.kt
│   │       │   │   │   ├── MilestoneDao.kt
│   │       │   │   │   └── ProgressDao.kt
│   │       │   │   └── entity/
│   │       │   │       ├── GoalEntity.kt
│   │       │   │       ├── MilestoneEntity.kt
│   │       │   │       └── ProgressEntity.kt
│   │       │   ├── model/                  # UI 消费的纯数据模型（非 Entity）
│   │       │   │   ├── Goal.kt
│   │       │   │   ├── Milestone.kt
│   │       │   │   └── GoalDetail.kt
│   │       │   └── repository/
│   │       │       ├── GoalRepository.kt
│   │       │       └── ProgressRepository.kt
│   │       │
│   │       ├── ui/                         # UI 层
│   │       │   ├── navigation/
│   │       │   │   ├── AppNavHost.kt       # 路由总图
│   │       │   │   └── Screen.kt          # sealed class 路由定义
│   │       │   ├── theme/
│   │       │   │   ├── Color.kt
│   │       │   │   ├── Theme.kt
│   │       │   │   └── Type.kt
│   │       │   ├── goal/                   # 目标功能模块
│   │       │   │   ├── GoalListScreen.kt
│   │       │   │   ├── GoalListViewModel.kt
│   │       │   │   ├── GoalDetailScreen.kt
│   │       │   │   ├── GoalDetailViewModel.kt
│   │       │   │   ├── GoalEditScreen.kt
│   │       │   │   ├── GoalEditViewModel.kt
│   │       │   │   └── components/
│   │       │   │       ├── GoalCard.kt
│   │       │   │       ├── ProgressBar.kt
│   │       │   │       └── MilestoneItem.kt
│   │       │   ├── dashboard/
│   │       │   │   ├── DashboardScreen.kt
│   │       │   │   ├── DashboardViewModel.kt
│   │       │   │   └── components/
│   │       │   │       └── SummaryCard.kt
│   │       │   └── settings/
│   │       │       ├── SettingsScreen.kt
│   │       │       └── SettingsViewModel.kt
│   │       │
│   │       ├── worker/                     # WorkManager 任务
│   │       │   ├── ReminderWorker.kt       # 定时提醒
│   │       │   └── WidgetSyncWorker.kt     # Widget 数据同步
│   │       │
│   │       ├── widget/                     # Glance Widget
│   │       │   ├── GoalWallWidget.kt       # Widget 主体
│   │       │   ├── GoalWallWidgetReceiver.kt
│   │       │   └── WidgetDataProvider.kt   # Widget 专用数据读取
│   │       │
│   │       ├── notification/
│   │       │   ├── NotificationHelper.kt
│   │       │   └── NotificationChannels.kt
│   │       │
│   │       └── di/                         # Hilt 依赖注入
│   │           ├── DatabaseModule.kt
│   │           ├── RepositoryModule.kt
│   │           └── WorkerModule.kt
│   │
│   ├── res/
│   │   ├── layout/                         # widget XML layout（Glance 需要）
│   │   ├── xml/
│   │   │   └── goalwall_widget_info.xml
│   │   └── values/
│   │
│   └── build.gradle.kts
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

> **设计原则**：每个文件职责单一，文件名即功能说明，无需查注释即可理解结构。

---

## 2. 模块划分

GoalWall 采用**单模块 + 包结构分层**，不引入 Gradle 多模块，适合个人开发速度与 AI 协作。

| 包名 | 职责 | 允许依赖 |
|---|---|---|
| `data/db` | Room Entity + DAO | 无外部依赖 |
| `data/model` | UI 数据模型（映射层） | `data/db` |
| `data/repository` | 数据聚合、Flow 暴露 | `data/db`, `data/model` |
| `ui/navigation` | 路由定义与 NavHost | `ui/**` |
| `ui/theme` | 设计系统 | 无 |
| `ui/goal` | 目标增删改查 UI + ViewModel | `data/repository` |
| `ui/dashboard` | 汇总视图 | `data/repository` |
| `ui/settings` | 用户偏好设置 | `data/repository` |
| `worker` | 后台定时任务 | `data/repository` |
| `widget` | Glance Widget | `data/repository` |
| `notification` | 通知工具类 | 无业务依赖 |
| `di` | Hilt Module | 所有层 |

```
依赖方向（单向）：
UI ──► ViewModel ──► Repository ──► DAO ──► Room
Widget ──► WidgetDataProvider ──► DAO（直接，轻量）
Worker ──► Repository
```

> **禁止**：UI 直接访问 DAO；ViewModel 直接 import Room Entity。

---

## 3. 数据流设计

### 主数据流（读）

```
Room（Flow<List<Entity>>）
    │
    ▼
GoalDao.observeAll()          ← 热流，数据库变更自动触发
    │
    ▼
GoalRepository.goals: Flow<List<Goal>>   ← Entity → Model 映射在此完成
    │
    ▼
GoalListViewModel.uiState: StateFlow<GoalListUiState>
    │
    ▼
GoalListScreen（Compose collectAsStateWithLifecycle）
```

### 写数据流

```
用户操作（点击/输入）
    │
    ▼
Screen 调用 ViewModel 函数（如 vm.addGoal(title, deadline)）
    │
    ▼
ViewModel 调用 Repository suspend fun
    │
    ▼
Repository 调用 DAO（insert / update / delete）
    │
    ▼
Room 写入 → Flow 自动 emit → UI 响应更新
```

### 副作用流（Widget / Notification）

```
数据写入完成
    │
    ├──► WorkManager 调度 WidgetSyncWorker（立即或延迟）
    │        │
    │        ▼
    │    GlanceAppWidgetManager.updateAll()
    │
    └──► 必要时触发 NotificationHelper（进度里程碑达成）
```

---

## 4. Widget 同步方案

### 架构选择

使用 **Glance + WorkManager** 组合，避免在每次数据变更时直接刷新 Widget（耗电）。

### 触发机制

| 场景 | 触发方式 |
|---|---|
| 用户修改目标数据 | Repository 写入后，调用 `enqueueWidgetSync()` |
| 每日定时刷新 | WorkManager `PeriodicWorkRequest`（每 6 小时）|
| 应用启动 | `GoalWallApp.onCreate()` 触发一次同步 |
| 系统重启 | `BootReceiver` → 重新调度 WorkManager |

### WidgetSyncWorker.kt

```kotlin
@HiltWorker
class WidgetSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val widgetDataProvider: WidgetDataProvider
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val goals = widgetDataProvider.getTopGoals()  // 直接 DAO，不走 ViewModel
        GoalWallWidget().updateAll(applicationContext)
        return Result.success()
    }
}
```

### WidgetDataProvider.kt

```kotlin
// Widget 专属数据层，轻量、无 Flow，只做一次性查询
class WidgetDataProvider @Inject constructor(
    private val goalDao: GoalDao
) {
    suspend fun getTopGoals(): List<Goal> =
        goalDao.getTopByProgress(limit = 3).map { it.toModel() }
}
```

### Widget UI（GoalWallWidget.kt）

```kotlin
class GoalWallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val goals = WidgetDataProvider(...).getTopGoals()
        provideContent {
            GlanceTheme {
                GoalWallWidgetContent(goals)
            }
        }
    }
}
```

### 调度工具函数（在 Repository 或 App 层调用）

```kotlin
fun Context.enqueueWidgetSync() {
    val request = OneTimeWorkRequestBuilder<WidgetSyncWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()
    WorkManager.getInstance(this).enqueue(request)
}
```

---

## 5. Repository 设计

### 设计原则

- Repository **只负责数据聚合**，不含业务逻辑判断
- 暴露 `Flow` 用于 UI 订阅，暴露 `suspend fun` 用于写操作
- 负责 Entity ↔ Model 的映射转换
- **不依赖** Context、ViewModel、任何 Android Framework（除 Room）
- **统一使用 `@Singleton class XxxRepository @Inject constructor(...)` 单类构造器注入**；**禁止** `interface + Impl + @Binds` 双层结构（单模块单实现下属于过度设计，违反 §10 "扩展性禁忌"；如未来出现远端数据源，扩展点在 Repository **内部**新增数据源，不另起类）
- **禁止在 Repository 层注入或持有 `CoroutineDispatcher`**（含 `@IoDispatcher` 等 Qualifier）。Room 的 `suspend fun` / `Flow` 已自动派发至内部 IO 池，DataStore 同样自带 IO 调度；Repository 内**不得**出现 `withContext(IO)` —— 线程切换由数据源内部保证

### GoalRepository.kt

```kotlin
@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val milestoneDao: MilestoneDao
) {
    // ── 读：Flow（Room 自动感知变更）──────────────────────────
    val goals: Flow<List<Goal>> =
        goalDao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeGoalDetail(goalId: Long): Flow<GoalDetail?> =
        goalDao.observeWithMilestones(goalId).map { it?.toDetail() }

    // ── 写：suspend fun ───────────────────────────────────────
    suspend fun addGoal(
        title: String,
        targetValue: Int,
        unit: String,
        startDate: Long,
        targetDate: Long? = null,
        description: String? = null,
        color: String = "#4F8EF7"
    ): Long = goalDao.insert(
        GoalEntity(
            title = title,
            description = description,
            targetValue = targetValue,
            currentValue = 0,
            unit = unit,
            startDate = startDate,
            targetDate = targetDate,
            status = GoalStatus.ACTIVE,
            color = color,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    )

    suspend fun updateGoal(goal: Goal) =
        goalDao.update(goal.toEntity())

    suspend fun deleteGoal(goalId: Long) =
        goalDao.deleteById(goalId)

    // 进度推进：直接写入整数 currentValue，UI 端的派生 progress 自动随 Flow 更新
    suspend fun updateCurrentValue(goalId: Long, currentValue: Int) {
        goalDao.updateCurrentValue(goalId, currentValue)
    }

    suspend fun setStatus(goalId: Long, status: GoalStatus) {
        goalDao.updateStatus(goalId, status)
    }

    suspend fun addMilestone(goalId: Long, title: String, targetValue: Int): Long =
        milestoneDao.insert(MilestoneEntity(goalId = goalId, title = title, targetValue = targetValue))

    suspend fun toggleMilestone(milestoneId: Long, completed: Boolean) =
        milestoneDao.setCompleted(milestoneId, completed)
}
```

### ProgressRepository.kt

```kotlin
@Singleton
class ProgressRepository @Inject constructor(
    private val progressDao: ProgressDao
) {
    fun observeProgressHistory(goalId: Long): Flow<List<ProgressRecord>> =
        progressDao.observeByGoal(goalId).map { it.map { e -> e.toModel() } }

    suspend fun recordProgress(goalId: Long, value: Int, note: String? = null) {
        progressDao.insert(
            ProgressEntity(
                goalId = goalId,
                value = value,
                note = note,
                recordDate = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
```

---

## 6. Room 数据结构

### GoalEntity.kt

```kotlin
@Entity(
    tableName = "goals",
    indices = [Index(value = ["title"])]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val targetValue: Int,                       // 目标值（如 100 次、5 km）
    val currentValue: Int = 0,                  // 当前累计值
    val unit: String,                           // 单位（如 "次"、"km"、"小时"）
    val startDate: Long,                        // 开始日期 Unix ms
    val targetDate: Long? = null,               // 截止日期 Unix ms（可空）
    val status: GoalStatus = GoalStatus.ACTIVE, // 详见下方枚举
    val color: String = "#4F8EF7",              // 目标卡片颜色
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

> **进度衍生策略**：UI 展示进度 = `currentValue / targetValue.toFloat()`，需在 Mapper / Goal Model 派生属性中做除零保护（`targetValue == 0` 时返回 `0f`，并 `coerceIn(0f, 1f)`）。**Entity 不持久化 `progress` 字段**，避免双写不一致；任何排序 / 过滤的"进度"逻辑均基于该派生值。

### GoalStatus.kt（枚举 + TypeConverter）

```kotlin
enum class GoalStatus {
    ACTIVE,      // 进行中（默认列表展示，计入活跃统计）
    COMPLETED,   // 已完成（保留在列表，可标识勋章）
    PAUSED,      // 暂停（不计入活跃统计，但不归档）
    ARCHIVED     // 已归档（默认列表过滤掉）
}

class GoalStatusConverter {
    @TypeConverter
    fun fromGoalStatus(status: GoalStatus): String = status.name

    @TypeConverter
    fun toGoalStatus(value: String): GoalStatus = GoalStatus.valueOf(value)
}
```

> `GoalWallDatabase` 类上需添加 `@TypeConverters(GoalStatusConverter::class)`。状态枚举为 OCP 友好的可扩展点 —— 未来增加 `OVERDUE` / `IN_REVIEW` 仅需扩展枚举值，不触发 schema 迁移（前提是已存在 `String` 列）。

### MilestoneEntity.kt

```kotlin
@Entity(
    tableName = "milestones",
    foreignKeys = [ForeignKey(
        entity = GoalEntity::class,
        parentColumns = ["id"],
        childColumns = ["goalId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("goalId")]
)
data class MilestoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val title: String,
    val targetValue: Int,                       // 达到该值视为里程碑完成（与 Goal.currentValue 对齐）
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
```

### ProgressEntity.kt

```kotlin
@Entity(
    tableName = "progress_records",
    foreignKeys = [ForeignKey(
        entity = GoalEntity::class,
        parentColumns = ["id"],
        childColumns = ["goalId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("goalId")]
)
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val value: Int,                              // 当次记录的整数增量（如本次跑 3km、做 20 个）
    val note: String? = null,
    val recordDate: Long = System.currentTimeMillis(),  // 业务记录日期（用户可选）
    val createdAt: Long = System.currentTimeMillis()    // 写入时间戳（系统）
)
```

### DAO 示例

```kotlin
@Dao
interface GoalDao {
    // 默认列表：排除已归档（PAUSED / COMPLETED 仍可见）
    @Query("SELECT * FROM goals WHERE status != 'ARCHIVED' ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun observeWithMilestones(goalId: Long): Flow<GoalWithMilestones?>

    // 派生进度排序：currentValue/targetValue，CASE WHEN 做除零保护
    @Query(
        """
        SELECT * FROM goals
        WHERE status = 'ACTIVE'
        ORDER BY (CAST(currentValue AS REAL) / CASE WHEN targetValue = 0 THEN 1 ELSE targetValue END) DESC
        LIMIT :limit
        """
    )
    suspend fun getTopByProgress(limit: Int): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GoalEntity): Long

    @Update
    suspend fun update(entity: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE goals SET currentValue = :currentValue, updatedAt = :now WHERE id = :id")
    suspend fun updateCurrentValue(id: Long, currentValue: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE goals SET status = :status, updatedAt = :now WHERE id = :id")
    suspend fun updateStatus(id: Long, status: GoalStatus, now: Long = System.currentTimeMillis())
}
```

### Relation 聚合对象

```kotlin
data class GoalWithMilestones(
    @Embedded val goal: GoalEntity,
    @Relation(parentColumn = "id", entityColumn = "goalId")
    val milestones: List<MilestoneEntity>
)
```

### Database

```kotlin
@Database(
    entities = [GoalEntity::class, MilestoneEntity::class, ProgressEntity::class],
    version = 1,
    exportSchema = true
)
abstract class GoalWallDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun progressDao(): ProgressDao
}
```

---

## 7. ViewModel 设计

### 核心规范

| 规则 | 说明 |
|---|---|
| ViewModel 不 import Room | 只依赖 Repository |
| 不直接持有 Entity | 只操作 Model / UiState |
| UiState 为 data class，immutable | 用 `copy()` 更新 |
| 副作用通过 `Channel<UiEvent>` 发出 | 导航、Toast 等一次性事件 |
| 所有异步在 `viewModelScope` 中 | 不泄漏协程 |

### GoalListViewModel.kt

```kotlin
@HiltViewModel
class GoalListViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val context: Application        // 仅用于 enqueueWidgetSync
) : ViewModel() {

    // ── UI State ───────────────────────────────────────────────
    private val _uiState = MutableStateFlow(GoalListUiState())
    val uiState: StateFlow<GoalListUiState> = _uiState.asStateFlow()

    // ── 一次性事件（导航、Snackbar）───────────────────────────
    private val _events = Channel<GoalListEvent>(Channel.BUFFERED)
    val events: Flow<GoalListEvent> = _events.receiveAsFlow()

    init {
        observeGoals()
    }

    private fun observeGoals() {
        goalRepository.goals
            .onEach { goals ->
                _uiState.update { it.copy(goals = goals, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            goalRepository.deleteGoal(goalId)
            context.enqueueWidgetSync()
            _events.send(GoalListEvent.ShowSnackbar("目标已删除"))
        }
    }

    fun archiveGoal(goalId: Long) {
        viewModelScope.launch {
            // 直接走专用 status setter，避免读—改—写竞态
            goalRepository.setStatus(goalId, GoalStatus.ARCHIVED)
        }
    }
}
```

### GoalDetailViewModel.kt

```kotlin
@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val progressRepository: ProgressRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val goalId: Long = checkNotNull(savedStateHandle["goalId"])

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    init {
        goalRepository.observeGoalDetail(goalId)
            .onEach { detail ->
                _uiState.update { it.copy(detail = detail, isLoading = false) }
            }
            .launchIn(viewModelScope)

        progressRepository.observeProgressHistory(goalId)
            .onEach { history ->
                _uiState.update { it.copy(progressHistory = history) }
            }
            .launchIn(viewModelScope)
    }

    fun setCurrentValue(currentValue: Int, note: String? = null) {
        viewModelScope.launch {
            goalRepository.updateCurrentValue(goalId, currentValue)
            progressRepository.recordProgress(goalId, currentValue, note)
        }
    }

    fun toggleMilestone(milestoneId: Long, completed: Boolean) {
        viewModelScope.launch {
            goalRepository.toggleMilestone(milestoneId, completed)
        }
    }
}
```

---

## 8. 状态管理方案

### UiState 设计规范

所有 UiState 为**不可变 data class**，ViewModel 通过 `copy()` 变更。

```kotlin
// GoalListUiState.kt
data class GoalListUiState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val filterType: GoalFilter = GoalFilter.ACTIVE
)

// GoalDetailUiState.kt
data class GoalDetailUiState(
    val detail: GoalDetail? = null,
    val progressHistory: List<ProgressRecord> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false
)

// GoalEditUiState.kt
data class GoalEditUiState(
    val title: String = "",
    val description: String = "",
    val targetValue: Int = 0,                          // 目标值（必填，> 0）
    val unit: String = "",                             // 单位（必填）
    val startDate: Long = System.currentTimeMillis(),  // 开始日期，默认今日
    val targetDate: Long? = null,                      // 截止日期，可空
    val color: String = "#4F8EF7",
    val isSaving: Boolean = false,
    val titleError: String? = null,                    // title 校验错误
    val targetValueError: String? = null,              // targetValue 校验错误（如 ≤ 0）
    val unitError: String? = null                      // unit 校验错误（如为空）
)
```

### 一次性事件（UiEvent）

```kotlin
// 不放入 UiState，用 Channel 处理
sealed class GoalListEvent {
    data class ShowSnackbar(val message: String) : GoalListEvent()
    data class NavigateToDetail(val goalId: Long) : GoalListEvent()
}

// Screen 中消费
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is GoalListEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            is GoalListEvent.NavigateToDetail -> navController.navigate(Screen.GoalDetail(event.goalId))
        }
    }
}
```

### StateFlow 收集规范

```kotlin
// ✅ 正确：感知生命周期，避免后台刷新
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// ❌ 错误：不感知生命周期
val uiState by viewModel.uiState.collectAsState()
```

### 过滤与排序（在 ViewModel 层处理）

```kotlin
// 不在 Repository 增加参数，而在 ViewModel combine filter state
val filteredGoals: StateFlow<List<Goal>> = combine(
    goalRepository.goals,
    _filterState
) { goals, filter ->
    when (filter) {
        GoalFilter.ACTIVE -> goals.filter { it.status == GoalStatus.ACTIVE }
        GoalFilter.ARCHIVED -> goals.filter { it.status == GoalStatus.ARCHIVED }
        GoalFilter.ALL -> goals
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

---

## 9. 通知系统设计

### 通知渠道

```kotlin
// NotificationChannels.kt
object NotificationChannels {
    const val REMINDER = "goalwall_reminder"
    const val MILESTONE = "goalwall_milestone"
    const val DAILY_SUMMARY = "goalwall_daily"
}

fun Context.createNotificationChannels() {
    val manager = getSystemService(NotificationManager::class.java)
    listOf(
        NotificationChannel(REMINDER, "目标提醒", NotificationManager.IMPORTANCE_DEFAULT),
        NotificationChannel(MILESTONE, "里程碑达成", NotificationManager.IMPORTANCE_HIGH),
        NotificationChannel(DAILY_SUMMARY, "每日汇总", NotificationManager.IMPORTANCE_LOW)
    ).forEach { manager.createNotificationChannel(it) }
}
```

### NotificationHelper.kt

```kotlin
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun showMilestoneAchieved(goalTitle: String, milestoneTitle: String) {
        val notification = NotificationCompat.Builder(context, MILESTONE)
            .setSmallIcon(R.drawable.ic_trophy)
            .setContentTitle("里程碑达成 🎉")
            .setContentText("$goalTitle · $milestoneTitle")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context)
            .notify(milestoneTitle.hashCode(), notification)
    }

    fun showDailyReminder(pendingGoalCount: Int) {
        val notification = NotificationCompat.Builder(context, DAILY_SUMMARY)
            .setSmallIcon(R.drawable.ic_goal)
            .setContentTitle("今日目标打卡")
            .setContentText("你有 $pendingGoalCount 个目标待更新")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(1001, notification)
    }
}
```

### ReminderWorker.kt

```kotlin
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val goalRepository: GoalRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val activeGoals = goalRepository.goals.first().filter { it.status == GoalStatus.ACTIVE }
        if (activeGoals.isNotEmpty()) {
            notificationHelper.showDailyReminder(activeGoals.size)
        }
        return Result.success()
    }
}
```

### WorkManager 调度（在 App 启动时注册）

```kotlin
// GoalWallApp.kt
fun scheduleReminders(context: Context) {
    val dailyReminder = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(calculateDelayToNineAM(), TimeUnit.MILLISECONDS)
        .addTag("daily_reminder")
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "daily_reminder",
        ExistingPeriodicWorkPolicy.KEEP,
        dailyReminder
    )
}
```

---

## 10. 后续扩展性设计

### 已预留扩展点

| 功能 | 扩展方式 | 影响范围 |
|---|---|---|
| 云同步（Firebase / Supabase）| Repository 增加远端数据源，本地优先 | 仅 Repository 层 |
| 多用户/账号 | Entity 增加 `userId` 字段，DAO 加过滤 | DB + Repository |
| 目标分类/标签 | 新增 `TagEntity` + 关联表 | DB + Repository + UI |
| 数据导出（CSV/PDF）| 新增 `ExportRepository` + Worker | 独立模块 |
| AI 建议（本地/云端）| 新增 `AiSuggestionViewModel` | 独立功能包 |
| 深色主题/主题切换 | `Theme.kt` 已用 MaterialTheme，DataStore 存储偏好 | 仅 UI 层 |
| 多语言 | 标准 Android `strings.xml` 结构已就绪 | res/values |
| Android 平板适配 | `WindowSizeClass` + 双栏布局，NavHost 可扩展 | `AppNavHost.kt` |

### DataStore 集成（用户偏好）

```kotlin
// 替代 SharedPreferences，适合设置项
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore = context.createDataStore("user_prefs")

    val reminderEnabled: Flow<Boolean> = dataStore.data.map { it[REMINDER_ENABLED] ?: true }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: "system" }

    suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[REMINDER_ENABLED] = enabled }
    }
}
```

### Room 数据库迁移预案

```kotlin
// 版本升级时，在 DatabaseModule 中添加 Migration
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE goals ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
    }
}

// Database builder 中注册
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_1_2)
    .build()
```

### 扩展性禁忌（避免过度设计）

```
❌ 不要提前引入 UseCaseInteractor 层
❌ 不要为每个 Entity 创建 Mapper 接口
❌ 不要引入 MVI 替换 MVVM（除非 UI 交互极度复杂）
❌ 不要将单模块拆分为 Gradle 多模块（个人项目无必要）
❌ 不要引入 Paging3（数据量 < 10000 时无必要）
```

---

## 附录：依赖配置参考

```kotlin
// build.gradle.kts（app）
dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.hilt:hilt-work:1.2.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Glance Widget
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.glance:glance-material3:1.1.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coil（图片，可选）
    implementation("io.coil-kt:coil-compose:2.7.0")
}
```

---

## 附录：README 模板（每模块）

```markdown
# [模块名] — GoalWall

## 职责
[一句话描述]

## 包含文件
- `XxxScreen.kt` — Compose UI，消费 UiState，发出用户事件
- `XxxViewModel.kt` — 状态持有，业务协调
- `components/` — 可复用 Compose 组件

## 对外依赖
- `XxxRepository`（通过 Hilt 注入）

## 禁止引入
- Room Entity / DAO
- Context（ViewModel 中尽量避免）

## AI 开发提示
> 新增功能请先更新 UiState，再同步 ViewModel 函数，最后修改 Screen。
```

---

*文档生成于 GoalWall v1.0 架构设计阶段。随项目迭代，请同步更新本文档。*
