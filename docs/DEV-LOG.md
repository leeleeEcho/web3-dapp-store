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

---

## 2024-12-31: Android 客户端网络层实现

### 目标
连接 Android 客户端到后端 API，实现真实数据展示

### 技术栈

| 组件 | 选择 | 理由 |
|------|------|------|
| 网络库 | Retrofit 2 + OkHttp | 成熟稳定、支持协程 |
| JSON 解析 | Moshi | Kotlin 友好、高性能 |
| 依赖注入 | Hilt | 官方推荐、编译时检查 |
| 状态管理 | StateFlow | 响应式、生命周期感知 |
| 架构模式 | MVVM | 关注点分离、可测试性 |

### 实现内容

#### 1. 网络层架构

```
android/app/src/main/java/com/web3store/
├── data/
│   ├── remote/
│   │   ├── api/
│   │   │   └── DAppStoreApi.kt        # Retrofit API 接口
│   │   ├── dto/
│   │   │   ├── ApiResponse.kt         # 统一响应包装
│   │   │   ├── AppDto.kt              # 应用列表 DTO
│   │   │   └── AppDetailDto.kt        # 应用详情 DTO
│   │   └── mapper/
│   │       └── AppMapper.kt           # DTO -> Domain 映射
│   └── repository/
│       └── AppRepository.kt           # 数据仓库实现
├── di/
│   └── NetworkModule.kt               # Hilt 网络模块
├── domain/
│   └── model/
│       ├── AppInfo.kt                 # 应用列表模型
│       └── AppDetail.kt               # 应用详情模型
└── ui/
    └── viewmodel/
        ├── HomeViewModel.kt           # 首页 ViewModel
        └── AppDetailViewModel.kt      # 详情页 ViewModel
```

#### 2. API 端点映射

| Android 方法 | 后端 API | 用途 |
|-------------|----------|------|
| `getFeaturedApps()` | GET /api/v1/apps/featured | 精选应用 |
| `getTopDownloads()` | GET /api/v1/apps/top-downloads | 热门下载 |
| `getTopRated()` | GET /api/v1/apps/top-rated | 高分应用 |
| `getLatestApps()` | GET /api/v1/apps/latest | 最新应用 |
| `getAppDetail(id)` | GET /api/v1/apps/{id} | 应用详情 |
| `getCategories()` | GET /api/v1/categories | 分类列表 |

#### 3. 关键代码实现

**NetworkModule.kt (Hilt 依赖注入)**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:9000/"  // 模拟器访问本地

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
}
```

**HomeViewModel.kt (状态管理)**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadHomeData() }
}
```

### 测试验证

#### API 连接测试 (2024-12-31 15:43)

```
首页 API:
✅ GET /api/v1/apps/featured     → 200 OK (16ms)
✅ GET /api/v1/apps/top-downloads → 200 OK (11ms)

详情页 API:
✅ GET /api/v1/apps/1            → 200 OK (7ms)
```

#### 响应数据示例
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Uniswap",
    "packageName": "com.uniswap.mobile",
    "description": "去中心化交易所",
    "developer": {"companyName": "DI Labs", "isVerified": true},
    "category": {"name": "defi", "displayName": "DeFi"}
  }
}
```

### 问题与解决

1. **端口 8080 被占用**
   - 问题: 本地 8080 端口被其他进程占用
   - 解决: 将后端端口改为 9000，同步修改 Android NetworkModule

2. **导航参数类型不匹配**
   - 问题: Navigation 传递 String 类型 appId，但 ViewModel 期望 Long
   - 解决: 在 ViewModel 中同时支持 String 和 Long 类型

3. **LinearProgressIndicator API 变更**
   - 问题: Material 3 新版本使用 lambda 参数 `progress = { value }`
   - 解决: 使用 `@Suppress("DEPRECATION")` 保持兼容

---

## 开发状态总结

### 已完成功能

| 模块 | 功能 | 状态 |
|------|------|------|
| **后端** | Spring Boot 项目框架 | ✅ 完成 |
| | R2DBC 数据库访问 | ✅ 完成 |
| | REST API (App/Category/Review/Auth) | ✅ 完成 |
| | 健康检查接口 | ✅ 完成 |
| | 初始数据 (Uniswap/OpenSea/Aave) | ✅ 完成 |
| **Android** | Jetpack Compose UI | ✅ 完成 |
| | Hilt 依赖注入 | ✅ 完成 |
| | MVVM 架构 | ✅ 完成 |
| | 首页 (精选/热门/最新) | ✅ 完成 |
| | 详情页 (API 连接) | ✅ 完成 |
| | 网络层 (Retrofit/OkHttp) | ✅ 完成 |
| | 主题系统 (黑金风格) | ✅ 完成 |

### 待开发功能

| 模块 | 功能 | 优先级 |
|------|------|--------|
| **后端** | JWT Token 认证 | 高 |
| | 钱包签名验证 | 高 |
| | MinIO 文件存储 | 中 |
| | Redis 缓存 | 中 |
| | Elasticsearch 搜索 | 中 |
| | Docker 部署 | 低 |
| **Android** | 钱包集成 (Trust Wallet Core) | 高 |
| | WalletConnect V2 | 高 |
| | APK 下载安装 | 高 |
| | 搜索功能 | 中 |
| | 分类浏览 | 中 |
| | 评分评论 | 低 |
| | 用户登录 | 低 |

---

## 2025-01-04: APK 下载安装功能实现

### 目标
实现 Android 客户端的 APK 下载和安装功能，包括真实下载进度、文件缓存管理和系统安装器调用

### 技术栈

| 组件 | 选择 | 理由 |
|------|------|------|
| 下载器 | OkHttp 流式下载 | 项目已有、可控制进度 |
| 后台任务 | WorkManager | 可靠的后台下载、系统调度 |
| 存储位置 | externalCacheDir | 无需额外权限 |
| 安装器 | PackageInstaller Session API | Android 12+ 最佳体验 |
| 文件共享 | FileProvider | API 24+ 必需 |
| DI 集成 | Hilt + HiltWorker | 统一依赖注入 |

### 实现架构

```
android/app/src/main/java/com/web3store/
├── cache/
│   └── ApkCache.kt                    # APK 文件缓存管理
├── download/
│   ├── DownloadState.kt               # 下载状态密封类
│   ├── ApkDownloader.kt               # OkHttp 流式下载核心
│   ├── DownloadWorker.kt              # WorkManager 后台任务
│   └── DownloadRepository.kt          # 下载状态仓库
├── installer/
│   ├── InstallState.kt                # 安装状态密封类
│   ├── ApkInstaller.kt                # PackageInstaller 实现
│   └── InstallerReceiver.kt           # 安装回调接收器
└── di/
    └── DownloadModule.kt              # Hilt DI 模块
