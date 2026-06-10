# UI Beautification Implementation Plan — 用户端 & 咨询师端

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将用户端和咨询师端微信小程序从冷调蓝色改为暖愈风格（用户端暖珊瑚粉、咨询师端暖紫罗兰），同时统一卡片圆角、阴影、间距等视觉细节。只改 WXSS + app.json，不动 WXML 和 JS。

**Architecture:** 逐文件替换色值 + 调整设计令牌（圆角/阴影/间距）。两端独立改造，先全局后页面，按依赖顺序执行。app.json 改导航栏和 TabBar 色值；app.wxss 改全局类；各页面 WXSS 改页面级样式。

**Tech Stack:** 微信小程序原生 WXSS

---

### Task 1: 用户端全局样式 — app.json + app.wxss

**Files:**
- Modify: `miniprogram/app.json`
- Modify: `miniprogram/app.wxss`

- [ ] **Step 1: 改 app.json 导航栏和 TabBar 色值**

在 `miniprogram/app.json` 中做以下替换：

- `"navigationBarBackgroundColor": "#5C9EFF"` → `"#FF7E67"`
- `"selectedColor": "#5C9EFF"` → `"#FF7E67"`
- `"backgroundColor": "#F5F5F5"` → `"#FDF8F5"`

- [ ] **Step 2: 改 app.wxss 全局样式**

在 `miniprogram/app.wxss` 中：

1. `.announcement-bar` — `background: linear-gradient(135deg, #fff8e1, #fff3cd)` → `linear-gradient(135deg, #FFF0ED, #FFE8E3)`；`border-left: 6rpx solid #ff9800` → `#FF7E67`
2. `.announcement-text` — `color: #e65100` → `#E06952`
3. `.recommend-section` — `border-radius: 16rpx` → `24rpx`；`box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04)` → `0 8rpx 32rpx rgba(0,0,0,0.05)`
4. `.chip` — `background: #f5f5f5` → `#FFF5F2`；`color: #666` → `#8C7373`
5. `.chip.active` — `background: #e8f0fe` → `#FFF0ED`；`color: #5C9EFF` → `#FF7E67`；`border-color: #5C9EFF` → `#FF7E67`
6. `.apply-btn` — `background: #5C9EFF` → `linear-gradient(135deg, #FF7E67, #E06952)`；`border-radius: 28rpx` → `48rpx`；加 `box-shadow: 0 6rpx 20rpx rgba(255,126,103,0.25)`
7. `.reset-btn` — `background: #f5f5f5` → `#FFF5F2`；`color: #666` → `#8C7373`；`border-radius: 28rpx` → `48rpx`
8. `.recommend-title` — `color: #333` → `#3D2C2C`
9. `.recommend-hint` — `color: #999` → `#B8A0A0`
10. `.picker-label` — `color: #666` → `#8C7373`
11. `.filter-picker` — `background: #f5f5f5` → `#FFF5F2`；`color: #333` → `#3D2C2C`
12. `.picker-arrow` — `color: #999` → `#B8A0A0`

### Task 2: 用户端登录/注册页

**Files:**
- Modify: `miniprogram/pages/login/login.wxss`
- Modify: `miniprogram/pages/register/register.wxss`

- [ ] **Step 1: 改 login.wxss**

1. 表单卡片 `.login-form` — `border-radius: 30rpx` → `28rpx`；`box-shadow: 0 20rpx 60rpx rgba(0,0,0,0.15)` → `0 16rpx 48rpx rgba(0,0,0,0.10)`
2. `.label` — `color: #333` → `#3D2C2C`
3. `.input` — `background: #f5f5f5` → `#FFF5F2`；`border-radius: 20rpx` → `16rpx`
4. `.btn-login` — `background: linear-gradient(135deg, #667eea, #764ba2)` → `linear-gradient(135deg, #FF7E67, #E06952)`；`border-radius: 50rpx` → `48rpx`；加 `box-shadow: 0 8rpx 24rpx rgba(255,126,103,0.3)`
5. `.register-link` — `color: #667eea` → `#FF7E67`

