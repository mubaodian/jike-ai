# 前端改动总结

**疑难解决：**
- 创建app成功后，获取应用信息失败，是因为 id 丢失了精度（转为 Number 类型后精度丢失）--> 解决方法：无论任何，整个项目都不应该将 appId 和 userId 转为 Number ，这样会丢失精度！！如果有 ts 报错，需要使用其他的方式修复

## 1. 首页应用卡片布局重构

### HomePage.vue
- **改动内容**：
  - 将表格布局改为卡片网格布局（`grid-template-columns: repeat(auto-fill, minmax(240px, 1fr))`）
  - 卡片结构：
    - 上方：应用封面（无封面时使用渐变色块 + "无封面"文字）
    - 下方：左侧用户头像 + 右侧应用名称/用户昵称
    - 底部：操作按钮
  - 卡片 hover 效果：阴影 + 上移动画
  - 按钮 overlay 悬停显示（半透明暗色背景）

- **按钮配置**：
  - "查看对话"：始终显示，跳转到 `/app/generate/:appId?view=1`
  - "查看作品"：仅当有 deployKey 时显示，新窗口打开 `//{deployKey}`
  - "删除"：仅在"我的应用"中显示

- **排序优化**：
  - 精选应用按创建时间降序排列（`sortField: 'createTime', sortOrder: 'desc'`）

- **Bug 修复**（✨ 新增）：
  - **问题**：点击"生成应用"按钮或按 Enter 键会创建两个应用（相同提示词，不同appId）
  - **原因**：同时绑定了 `@search` 和 `@keyup.enter` 两个事件，都会触发 `handleCreateApp`
  - **解决**：在 `handleCreateApp` 函数开头添加防护检查：如果 `loading.value` 为 true（表示正在处理），则直接返回，防止重复提交
  - **优势**：保留两个事件触发方式（按钮点击和 Enter 键），同时防止重复创建

---

## 2. 应用对话页面权限与交互优化

### AppGeneratePage.vue
- **查看模式**：
  - 添加查询参数 `?view=1` 判断
  - 有此参数时不自动发送初始提示词

- **权限校验**：
  - 输入框在非自己的应用时禁用，鼠标悬停提示"无法在别人的作品下对话哦~"
  - 部署按钮在非自己的应用时禁用

- **应用详情功能**：
  - Header 添加"应用详情"按钮（部署按钮左侧）
  - 点击展示模态框（Modal，悬浮弹窗）
  - 内容展示：
    - 创建者头像+昵称
    - 创建时间
    - 操作栏（仅本人或管理员可见）：修改、删除按钮
  - 修改按钮：跳转到 `/app/detail/:appId`
  - 删除按钮：删除后自动返回首页

- **消息头像显示**（✨ 新增）：
  - AI 消息：显示 AI 头像（`@/assets/aiAvatar.png`），32×32px 圆形
  - 用户消息：显示当前登录用户的头像
  - 流式输出时也显示 AI 头像
  - 头像与消息内容并排显示，布局优化

- **预览区域增强**（✨ 新增）：
  - 预览上方新增"实时预览"标题和"新窗口打开"按钮
  - 点击"新窗口打开"在浏览器新标签页中打开预览地址
  - 预览地址格式：`http://localhost:8123/api/static/{codeGenType}_{appId}/`

- **部署弹窗功能增强**（✨ 新增）：
  - 部署成功弹窗改为：输入框显示部署地址（readonly，无边框）
  - Hover 输入框时，右侧出现两个小图标：
    - **复制图标** (`CopyOutlined`)：点击复制地址到剪贴板
    - **打开图标** (`ExportOutlined`)：点击在新窗口打开部署地址
  - 图标默认隐藏（opacity: 0），hover 时平滑显示（opacity: 1）
  - 输入框 hover 时边框高亮（`#40a9ff`）
  - 使用 monospace 字体显示 URL，提高可读性