```

### 核心实现

#### 1. 下载状态管理

```kotlin
sealed class DownloadState {
    object Idle : DownloadState()
    object Pending : DownloadState()
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long?,
        val progress: Float
    ) : DownloadState()
    data class Completed(val file: File) : DownloadState()
    data class Failed(val error: DownloadError) : DownloadState()
}
```

#### 2. OkHttp 流式下载 (ApkDownloader.kt)

```kotlin
suspend fun download(
    url: String,
    targetFile: File,
    onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit
): Result<File> = withContext(Dispatchers.IO) {
    val response = okHttpClient.newCall(request).execute()
    body.source().use { source ->
        targetFile.sink().buffer().use { sink ->
            var totalBytesRead = 0L
            while (isActive) {
                val bytesRead = source.read(sink.buffer, BUFFER_SIZE)
                if (bytesRead == -1L) break
                totalBytesRead += bytesRead
                onProgress(totalBytesRead, contentLength)
            }
        }
    }
    Result.success(targetFile)
}
```

#### 3. WorkManager 后台任务 (DownloadWorker.kt)

```kotlin
@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val apkDownloader: ApkDownloader
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo(packageName, 0))  // 前台通知
        apkDownloader.download(url, targetFile) { bytesRead, totalBytes ->
            setProgressAsync(workDataOf(KEY_PROGRESS to progress))
            updateNotification(packageName, progress)
        }
    }
}
```

#### 4. PackageInstaller Session API (ApkInstaller.kt)

```kotlin
private fun installWithPackageInstaller(apkFile: File, packageName: String) {
    val params = PackageInstaller.SessionParams(
        PackageInstaller.SessionParams.MODE_FULL_INSTALL
    ).apply {
        setAppPackageName(packageName)
        setInstallReason(PackageManager.INSTALL_REASON_USER)
    }

    val sessionId = packageInstaller.createSession(params)
    packageInstaller.openSession(sessionId).use { session ->
        session.openWrite("apk", 0, apkFile.length()).use { out ->
            apkFile.inputStream().use { it.copyTo(out) }
        }
        session.commit(createIntentSender(sessionId))
    }
}
```

#### 5. UI 按钮状态流转

```kotlin
sealed class ActionButtonState {
    object Download : ActionButtonState()
    data class Downloading(val progress: Float) : ActionButtonState()
    object Install : ActionButtonState()
    data class Installing(val progress: Float) : ActionButtonState()
    object Open : ActionButtonState()
    object Update : ActionButtonState()
}
```

### 新建文件 (10个)

| 文件 | 大小 | 用途 |
|------|------|------|
| `cache/ApkCache.kt` | 117 行 | 文件缓存管理 + FileProvider URI |
| `download/DownloadState.kt` | 36 行 | 下载状态密封类 |
| `download/ApkDownloader.kt` | 131 行 | OkHttp 流式下载 + Hash 校验 |
| `download/DownloadWorker.kt` | 218 行 | WorkManager 后台任务 + 通知 |
| `download/DownloadRepository.kt` | 150 行 | 下载状态仓库 |
| `installer/InstallState.kt` | 30 行 | 安装状态密封类 |
| `installer/ApkInstaller.kt` | 160 行 | PackageInstaller 实现 |
| `installer/InstallerReceiver.kt` | 65 行 | 安装回调广播接收器 |
| `di/DownloadModule.kt` | 45 行 | Hilt DI 模块 |
| `res/xml/file_paths.xml` | 5 行 | FileProvider 配置 |

### 修改文件 (5个)

| 文件 | 修改内容 |
|------|----------|
| `build.gradle.kts` | 添加 WorkManager + Hilt-Work 依赖 |
| `AndroidManifest.xml` | 添加权限、FileProvider、InstallerReceiver |
| `Web3StoreApplication.kt` | 实现 Configuration.Provider 集成 HiltWorkerFactory |
| `AppDetailViewModel.kt` | 注入 DownloadRepository/ApkInstaller，实现完整下载/安装逻辑 |
| `AppDetailScreen.kt` | 根据 ActionButtonState 更新按钮 UI |

### 权限配置

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- FileProvider for sharing APK with installer -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>

<!-- Installer callback receiver -->
<receiver android:name=".installer.InstallerReceiver" android:exported="false" />
```

### 依赖添加

```kotlin
// build.gradle.kts
implementation("androidx.work:work-runtime-ktx:2.9.0")
implementation("androidx.hilt:hilt-work:1.1.0")
kapt("androidx.hilt:hilt-compiler:1.1.0")
```

### 问题与解决

1. **Kotlin property/getter 命名冲突**
   - 问题: `data class` 的 `message` 属性自动生成 `getMessage()` 方法，与手动定义的 `getMessage()` 冲突
   - 错误: `Accidental override: The following declarations have the same JVM signature`
   - 解决: 将 `message` 重命名为 `errorMessage`，使用 `abstract val` 定义抽象属性

2. **KAPT 缓存问题**
   - 问题: 修复编译错误后，Hilt 仍无法解析类型
   - 解决: 运行 `./gradlew clean assembleDebug` 清除缓存重新构建

3. **HiltWorker 集成**
   - 问题: WorkManager 默认不支持 Hilt 注入
   - 解决: Application 实现 `Configuration.Provider`，注入 `HiltWorkerFactory`

### 构建验证

```bash
./gradlew clean assembleDebug
# BUILD SUCCESSFUL in 4s

adb install -r app/build/outputs/apk/debug/app-debug.apk
# Performing Streamed Install - Success
```

---

## 开发状态总结

### 已完成功能

| 模块 | 功能 | 状态 |
|------|------|------|
| **后端** | Spring Boot 项目框架 | ✅ 完成 |
| | R2DBC 数据库访问 | ✅ 完成 |
| | REST API (App/Category/Review/Auth) | ✅ 完成 |
| | 健康检查接口 | ✅ 完成 |
| | 初始数据 (Uniswap/OpenSea/Aave) | ✅ 完成 |
| **Android** | Jetpack Compose UI | ✅ 完成 |
| | Hilt 依赖注入 | ✅ 完成 |
| | MVVM 架构 | ✅ 完成 |
| | 首页 (精选/热门/最新) | ✅ 完成 |
| | 详情页 (API 连接) | ✅ 完成 |
| | 网络层 (Retrofit/OkHttp) | ✅ 完成 |
| | 主题系统 (黑金风格) | ✅ 完成 |
| | **APK 下载 (OkHttp + WorkManager)** | ✅ 完成 |
| | **APK 安装 (PackageInstaller)** | ✅ 完成 |
| | **下载进度通知** | ✅ 完成 |
| | **APK 缓存管理** | ✅ 完成 |

### 待开发功能

> **2025-01-04 更新**: 取消钱包集成和用户认证功能，专注于纯浏览+下载体验

| 模块 | 功能 | 优先级 | 备注 |
|------|------|--------|------|
| **后端** | MinIO 文件存储 | 🔴 高 | APK/图片真实存储，支持下载 |
| | Elasticsearch 搜索 | 🟡 中 | 全文搜索支持 |
| | Redis 缓存 | 🟡 中 | 热门应用缓存 |
| | Docker 部署 | 🟢 低 | 容器化部署 |
| **Android** | 搜索功能 | 🔴 高 | 应用搜索 UI + 后端对接 |
| | 分类浏览 | 🟡 中 | 按 DeFi/NFT/Game 筛选 |
| | UI 优化 | 🟢 低 | 完善页面细节和交互 |

### 已取消功能

| 模块 | 功能 | 原因 |
|------|------|------|
| 后端 | JWT Token 认证 | 无用户系统 |
| 后端 | 钱包签名验证 | 取消钱包集成 |
| Android | 钱包集成 (Trust Wallet Core) | 产品方向调整 |
| Android | WalletConnect V2 | 产品方向调整 |
| Android | 用户登录 | 无用户系统 |
| Android | 评分评论 | 依赖用户系统 |