- [ ] **Step 2: 改 register.wxss**

（与 login.wxss 同样的色值替换模式）
1. 表单卡片圆角 → `28rpx`
2. 输入框背景 → `#FFF5F2`，圆角 → `16rpx`
3. 注册按钮 → `linear-gradient(135deg, #FF7E67, #E06952)` + `border-radius: 48rpx` + 阴影
4. 链接色 → `#FF7E67`

### Task 3: 用户端首页

**Files:**
- Modify: `miniprogram/pages/index/index.wxss`

- [ ] **Step 1: 全面替换 index.wxss**

1. `.container` — `background-color: #f5f7fa` → `#FDF8F5`
2. `.user-status-card.online` — `background: linear-gradient(135deg, #10B981, #059669)` → `linear-gradient(135deg, #4ECB9C, #3AB88A)`
3. `.user-status-card.offline` — `background: linear-gradient(135deg, #9CA3AF, #6B7280)` → `linear-gradient(135deg, #B8A0A0, #8C7373)`
4. 所有 `color: #2c3e50` → `#3D2C2C`
5. 所有 `color: #666` → `#8C7373`
6. 所有 `color: #999` → `#B8A0A0`
7. 所有卡片 `border-radius: 16rpx` → `24rpx`
8. 所有卡片 `box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.08)` → `0 8rpx 32rpx rgba(0,0,0,0.05)`
9. 所有渐变 `linear-gradient(135deg, #5C9EFF, #3B7AFF)` → `linear-gradient(135deg, #FF7E67, #E06952)`
10. `.section-title` — `border-left: 6rpx solid #5C9EFF` → `border-left: 6rpx solid #FF7E67`
11. `.mode-item` — `background: #f8f9fa` → `#FFF5F2`
12. `.counselor-card` — 加 `border-top: 4rpx solid #FF7E67` on hover 替换原阴影增强
13. `.consult-btn` — `border-radius: 48rpx` → `48rpx`（保持）；加 `box-shadow: 0 6rpx 20rpx rgba(255,126,103,0.25)`
14. `.price` — `color: #e74c3c` → `#FF7E67`
15. `.loading-spinner` — `border-top: 6rpx solid #5C9EFF` → `#FF7E67`

### Task 4: 用户端咨询师详情页

**Files:**
- Modify: `miniprogram/pages/counselor/detail/detail.wxss`

- [ ] **Step 1: 改色值 + 圆角 + 阴影**

1. 页面背景 → `#FDF8F5`
2. 卡片圆角 → `24rpx`，阴影 → 柔和
3. 标题色 `#333` → `#3D2C2C`，辅助 `#666` → `#8C7373`，弱色 `#999` → `#B8A0A0`
4. 主按钮渐变 → `linear-gradient(135deg, #FF7E67, #E06952)` + 阴影
5. 星级金色 `#f59e0b` 保持不变
6. 头像边框改为 `4rpx solid #FF7E67`
7. 在线状态绿点 → `#4ECB9C`

### Task 5: 用户端预约页

**Files:**
- Modify: `miniprogram/pages/appointment/book/book.wxss`
- Modify: `miniprogram/pages/appointment/my/my.wxss`

- [ ] **Step 1: 改 book.wxss**

1. `.tab.active` — `background: linear-gradient(135deg, #5C9EFF, #3B7AFF)` → `linear-gradient(135deg, #FF7E67, #E06952)`
2. `.problem-item` — `background: #f3f4f6` → `#FFF5F2`；`color: #666` → `#8C7373`
3. `.problem-item.active` — `background: #e8f0fe` → `#FFF0ED`；`color: #5C9EFF` → `#FF7E67`；`border-color: #5C9EFF` → `#FF7E67`
4. `.match-btn` — `background: linear-gradient(135deg, #5C9EFF, #3B7AFF)` → `linear-gradient(135deg, #FF7E67, #E06952)` + 阴影
5. `.time-chip` — `background: #f3f4f6` → `#FFF5F2`
6. `.time-chip.active` — `background: #e8f0fe` → `#FFF0ED`；`color: #5C9EFF` → `#FF7E67`；`border-color: #5C9EFF` → `#FF7E67`
7. `.section-title` — `border-left: 6rpx solid #5C9EFF` → `#FF7E67`
8. 卡片圆角 `16rpx` → `24rpx`，阴影柔和化
9. `.price` — `color: #e74c3c` → `#FF7E67`
10. `.spinner` — `border-top: 4rpx solid #5C9EFF` → `#FF7E67`

