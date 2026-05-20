# app — GoalWall

## 职责

唯一应用模块：承载 `Application`、`MainActivity`、包分层（`data/`、`ui/`、`di/` 等）及全部依赖配置。

## 包结构

```
com.goalwall/
├── common/          # 共享基础设施
├── data/
│   ├── db/        # Room（entity、dao、database）
│   ├── model/     # UI 数据模型
│   └── repository/
├── di/              # Hilt Module
├── domain/          # 预留
├── ui/
│   ├── navigation/
│   └── theme/
├── worker/
├── widget/
└── notification/
```

## 禁止引入

- ViewModel 直接 import Room Entity / DAO
- UI 层直接访问 Repository 或 DAO

## AI 开发提示

> 新增功能按 Architecture.md 包职责放置；Room schema 导出目录为 `app/schemas/`。