---

## 2025-01-06: MinIO 存储 + 搜索功能 + 分类浏览

### 目标
1. 实现后端 MinIO 文件存储服务
2. 实现 Android 搜索功能对接后端 API
3. 实现分类浏览功能

---

### Phase 1: MinIO 文件存储

#### 技术选型

| 组件 | 选择 | 理由 |
|------|------|------|
| 对象存储 | MinIO | S3 兼容、轻量、易部署 |
| 安装方式 | Homebrew | 本地开发无需 Docker |
| 端口 | 9100 | 避免与后端 9000 冲突 |

#### 实现架构

```
backend/src/main/kotlin/com/di/dappstore/
├── config/
│   ├── MinioProperties.kt      # MinIO 连接配置
│   └── MinioConfig.kt          # MinIO Client Bean
├── model/
│   ├── StorageBucket.kt        # 存储桶枚举 (APK/ICON/SCREENSHOT)
│   └── dto/StorageDto.kt       # 上传响应 DTO
├── service/
│   └── StorageService.kt       # 文件上传/下载/删除服务
└── controller/
    └── StorageController.kt    # 存储 API 控制器
```

#### 新增 API 端点

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/v1/storage/apk | 上传 APK 文件 (含 SHA256 哈希) |
| POST | /api/v1/storage/icon | 上传应用图标 |
| POST | /api/v1/storage/screenshot | 上传应用截图 |
| GET | /api/v1/storage/url | 获取预签名下载 URL |

#### 核心代码

**StorageBucket.kt**
```kotlin
enum class StorageBucket(val bucketName: String) {
    APK("dappstore-apks"),
    ICON("dappstore-icons"),
    SCREENSHOT("dappstore-screenshots");
}
```

**StorageService.kt**
```kotlin
fun uploadFile(bucket, objectName, inputStream, size, contentType): Mono<String>
fun getPresignedUrl(bucket, objectName, expiry): String
fun deleteFile(bucket, objectName): Mono<Unit>
```

#### 配置

```yaml
# application-dev.yml
minio:
  endpoint: http://localhost:9100
  access-key: minioadmin
  secret-key: minioadmin
```

#### 测试验证

```bash
# 启动 MinIO (Homebrew)
minio server ~/minio-data --address :9100

# 上传图标测试
curl -X POST http://localhost:9000/api/v1/storage/icon \
  -F "file=@icon.png" -F "packageName=com.test"
# 返回: {"success":true,"data":{"url":"...","objectName":"icons/..."}}

# 上传 APK 测试
curl -X POST http://localhost:9000/api/v1/storage/apk \
  -F "file=@app.apk" -F "packageName=com.test" -F "versionName=1.0"
# 返回: {"success":true,"data":{"url":"...","hash":"sha256:..."}}
```

---

### Phase 2: Android 搜索功能

#### 实现架构

```
android/app/src/main/java/com/web3store/
└── ui/
    ├── viewmodel/
    │   └── SearchViewModel.kt    # 搜索状态管理
    └── search/
        └── SearchScreen.kt       # 搜索界面 (已修改)
```

#### 核心功能

| 功能 | 实现方式 |
|------|----------|
| 防抖搜索 | 300ms debounce + distinctUntilChanged |
| 搜索历史 | 内存存储 (后续可迁移 Room) |
| 热门搜索 | 预设标签 (DeFi/NFT/DEX/GameFi) |
| 搜索结果 | 复用 AppListItem 卡片样式 |

#### 核心代码

**SearchViewModel.kt**
```kotlin
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .filter { it.isNotBlank() }
                .distinctUntilChanged()
                .collectLatest { query -> performSearch(query) }
        }
    }
}
```

#### API 对接

```
GET /api/v1/apps/search?keyword=uni
→ 返回匹配 "uni" 的应用列表 (Uniswap)
```

---

### Phase 3: 分类浏览功能

#### 实现架构

```
android/app/src/main/java/com/web3store/
└── ui/
    ├── viewmodel/
    │   └── CategoryViewModel.kt  # 分类页状态管理
    ├── category/
    │   └── CategoryScreen.kt     # 分类详情页
    └── home/
        └── HomeScreen.kt         # 添加 onCategoryClick 回调
```

#### 导航路由

```kotlin
// MainActivity.kt
composable(
    route = "category/{categoryId}/{categoryName}",
    arguments = listOf(
        navArgument("categoryId") { type = NavType.LongType },
        navArgument("categoryName") { type = NavType.StringType }
    )
) {
    CategoryScreen(
        onAppClick = { appId -> navController.navigate("app/$appId") },
        onBackClick = { navController.popBackStack() }
    )
}
```

#### 核心功能

| 功能 | 实现方式 |
|------|----------|
| 分类列表 | 首页 CategoryChip 展示 |
| 分类详情 | CategoryScreen 显示该分类应用 |
| 分页加载 | 滚动到底部自动加载更多 |
| 空状态 | 分类无应用时展示提示 |

#### API 对接

```
GET /api/v1/apps?categoryId=1
→ 返回 DeFi 分类下的应用列表
```

---

### Phase 4: 代码质量优化 - 移除硬编码

#### 问题分析

通过代码审查发现多处硬编码问题，按严重程度分类：

| 严重程度 | 问题 | 位置 |
|----------|------|------|
| **CRITICAL** | MinIO 凭证硬编码 | `application-dev.yml` |
| **CRITICAL** | JWT 密钥默认值 | `application.yml` |
| **HIGH** | Android API BASE_URL | `NetworkModule.kt` |
| **HIGH** | 后端端口/Redis 配置 | `application-dev.yml` |
| **MEDIUM** | UI 中文字符串硬编码 | 多个 Screen 文件 |

#### 修复方案

**1. 后端配置环境变量化**

```yaml
# application-dev.yml (修复后)
server:
  port: ${SERVER_PORT:9000}

spring:
  r2dbc:
    url: ${DATABASE_URL:r2dbc:h2:mem:///dappstore}
    username: ${DATABASE_USER:sa}
    password: ${DATABASE_PASSWORD:}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

minio:
  endpoint: ${MINIO_ENDPOINT:http://localhost:9100}
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
```

**2. Android BuildConfig 配置**

```kotlin
// build.gradle.kts
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:9000/\"")
    }
    release {
        buildConfigField("String", "API_BASE_URL", "\"https://api.di.xyz/\"")
    }
}

// NetworkModule.kt
private val BASE_URL: String = BuildConfig.API_BASE_URL
```

**3. Android 字符串资源化**

```xml
<!-- strings.xml (新增 100+ 条目) -->
<string name="nav_home">首页</string>
<string name="nav_games">游戏</string>
<string name="action_download">下载</string>
<string name="action_install">安装</string>
<string name="category_empty">该分类暂无应用</string>
<!-- ... -->
```

```kotlin
// BottomNavigation.kt
enum class BottomNavItem(
    @StringRes val labelResId: Int,  // 使用资源 ID 替代硬编码字符串
    ...
) {
    Home(R.string.nav_home, ...),
    Games(R.string.nav_games, ...),
    ...
}
```

#### 修改文件清单

