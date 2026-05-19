# core:navigation — GoalWall

## 职责

Navigation Compose 基础设施：类型安全路由定义与 `NavHost` 骨架（对应 Architecture 中的 `ui/navigation`）。

## 包含内容

- `Screen.kt` — 路由 sealed interface
- `GoalWallNavHost.kt` — 全局 NavHost

## 模块依赖

- `:core:common`
- `:core:designsystem`

## 禁止引入

- Room Entity / DAO
- Repository / ViewModel（导航参数类型除外，由 app 层 composable 注入）

## AI 开发提示

> 新增页面时先在 `Screen` 增加路由，再在 `GoalWallNavHost` 注册 `composable`；具体 Screen UI 可放在 app 或后续 `feature:*` 模块。
