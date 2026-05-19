# core:model — GoalWall

## 职责

UI 与领域层使用的纯 Kotlin 数据模型（对应 Architecture 中的 `data/model`），与 Room Entity 分离。

## 包含内容

- （待实现）`Goal`、`Milestone`、`GoalDetail` 等 Model

## 模块依赖

- `:core:common`

## 禁止引入

- Room Entity / DAO
- Android Framework（Context、View 等）
- Compose UI

## AI 开发提示

> 新增 Model 时同步在 `:core:data` 添加 Entity → Model 映射；ViewModel 只依赖 Model，不 import Entity。