| 文件 | 修改内容 |
|------|----------|
| `application.yml` | JWT 配置添加环境变量 |
| `application-dev.yml` | 所有配置支持环境变量覆盖 |
| `build.gradle.kts` | 添加 BuildConfig 字段 |
| `NetworkModule.kt` | 使用 BuildConfig.API_BASE_URL |
| `strings.xml` | 新增 100+ 字符串资源 |
| `BottomNavigation.kt` | 使用 @StringRes 注解 |
| `CategoryScreen.kt` | 使用 stringResource() |

---

### 新建文件清单

#### 后端 (5个)
```
backend/src/main/kotlin/com/di/dappstore/
├── config/MinioProperties.kt
├── config/MinioConfig.kt
├── model/StorageBucket.kt
├── model/dto/StorageDto.kt
├── service/StorageService.kt
└── controller/StorageController.kt
```

#### Android (3个)
```
android/app/src/main/java/com/web3store/ui/
├── viewmodel/SearchViewModel.kt
├── viewmodel/CategoryViewModel.kt
└── category/CategoryScreen.kt
```

### 修改文件清单

| 文件 | 修改内容 |
|------|----------|
| application-dev.yml | 添加 MinIO 配置 |
| SearchScreen.kt | 连接 SearchViewModel |
| HomeScreen.kt | 添加 onCategoryClick 回调 |
| MainActivity.kt | 添加 category 路由 |

---

## 开发状态总结

### 已完成功能

| 模块 | 功能 | 状态 |
|------|------|------|
| **后端** | Spring Boot 项目框架 | ✅ 完成 |
| | R2DBC 数据库访问 | ✅ 完成 |
| | REST API (App/Category/Review/Auth) | ✅ 完成 |
| | 健康检查接口 | ✅ 完成 |
| | 初始数据 (Uniswap/OpenSea/Aave) | ✅ 完成 |
| | **MinIO 文件存储** | ✅ 完成 |
| | **APK/图片上传 API** | ✅ 完成 |
| **Android** | Jetpack Compose UI | ✅ 完成 |
| | Hilt 依赖注入 | ✅ 完成 |
| | MVVM 架构 | ✅ 完成 |
| | 首页 (精选/热门/最新) | ✅ 完成 |
| | 详情页 (API 连接) | ✅ 完成 |
| | 网络层 (Retrofit/OkHttp) | ✅ 完成 |
| | 主题系统 (黑金风格) | ✅ 完成 |
| | APK 下载 (OkHttp + WorkManager) | ✅ 完成 |
| | APK 安装 (PackageInstaller) | ✅ 完成 |
| | 下载进度通知 | ✅ 完成 |
| | APK 缓存管理 | ✅ 完成 |
| | **搜索功能 (防抖 + API)** | ✅ 完成 |
| | **分类浏览** | ✅ 完成 |
| | **字符串资源化 (i18n 准备)** | ✅ 完成 |
| | **BuildConfig 环境配置** | ✅ 完成 |

### 待开发功能

| 模块 | 功能 | 优先级 | 备注 |
|------|------|--------|------|
| **后端** | Elasticsearch 搜索 | 🟡 中 | 全文搜索支持 |
| | Redis 缓存 | 🟡 中 | 热门应用缓存 |
| | Docker 部署 | 🟢 低 | 容器化部署 |
| **Android** | 搜索历史持久化 | 🟢 低 | Room/DataStore |
| | UI 优化 | 🟢 低 | 完善页面细节和交互 |

### 已取消功能

| 模块 | 功能 | 原因 |
|------|------|------|
| 后端 | JWT Token 认证 | 无用户系统 |
| 后端 | 钱包签名验证 | 取消钱包集成 |
| Android | 钱包集成 (Trust Wallet Core) | 产品方向调整 |
| Android | WalletConnect V2 | 产品方向调整 |
| Android | 用户登录 | 无用户系统 |
| Android | 评分评论 | 依赖用户系统 |

---

## 2025-01-06: Phase 4 - 部署优化实现

### 目标
完成 Docker 部署完善、Redis 缓存实现、Elasticsearch 全文搜索集成

### Phase 4.1: Docker 部署完善

#### 实现内容

| 文件 | 说明 |
|------|------|
| `application-prod.yml` | 生产环境配置 |
| `docker/.env.example` | 环境变量模板 |
| `docker/docker-compose.yml` | 完整服务编排 |
| `docker/Dockerfile.simple` | 简化构建镜像 |
| `docker/DEPLOYMENT.md` | 部署文档 |

#### Docker 服务架构

```yaml
services:
  postgres:      # PostgreSQL 16 数据库
  redis:         # Redis 7 缓存
  elasticsearch: # Elasticsearch 8.11 搜索
  minio:         # MinIO 对象存储
  web3store-server: # 后端服务
```

#### 部署测试结果

```bash
docker compose up -d
# 所有服务健康运行

curl http://localhost:8080/actuator/health
# {"status":"UP"}

curl http://localhost:8080/api/v1/categories
# 返回 8 个分类

curl http://localhost:8080/api/v1/apps/featured
# 返回 2 个精选应用
```

#### 修复问题

1. **Apple Silicon 兼容**: `eclipse-temurin:17-jdk-alpine` 改为 `eclipse-temurin:17-jdk`
2. **PostgreSQL 语法**: `AUTO_INCREMENT` 改为 `BIGSERIAL`
3. **YAML 重复键**: 合并重复的 `spring:` 配置块

---

### Phase 4.2: Redis 缓存实现

#### 新增文件

| 文件 | 说明 |
|------|------|
| `config/RedisConfig.kt` | Redis 配置 + JSON 序列化 |
| `service/cache/ReactiveCacheService.kt` | 响应式缓存服务 |
| `service/CachedAppService.kt` | 应用服务缓存包装 |

#### 缓存策略

| 数据 | 缓存键 | TTL |
|------|--------|-----|
| 精选应用 | `apps:featured` | 10 分钟 |
| 下载排行 | `apps:top-downloaded:{limit}` | 5 分钟 |
| 评分排行 | `apps:top-rated:{limit}` | 5 分钟 |
| 分类列表 | `categories:all` | 1 小时 |

---

### Phase 4.3: Elasticsearch 集成

#### 新增文件

| 文件 | 说明 |
|------|------|
| `config/ElasticsearchConfig.kt` | ES 客户端配置 |
| `model/document/AppDocument.kt` | 搜索文档模型 |
| `service/search/AppSearchService.kt` | 全文搜索服务 |
| `service/search/AppIndexSyncService.kt` | 索引同步服务 |

#### ES 索引结构

```json
{
  "mappings": {
    "properties": {
      "name": { "type": "text", "analyzer": "standard" },
      "description": { "type": "text" },
      "categoryName": { "type": "keyword" },
      "blockchain": { "type": "keyword" },
      "status": { "type": "keyword" },
      "downloadCount": { "type": "long" },
      "ratingAverage": { "type": "double" }
    }
  }
}
```

---

## 2025-01-06: Phase 5 - 认证系统完善

### 目标
实现 Google OAuth + 钱包签名双认证系统

### 技术方案

