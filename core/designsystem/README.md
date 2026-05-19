# core:designsystem — GoalWall

## 职责

Compose 设计系统：颜色、字体、Material3 主题（对应 Architecture 中的 `ui/theme`）。

## 包含内容

- `theme/Color.kt` — 品牌色
- `theme/Type.kt` — Typography
- `theme/Theme.kt` — `GoalWallTheme` Composable

## 模块依赖

- `:core:common`

## 禁止引入

- Room / Repository / ViewModel
- Navigation 路由与 Screen

## AI 开发提示

> 功能 Screen 通过 `GoalWallTheme { }` 包裹；深色模式切换后续可接 DataStore，仅改本模块与设置 UI。