- **按钮图标美化**（✨ 新增）：
  - Header 中"应用详情"按钮：添加 `InfoCircleOutlined` 图标
  - Header 中"部署应用"按钮：添加 `CloudUploadOutlined` 图标
  - 预览区域"新窗口打开"按钮：添加 `ExportOutlined` 图标
  - 部署弹窗"复制"按钮：添加 `CopyOutlined` 图标
  - 部署弹窗"新窗口打开"按钮：添加 `ExportOutlined` 图标
  - 使用 Ant Design Vue 官方图标库，统一美观的视觉风格

- **打字机效果**（✨ 新增）：
  - 使用 Vue 的 `reactive()` 实时更新 AI 消息内容
  - 用户可看到 AI 输出逐字显示
  - 自动滚动到最新消息

- **Markdown 渲染 + 代码高亮**（✨ 新增）：
  - 集成 **marked** 和 **highlight.js**
  - Assistant 消息使用 `v-html="renderMarkdown(msg.content)"` 渲染 Markdown
  - User 消息保持纯文本
  - 支持标题、列表、代码块、表格、引用、链接等格式
  - 代码块支持 HTML/CSS/JS 等 180+ 种语言的语法高亮
  - 深色主题（github-dark）美观展示

---

## 3. 应用编辑页面修复

### AppDetailPage.vue
- **问题修复**：
  - 管理员更新应用时，后端返回"请求数据不存在"错误
  - 原因：发送了空值字段给后端

- **解决方案**：
  - 只发送非空字段
  - `cover`：仅当不为空时才发送
  - `priority`：仅当不为 undefined 和 0 时才发送
  - `appName`：始终发送

---

## 4. 应用管理页面优化

### AppManagePage.vue ✨ 新增
- **表格新增4列**：
  - 封面：显示应用封面图片预览（80×60px），无封面时显示"无封面"占位符
  - 创作者：显示应用创建者用户名（从 user.userName 获取）
  - 生成类型：显示代码生成类型，使用蓝色 Tag 标签
  - 精选状态：用 Tag 标签显示"精选"（金色，priority=99）或"普通"（灰色），不显示数字

- **搜索功能升级**：
  - 应用名称：文本输入框
  - 创作者ID：字符串输入框（保持 string 类型，调用 API 时转换为 number）
  - 生成类型：下拉选择（全部、HTML、React、Vue、multi_file）

- **精选管理优化**：
  - 精选按钮（priority ≠ 99）：设置优先级为 99
  - 取消精选按钮（priority = 99）：设置优先级为 0，黄色文字
  - 按钮根据状态动态显示/隐藏

---

## 5. 时间格式化统一

### 使用工具
- 文件：`src/utils/time.ts`
- 函数：`formatTime(time, format?)` - 默认格式 'YYYY-MM-DD HH:mm:ss'

### 应用到的页面
- **AppDetailPage.vue**：创建时间显示
- **AppGeneratePage.vue**：应用详情弹窗创建时间
- **AppManagePage.vue**：应用列表表格创建时间

---

## 6. Markdown 渲染工具新增

### 新建 src/utils/markdown.ts
- **功能**：Markdown 渲染 + 代码高亮
- **依赖**：
  - `marked`：Markdown 解析
  - `highlight.js`：代码语法高亮
- **导出函数**：
  - `renderMarkdown(content: string): string` - 将 Markdown 转换为 HTML
- **特性**：
  - 支持标题、列表、代码块、表格、引用、链接等
  - 代码块支持 180+ 种语言高亮
  - 内置 HTML 转义防止 XSS
  - 完整的错误处理

### 全局样式配置
- 在 `src/main.ts` 导入：`import 'highlight.js/styles/github-dark.css'`
- 确保 highlight.js 样式全局可用，不受 scoped 限制

---

## 7. 编译验证
- 项目已成功编译验证✅
- 无 TypeScript 类型错误
- 所有改动都已生效