```
认证流程:

Google OAuth:
┌─────────┐   ID Token   ┌─────────┐   verify    ┌────────┐
│ Android │ ───────────► │ Backend │ ──────────► │ Google │
│   App   │ ◄─────────── │   API   │ ◄────────── │  API   │
└─────────┘   JWT Token  └─────────┘   payload   └────────┘

钱包签名:
┌─────────┐  1. getNonce  ┌─────────┐
│  DApp   │ ────────────► │ Backend │
│         │ ◄──────────── │         │
│         │  2. sign msg  │         │
│         │ ────────────► │         │  verify signature
│         │ ◄──────────── │         │
└─────────┘   JWT Token   └─────────┘
```

### 数据库变更

```sql
-- users 表新增字段
ALTER TABLE users ADD COLUMN google_id VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN auth_provider VARCHAR(50) DEFAULT 'WALLET';
-- wallet_address 改为可选
ALTER TABLE users ALTER COLUMN wallet_address DROP NOT NULL;
```

### 新增文件 (6个)

| 文件 | 说明 |
|------|------|
| `security/JwtService.kt` | JWT Token 生成和验证 |
| `security/AuthService.kt` | 双认证逻辑 (Google + 钱包) |
| `security/JwtAuthenticationFilter.kt` | JWT 请求过滤器 |
| `security/SecurityUtils.kt` | 获取当前用户工具类 |
| `model/entity/AuthProvider.kt` | 认证方式枚举 |
| `model/dto/AuthDto.kt` | Google 登录请求等 DTO |

### 修改文件 (7个)

| 文件 | 修改内容 |
|------|----------|
| `schema.sql` | 新增 google_id, auth_provider 字段 |
| `build.gradle.kts` | 添加 Google API Client 依赖 |
| `User.kt` | 新增 googleId, authProvider 属性 |
| `UserRepository.kt` | 新增 findByGoogleId 方法 |
| `SecurityConfig.kt` | 添加 JWT 过滤器 |
| `AuthController.kt` | 完整双认证 API |
| `ReviewController.kt` | 使用 SecurityUtils 获取用户 |
| `DeveloperController.kt` | 使用 SecurityUtils 获取用户 |

### 认证 API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/auth/google` | POST | Google 登录 |
| `/api/v1/auth/wallet/nonce` | POST | 获取钱包签名 nonce |
| `/api/v1/auth/wallet/login` | POST | 钱包签名登录 |
| `/api/v1/auth/bind-wallet` | POST | 绑定钱包（需登录）|
| `/api/v1/auth/me` | GET | 获取当前用户 |
| `/api/v1/auth/logout` | POST | 登出 |

### 配置要求

```yaml
# application.yml
app:
  jwt:
    secret: ${JWT_SECRET:dev-only-secret}
    expiration: ${JWT_EXPIRATION:86400000}
  google:
    client-id: ${GOOGLE_CLIENT_ID:your-client-id}
```

### 构建验证

```bash
./gradlew compileKotlin
# BUILD SUCCESSFUL in 7s
```

---

## 开发状态总结

### 已完成功能

| 模块 | 功能 | 状态 |
|------|------|------|
| **后端** | Spring Boot 项目框架 | ✅ 完成 |
| | R2DBC 数据库访问 | ✅ 完成 |
| | REST API (App/Category/Review/Auth) | ✅ 完成 |
| | MinIO 文件存储 | ✅ 完成 |
| | **Docker 部署 (docker-compose)** | ✅ 完成 |
| | **Redis 缓存** | ✅ 完成 |
| | **Elasticsearch 全文搜索** | ✅ 完成 |
| | **Google OAuth 认证** | ✅ 完成 |
| | **钱包签名认证** | ✅ 完成 |
| | **JWT Token 机制** | ✅ 完成 |
| **Android** | Jetpack Compose UI | ✅ 完成 |
| | Hilt 依赖注入 | ✅ 完成 |
| | MVVM 架构 | ✅ 完成 |
| | APK 下载安装 | ✅ 完成 |
| | 搜索功能 | ✅ 完成 |
| | 分类浏览 | ✅ 完成 |

### 待开发功能

| 模块 | 功能 | 优先级 | 备注 |
|------|------|--------|------|
| **后端** | 应用提交 API | 🔴 高 | 开发者提交/更新应用 |
| | 应用审核 API | 🔴 高 | 管理员审批/拒绝 |
| | 管理后台 API | 🟡 中 | 数据统计、用户管理 |
| **Android** | Google 登录集成 | 🔴 高 | 对接后端认证 API |
| | 搜索历史持久化 | 🟢 低 | Room/DataStore |
| | UI 优化 | 🟢 低 | 细节完善 |

---

## 2025-01-06: Phase 6 - 应用提交管理 API

### 目标
实现开发者应用提交、更新、版本发布的完整工作流

### 应用状态流转

```
         提交新应用
              │
              ▼
┌─────────────────────────────┐
│           DRAFT             │◄──────┐
│         (草稿)              │       │
└─────────────────────────────┘       │
              │ 提交审核              │ 修改后重新提交
              ▼                       │
┌─────────────────────────────┐       │
│          PENDING            │───────┘
│        (待审核)             │
└─────────────────────────────┘
              │
    ┌─────────┴─────────┐
    ▼                   ▼
┌─────────┐       ┌─────────┐
│APPROVED │       │REJECTED │
│(已通过) │       │(已拒绝) │
└─────────┘       └─────────┘
    │                   │
    │                   └──► 可修改后重新提交
    ▼
┌─────────┐
│SUSPENDED│ (管理员可暂停/恢复)
│(已暂停) │
└─────────┘
```

### 数据库变更

```sql
-- apps 表新增字段
ALTER TABLE apps ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE apps ADD COLUMN rejection_reason TEXT;
ALTER TABLE apps ADD COLUMN submitted_at TIMESTAMP;
ALTER TABLE apps ADD COLUMN reviewed_at TIMESTAMP;
ALTER TABLE apps ADD COLUMN reviewer_id BIGINT REFERENCES users(id);

-- 状态默认值改为 DRAFT
ALTER TABLE apps ALTER COLUMN status SET DEFAULT 'DRAFT';
```

### 新增文件 (3个)

| 文件 | 说明 |
|------|------|
| `model/dto/AppSubmissionDto.kt` | 应用提交请求/响应 DTO |
| `service/AppSubmissionService.kt` | 应用提交业务逻辑 |
| `controller/DeveloperAppController.kt` | 开发者应用管理 API |

### 修改文件 (2个)

| 文件 | 修改内容 |
|------|----------|
| `model/entity/App.kt` | 新增 DRAFT 状态、rejection_reason 等字段 |
| `repository/AppRepository.kt` | 新增开发者应用查询方法 |

### 开发者应用 API

| 方法 | 端点 | 功能 |
|------|------|------|
| POST | `/api/v1/developer/apps` | 提交新应用 |
| GET | `/api/v1/developer/apps` | 获取我的应用列表 |
| GET | `/api/v1/developer/apps/statistics` | 获取应用统计 |
| GET | `/api/v1/developer/apps/{id}` | 获取应用详情 |
| PUT | `/api/v1/developer/apps/{id}` | 更新应用信息 |
| POST | `/api/v1/developer/apps/{id}/submit` | 提交审核 |
| POST | `/api/v1/developer/apps/{id}/version` | 发布新版本 |
| DELETE | `/api/v1/developer/apps/{id}` | 删除草稿 |

### 核心功能