- [ ] **Step 2: 改 my.wxss**

同样的色值替换模式：主色 → `#FF7E67`，背景 → `#FDF8F5`，标题 → `#3D2C2C`，辅助 → `#8C7373`，圆角 `24rpx`，阴影柔和化。

### Task 6: 用户端聊天页 + AI聊天页

**Files:**
- Modify: `miniprogram/pages/chat/chat.wxss`
- Modify: `miniprogram/pages/ai/chat/chat.wxss`

- [ ] **Step 1: 改 chat.wxss**

1. 我方气泡背景 → `linear-gradient(135deg, #FF7E67, #E06952)`
2. 对方气泡背景 → `#FFFFFF` + `border: 1rpx solid #F0E0DC`
3. 输入区背景 → `#FFF5F2`
4. 发送按钮 → `#FF7E67`
5. 页面背景 → `#FDF8F5`

- [ ] **Step 2: 改 ai/chat/chat.wxss**

同 chat.wxss 模式，AI 气泡用对方样式。

### Task 7: 用户端科普页

**Files:**
- Modify: `miniprogram/pages/science/index.wxss`
- Modify: `miniprogram/pages/science/detail.wxss`

- [ ] **Step 1: 改 index.wxss 和 detail.wxss**

1. 页面背景 → `#FDF8F5`
2. 卡片圆角 `24rpx`，阴影柔和
3. 分类 chip 选中 → `#FFF0ED` + `#FF7E67`
4. 标题色 → `#3D2C2C`，辅助 → `#8C7373`

### Task 8: 用户端测评页

**Files:**
- Modify: `miniprogram/pages/test/index.wxss`
- Modify: `miniprogram/pages/test/detail.wxss`
- Modify: `miniprogram/pages/test/history.wxss`

- [ ] **Step 1: 改三个测评页 WXSS**

1. 页面背景 → `#FDF8F5`
2. 卡片圆角 `24rpx`，阴影柔和
3. 选项选中态 → `background: #FFF0ED` + `border-color: #FF7E67`
4. 提交按钮 → `linear-gradient(135deg, #FF7E67, #E06952)` + 阴影
5. 进度条 → `#FF7E67`
6. 标题色 → `#3D2C2C`

### Task 9: 用户端评价 + 咨询记录页

**Files:**
- Modify: `miniprogram/pages/review/review.wxss`
- Modify: `miniprogram/pages/my-reviews/my-reviews.wxss`
- Modify: `miniprogram/pages/consultation-record/consultation-record.wxss`

- [ ] **Step 1: 改三个页面 WXSS**

1. 页面背景 → `#FDF8F5`
2. 卡片圆角 `24rpx`，阴影柔和
3. 提交按钮 → 暖色渐变 + 阴影
4. 星级金色保持不变
5. 标题色 → `#3D2C2C`

### Task 10: 用户端个人中心

**Files:**
- Modify: `miniprogram/pages/user/user.wxss`

- [ ] **Step 1: 改 user.wxss**

1. `.container` — `background-color: #f5f7fa` → `#FDF8F5`
2. 头像加 `border: 4rpx solid #FF7E67`
3. 所有 `color: #2c3e50` → `#3D2C2C`
4. 所有 `color: #666` → `#8C7373`
5. 所有 `color: #999` → `#B8A0A0`
6. `.logout-btn` — `background: linear-gradient(135deg, #ff6b6b, #ee5a6f)` → `linear-gradient(135deg, #FF6B6B, #E55A5A)`（暖红保持）
7. 卡片 `border-radius: 16rpx` → `24rpx`
8. `.menu-item:hover` — `background-color: #f8f9fa` → `#FFF5F2`

