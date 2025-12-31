# Web3 DApp Store 开发日志

## 概述

本文档记录 Web3 DApp Store 项目的技术开发过程，包括架构决策、实现细节和问题解决。

---

## 2024-12-31: 后端项目初始化

### 目标
搭建 Kotlin + Spring Boot 后端项目基础框架

### 技术栈选型

| 组件 | 选择 | 理由 |
|------|------|------|
| 语言 | Kotlin 1.9 | 简洁、空安全、协程支持 |
| 框架 | Spring Boot 3.2 | 成熟稳定、生态丰富 |
| API 模式 | WebFlux (响应式) | 高并发场景性能更好 |
| 数据库访问 | R2DBC | 响应式非阻塞数据库驱动 |
| 数据库 | PostgreSQL 15 | 可靠、功能丰富 |
| 缓存 | Redis 7 | 高性能缓存 + 会话存储 |
| 消息队列 | Kafka | 链上事件处理、推送通知 |
| 对象存储 | MinIO | APK 和图片存储 |
| 搜索 | Elasticsearch 8 | 应用全文搜索 |

### 项目结构设计

```
backend/
├── src/main/kotlin/com/di/dappstore/
│   ├── DAppStoreApplication.kt          # 应用入口
│   ├── config/                           # 配置类
│   │   ├── SecurityConfig.kt            # 安全配置
│   │   ├── R2dbcConfig.kt               # 数据库配置
│   │   ├── RedisConfig.kt               # Redis 配置
│   │   ├── MinioConfig.kt               # 对象存储配置
│   │   └── WebConfig.kt                 # Web 配置
│   ├── controller/                       # API 控制器
│   │   ├── AppController.kt             # 应用 API
│   │   ├── CategoryController.kt        # 分类 API
│   │   ├── UserController.kt            # 用户 API
│   │   ├── DeveloperController.kt       # 开发者 API
│   │   └── SearchController.kt          # 搜索 API
│   ├── service/                          # 业务服务
│   │   ├── AppService.kt
│   │   ├── UserService.kt
│   │   ├── ReviewService.kt
│   │   ├── StorageService.kt
│   │   └── PushService.kt
│   ├── repository/                       # 数据访问层
│   │   ├── AppRepository.kt
│   │   ├── UserRepository.kt
│   │   └── ReviewRepository.kt
│   ├── model/                            # 数据模型
│   │   ├── entity/                      # 数据库实体
│   │   ├── dto/                         # 数据传输对象
│   │   └── vo/                          # 视图对象
│   ├── security/                         # 安全模块
│   │   ├── JwtTokenProvider.kt
│   │   └── WalletAuthenticationFilter.kt
│   ├── exception/                        # 异常处理
│   │   ├── GlobalExceptionHandler.kt
│   │   └── BusinessException.kt
│   └── util/                             # 工具类
├── src/main/resources/
│   ├── application.yml                   # 主配置
│   ├── application-dev.yml               # 开发环境
│   ├── application-prod.yml              # 生产环境
│   └── db/migration/                     # 数据库迁移脚本
├── src/test/kotlin/                      # 测试代码
├── build.gradle.kts                      # Gradle 构建脚本
├── settings.gradle.kts
└── Dockerfile
```

### 执行步骤

#### Step 1: 创建项目目录结构
```bash
mkdir -p backend/src/main/kotlin/com/di/dappstore/{config,controller,service,repository,model/{entity,dto,vo},security,exception,util}
mkdir -p backend/src/main/resources/db/migration
mkdir -p backend/src/test/kotlin/com/di/dappstore
```

#### Step 2: 创建 Gradle 构建文件
- 配置 Kotlin 1.9.22
- 配置 Spring Boot 3.2.1
- 添加 WebFlux、R2DBC、Redis、Security 等依赖

#### Step 3: 创建应用配置
- application.yml: 通用配置
- application-dev.yml: 开发环境 (H2 内存数据库)
- application-prod.yml: 生产环境 (PostgreSQL)

#### Step 4: 创建核心代码
- 应用入口类
- 基础配置类
- 实体模型
- Repository 接口
- Service 层
- Controller 层

### 执行记录

#### 已完成
1. **项目初始化**
   - 创建 `settings.gradle.kts` 和 `build.gradle.kts`
   - 配置 Spring Boot 3.2.1 + Kotlin 1.9.22
   - 添加 WebFlux、R2DBC、Redis、Security、JWT、Web3j、MinIO 等依赖

