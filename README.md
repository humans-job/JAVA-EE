# 民兵系统（Vue3 前端框架）

本项目是一个 **Vue3 + Vite + TypeScript + Pinia + Vue Router + Element Plus** 的前端代码框架，按你提供的后端接口/README 思路进行页面与路由设计。

## 1) 功能覆盖

### 已实现（按你的要求）
- **登录**：调用 `POST /api/auth/login`，拿到 `{ token, username, userType }` 后：
  - 写入 **Pinia + localStorage**（key: `militia_auth`）
  - 按 `userType` 自动跳转到对应主页
  - axios 自动带上 `Authorization: Bearer <token>`
- **路由守卫**：
  - 未登录访问受保护页面 -> 跳转 `/login` + 弹窗
  - 越权访问（访问不属于自己的界面）-> 清理登录信息 -> 跳转 `/login` + 弹窗
- **已完成 3 个模块页面**：
  - 档案管理（Militia Archive）
  - 通知教育（Notice）
  - 认证安全（登录/个人信息/退出）

### 预留占位（按你的要求先置空，仅保留按钮/路由/菜单）
- 地图态势 `/app/map`
- 请销假 `/app/leave`
- 报表服务 `/app/reports`

## 2) userType 约定（来自你后端 SQL 注释）
- 1：民兵
- 2：营/连/分队
- 3：团机关
- 4：师机关
- 5：兵团机关

## 3) 路由设计（核心）

### 登录后自动跳转
```
1 -> /app/militia/home
2 -> /app/company/home
3 -> /app/regiment/home
4 -> /app/division/home
5 -> /app/corps/home
```

### 受保护页面（部分）
- `/app/notices`：1-5 都可访问
- `/app/archives`：仅 3/4/5 可访问
- `/app/map`：2/3/4/5 可访问（占位）
- `/app/leave`：1-5 可访问（占位）
- `/app/reports`：2/3/4/5 可访问（占位）

## 4) 档案管理页面（已做）
后端接口对应：
- `GET /api/militia/archive/list`
- `POST /api/militia/archive/import`（前端会把 Excel 解析成 JSON 后提交）
- `POST /api/militia/archive/submit`（团提交审核）
- `POST /api/militia/archive/audit`（师审核）
- `PUT /api/militia/archive/update`
- `DELETE /api/militia/archive/delete/{id}`

页面能力（前端做了基础限制，最终以**后端**校验为准）：
- 团机关(3)：批量导入、编辑、删除、提交审核
- 师机关(4)：对 `auditStatus=1` 的记录进行审核（通过/驳回）
- 兵团机关(5)：默认放开查看/编辑入口（如你后端有更严格校验会返回失败）

## 5) 通知教育页面（已做）
后端接口对应：
- `POST /api/notice`（发布）
- `GET /api/notice/my`（我的通知列表）
- `PUT /api/notice/{noticeId}/read`（标记已读）
- `GET /api/notice/{noticeId}/records`（阅读反馈）

说明：后端当前没有提供“按 noticeId 查询通知内容”的接口，因此接收端页面暂仅展示标题/状态/阅读状态；发布端仍会把 content 写入后端。

## 6) 后端地址/端口（你可以随时改）
- 项目使用 Vite 代理转发 `/api`：`vite.config.ts` 中 `proxy.target = VITE_API_BASE_URL || http://localhost:8080`。
- 开发环境 axios `baseURL` 会留空，默认走 Vite 代理，避免 CORS。
- 修改端口：改 `.env.development` 的 `VITE_API_BASE_URL` 即可。

## 7) 运行方式
```bash
npm i
npm run dev
```

## 8) 目录结构（关键）
```
src/
  api/        # axios 封装 + 接口
  router/     # 路由 + 守卫
  stores/     # Pinia（登录态）
  layouts/    # 主布局（侧边栏/头部）
  views/      # 页面：登录、主页、档案、通知、占位、个人信息
```