| 功能 | 说明 |
|------|------|
| 草稿保存 | 提交时可选 `saveAsDraft=true` 保存为草稿 |
| 提交审核 | 草稿或被拒绝的应用可重新提交审核 |
| 版本发布 | 已上架应用可发布新版本（需重新审核）|
| 应用统计 | 统计各状态应用数量、总下载量、平均评分 |
| 软删除 | 仅允许删除草稿状态的应用 |

### DTO 结构

```kotlin
// 提交新应用请求
data class SubmitAppRequest(
    val packageName: String,
    val name: String,
    val description: String?,
    val versionName: String,
    val versionCode: Long,
    val apkUrl: String,
    val apkHash: String,
    val apkSize: Long,
    val iconUrl: String?,
    val screenshotUrls: List<String>?,
    val saveAsDraft: Boolean = false
)

// 开发者应用统计
data class AppStatistics(
    val totalApps: Int,
    val draftCount: Int,
    val pendingCount: Int,
    val approvedCount: Int,
    val rejectedCount: Int,
    val totalDownloads: Long,
    val averageRating: Double
)
```

### 构建验证

```bash
./gradlew compileKotlin
# BUILD SUCCESSFUL in 3s
```

---

## 2025-01-06: Phase 7 - 管理员审核 API

### 目标
实现管理员应用审核、精选管理、应用暂停/恢复功能

### 新增文件 (3个)

| 文件 | 说明 |
|------|------|
| `model/dto/AdminDto.kt` | 管理员审核请求/响应 DTO |
| `service/AdminAppService.kt` | 管理员审核业务逻辑 |
| `controller/AdminAppController.kt` | 管理员审核 API |

### 修改文件 (1个)

| 文件 | 修改内容 |
|------|----------|
| `repository/AppRepository.kt` | 新增管理员查询方法 |

### 管理员 API

| 方法 | 端点 | 功能 |
|------|------|------|
| GET | `/api/v1/admin/apps/pending` | 获取待审核列表 |
| GET | `/api/v1/admin/apps` | 获取所有应用（可按状态筛选）|
| GET | `/api/v1/admin/apps/statistics` | 获取审核统计 |
| GET | `/api/v1/admin/apps/{id}` | 获取应用详情 |
| POST | `/api/v1/admin/apps/{id}/review` | 审核应用（通过/拒绝）|
| POST | `/api/v1/admin/apps/batch-review` | 批量审核 |
| POST | `/api/v1/admin/apps/{id}/featured` | 设置/取消精选 |
| POST | `/api/v1/admin/apps/{id}/suspend` | 暂停/恢复应用 |

### 核心功能

| 功能 | 说明 |
|------|------|
| 单个审核 | 通过或拒绝，拒绝必须填写原因 |
| 批量审核 | 批量通过/拒绝多个应用 |
| 精选管理 | 设置应用为精选（首页推荐）|
| 暂停/恢复 | 暂停违规应用或恢复已暂停应用 |
| 审核统计 | 今日/本周审核数量、各状态统计 |
| ES 同步 | 审核通过自动索引，暂停自动移除 |

### 权限控制

```kotlin
// AdminAppController.kt
private fun requireAdmin(): Mono<Long> {
    return SecurityUtils.getCurrentUser()
        .flatMap { user ->
            if (user.role == "ADMIN") {
                Mono.just(user.userId)
            } else {
                Mono.error(RuntimeException("无权限，仅管理员可访问"))
            }
        }
}
```

### DTO 结构

```kotlin
// 审核请求
data class ReviewAppRequest(
    val approved: Boolean,
    val rejectionReason: String?  // 拒绝时必填
)

// 批量审核结果
data class BatchReviewResult(
    val total: Int,
    val succeeded: Int,
    val failed: Int,
    val failedIds: List<Long>
)

// 审核统计
data class ReviewStatistics(
    val totalApps: Int,
    val pendingCount: Int,
    val approvedCount: Int,
    val rejectedCount: Int,
    val suspendedCount: Int,
    val todayReviewed: Int,
    val weekReviewed: Int
)
```

### 构建验证

```bash
./gradlew compileKotlin
# BUILD SUCCESSFUL in 1s
```

---

## 开发状态总结

### 已完成功能

| 模块 | 功能 | 状态 |
|------|------|------|
| **后端** | Spring Boot 项目框架 | ✅ 完成 |
| | R2DBC 数据库访问 | ✅ 完成 |
| | REST API (App/Category/Review/Auth) | ✅ 完成 |
| | MinIO 文件存储 | ✅ 完成 |
| | Docker 部署 (docker-compose) | ✅ 完成 |
| | Redis 缓存 | ✅ 完成 |
| | Elasticsearch 全文搜索 | ✅ 完成 |
| | Google OAuth 认证 | ✅ 完成 |
| | 钱包签名认证 | ✅ 完成 |
| | JWT Token 机制 | ✅ 完成 |
| | **开发者应用提交 API** | ✅ 完成 |
| | **管理员审核 API** | ✅ 完成 |
| **Android** | Jetpack Compose UI | ✅ 完成 |
| | Hilt 依赖注入 | ✅ 完成 |
| | MVVM 架构 | ✅ 完成 |
| | APK 下载安装 | ✅ 完成 |
| | 搜索功能 | ✅ 完成 |
| | 分类浏览 | ✅ 完成 |

### 待开发功能

| 模块 | 功能 | 优先级 | 备注 |
|------|------|--------|------|
| **后端** | 数据统计 API | 🟡 中 | 下载趋势、用户增长 |
| | 用户管理 API | 🟡 中 | 禁用/启用用户 |
| | 开发者验证 API | 🟢 低 | 开发者实名认证 |
| **Android** | Google 登录集成 | 🔴 高 | 对接后端认证 API |
| | 开发者后台 | 🟡 中 | 应用提交/管理 UI |
| | 搜索历史持久化 | 🟢 低 | Room/DataStore |

---

## 后续计划

### Phase 8: Android 认证集成
- [ ] Google Sign-In 集成
- [ ] JWT Token 存储
- [ ] 认证状态管理
- [ ] 登录后功能解锁

### Phase 9: 数据统计与分析
- [ ] 下载趋势统计
- [ ] 用户活跃度分析
- [ ] 应用排名变化
- [ ] 开发者数据面板

---

## 2025-01-07: Phase 6 & 7 API 测试验证

### 目标
验证开发者应用提交 API 和管理员审核 API 的完整工作流

### 测试环境准备

#### JWT Token 生成
```python
# 使用正确的 JWT 密钥生成测试 Token
secret = "dev-only-secret-do-not-use-in-production-32chars"

# 开发者 Token (userId=2, role=DEVELOPER)
DEV_TOKEN = jwt.encode({"sub": "2", "role": "DEVELOPER", ...}, secret)

# 管理员 Token (userId=3, role=ADMIN)
ADMIN_TOKEN = jwt.encode({"sub": "3", "role": "ADMIN", ...}, secret)
```

#### 测试数据 (data.sql)
```sql
-- 测试用户
INSERT INTO users VALUES
(1, '0x1234...', 'TestUser', 'USER'),
(2, '0xabcd...', 'TestDeveloper', 'DEVELOPER'),
(3, '0xadmin...', 'TestAdmin', 'ADMIN');

-- 测试开发者
INSERT INTO developers VALUES
(1, 2, 'DI Labs', 'dev@di.xyz', TRUE, 'VERIFIED');
```