2. **实体模型 (Entity)**
   - `BaseEntity.kt` - 基础实体类
   - `App.kt` - DApp 应用实体
   - `User.kt` - 用户实体
   - `Developer.kt` - 开发者实体
   - `Category.kt` - 分类实体
   - `Review.kt` - 评论实体
   - `Screenshot.kt` - 截图实体

3. **数据传输对象 (DTO)**
   - `AppDto.kt` - 应用创建/更新请求
   - `AuthDto.kt` - 认证相关请求/响应
   - `ReviewDto.kt` - 评论请求
   - `DeveloperDto.kt` - 开发者注册/更新请求

4. **视图对象 (VO)**
   - `AppVo.kt` - 应用列表项/详情视图
   - `PageResponse.kt` - 分页响应和 API 统一响应格式

5. **Repository 层**
   - `AppRepository.kt` - 应用数据访问
   - `UserRepository.kt` - 用户数据访问
   - `DeveloperRepository.kt` - 开发者数据访问
   - `CategoryRepository.kt` - 分类数据访问
   - `ReviewRepository.kt` - 评论数据访问
   - `ScreenshotRepository.kt` - 截图数据访问

6. **Service 层**
   - `AppService.kt` - 应用业务逻辑
   - `UserService.kt` - 用户业务逻辑
   - `CategoryService.kt` - 分类业务逻辑
   - `ReviewService.kt` - 评论业务逻辑
   - `DeveloperService.kt` - 开发者业务逻辑

7. **Controller 层**
   - `AppController.kt` - 应用 API
   - `CategoryController.kt` - 分类 API
   - `ReviewController.kt` - 评论 API
   - `AuthController.kt` - 认证 API
   - `DeveloperController.kt` - 开发者 API
   - `HealthController.kt` - 健康检查 API

8. **配置类**
   - `SecurityConfig.kt` - Spring Security 配置
   - `R2dbcConfig.kt` - R2DBC 数据库配置
   - `JacksonConfig.kt` - JSON 序列化配置

9. **异常处理**
   - `GlobalExceptionHandler.kt` - 全局异常处理
   - `BusinessException.kt` - 业务异常类

10. **数据库 Schema**
    - `schema.sql` - 表结构定义
    - `data.sql` - 初始数据

### 问题与解决

1. **Kotlin Nullable 类型与 Reactor**
   - 问题: `Mono.zip()` 需要非空类型，但 Category 可能为空
   - 解决: 使用 `java.util.Optional` 包装可空类型

2. **Gradle Wrapper 不存在**
   - 问题: 系统未安装 Gradle
   - 解决: 手动创建 `gradle-wrapper.properties` 并下载 `gradle-wrapper.jar`

### 构建验证

```bash
./gradlew build -x test
# BUILD SUCCESSFUL in 13s
```

---

## API 接口列表

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/v1/health | 健康检查 |
| GET | /api/v1/version | 版本信息 |
| GET | /api/v1/apps | 获取应用列表 |
| GET | /api/v1/apps/{id} | 获取应用详情 |
| GET | /api/v1/apps/search | 搜索应用 |
| GET | /api/v1/apps/featured | 推荐应用 |
| GET | /api/v1/apps/top-downloads | 热门下载 |
| GET | /api/v1/apps/top-rated | 高分应用 |
| GET | /api/v1/apps/latest | 最新应用 |
| POST | /api/v1/apps/{id}/download | 记录下载 |
| GET | /api/v1/categories | 获取分类 |
| POST | /api/v1/auth/nonce | 获取登录 nonce |
| POST | /api/v1/auth/login | 钱包登录 |
| POST | /api/v1/developers/register | 开发者注册 |
| GET | /api/v1/developers/me | 当前开发者信息 |
| GET | /api/v1/apps/{appId}/reviews | 应用评论 |
| POST | /api/v1/apps/{appId}/reviews | 提交评论 |

---

## 后续计划

- [ ] 实现钱包签名验证逻辑
- [ ] 集成 JWT Token 生成
- [ ] 实现 APK 上传到 MinIO
- [ ] 集成 Elasticsearch 搜索
- [ ] 添加 Redis 缓存
- [ ] Docker 容器化
- [ ] Kubernetes 部署配置
- [ ] 单元测试和集成测试
