# UI Beautification Design — 心理咨询系统用户端 & 咨询师端

> 2026-05-11 | 纯 WXSS 改动，不动 WXML 结构 / JS 逻辑

## 设计方向：暖愈风

温暖治愈的情感调性，两端口分色但同属暖系。

## 色板

### 用户端（暖珊瑚粉）

| 角色 | 色值 | 用途 |
|------|------|------|
| 主色 primary | `#FF7E67` | 按钮、选中态、图标强调 |
| 主色深 primary-dark | `#E06952` | 渐变收束、active 态 |
| 主色浅 primary-light | `#FFF0ED` | 标签底、卡片强调区 |
| 背景 bg | `#FDF8F5` | 全局底色 |
| 卡片 card-bg | `#FFFFFF` | 卡片、列表项 |
| 标题 text-heading | `#3D2C2C` | 标题、正文强调 |
| 辅助 text-secondary | `#8C7373` | 副文本 |
| 浅灰 text-muted | `#B8A0A0` | 占位、时间等弱文字 |

### 咨询师端（暖紫罗兰）

| 角色 | 色值 | 用途 |
|------|------|------|
| 主色 primary | `#7C6FF7` | 按钮、选中态、图标强调 |
| 主色深 primary-dark | `#6558D3` | 渐变收束、active 态 |
| 主色浅 primary-light | `#F0EDFF` | 标签底、卡片强调区 |
| 背景 bg | `#F8F7FC` | 全局底色 |
| 卡片 card-bg | `#FFFFFF` | 卡片、列表项 |
| 标题 text-heading | `#2D2438` | 标题、正文强调 |
| 辅助 text-secondary | `#6B6380` | 副文本 |
| 浅灰 text-muted | `#A099B0` | 占位、时间等弱文字 |

### 登录页（两端共用）

保留现有紫色渐变 `#667eea → #764ba2`，作为独立品牌页不受主色影响。

### 语义色

| 角色 | 色值 |
|------|------|
| 成功/在线 | `#4ECB9C` |
| 警告/待处理 | `#FFB84D` |
| 危险/离线 | `#FF6B6B` |
| 信息 | 跟随各端主色 |

## 设计系统

### 全局

- 圆角语言：卡片 24rpx，按钮 48rpx，输入框 16rpx，标签/chip 28rpx，模态框 32rpx
- 阴影：卡片 `0 8rpx 32rpx rgba(0,0,0,0.05)`；悬浮 `0 12rpx 40rpx rgba(0,0,0,0.08)`；按钮 `0 6rpx 20rpx rgba(主色, 0.25)`
- 间距：页面 padding 24rpx，卡片内 padding 28~32rpx，元素间距 20~24rpx

### 导航栏 & TabBar

- navigationBar 背景色：用户端 `#FF7E67`，咨询师端 `#7C6FF7`
- navigationBar 文字：白色
- TabBar 选中色：跟各自主色；未选中 `#B8A0A0` / `#A099B0`

### 按钮

- 主按钮：`background: linear-gradient(135deg, primary, primary-dark)` + 白字 + 48rpx 圆角 + 阴影
- 次要按钮：主色浅底 + 主色文字 + 无阴影
- 危险按钮：`#FF6B6B` 暖红底，协调暖调整体

### 卡片

- 白底 24rpx 圆角 + 柔和扩散阴影
- 状态类卡片（在线状态等）：用主色浅底 + 左边框 6rpx accent 色条

### 输入框

- 16rpx 圆角，背景 `#FFF5F2`（用户端）/ `#F5F2FC`（咨询师端）
- focus 状态：1rpx 主色边框

### 头像

- 用户端头像环：珊瑚粉 4rpx 边框
- 咨询师端头像环：暖紫 4rpx 边框
- 在线状态小圆点统一用语义绿/灰

## 页面改动清单

### 用户端 (miniprogram/pages/*)

| 页面 | 改动要点 |
|------|---------|
| login/register | 保持紫色渐变背景；表单卡片加大圆角；输入框改暖灰底；按钮改珊瑚渐变 |
| index | 状态卡片从绿/灰渐变改为珊瑚粉浅底；咨询师卡片加顶部暖色条；快速入口图标换暖色底；筛选chip选中改主色 |
| counselor/detail | 头像加珊瑚色环；预约按钮改暖渐变；评价星级金色保持 |
| appointment/book | 方法tab选中改暖渐变；条件chip选中改主色浅底；匹配按钮改暖色 |
| appointment/my | 卡片顶条改暖色；状态badge暖色系 |
| chat | 聊天气泡：我方改暖渐变，对方改白底暖边框；输入区背景暖灰 |
| ai/chat | 同上气泡风格 |
| test | 选项选中改主色浅底+主色边框；提交按钮暖渐变 |
| science | 卡片阴影柔和化；分类chip暖色 |
| review | 星级金色保持；提交按钮暖渐变 |
| user | 头像加珊瑚环；菜单项hover暖色底；退出按钮变暖红 |

### 咨询师端 (counselor/miniprogram/pages/*)

| 页面 | 改动要点 |
|------|---------|
| login | 保持紫色渐变背景；输入框改暖灰底；按钮改暖紫渐变 |
| index | 状态卡片渐变改暖紫；统计图标底色改暖紫系；快速操作图标底各色；upcoming卡片暖化 |
| appointments | tab选中改暖紫；卡片顶条改暖紫渐变；状态badge暖化；按钮改暖色渐变 |
| appointment-detail | 同上风格 |
| chat | 气泡：我方暖紫渐变，对方白底暖边框 |
| records | 记录卡片暖化；草稿/已完成状态色跟随语义色 |
| schedule | 排班卡片柔和化 |
| profile | 头部渐变改暖紫；统计图标底各色；菜单项暖化；退出按钮暖红 |

## 不改动

- 所有 WXML 结构
- 所有 JS 逻辑
- 微信小程序组件生命周期
- WebSocket 逻辑
- 管理端（admin-web）样式