---

### Task 11: 咨询师端全局样式 — app.json + app.wxss

**Files:**
- Modify: `counselor/miniprogram/app.json`
- Modify: `counselor/miniprogram/app.wxss`

- [ ] **Step 1: 改 app.json 导航栏和 TabBar 色值**

- `"navigationBarBackgroundColor": "#5C9EFF"` → `"#7C6FF7"`
- `"selectedColor": "#5C9EFF"` → `"#7C6FF7"`
- `"backgroundColor": "#f5f7fa"` → `"#F8F7FC"`

- [ ] **Step 2: 改 app.wxss 全局**

1. `.announcement-bar` — 同用户端但主色换 `#7C6FF7`
2. `page` — `background-color: #f5f7fa` → `#F8F7FC`
3. `.tab-bar` — 阴影柔和化
4. 全局文字色 → 标题 `#2D2438`，辅助 `#6B6380`

### Task 12: 咨询师端登录页

**Files:**
- Modify: `counselor/miniprogram/pages/login/login.wxss`

- [ ] **Step 1: 改 login.wxss**

1. 保持紫色渐变背景不变
2. 表单卡片圆角 → `28rpx`
3. `.input` — `background: #f8f9fa` → `#F5F2FC`；`border-radius: 12rpx` → `16rpx`
4. `.btn-login` — `background: linear-gradient(135deg, #667eea, #764ba2)` → `linear-gradient(135deg, #7C6FF7, #6558D3)`；`border-radius: 12rpx` → `48rpx`；加 `box-shadow: 0 8rpx 24rpx rgba(124,111,247,0.3)`
5. `.label` — `color: #2c3e50` → `#2D2438`

### Task 13: 咨询师端首页

**Files:**
- Modify: `counselor/miniprogram/pages/index/index.wxss`

- [ ] **Step 1: 改 index.wxss**

1. `.container` — `background: linear-gradient(180deg, #f0f4ff, #f5f7fa)` → `linear-gradient(180deg, #F0EDFF, #F8F7FC)`
2. `.status-card.online` — `background: linear-gradient(135deg, #10b981, #059669)` → `linear-gradient(135deg, #4ECB9C, #3AB88A)`
3. `.status-card.offline` — `background: linear-gradient(135deg, #6b7280, #4b5563)` → `linear-gradient(135deg, #A099B0, #6B6380)`
4. 统计图标底 — 蓝 `#dbeafe/#bfdbfe` → `#E8E2FF/#D5CCFF`；绿保持；紫 `#ede9fe/#ddd6fe` → `#F0EDFF/#E2DCFF`；橙保持
5. `.stat-value` — `color: #1f2937` → `#2D2438`
6. `.stat-label` — `color: #9ca3af` → `#A099B0`
7. `.section-title` — `color: #1f2937` → `#2D2438`
8. `.action-icon-wrapper.blue` → use purple tones `#E8E2FF/#D5CCFF`
9. `.action-badge` — 暖红保持 `#ef4444`
10. `.more-link` — `color: #5C9EFF` → `#7C6FF7`
11. `.time-text` — `color: #5C9EFF` → `#7C6FF7`
12. `.loading-spinner` — `border-top: 6rpx solid #5C9EFF` → `#7C6FF7`

### Task 14: 咨询师端预约管理页

**Files:**
- Modify: `counselor/miniprogram/pages/appointments/appointments.wxss`
- Modify: `counselor/miniprogram/pages/appointment-detail/appointment-detail.wxss`

- [ ] **Step 1: 改 appointments.wxss**

