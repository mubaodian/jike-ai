# 即刻 AI 应用平台 - 前端项目

## 📱 项目简介

即刻 AI 应用平台是一个基于 Vue 3 + TypeScript 的前端应用，用户可以通过与 AI 对话来创建、生成、管理和部署网站应用。

**核心特性：**
- 🤖 AI 驱动的代码生成（SSE 流式处理）
- 📝 实时代码对话交互
- 👁️ 网站效果实时预览
- 🚀 一键部署应用
- 👨‍💼 完整的应用管理系统
- 🔐 严格的权限控制

## 🏗️ 项目架构

### 技术栈
- **框架**：Vue 3（Composition API + `<script setup>`）
- **语言**：TypeScript
- **UI 组件库**：Ant Design Vue
- **状态管理**：Pinia
- **路由**：Vue Router
- **HTTP 请求**：Axios
- **构建工具**：Vite

### 目录结构
```
src/
├── pages/
│   ├── HomePage.vue              # 主页（应用创建、列表展示）
│   ├── NotFoundPage.vue          # 404 页面
│   ├── app/
│   │   ├── AppGeneratePage.vue   # 应用生成对话页（核心功能）
│   │   └── AppDetailPage.vue     # 应用信息修改页
│   ├── admin/
│   │   ├── AppManagePage.vue     # 应用管理页（管理员）
│   │   └── UserManagePage.vue    # 用户管理页（管理员）
│   └── user/
│       ├── UserLoginPage.vue     # 用户登录
│       └── UserRegisterPage.vue  # 用户注册
├── api/
│   ├── appController.ts          # 应用相关 API
│   ├── userController.ts         # 用户相关 API
│   ├── staticResourceController.ts
│   ├── index.ts
│   └── typings.d.ts              # API 类型定义
├── router/
│   └── index.ts                  # 路由配置
├── stores/
│   └── loginUser.ts              # 用户登录状态管理
├── components/
│   ├── GlobalHeader.vue          # 全局头部
│   └── GlobalFooter.vue          # 全局底部
├── layouts/
│   └── BasicLayout.vue           # 基础布局
├── App.vue
├── main.ts
├── request.ts                    # Axios 配置
└── access.ts                     # 权限控制
```

## 🎯 功能模块

### 1. 主页（HomePage.vue）
- **应用创建**：用户输入提示词，一键创建应用
- **我的应用列表**：分页展示用户创建的应用
- **精选应用列表**：展示管理员推荐的应用
- **快速操作**：编辑、生成、删除等

### 2. 应用生成页（AppGeneratePage.vue）⭐ 核心
- **实时对话**：用户与 AI 实时交互
- **SSE 流式处理**：使用 EventSource 处理 Server-Sent Events
- **网站预览**：通过 iframe 实时显示生成的网站效果
- **应用部署**：一键部署，获得可访问的 URL
- **对话历史**：完整的消息历史记录

**技术亮点：**
```typescript
// EventSource SSE 处理
const eventSource = new EventSource(url)

// 监听消息事件（接收 AI 生成的代码）
eventSource.addEventListener('message', (event) => {
  const parsed = JSON.parse(event.data)  // 解析 JSON 包装
  aiMessage += parsed.d                  // 提取数据内容
})

// 监听完成事件（流式生成完成）
eventSource.addEventListener('done', () => {
  eventSource.close()  // 关闭连接
  // 显示预览...
})
```

### 3. 应用详情页（AppDetailPage.vue）
- **编辑应用信息**：修改应用名称
- **权限控制**：普通用户只能编辑自己的应用
- **管理员权限**：可编辑任意应用的名称、封面、优先级

### 4. 应用管理页（AppManagePage.vue）
- **应用列表**：管理员查看所有应用
- **搜索过滤**：按应用名称搜索
- **CRUD 操作**：编辑、删除、精选应用
- **精选管理**：将优先级设置为 99，标记为精选

### 5. 其他页面
- **UserLoginPage**：用户登录
- **UserRegisterPage**：用户注册
- **UserManagePage**：用户管理（管理员）
- **NotFoundPage**：404 错误页面

## 🛣️ 路由配置

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | HomePage | 主页 |
| `/user/login` | UserLoginPage | 用户登录 |
| `/user/register` | UserRegisterPage | 用户注册 |
| `/admin/userManage` | UserManagePage | 用户管理（管理员） |
| `/admin/appManage` | AppManagePage | 应用管理（管理员） |
| `/app/generate/:appId` | AppGeneratePage | 应用生成对话页 |
| `/app/detail/:appId` | AppDetailPage | 应用详情修改页 |
| `/:pathMatch(.*)*` | NotFoundPage | 404 页面 |

## 📡 API 接口

