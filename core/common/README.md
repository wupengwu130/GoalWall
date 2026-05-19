# core:common — GoalWall

## 职责

跨模块共享的基础设施：协程 Dispatcher、通用扩展与常量（无 Android UI、无业务模型）。

## 包含内容

- `GoalWallDispatchers.kt` — 可注入的协程调度器

## 对外依赖

- 无项目内模块依赖

## 禁止引入

- Room / Compose / Navigation
- 业务 Entity 或 Repository

## AI 开发提示

> 仅放置真正跨模块复用的工具；功能专属逻辑留在对应 feature 或 `core:data` 模块。