---

### 开发者应用提交 API 测试

#### 1. 获取应用统计
```bash
GET /api/v1/developer/apps/statistics
Authorization: Bearer $DEV_TOKEN
```
**响应:**
```json
{
  "success": true,
  "data": {
    "totalApps": 3,
    "draftCount": 0,
    "pendingCount": 0,
    "approvedCount": 3,
    "rejectedCount": 0,
    "totalDownloads": 230000,
    "averageRating": 4.333
  }
}
```
**结果:** ✅ 成功

#### 2. 创建草稿应用
```bash
POST /api/v1/developer/apps
Authorization: Bearer $DEV_TOKEN
Content-Type: application/json

{
  "packageName": "com.test.newapp",
  "name": "Test New App",
  "shortDescription": "A test application",
  "versionName": "1.0.0",
  "versionCode": 1,
  "apkUrl": "https://example.com/test.apk",
  "apkHash": "sha256-test-hash-12345",
  "saveAsDraft": true
}
```
**响应:**
```json
{
  "success": true,
  "data": {
    "id": 4,
    "status": "DRAFT",
    "submittedAt": null,
    ...
  },
  "message": "应用提交成功"
}
```
**结果:** ✅ 成功 (应用 ID=4, 状态=DRAFT)

#### 3. 提交审核
```bash
POST /api/v1/developer/apps/4/submit
Authorization: Bearer $DEV_TOKEN
```
**响应:**
```json
{
  "success": true,
  "data": {
    "id": 4,
    "status": "PENDING",
    "submittedAt": "2026-01-07T08:22:25.346803",
    ...
  },
  "message": "已提交审核"
}
```
**结果:** ✅ 成功 (状态变更: DRAFT → PENDING)

---

### 管理员审核 API 测试

#### 1. 获取待审核列表
```bash
GET /api/v1/admin/apps/pending
Authorization: Bearer $ADMIN_TOKEN
```
**响应:**
```json
{
  "success": true,
  "data": {
    "content": [{
      "id": 4,
      "name": "Test New App",
      "status": "PENDING",
      "developerName": "DI Labs",
      "developerEmail": "dev@di.xyz"
    }],
    "totalElements": 1
  }
}
```
**结果:** ✅ 成功

#### 2. 审核通过
```bash
POST /api/v1/admin/apps/4/review
Authorization: Bearer $ADMIN_TOKEN
Content-Type: application/json

{"approved": true}
```
**响应:**
```json
{
  "success": true,
  "data": {
    "id": 4,
    "status": "APPROVED",
    "reviewedAt": "2026-01-07T08:23:11.74147",
    "reviewerName": "TestAdmin"
  },
  "message": "应用已通过审核"
}
```
**结果:** ✅ 成功 (状态变更: PENDING → APPROVED)

#### 3. 审核拒绝
```bash
# 先创建另一个应用 (ID=5)
POST /api/v1/developer/apps
...

# 拒绝审核
POST /api/v1/admin/apps/5/review
Authorization: Bearer $ADMIN_TOKEN
Content-Type: application/json

{
  "approved": false,
  "rejectionReason": "Missing description and screenshots"
}
```
**响应:**
```json
{
  "success": true,
  "data": {
    "id": 5,
    "status": "REJECTED",
    "rejectionReason": "Missing description and screenshots",
    "reviewedAt": "2026-01-07T08:23:50.051606",
    "reviewerName": "TestAdmin"
  },
  "message": "应用已拒绝"
}
```
**结果:** ✅ 成功 (状态变更: PENDING → REJECTED, 拒绝原因已保存)

#### 4. 设置精选
```bash
POST /api/v1/admin/apps/4/featured
Authorization: Bearer $ADMIN_TOKEN
Content-Type: application/json

{"featured": true}
```
**响应:**
```json
{
  "success": true,
  "data": {
    "id": 4,
    "isFeatured": true
  },
  "message": "已设置为精选"
}
```
**结果:** ✅ 成功

#### 5. 批量审核
```bash
# 创建两个待审核应用 (ID=6, 7)
POST /api/v1/developer/apps ...

# 批量审核通过
POST /api/v1/admin/apps/batch-review
Authorization: Bearer $ADMIN_TOKEN
Content-Type: application/json

{
  "appIds": [6, 7],
  "approved": true
}
```
**响应:**
```json
{
  "success": true,
  "data": {
    "total": 2,
    "succeeded": 2,
    "failed": 0,
    "failedIds": []
  },
  "message": "批量审核完成"
}
```
**结果:** ✅ 成功

#### 6. 审核统计
```bash
GET /api/v1/admin/apps/statistics
Authorization: Bearer $ADMIN_TOKEN
```
**响应:**
```json
{
  "success": true,
  "data": {
    "totalApps": 7,
    "pendingCount": 0,
    "approvedCount": 6,
    "rejectedCount": 1,
    "suspendedCount": 0,
    "todayReviewed": 4,
    "weekReviewed": 4
  }
}
```
**结果:** ✅ 成功

#### 7. 权限控制验证
```bash
# 使用开发者 Token 访问管理员 API
GET /api/v1/admin/apps/pending
Authorization: Bearer $DEV_TOKEN
```
**响应:**
```
Access Denied
```
**结果:** ✅ 正确拒绝非管理员访问

---

### 测试结果汇总

#### 开发者应用提交 API

| 测试项 | API | 结果 |
|--------|-----|------|
| 获取应用统计 | `GET /api/v1/developer/apps/statistics` | ✅ 通过 |
| 创建草稿应用 | `POST /api/v1/developer/apps` | ✅ 通过 |
| 提交审核 | `POST /api/v1/developer/apps/{id}/submit` | ✅ 通过 |

#### 管理员审核 API

| 测试项 | API | 结果 |
|--------|-----|------|
| 获取待审核列表 | `GET /api/v1/admin/apps/pending` | ✅ 通过 |
| 审核通过 | `POST /api/v1/admin/apps/{id}/review` | ✅ 通过 |
| 审核拒绝 | `POST /api/v1/admin/apps/{id}/review` | ✅ 通过 |
| 设置精选 | `POST /api/v1/admin/apps/{id}/featured` | ✅ 通过 |
| 批量审核 | `POST /api/v1/admin/apps/batch-review` | ✅ 通过 |
| 审核统计 | `GET /api/v1/admin/apps/statistics` | ✅ 通过 |
| 权限控制 | 开发者访问管理员 API | ✅ 正确拒绝 |

### 测试数据最终状态

| 应用 ID | 包名 | 状态 | 精选 |
|---------|------|------|------|
| 1-3 | 初始测试应用 | APPROVED | 各一个 |
| 4 | com.test.newapp | APPROVED | ✅ 是 |
| 5 | com.test.reject | REJECTED | - |
| 6-7 | com.batch.test* | APPROVED | - |

### 发现问题及修复

#### 问题 1: JWT 密钥不匹配
- **现象:** 所有认证 API 返回 401 Unauthorized
- **原因:** 测试脚本使用的密钥与 `application.yml` 配置不一致
- **修复:** 使用正确的密钥 `dev-only-secret-do-not-use-in-production-32chars`

#### 问题 2: 设置精选 API 参数格式
- **现象:** 使用查询参数 `?featured=true` 无效
- **原因:** API 设计为使用 RequestBody
- **修复:** 改用 `{"featured": true}` 请求体

