# app — GoalWall

## 职责

应用入口模块：承载 `Application`、`MainActivity`、Hilt 装配与全局 DI Module。

## 包含内容

- `GoalWallApp.kt` — `@HiltAndroidApp` 入口
- `MainActivity.kt` — 单 Activity，承载 Compose 与 `GoalWallNavHost`
- `di/` — Hilt Module（Database、Dispatchers 等）

## 模块依赖

- `:core:common`
- `:core:model`
- `:core:database`
- `:core:data`
- `:core:designsystem`
- `:core:navigation`

## 禁止引入

- 直接访问 Room DAO（通过 Repository，位于 `:core:data`）
- 在 Activity 中编写业务逻辑

## AI 开发提示

> 新增 Worker、Widget、Notification 时，在 `di/` 增加对应 Module，业务实现放在独立包（`worker/`、`widget/` 等），保持 `MainActivity` 精简。
