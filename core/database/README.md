# core:database — GoalWall

## 职责

Room 数据库层：Entity、DAO、`GoalWallDatabase`（对应 Architecture 中的 `data/db`）。

## 包含内容

- `GoalWallDatabase.kt` — Room 数据库定义
- `internal/SchemaPlaceholderEntity.kt` — 占位表，待业务 Entity 接入后移除
- `schemas/` — Room 导出的 schema JSON（由 KSP 生成）

## 模块依赖

- `:core:common`

## 禁止引入

- UI / Compose / ViewModel
- Repository 实现

## AI 开发提示

> 新增 Entity 时更新 `@Database(entities = …)`、版本号与 Migration；DAO 仅做 CRUD 与 `Flow` 查询，不含业务判断。
