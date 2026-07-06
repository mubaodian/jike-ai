# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Development
npm run dev              # Start dev server (Vite)

# Build
npm run build            # Type-check + build
npm run build-only       # Build without type-check
npm run type-check       # Run vue-tsc type checking only

# Lint & Format
npm run lint             # Run oxlint + eslint (with --fix)
npm run format           # Prettier format src/

# Preview
npm run preview          # Preview production build

# API type generation
npm run openapi2ts       # Regenerate API types from OpenAPI spec
```

## Architecture

### Tech Stack
- **Vue 3** (Composition API + `<script setup>`) + TypeScript
- **Ant Design Vue 4** for UI components
- **Pinia** for state management (login user store)
- **vue-router 5** in hash mode
- **Axios** with session credentials (`withCredentials: true`)

### File Layout
```
src/
├── main.ts           # App bootstrap: Pinia, Ant Design Vue, router, highlight.js CSS
├── request.ts        # Axios instance (base URL, 60s timeout, 40100 → /user/login redirect)
├── env.ts            # Env var exports + URL builders (getDeployUrl, getStaticPreviewUrl)
├── access.ts         # Global router guard: enforces admin role for /admin/* routes
├── api/              # Auto-generated from OpenAPI — do not hand-edit typings.d.ts
│   ├── typings.d.ts  # All request/response types under namespace API.*
│   ├── appController.ts
│   └── userController.ts
├── stores/
│   └── loginUser.ts  # Pinia: loginUser state, fetchLoginUser(), setLoginUser()
├── router/index.ts   # Route definitions
├── utils/
│   ├── time.ts       # dayjs wrappers: formatTime, formatDate, formatRelativeTime
│   └── markdown.ts   # marked + highlight.js: renderMarkdown(content) → HTML string
└── pages/
    ├── HomePage.vue
    ├── app/
    │   ├── AppGeneratePage.vue   # SSE chat + iframe preview
    │   └── AppDetailPage.vue     # Edit app name/cover/priority
    └── admin/
        ├── AppManagePage.vue
        └── UserManagePage.vue
```

### Key Patterns

**Component structure order:** `<template>` first, then `<script setup>`, then `<style scoped>`.

**ID precision:** App IDs and user IDs are stored as `string` on the frontend to avoid JS number precision loss. Convert to `Number()` only when passing to API calls.

**SSE streaming:** `chatToGenCode()` uses native `fetch` (not Axios) because Axios doesn't support streaming. The pattern is: push a `reactive()` message object into the messages array first, then mutate its `.content` as chunks arrive.

**Markdown rendering:** AI assistant messages use `v-html="renderMarkdown(msg.content)"`. User messages use plain `{{ msg.content }}`. The `renderMarkdown` function handles XSS escaping internally.

**Admin check:** `loginUserStore.loginUser.userRole === 'admin'`

**API response shape:** All responses follow `{ code, data, message }`. The Axios interceptor handles `code === 40100` by redirecting to login.

### Environment Variables
```
VITE_API_BASE_URL       # Backend API base (e.g. http://localhost:8123/api)
VITE_DEPLOY_BASE_URL    # Deployed app domain (e.g. http://localhost)
```
Use `src/env.ts` exports — never read `import.meta.env` directly in components.

### Static Preview URL Pattern
Generated apps are served at: `{API_BASE_URL}/static/{codeGenType}_{appId}/`
Use `getStaticPreviewUrl(codeGenType, appId)` from `src/env.ts`.
