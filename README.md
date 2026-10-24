# 心理咨询管理系统（本科毕业设计）

面向高校心理咨询场景的多端系统：来访者可以浏览咨询师、预约咨询、在线沟通、完成心理测评和阅读科普文章；咨询师处理预约、管理排班、与来访者沟通并填写咨询记录；管理员维护用户、咨询师、测评题库、科普内容和公告，并查看统计数据。

这是一个毕业设计项目，用于学习和展示，不是正式上线的生产系统。仓库中的示例数据均为虚构。

## 技术栈

| 部分 | 技术 |
|---|---|
| 后端 | Java、Spring Boot 3、MyBatis-Plus、MySQL |
| 认证 | JWT（jjwt）、BCrypt 密码加密（spring-security-crypto） |
| 实时通信 | Spring WebSocket |
| 网页端 | 原生 HTML / CSS / JavaScript（用户端、咨询师端、管理端） |
| 小程序 | 微信小程序（用户端、咨询师端） |

## 功能

- **账号与认证**：来访者、咨询师、管理员三种角色登录；接口通过拦截器校验 `Authorization: Bearer <token>`，Token 中携带用户 ID 与角色。
- **预约与排班**：咨询师维护周排班，来访者按可用时段预约，咨询师确认或取消。
- **在线沟通**：消息先通过 HTTP 接口写入数据库，再经 WebSocket（`/api/chat/ws`）推送给对方；小程序聊天页另有定时拉取，作为连接不可用时的兜底。包含咨询师在线状态与心跳。
- **咨询记录与评价**：咨询师填写咨询记录；来访者对已完成的咨询进行评价。
- **心理测评**：题库与选项维护、在线作答、测评记录。
- **科普与公告**：文章分类、发布、点赞；系统公告推送。
- **统计**：预约趋势、服务类型分布、咨询师排行等管理端统计。
- **AI 对话（早期实验）**：保留了一版接入讯飞星火大模型的对话接口代码，默认不配置任何密钥，不可直接使用。

## 目录结构

```
backend/psychology-backend/   Spring Boot 后端（controller / service / mapper / entity / interceptor / handler）
user-web/                     来访者网页端
counselor-web/                咨询师网页端
admin-web/                    管理端
miniprogram/                  来访者微信小程序
counselor/miniprogram/        咨询师微信小程序
database/                     建表补充、排班、测评、科普等 SQL 脚本
docs/                         开发过程中的界面优化方案
```

## 本地运行

1. **数据库**：安装 MySQL，创建数据库 `psychology_db`，依次执行 `backend/psychology-backend/src/main/resources/init_database.sql`、`init_data.sql`，再按需执行 `database/` 下的脚本补充示例数据。
2. **配置**：所有敏感配置都通过环境变量提供，代码中不保存密钥。

   | 环境变量 | 说明 |
   |---|---|
   | `DB_USERNAME` / `DB_PASSWORD` | MySQL 账号，用户名默认 `root` |
   | `JWT_SECRET` | JWT 签名密钥，至少 32 字节；未配置时启动会生成临时随机密钥并告警，重启后旧 Token 失效，仅适合本地演示 |
   | `WX_APPID` / `WX_SECRET` | 小程序微信登录所需，可不配置 |
   | `XF_APPID` / `XF_API_KEY` / `XF_API_SECRET` | 早期 AI 对话实验所需，可不配置 |

3. **后端**：进入 `backend/psychology-backend`，执行 `mvn spring-boot:run`，服务默认运行在 `http://localhost:8080`。`pom.xml` 中指定的 Java 版本为 25。
4. **网页端**：前端代码中的接口地址为 `http://localhost:8080`，用任意静态服务器打开对应目录即可。
5. **小程序**：用微信开发者工具导入 `miniprogram/` 或 `counselor/miniprogram/`。仓库中的 AppID 已替换为测试号 `touristappid`，微信登录等需要真实 AppID 的能力不可用。

示例账号的默认密码见 `database/update_passwords.sql` 中的注释。

## 说明

- 本仓库为整理后的展示版本：已移除个人信息、论文生成脚本和本机路径，密钥改为环境变量读取。
- 整理时未在新环境中重新完整运行，如遇依赖或脚本问题，以代码为准。
