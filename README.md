# GoalWall

Keep your daily targets in sight with GoalWall — a customizable home screen widget for tracking small goals.

## 工程结构

```
GoalWall/
├── app/                    # 应用入口、Hilt DI、MainActivity
├── core/
│   ├── common/             # 共享基础设施
│   ├── model/              # UI 数据模型
│   ├── database/           # Room
│   ├── data/               # Repository
│   ├── designsystem/       # Compose 主题
│   └── navigation/         # Navigation Compose
├── gradle/
│   └── libs.versions.toml  # Version Catalog
└── docs/
    └── Architecture.md     # 架构设计文档
```

## 技术栈

- Kotlin · Jetpack Compose · Room · MVVM · StateFlow · Hilt · WorkManager · Glance

## 构建

```bash
./gradlew :app:assembleDebug
```

需安装 Android SDK，并配置 `local.properties`（`sdk.dir`）。

## 模块说明

各模块职责见对应 `README.md`（`app/README.md`、`core/*/README.md`）。

架构细节见 [docs/Architecture.md](docs/Architecture.md)。