1. `page` — `background: linear-gradient(180deg, #f0f4ff, #f5f7fa)` → `linear-gradient(180deg, #F0EDFF, #F8F7FC)`
2. `.tab-item.active .tab-text` — `color: #5C9EFF` → `#7C6FF7`
3. `.tab-line` — `background: linear-gradient(135deg, #5C9EFF, #3B7AFF)` → `linear-gradient(135deg, #7C6FF7, #6558D3)`
4. `.appointment-card::before` — `background: linear-gradient(90deg, #5C9EFF, #3B7AFF)` → `linear-gradient(90deg, #7C6FF7, #6558D3)`
5. `.appointment-card.completed::before` — `background: linear-gradient(90deg, #10b981, #059669)` → `linear-gradient(90deg, #4ECB9C, #3AB88A)`
6. `.avatar` — `background: linear-gradient(135deg, #667eea, #764ba2)` → `linear-gradient(135deg, #7C6FF7, #6558D3)`
7. `.btn-primary` — `background: linear-gradient(135deg, #5C9EFF, #3B7AFF)` → `linear-gradient(135deg, #7C6FF7, #6558D3)` + 阴影
8. `.btn-start` — `background: linear-gradient(135deg, #10b981, #059669)` → `linear-gradient(135deg, #4ECB9C, #3AB88A)`
9. `.btn-record` — `background: linear-gradient(135deg, #667eea, #764ba2)` → `linear-gradient(135deg, #7C6FF7, #6558D3)`
10. 卡片 `border-radius: 24rpx` → `24rpx`（保持）；阴影柔和化
11. `.loading-spinner` — `border-top: 8rpx solid #5C9EFF` → `#7C6FF7`

- [ ] **Step 2: 改 appointment-detail.wxss**

同样色值替换模式。

### Task 15: 咨询师端聊天页

**Files:**
- Modify: `counselor/miniprogram/pages/chat/chat-list.wxss`
- Modify: `counselor/miniprogram/pages/chat/chat-detail.wxss`

- [ ] **Step 1: 改 chat-list.wxss 和 chat-detail.wxss**

1. 页面背景 → `#F8F7FC`
2. 我方气泡 → `linear-gradient(135deg, #7C6FF7, #6558D3)` + 白字
3. 对方气泡 → 白底 + `border: 1rpx solid #E2DCFF`
4. 输入区背景 → `#F5F2FC`
5. 发送按钮 → `#7C6FF7`

### Task 16: 咨询师端咨询记录页

**Files:**
- Modify: `counselor/miniprogram/pages/records/records.wxss`
- Modify: `counselor/miniprogram/pages/records/record-detail.wxss`

- [ ] **Step 1: 色值替换**

1. 页面背景 → `#F8F7FC`
2. 卡片圆角 `24rpx`，阴影柔和
3. 按钮 → 暖紫渐变 + 阴影
4. 标题色 → `#2D2438`，辅助 → `#6B6380`

### Task 17: 咨询师端排班页

**Files:**
- Modify: `counselor/miniprogram/pages/schedule/schedule.wxss`

- [ ] **Step 1: 色值替换**

1. 页面背景 → `#F8F7FC`
2. 卡片圆角 `24rpx`，阴影柔和
3. 标题色 → `#2D2438`

### Task 18: 咨询师端个人中心

**Files:**
- Modify: `counselor/miniprogram/pages/profile/profile.wxss`

- [ ] **Step 1: 改 profile.wxss**

1. `page` — `background-color: #f5f7fa` → `#F8F7FC`
2. `.profile-header` — `background: linear-gradient(135deg, #5C9EFF, #3B7AFF)` → `linear-gradient(135deg, #7C6FF7, #6558D3)`
3. `.edit-btn text` — `color: #5C9EFF` → `#7C6FF7`
4. `.stat-value` — `color: #1f2937` → `#2D2438`
5. `.stat-label` — `color: #9ca3af` → `#A099B0`
6. `.menu-label` — `color: #374151` → `#2D2438`
7. `.contact-label` — `color: #6b7280` → `#6B6380`
8. `.logout-btn text` — `color: #ef4444` → `#FF6B6B`（暖红保持）
9. `.loading-spinner` — `border-top: 6rpx solid #5C9EFF` → `#7C6FF7`