### 应用相关
- `POST /app/add` - 创建应用
- `POST /app/update` - 更新应用（用户）
- `POST /app/delete` - 删除应用（用户）
- `GET /app/get/vo` - 获取应用详情
- `GET /app/chat/gen/code` - SSE 流式生成代码 ⭐
- `POST /app/deploy` - 部署应用
- `POST /app/my/list/page/vo` - 查询我的应用列表
- `POST /app/good/list/page/vo` - 查询精选应用列表
- `POST /app/admin/list/page/vo` - 查询应用列表（管理员）
- `POST /app/admin/update` - 更新应用（管理员）
- `POST /app/admin/delete` - 删除应用（管理员）

### 用户相关
- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `POST /user/logout` - 用户登出
- `GET /user/get/current` - 获取当前用户信息
- 更多接口见 `userController.ts`

## 🔐 权限控制

### 角色区分
- **普通用户**：`userRole !== 'admin'`
- **管理员**：`userRole === 'admin'`

### 权限规则
| 操作 | 普通用户 | 管理员 |
|------|--------|--------|
| 创建应用 | ✅ | ✅ |
| 编辑自己的应用 | ✅ | ✅ |
| 编辑他人的应用 | ❌ | ✅ |
| 删除自己的应用 | ✅ | ✅ |
| 删除他人的应用 | ❌ | ✅ |
| 访问应用管理页 | ❌ | ✅ |
| 访问用户管理页 | ❌ | ✅ |
| 设置应用为精选 | ❌ | ✅ |

## 💻 开发指南

### 环境要求
- Node.js >= 16
- npm >= 8 或 yarn >= 1.22

### 安装依赖
```bash
npm install
```

### 开发服务器
```bash
npm run dev
# 访问 http://localhost:5173
```

### 类型检查
```bash
npm run type-check
```

### 生产构建
```bash
npm run build
# 输出目录：dist/
```

### 代码检查
```bash
npm run lint
```

## 🧪 测试流程

### 完整的用户操作流程
1. **注册/登录**
   - 访问 `/user/register` 或 `/user/login`
   - 创建账户或登录

2. **创建应用**
   - 在主页输入提示词（例如："生成一个待办事项应用"）
   - 点击"生成应用"按钮
   - 系统自动创建应用并跳转到生成页

3. **应用生成**
   - 页面自动调用 SSE 接口开始生成代码
   - 实时看到"输出中..."的动画
   - 接收并累积生成的代码
   - 生成完成后在右侧 iframe 显示预览

4. **应用管理**
   - 在主页"我的应用"列表中查看应用
   - 点击"编辑"修改应用名称
   - 点击"生成"再次进入对话页
   - 点击"删除"删除应用

5. **应用部署**
   - 在应用生成页点击"部署应用"按钮
   - 获取部署后的访问 URL
   - 分享 URL 给其他人访问

6. **管理员操作**（需要 admin 角色）
   - 点击菜单中的"应用管理"
   - 查看所有用户创建的应用
   - 编辑任意应用的名称、封面、优先级
   - 删除任意应用
   - 设置应用为精选（优先级设置为 99）

## 🔍 常见问题

### Q: 如何调试 SSE 流式请求？
A: 打开浏览器 F12 控制台，查看以下日志：
```
调用 chatToGenCode 接口...
收到 SSE 消息: {"d":"<html>..."}
解析数据块: <html>...
...
收到 done 事件，流式生成完成
生成完成，总长度: xxxxx
```

### Q: 应用 ID 为什么这么长？
A: 后端使用 long 类型的分布式 ID（雪花算法），支持海量应用。前端通过字符串传输避免精度丢失。

### Q: 预览 URL 是什么格式？
A: `http://localhost:8123/api/static/{codeGenType}_{appId}/`

### Q: 如何复制部署后的 URL？
A: 部署成功后会显示弹窗，直接点击"复制"按钮即可复制到剪贴板。

## 📊 性能优化

- ✅ **代码分割**：按路由加载组件
- ✅ **懒加载**：按需加载功能模块
- ✅ **缓存**：使用 Pinia 缓存用户状态
- ✅ **虚拟滚动**：大列表性能优化（可选）
- ✅ **流式处理**：SSE 不占用内存

## 🎨 代码规范

- **组件结构**：`<template>` 在前，`<script setup>` 在后
- **命名规则**：组件名使用 PascalCase，变量/函数使用 camelCase
- **TypeScript**：所有组件都使用 TypeScript 编写
- **注释**：关键业务逻辑都有注释说明
- **错误处理**：完善的错误提示和异常捕获

## 📝 提交规范

| 类型 | 说明 | 示例 |
|------|------|------|
| feat | 新功能 | feat: 添加应用生成功能 |
| fix | 修复 bug | fix: 修复 SSE 连接中断问题 |
| docs | 文档更新 | docs: 更新 README |
| style | 代码风格 | style: 调整缩进 |
| refactor | 代码重构 | refactor: 优化应用列表组件 |
| test | 测试 | test: 添加应用创建测试 |

## 📞 支持

- **问题报告**：提交 Issue
- **功能建议**：提交 PR
- **文档反馈**：发起讨论

## 📄 许可证

MIT License

---

**项目状态**：✅ **生产就绪**

**最后更新**：2026-06-16

**版本**：v1.0.0