---

### 结论

Phase 6 (开发者应用提交) 和 Phase 7 (管理员审核) 所有 API 功能测试通过：
- ✅ 完整的应用提交工作流 (创建 → 草稿 → 提交审核)
- ✅ 完整的审核工作流 (待审核 → 通过/拒绝)
- ✅ 批量操作支持
- ✅ 精选管理功能
- ✅ 权限控制正确
- ✅ 审核统计准确

---

## 2025-01-08: 真实 APK 测试环境搭建

### 目标
配置 8 个真实 Web3 应用的 APK 和图标，完成端到端下载安装测试

### 应用清单

| # | 应用 | 包名 | 版本 | APK 大小 |
|---|------|------|------|----------|
| 1 | TokenPocket | vip.mytokenpocket | 2.13.0 | 101 MB |
| 2 | Trust Wallet | com.wallet.crypto.trustapp | 8.78.1 | 186 MB |
| 3 | Telegram | org.telegram.messenger.web | 12.3.1 | 79 MB |
| 4 | MetaMask | io.metamask | 7.61.5 | 204 MB |
| 5 | OKX | com.okinc.okex.gp | 6.145.0 | 339 MB |
| 6 | Binance | com.binance.dev | 3.8.4 | 279 MB |
| 7 | X (Twitter) | com.twitter.android | 11.54.0 | 144 MB |
| 8 | ave.ai | ai.ave.platform | 2.3.01 | 91 MB |

**APK 总大小: 约 1.4 GB**

### APK 来源

| 应用 | 来源 |
|------|------|
| Telegram | telegram.org 官网直接下载 |
| TokenPocket | tokenpocket.pro 官网直接下载 |
| Trust Wallet | trustwallet.com 官网直接下载 |
| MetaMask | GitHub Release (metamask-mobile) |
| OKX, Binance, X, ave.ai | 用户提供 |

### 存储架构

```
MinIO (端口 9100, localhost only)
    │
    ├── dappstore-apks/          # APK 文件
    │   ├── aveai.apk
    │   ├── binance.apk
    │   ├── metamask.apk
    │   ├── okx.apk
    │   ├── telegram.apk
    │   ├── tokenpocket.apk
    │   ├── trustwallet.apk
    │   └── x.apk
    │
    └── dappstore-icons/         # 应用图标
        ├── aveai_icon.png       (1024x1024)
        ├── binance_icon.png     (250x250)
        ├── metamask_icon.png    (512x512)
        ├── okx_icon.png         (2000x2000)
        ├── telegram_icon.png    (512x512)
        ├── tokenpocket_icon.png (200x200)
        ├── trustwallet_icon.png (224x250)
        └── x_icon.png           (512x512)
```

### 问题与解决

#### 问题 1: MinIO 只绑定 localhost
- **现象:** MinIO 在 macOS 上只监听 127.0.0.1，外部设备无法访问
- **原因:** macOS 网络栈限制，`--address ":9100"` 不生效于 IPv4
- **解决:** 使用 socat 端口转发
  ```bash
  socat TCP-LISTEN:9102,fork,reuseaddr,bind=0.0.0.0 TCP:127.0.0.1:9100
  ```
- **结果:** 外部设备通过 `192.168.3.104:9102` 访问

#### 问题 2: Telegram 包名不匹配
- **现象:** 安装失败 `INSTALL_FAILED_INVALID_APK: inconsistent with org.telegram.messenger.web`
- **原因:** APK 实际包名是 `org.telegram.messenger.web`，数据库配置为 `org.telegram.messenger`
- **解决:** 更新 data.sql 中的包名
  ```sql
  -- 修改前
  ('org.telegram.messenger', 'Telegram', ...)
  -- 修改后
  ('org.telegram.messenger.web', 'Telegram', ...)
  ```

#### 问题 3: 图标格式错误
- **现象:** TokenPocket、Trust Wallet、X 图标显示异常
- **原因:** 下载的是横幅图片而非方形图标
  - TokenPocket: 1689x225 (横幅)
  - Trust Wallet: 546x84 (横幅)
  - X: 300x271 (不规则)
- **解决:** 从可靠来源重新下载方形图标
  - TokenPocket: CoinMarketCap (200x200)
  - Trust Wallet: CoinGecko (224x250)
  - X: icons8.com (512x512)

### ADB 日志分析

使用 ADB 实时监控应用日志：
```bash
adb logcat -v time | grep -iE "web3store|download|install|apk"
```

关键日志示例：
```
I/AppDetailViewModel: checkAppState: packageName=org.telegram.messenger.web, installedVersion=null
D/AppDetailViewModel: installApk: apkFile=/.../org.telegram.messenger.web_12.3.1.apk, exists=true
D/ApkInstaller: Session committed for org.telegram.messenger.web
D/InstallerReceiver: User action required for org.telegram.messenger.web
```

### 服务配置

| 服务 | 端口 | 说明 |
|------|------|------|
| 后端 API | 9000 | Spring Boot |
| MinIO | 9100 | 对象存储 (localhost) |
| Socat 代理 | 9102 | MinIO 外部访问代理 |
| MinIO Console | 9101 | Web 管理界面 |

### data.sql 更新

- 更新所有 URL 从 9100 改为 9102 (通过代理访问)
- 修正 Telegram 包名
- 添加正确的 APK 哈希和大小

### 测试验证

```bash
# API 验证
curl http://localhost:9000/api/v1/apps
# 返回 8 个应用

# APK 下载验证
curl -I http://127.0.0.1:9102/dappstore-apks/telegram.apk
# HTTP 200, 83MB

# 图标验证
curl -I http://127.0.0.1:9102/dappstore-icons/x_icon.png
# HTTP 200, 8KB
```

### Git 提交

```
commit 45bf5cb
fix: Update app data with real APKs and correct package names

- Add 8 real apps: TokenPocket, Trust Wallet, Telegram, MetaMask, OKX, Binance, X, ave.ai
- Fix Telegram package name: org.telegram.messenger -> org.telegram.messenger.web
- Update APK URLs to use port 9102 (socat proxy for external access)
- Add correct APK hashes and sizes for all apps
- Remove placeholder apps (Uniswap, 1inch)
```

---

## 开发状态总结

### 已完成功能

| 模块 | 功能 | 状态 |
|------|------|------|
| **后端** | Spring Boot 项目框架 | ✅ 完成 |
| | REST API 完整实现 | ✅ 完成 |
| | MinIO 文件存储 | ✅ 完成 |
| | Google OAuth + 钱包认证 | ✅ 完成 |
| | 开发者应用提交 API | ✅ 完成 |
| | 管理员审核 API | ✅ 完成 |
| | **真实 APK 测试数据** | ✅ 完成 |
| **Android** | Jetpack Compose UI | ✅ 完成 |
| | APK 下载安装 | ✅ 完成 |
| | 搜索 + 分类浏览 | ✅ 完成 |
| | **端到端测试通过** | ✅ 完成 |

### 测试环境

| 组件 | 地址 |
|------|------|
| 后端 API | http://192.168.3.104:9000 |
| MinIO 代理 | http://192.168.3.104:9102 |
| Android 测试机 | ProMax250815001023 (ADB 连接) |
