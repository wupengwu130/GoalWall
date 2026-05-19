# core:data — GoalWall

## 职责

数据聚合层：Repository 实现，Entity ↔ Model 映射，对外暴露 `Flow` 与 `suspend` API（对应 Architecture 中的 `data/repository`）。

## 包含内容

- （待实现）`GoalRepository`、`ProgressRepository` 等

## 模块依赖

- `:core:common`
- `:core:model`
- `:core:database`

## 禁止引入

- Compose / Navigation
- ViewModel
- Context（Repository 保持纯数据层）

## AI 开发提示

> Repository 只负责数据聚合与映射，不写 UI 状态；写操作完成后由上层触发 Widget 同步等副作用。
