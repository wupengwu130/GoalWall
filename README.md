# GoalWall

Keep your daily targets in sight with GoalWall — a customizable home screen widget for tracking small goals.

## 工程结构

单模块 `:app`，包分层对齐 `docs/Architecture.md`：

```
app/src/main/java/com/goalwall/
├── data/           # db、model、repository
├── ui/             # navigation、theme、功能 Screen
├── di/             # Hilt
├── worker/         # WorkManager
├── widget/         # Glance
├── notification/
└── common/
```

## 技术栈

- Kotlin · Jetpack Compose · Room · MVVM · StateFlow · Hilt · WorkManager · Glance

## 构建

```bash
.\gradlew.bat :app:assembleDebug
```

需安装 Android SDK，并配置 `local.properties`（`sdk.dir`）。

## Code Quality

GoalWall 使用 **ktlint** 与 **detekt** 作为 AI 协同开发的代码质量护栏，在提交前自动约束风格与分层边界。

| 工具 | 作用 |
|---|---|
| **ktlint** | Kotlin 代码风格（缩进、换行、import 等），与 `.editorconfig` 对齐 |
| **detekt** | 静态分析：复杂度、命名、潜在空安全，以及 `ui/` 层禁止直接依赖 Room / `data.db` / `data.repository` |

### 执行方式

```bash
# 风格检查（失败则构建失败）
.\gradlew.bat ktlintCheck

# 静态分析
.\gradlew.bat detekt

# 自动修复 ktlint 可修复项（提交前可选）
.\gradlew.bat ktlintFormat
```

### 提交前检查

建议在每次 commit / PR 前运行：

```bash
.\gradlew.bat ktlintCheck detekt
```

配置文件：

- `.editorconfig` — 编辑器与 ktlint 共享格式
- `detekt.yml` — detekt 规则（含架构 `ForbiddenImport`）

## CI

[GitHub Actions](.github/workflows/ci.yml) 在 **push**（`main` / `develop`）与 **pull_request** 时自动运行。

| 检查项 | 命令 |
|---|---|
| 代码风格 | `./gradlew ktlintCheck` |
| 架构 / 静态分析 | `./gradlew detekt` |
| Debug 编译 | `./gradlew assembleDebug` |
| 单元测试 | `./gradlew testDebugUnitTest` |

环境：Ubuntu、`Temurin 17`、Android SDK；启用 Gradle 与 Wrapper 缓存。

### 本地检查（与 CI 等价）

```bash
chmod +x gradlew   # Linux / macOS 首次
./gradlew ktlintCheck detekt assembleDebug testDebugUnitTest
```

Windows：

```bash
.\gradlew.bat ktlintCheck detekt assembleDebug testDebugUnitTest
```

CI 失败时可在 Actions 页面下载 `build-reports` 构件（测试与构建报告）。

## 文档

- [docs/Architecture.md](docs/Architecture.md)
- [docs/tasks.md](docs/tasks.md)
