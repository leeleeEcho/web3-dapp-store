# Web3 DApp Store 技术设计文档

## 项目概述

### 愿景
构建一款类似 Solana Phone dApp Store 的 Web3 应用市场，预装于自研手机中，为用户提供安全、便捷的去中心化应用分发和管理服务。

### 核心特性
- **应用市场**: 中心化审核的 Web3 应用分发，用户通过安装原生 APK 使用应用
- **内置钱包**: 多链钱包支持 (Ethereum, Solana, BSC 等)
- **安全存储**: 硬件级密钥保护 (类似 Saga Seed Vault)
- **预装集成**: 作为系统应用预装到手机 ROM

---

## 系统架构

### 整体架构图

```
┌────────────────────────────────────────────────────────────────────────┐
│                           手机端 (Android)                              │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌─────────────────────────────────┐  ┌─────────────────────────────┐  │
│  │                                 │  │                             │  │
│  │        DApp Store               │  │        内置钱包              │  │
│  │        Application              │  │        Application          │  │
│  │   (应用市场 + APK 安装管理)      │  │   (多链资产管理 + 交易签名)   │  │
│  │                                 │  │                             │  │
│  └───────────────┬─────────────────┘  └──────────────┬──────────────┘  │
│                  │                                   │                 │
│  ┌───────────────┴───────────────────────────────────┴───────────────┐ │
│  │                        Core SDK Layer                              │ │
│  │  ┌───────────────┐ ┌───────────────┐ ┌───────────────────────────┐│ │
│  │  │ Trust         │ │ WalletConnect │ │ Solana Mobile Wallet      ││ │
│  │  │ Wallet Core   │ │ V2 SDK        │ │ Adapter (MWA)             ││ │
│  │  └───────────────┘ └───────────────┘ └───────────────────────────┘│ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────────┐ │
│  │                        Security Layer                               │ │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────┐ │ │
│  │  │   Seed Vault     │  │  Android Keystore │  │   Biometric Auth  │ │ │
│  │  │   (TEE/SE)       │  │  (Encrypted)      │  │   (指纹/面容)     │ │ │
│  │  └──────────────────┘  └──────────────────┘  └───────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────┘ │
│                                                                         │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS / gRPC
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                           后端服务 (Cloud)                              │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │   API        │  │   审核服务    │  │   用户服务    │  │  推荐服务   │  │
│  │   Gateway    │  │   Service    │  │   Service    │  │  Service   │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └─────┬──────┘  │
│         │                 │                 │                │         │
│  ┌──────┴─────────────────┴─────────────────┴────────────────┴──────┐  │
│  │                        数据层                                     │  │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐  │  │
│  │  │ PostgreSQL │  │   Redis    │  │   MinIO    │  │ Elasticsearch│ │  │
│  │  │ (主数据库)  │  │  (缓存)    │  │ (APK存储)  │  │  (搜索)     │  │  │
│  │  └────────────┘  └────────────┘  └────────────┘  └────────────┘  │  │
│  └──────────────────────────────────────────────────────────────────┘  │
│                                                                         │
└────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ RPC
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         区块链网络                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │  Ethereum    │  │   Solana     │  │     BSC      │  │  Polygon   │  │
│  │  Mainnet     │  │   Mainnet    │  │   Mainnet    │  │  Mainnet   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  └────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 模块详细设计

### 1. DApp Store 应用市场

#### 1.1 功能模块

```
┌─────────────────────────────────────────────────────────────────┐
│                        DApp Store App                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────┐ │
│  │    首页     │  │    分类     │  │    搜索     │  │   我的   │ │
│  │   Home      │  │  Category   │  │   Search    │  │  Profile │ │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └────┬────┘ │
│         │                │                │              │      │
│  ┌──────┴────────────────┴────────────────┴──────────────┴────┐ │
│  │                      Feature Modules                        │ │
│  │                                                             │ │
│  │  • 精选推荐 (Featured)      • 排行榜 (Leaderboard)          │ │
│  │  • 新上架 (New Releases)    • 应用详情 (App Detail)          │ │
│  │  • 分类浏览 (Categories)    • 下载管理 (Download Manager)    │ │
│  │  • 应用更新 (Updates)       • 评分评论 (Reviews)             │ │
│  │                                                             │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 1.2 应用分类

| 分类 | 描述 | 示例应用 |
|------|------|----------|
| DeFi | 去中心化金融 | Uniswap, Aave, Compound |
| NFT | NFT 市场与工具 | OpenSea, Blur, Magic Eden |
| GameFi | 区块链游戏 | Axie Infinity, StepN |
| Social | 社交应用 | Lens, Farcaster |
| DAO | DAO 治理工具 | Snapshot, Tally |
| Tools | 开发者工具 | Etherscan, Solscan |
| Wallet | 钱包应用 | - |

#### 1.3 数据模型

```kotlin
// 应用信息
data class DApp(
    val id: String,
    val name: String,
    val packageName: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val shortDescription: String,
    val iconUrl: String,
    val screenshots: List<String>,
    val category: Category,
    val chains: List<Chain>,          // 支持的区块链
    val contractAddresses: Map<Chain, String>?, // 合约地址
    val downloadUrl: String,
    val apkHash: String,              // APK SHA-256
    val apkSignature: String,         // 开发者签名
    val size: Long,
    val developer: Developer,
    val rating: Float,
    val reviewCount: Int,
    val downloadCount: Long,
    val permissions: List<Permission>,
    val minSdkVersion: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val status: AppStatus             // PENDING, APPROVED, REJECTED
)

enum class Chain {
    ETHEREUM, SOLANA, BSC, POLYGON, ARBITRUM, OPTIMISM, AVALANCHE
}

enum class Category {
    DEFI, NFT, GAMEFI, SOCIAL, DAO, TOOLS, WALLET, OTHER
}
```

---

### 2. 内置钱包模块

#### 2.1 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                        Wallet Module                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                     Presentation Layer                    │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐ │   │
│  │  │ 资产总览  │ │ 发送交易  │ │ 接收地址  │ │ 交易历史    │ │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────────┘ │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐ │   │
│  │  │ Token管理 │ │ NFT展示  │ │ DApp连接  │ │ 设置       │ │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────────┘ │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                   │
│  ┌──────────────────────────┴───────────────────────────────┐   │
│  │                     Domain Layer                          │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────────────┐│   │
│  │  │WalletUseCase│ │TransferUC  │ │ SignTransactionUseCase ││   │
│  │  └────────────┘ └────────────┘ └────────────────────────┘│   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                   │
│  ┌──────────────────────────┴───────────────────────────────┐   │
│  │                     Data Layer                            │   │
│  │  ┌─────────────────┐  ┌─────────────────────────────────┐│   │
│  │  │  Trust Wallet   │  │      Blockchain RPC             ││   │
│  │  │     Core        │  │  (Ethereum, Solana, BSC...)     ││   │
│  │  └─────────────────┘  └─────────────────────────────────┘│   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                   │
│  ┌──────────────────────────┴───────────────────────────────┐   │
│  │                   Security Layer                          │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────────────┐│   │
│  │  │ Seed Vault │ │ Keystore   │ │   Biometric Auth       ││   │
│  │  │   (TEE)    │ │(Encrypted) │ │   (Fingerprint/Face)   ││   │
│  │  └────────────┘ └────────────┘ └────────────────────────┘│   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 2.2 支持的区块链

| 链 | 协议 | 代币标准 | RPC |
|----|------|----------|-----|
| Ethereum | EVM | ERC-20, ERC-721, ERC-1155 | Infura/Alchemy |
| Solana | SPL | SPL Token, Metaplex NFT | Helius/QuickNode |
| BSC | EVM | BEP-20, BEP-721 | BSC RPC |
| Polygon | EVM | ERC-20, ERC-721 | Polygon RPC |
| Arbitrum | EVM | ERC-20 | Arbitrum RPC |
| Optimism | EVM | ERC-20 | Optimism RPC |

#### 2.3 密钥管理

```kotlin
// 密钥存储策略
interface KeyStorage {
    // 创建新钱包
    suspend fun createWallet(): WalletCredentials

    // 导入助记词
    suspend fun importMnemonic(mnemonic: String): WalletCredentials

    // 导入私钥
    suspend fun importPrivateKey(privateKey: ByteArray, chain: Chain): Address

    // 获取派生地址
    suspend fun deriveAddress(chain: Chain, index: Int): Address

    // 签名交易
    suspend fun signTransaction(
        chain: Chain,
        transaction: RawTransaction,
        requireBiometric: Boolean = true
    ): SignedTransaction

    // 签名消息
    suspend fun signMessage(
        chain: Chain,
        message: ByteArray,
        requireBiometric: Boolean = true
    ): Signature
}

// Seed Vault 实现 (硬件安全)
class SeedVaultKeyStorage(
    private val seedVault: SeedVault,
    private val biometricManager: BiometricManager
) : KeyStorage {
    // 使用 TEE/SE 安全存储助记词
    // 所有签名操作在安全飞地内完成
}

// Android Keystore 实现 (软件加密)
class KeystoreKeyStorage(
    private val keyStore: AndroidKeyStore,
    private val encryptedPrefs: EncryptedSharedPreferences
) : KeyStorage {
    // 使用 Android Keystore 加密存储
    // AES-256-GCM 加密
}
```

---

### 3. 后端服务

#### 3.1 API 设计

```yaml
# OpenAPI 3.0 Specification

/api/v1/apps:
  GET:
    summary: 获取应用列表
    parameters:
      - name: category
        in: query
        schema:
          type: string
      - name: chain
        in: query
        schema:
          type: string
      - name: sort
        in: query
        schema:
          enum: [popular, recent, rating]
      - name: page
        in: query
        schema:
          type: integer
      - name: size
        in: query
        schema:
          type: integer

/api/v1/apps/{id}:
  GET:
    summary: 获取应用详情

/api/v1/apps/{id}/download:
  GET:
    summary: 获取下载链接
    responses:
      200:
        content:
          application/json:
            schema:
              type: object
              properties:
                downloadUrl: string
                apkHash: string
                signature: string

/api/v1/apps/search:
  GET:
    summary: 搜索应用
    parameters:
      - name: q
        in: query
        required: true
        schema:
          type: string

/api/v1/developer/apps:
  POST:
    summary: 提交应用审核
    requestBody:
      content:
        multipart/form-data:
          schema:
            type: object
            properties:
              apk: binary
              metadata: object

/api/v1/user/installed:
  GET:
    summary: 获取已安装应用
  POST:
    summary: 同步已安装应用

/api/v1/featured:
  GET:
    summary: 获取精选推荐
```

#### 3.2 数据库设计

```sql
-- 应用表
CREATE TABLE apps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    package_name VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL,
    version_code INTEGER NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    icon_url VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    developer_id UUID REFERENCES developers(id),
    download_url VARCHAR(500) NOT NULL,
    apk_hash VARCHAR(64) NOT NULL,
    apk_signature TEXT NOT NULL,
    size BIGINT NOT NULL,
    min_sdk_version INTEGER DEFAULT 26,
    rating DECIMAL(2,1) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    download_count BIGINT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 区块链支持表
CREATE TABLE app_chains (
    app_id UUID REFERENCES apps(id),
    chain VARCHAR(50) NOT NULL,
    contract_address VARCHAR(100),
    PRIMARY KEY (app_id, chain)
);

-- 截图表
CREATE TABLE app_screenshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID REFERENCES apps(id),
    url VARCHAR(500) NOT NULL,
    sort_order INTEGER DEFAULT 0
);

-- 开发者表
CREATE TABLE developers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    wallet_address VARCHAR(100),
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 评论表
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID REFERENCES apps(id),
    user_id UUID REFERENCES users(id),
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 用户表
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_address VARCHAR(100) UNIQUE,
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_apps_category ON apps(category);
CREATE INDEX idx_apps_status ON apps(status);
CREATE INDEX idx_apps_download_count ON apps(download_count DESC);
CREATE INDEX idx_app_chains_chain ON app_chains(chain);
```

---

### 4. 安全设计

#### 4.1 密钥安全

```
┌─────────────────────────────────────────────────────────────────┐
│                      安全层级架构                                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Level 1: Seed Vault (最高安全级别)                              │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • 助记词存储在 TEE/Secure Element                         │  │
│  │  • 私钥从不离开安全飞地                                     │  │
│  │  • 所有签名操作在 TEE 内完成                                │  │
│  │  • 需要生物识别认证                                         │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Level 2: Android Keystore (标准安全级别)                        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • 使用硬件支持的 Keystore                                  │  │
│  │  • AES-256-GCM 加密私钥                                    │  │
│  │  • 密钥绑定设备，无法导出                                    │  │
│  │  • 支持生物识别解锁                                         │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
│  Level 3: Encrypted Storage (基础安全)                           │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │  • EncryptedSharedPreferences                              │  │
│  │  • 用于非敏感配置数据                                       │  │
│  │  • Room 数据库加密                                          │  │
│  └───────────────────────────────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 4.2 应用安全

| 安全措施 | 描述 |
|----------|------|
| APK 签名验证 | 所有上架应用必须提供开发者签名 |
| SHA-256 完整性校验 | 下载后验证 APK 哈希 |
| 权限审查 | 敏感权限需要人工审核 |
| 恶意代码扫描 | 自动化安全扫描 |
| 证书透明度 | 公开开发者证书信息 |

#### 4.3 通信安全

- 所有 API 使用 HTTPS/TLS 1.3
- Certificate Pinning 防止中间人攻击
- JWT Token 认证
- 请求签名防重放

---

## 技术栈总结

### Android 客户端

| 技术 | 用途 |
|------|------|
| Kotlin | 主要开发语言 |
| Jetpack Compose | UI 框架 |
| Hilt | 依赖注入 |
| Room | 本地数据库 |
| Retrofit + OkHttp | 网络请求 |
| Trust Wallet Core | 多链钱包 |
| WalletConnect Kotlin V2 | dApp 连接 |
| Solana MWA | Solana 钱包适配 |
| Coil | 图片加载 |
| DataStore | 偏好设置存储 |

### 后端服务

| 技术 | 用途 | 版本 |
|------|------|------|
| **Kotlin** | 主开发语言 | 1.9+ |
| Spring Boot 3 | Web 框架 | 3.2+ |
| Spring WebFlux | 响应式 API (高并发场景) | - |
| PostgreSQL | 主数据库 | 15+ |
| Redis Cluster | 缓存 + 会话 | 7+ |
| MinIO | APK/图片对象存储 | - |
| Elasticsearch | 全文搜索 | 8+ |
| Kafka | 消息队列 | 3+ |
| Docker + K8s | 容器化部署 | - |

#### Kotlin 技术栈详情

```kotlin
// build.gradle.kts 核心依赖
dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-webflux")  // 响应式
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc") // 响应式数据库
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")  // 协程支持
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    // 数据库
    implementation("org.postgresql:r2dbc-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // Web3
    implementation("org.web3j:core:4.10.3")

    // 工具
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
```

#### 项目结构

```
backend/
├── src/main/kotlin/com/di/dappstore/
│   ├── DAppStoreApplication.kt
│   ├── config/                 # 配置类
│   │   ├── SecurityConfig.kt
│   │   ├── RedisConfig.kt
│   │   └── Web3Config.kt
│   ├── controller/             # API 控制器
│   │   ├── AppController.kt
│   │   ├── UserController.kt
│   │   └── DeveloperController.kt
│   ├── service/                # 业务服务
│   │   ├── AppService.kt
│   │   ├── ReviewService.kt
│   │   ├── PushService.kt
│   │   └── BlockchainService.kt
│   ├── repository/             # 数据访问
│   ├── model/                  # 数据模型
│   │   ├── entity/
│   │   ├── dto/
│   │   └── vo/
│   ├── security/               # 安全模块
│   └── util/                   # 工具类
├── src/main/resources/
│   ├── application.yml
│   └── application-prod.yml
└── build.gradle.kts
```

### 参考项目

| 项目 | 参考点 |
|------|--------|
| Neo Store | 应用市场 UI/UX |
| F-Droid Server | 应用仓库管理 |
| Trust Wallet Core | 多链钱包实现 |
| Solana Mobile MWA | Solana 钱包协议 |
| Seed Vault SDK | 安全存储实现 |
| WalletConnect | dApp 连接标准 |

---

## 6. 风险评估与应对策略

### 6.1 安全风险

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              安全风险矩阵                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   风险等级:  ■ 严重 (Critical)  ■ 高 (High)  ■ 中 (Medium)  ■ 低 (Low)      │
│                                                                              │
│   ┌────────────────────┬──────────┬─────────────────────────────────────┐   │
│   │ 风险项              │ 等级     │ 应对策略                             │   │
│   ├────────────────────┼──────────┼─────────────────────────────────────┤   │
│   │ 私钥泄露            │ ■ 严重   │ TEE/SE 存储 + 生物识别 + 不导出原则  │   │
│   │ 恶意 APK 分发       │ ■ 严重   │ 多重审核 + 代码扫描 + 签名验证       │   │
│   │ 中间人攻击          │ ■ 高     │ TLS 1.3 + 证书锁定 + 请求签名       │   │
│   │ 钓鱼交易            │ ■ 高     │ 交易详情展示 + 地址白名单 + 警告提示 │   │
│   │ 重放攻击            │ ■ 中     │ Nonce 机制 + 时间戳校验             │   │
│   │ 服务端数据泄露      │ ■ 高     │ 数据加密 + 访问控制 + 审计日志      │   │
│   │ DDoS 攻击           │ ■ 中     │ CDN + 限流 + WAF                    │   │
│   │ 供应链攻击          │ ■ 高     │ 依赖审计 + SBOM + 私有镜像          │   │
│   └────────────────────┴──────────┴─────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### 6.1.1 私钥安全防护

```kotlin
// 多层密钥保护策略
class KeyProtectionStrategy {

    // 层级 1: 硬件安全模块 (最高优先级)
    fun useHardwareSecurity(): Boolean {
        return when {
            SeedVault.isAvailable() -> useSeedVault()      // 自研 TEE 方案
            StrongBox.isAvailable() -> useStrongBox()       // Android StrongBox
            TrustZone.isAvailable() -> useTrustZone()       // ARM TrustZone
            else -> false
        }
    }

    // 层级 2: Android Keystore (软件方案)
    fun useSoftwareSecurity(): KeyStorage {
        return AndroidKeystoreStorage(
            algorithm = "AES/GCM/NoPadding",
            keySize = 256,
            userAuthRequired = true,
            biometricOnly = true,
            invalidatedByBiometricEnrollment = true
        )
    }

    // 交易签名流程
    suspend fun signTransaction(tx: Transaction): SignedTransaction {
        // 1. 展示交易详情，用户确认
        val confirmed = showTransactionConfirmation(tx)
        if (!confirmed) throw UserCancelledException()

        // 2. 生物识别验证
        val bioResult = biometricAuth.authenticate(
            title = "确认交易",
            subtitle = "使用指纹或面容确认",
            negativeButton = "取消"
        )
        if (!bioResult.success) throw AuthenticationFailedException()

        // 3. 在安全环境中签名
        return secureKeyStorage.sign(tx, bioResult.cryptoObject)
    }
}
```

#### 6.1.2 APK 安全审核流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           APK 安全审核流水线                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   开发者提交                                                                 │
│       │                                                                      │
│       ▼                                                                      │
│   ┌─────────────────┐                                                       │
│   │ 1. 签名验证     │ ──── 验证开发者签名证书有效性                          │
│   └────────┬────────┘                                                       │
│            │                                                                 │
│            ▼                                                                 │
│   ┌─────────────────┐                                                       │
│   │ 2. 静态分析     │ ──── 反编译检查危险代码模式                            │
│   │   • 权限分析    │      • 加密货币地址硬编码检测                          │
│   │   • API 调用    │      • 恶意 URL/域名检测                               │
│   │   • 代码混淆度  │      • 隐私数据收集检测                                │
│   └────────┬────────┘                                                       │
│            │                                                                 │
│            ▼                                                                 │
│   ┌─────────────────┐                                                       │
│   │ 3. 动态分析     │ ──── 沙箱环境运行检测                                  │
│   │   • 网络行为    │      • 可疑网络连接                                    │
│   │   • 文件操作    │      • 敏感数据访问                                    │
│   │   • 系统调用    │      • 异常系统行为                                    │
│   └────────┬────────┘                                                       │
│            │                                                                 │
│            ▼                                                                 │
│   ┌─────────────────┐                                                       │
│   │ 4. 人工审核     │ ──── 高风险应用必须人工审核                            │
│   │   • 功能验证    │      • 与描述是否一致                                  │
│   │   • 合规检查    │      • 法律合规性                                      │
│   └────────┬────────┘                                                       │
│            │                                                                 │
│            ▼                                                                 │
│   ┌─────────────────┐                                                       │
│   │ 5. 上架发布     │ ──── 生成平台签名 + 分发                               │
│   └─────────────────┘                                                       │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.2 技术风险

| 风险类型 | 风险描述 | 可能性 | 影响 | 应对策略 |
|---------|---------|-------|------|---------|
| 区块链网络不可用 | RPC 节点故障或网络拥堵 | 中 | 高 | 多节点冗余 + 降级策略 |
| 第三方服务故障 | WalletConnect/RPC 服务中断 | 中 | 中 | 本地缓存 + 备用服务 |
| 数据库性能瓶颈 | 高并发下响应变慢 | 低 | 中 | 读写分离 + 缓存优化 |
| 存储容量不足 | APK 文件持续增长 | 低 | 中 | 对象存储 + 清理策略 |
| 密钥丢失 | 服务端密钥管理问题 | 低 | 严重 | HSM + 密钥轮换 + 备份 |

### 6.3 业务与法律风险

| 风险类型 | 风险描述 | 应对策略 |
|---------|---------|---------|
| 虚假 dApp | 诈骗/钓鱼应用上架 | 严格审核 + 用户举报机制 + 快速下架 |
| 知识产权侵权 | 侵犯商标/版权的应用 | 提交时声明 + 侵权申诉通道 |
| 金融合规 | 不同地区的加密货币监管 | 地区限制 + 合规 KYC (可选) |
| 数据隐私 | GDPR/CCPA 合规要求 | 隐私政策 + 数据最小化 + 用户控制 |
| 反洗钱 (AML) | 加密资产转移监控 | 链上分析集成 + 风险地址库 |

---

## 7. 消息推送架构

### 7.1 推送架构总览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             消息推送架构                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                          推送触发源                                  │   │
│   │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐│   │
│   │  │ 应用更新  │  │ 链上事件  │  │ 系统通知  │  │ 开发者推送          ││   │
│   │  │ Service  │  │ Listener │  │ Service  │  │ (营销/公告)          ││   │
│   │  └─────┬────┘  └─────┬────┘  └─────┬────┘  └──────────┬───────────┘│   │
│   └────────┼─────────────┼─────────────┼──────────────────┼────────────┘   │
│            │             │             │                  │                 │
│            └─────────────┴─────────────┴──────────────────┘                 │
│                                    │                                        │
│                                    ▼                                        │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                       Message Queue (Kafka)                          │   │
│   │   ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌────────────┐   │   │
│   │   │ app.update │  │ chain.event│  │ system.    │  │ marketing  │   │   │
│   │   │   topic    │  │   topic    │  │ alert topic│  │   topic    │   │   │
│   │   └────────────┘  └────────────┘  └────────────┘  └────────────┘   │   │
│   └──────────────────────────────┬──────────────────────────────────────┘   │
│                                  │                                          │
│                                  ▼                                          │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                    Push Notification Service                         │   │
│   │                                                                      │   │
│   │   ┌────────────────┐    ┌────────────────┐    ┌─────────────────┐   │   │
│   │   │ Message Router │───▶│ Template Engine│───▶│ Rate Limiter    │   │   │
│   │   └────────────────┘    └────────────────┘    └────────┬────────┘   │   │
│   │                                                        │            │   │
│   │   ┌────────────────────────────────────────────────────┘            │   │
│   │   │                                                                 │   │
│   │   ▼                                                                 │   │
│   │   ┌────────────────┐  ┌────────────────┐  ┌─────────────────────┐   │   │
│   │   │  FCM Adapter   │  │  APNs Adapter  │  │  WebSocket Adapter  │   │   │
│   │   │  (Android)     │  │  (iOS 预留)    │  │  (实时连接)         │   │   │
│   │   └───────┬────────┘  └───────┬────────┘  └──────────┬──────────┘   │   │
│   └───────────┼───────────────────┼─────────────────────┼───────────────┘   │
│               │                   │                     │                   │
│               ▼                   ▼                     ▼                   │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                           客户端设备                                  │   │
│   │   ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐ │   │
│   │   │  DI Phone       │  │  Third-party    │  │  Web Dashboard      │ │   │
│   │   │  (预装应用)      │  │  Android        │  │  (开发者)           │ │   │
│   │   └─────────────────┘  └─────────────────┘  └─────────────────────┘ │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 7.2 推送类型与优先级

| 推送类型 | 优先级 | 描述 | 触发条件 | 处理策略 |
|---------|-------|------|---------|---------|
| 交易确认 | P0 (最高) | 链上交易状态变更 | 交易 pending/confirmed/failed | 即时推送，不可禁用 |
| 安全告警 | P0 | 异常登录/可疑交易 | 风控规则触发 | 即时推送 + 应用内弹窗 |
| 应用更新 | P1 | 已安装应用有新版本 | 版本检测服务 | 静默推送，用户可控 |
| 价格提醒 | P2 | Token 价格达到阈值 | 用户设置触发 | 批量合并，限频 |
| 活动通知 | P3 | 空投/活动信息 | 运营配置 | 用户可关闭 |
| 系统公告 | P2 | 维护/重要更新 | 管理员发布 | 全量推送 |

### 7.3 链上事件监听

```kotlin
// 链上事件监听服务
class ChainEventListener(
    private val rpcClients: Map<Chain, RpcClient>,
    private val eventProcessor: EventProcessor,
    private val pushService: PushNotificationService
) {
    // 监听的事件类型
    sealed class ChainEvent {
        data class TransactionConfirmed(
            val chain: Chain,
            val txHash: String,
            val from: String,
            val to: String,
            val value: BigInteger,
            val blockNumber: Long
        ) : ChainEvent()

        data class TokenTransfer(
            val chain: Chain,
            val token: TokenInfo,
            val from: String,
            val to: String,
            val amount: BigInteger
        ) : ChainEvent()

        data class NFTTransfer(
            val chain: Chain,
            val contract: String,
            val tokenId: String,
            val from: String,
            val to: String
        ) : ChainEvent()

        data class ContractEvent(
            val chain: Chain,
            val contract: String,
            val eventName: String,
            val data: Map<String, Any>
        ) : ChainEvent()
    }

    // 为每条链启动监听器
    fun startListeners() {
        rpcClients.forEach { (chain, client) ->
            launch {
                when (chain) {
                    Chain.ETHEREUM, Chain.BSC, Chain.POLYGON ->
                        listenEVMChain(chain, client)
                    Chain.SOLANA ->
                        listenSolanaChain(client)
                }
            }
        }
    }

    private suspend fun listenEVMChain(chain: Chain, client: Web3Client) {
        // 使用 WebSocket 订阅新区块
        client.subscribeNewHeads { block ->
            // 获取区块内的所有交易
            val txs = client.getBlockWithTransactions(block.number)

            // 筛选与我们用户相关的交易
            txs.transactions.forEach { tx ->
                if (isRelevantTransaction(tx)) {
                    val event = TransactionConfirmed(
                        chain = chain,
                        txHash = tx.hash,
                        from = tx.from,
                        to = tx.to,
                        value = tx.value,
                        blockNumber = block.number
                    )
                    eventProcessor.process(event)
                }
            }
        }

        // 订阅 ERC-20 Transfer 事件
        client.subscribeLogs(
            topics = listOf(ERC20_TRANSFER_TOPIC)
        ) { log ->
            val event = parseTokenTransfer(chain, log)
            eventProcessor.process(event)
        }
    }
}
```

### 7.4 推送服务实现

```kotlin
// 推送通知服务
@Service
class PushNotificationService(
    private val fcmClient: FirebaseMessaging,
    private val deviceRepository: DeviceTokenRepository,
    private val templateEngine: NotificationTemplateEngine,
    private val rateLimiter: RateLimiter,
    private val metricsCollector: MetricsCollector
) {
    // 发送推送通知
    suspend fun send(notification: PushNotification): Result<String> {
        // 1. 限流检查
        if (!rateLimiter.tryAcquire(notification.userId, notification.type)) {
            metricsCollector.incrementRateLimited(notification.type)
            return Result.failure(RateLimitedException())
        }

        // 2. 获取用户设备 Token
        val deviceTokens = deviceRepository.getTokensByUserId(notification.userId)
        if (deviceTokens.isEmpty()) {
            return Result.failure(NoDeviceException())
        }

        // 3. 渲染通知模板
        val content = templateEngine.render(notification)

        // 4. 构建 FCM 消息
        val message = Message.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(content.title)
                    .setBody(content.body)
                    .build()
            )
            .putAllData(content.data)
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setPriority(mapPriority(notification.priority))
                    .setNotification(
                        AndroidNotification.builder()
                            .setChannelId(notification.type.channelId)
                            .setIcon("ic_notification")
                            .setColor("#D4AF37") // DI 金色
                            .build()
                    )
                    .build()
            )
            .build()

        // 5. 批量发送到所有设备
        return try {
            val responses = deviceTokens.map { token ->
                fcmClient.send(message.toBuilder().setToken(token).build())
            }
            metricsCollector.incrementSent(notification.type, responses.size)
            Result.success(responses.first())
        } catch (e: Exception) {
            metricsCollector.incrementFailed(notification.type)
            Result.failure(e)
        }
    }

    // 通知渠道配置
    enum class NotificationChannel(
        val channelId: String,
        val name: String,
        val importance: Int
    ) {
        TRANSACTION("tx_channel", "交易通知", NotificationManager.IMPORTANCE_HIGH),
        SECURITY("security_channel", "安全提醒", NotificationManager.IMPORTANCE_MAX),
        UPDATE("update_channel", "应用更新", NotificationManager.IMPORTANCE_DEFAULT),
        PRICE("price_channel", "价格提醒", NotificationManager.IMPORTANCE_LOW),
        MARKETING("marketing_channel", "活动通知", NotificationManager.IMPORTANCE_MIN)
    }
}
```

### 7.5 客户端推送处理

```kotlin
// Android 客户端 FCM 服务
@AndroidEntryPoint
class DIPushService : FirebaseMessagingService() {

    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var eventBus: EventBus
    @Inject lateinit var securityChecker: SecurityChecker

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val type = NotificationType.valueOf(data["type"] ?: return)

        when (type) {
            NotificationType.TRANSACTION_CONFIRMED -> {
                handleTransactionNotification(data)
            }
            NotificationType.SECURITY_ALERT -> {
                handleSecurityAlert(data)
            }
            NotificationType.APP_UPDATE -> {
                handleAppUpdate(data)
            }
            NotificationType.PRICE_ALERT -> {
                handlePriceAlert(data)
            }
        }
    }

    private fun handleTransactionNotification(data: Map<String, String>) {
        val txHash = data["txHash"] ?: return
        val chain = Chain.valueOf(data["chain"] ?: return)
        val status = TxStatus.valueOf(data["status"] ?: return)

        // 显示通知
        showNotification(
            channelId = NotificationChannel.TRANSACTION.channelId,
            title = when (status) {
                TxStatus.CONFIRMED -> "交易已确认"
                TxStatus.FAILED -> "交易失败"
                TxStatus.PENDING -> "交易处理中"
            },
            body = "交易 ${txHash.take(10)}... 在 ${chain.displayName} 上${status.displayText}",
            intent = createDeepLinkIntent("dapp://transaction/$txHash")
        )

        // 通知应用内更新
        eventBus.post(TransactionStatusEvent(txHash, chain, status))
    }

    private fun handleSecurityAlert(data: Map<String, String>) {
        val alertType = data["alertType"] ?: return

        // 安全告警使用最高优先级
        showNotification(
            channelId = NotificationChannel.SECURITY.channelId,
            title = "⚠️ 安全提醒",
            body = getSecurityAlertMessage(alertType),
            intent = createDeepLinkIntent("dapp://security"),
            priority = NotificationCompat.PRIORITY_MAX,
            autoCancel = false // 用户必须手动处理
        )

        // 如果是严重安全事件，强制显示全屏提醒
        if (alertType == "SUSPICIOUS_TRANSACTION") {
            securityChecker.triggerSecurityReview()
        }
    }

    override fun onNewToken(token: String) {
        // 上报新的设备 Token
        CoroutineScope(Dispatchers.IO).launch {
            apiService.registerDeviceToken(
                DeviceTokenRequest(
                    token = token,
                    platform = "android",
                    deviceId = getDeviceId()
                )
            )
        }
    }
}
```

---

## 8. 服务部署与运维架构

### 8.1 基础设施架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           云基础设施架构 (AWS/阿里云)                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                         CDN / 边缘节点                                │   │
│   │   ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────────────┐│   │
│   │   │ CloudFlare│  │ 阿里云CDN │  │ AWS CF    │  │ APK 分发节点      ││   │
│   │   └───────────┘  └───────────┘  └───────────┘  └───────────────────┘│   │
│   └──────────────────────────────┬──────────────────────────────────────┘   │
│                                  │                                          │
│   ┌──────────────────────────────┼──────────────────────────────────────┐   │
│   │                        负载均衡层                                    │   │
│   │   ┌──────────────────────────┴──────────────────────────────────┐   │   │
│   │   │                    Application Load Balancer                │   │   │
│   │   │   • SSL/TLS 终止                                            │   │   │
│   │   │   • 健康检查                                                 │   │   │
│   │   │   • 流量分发                                                 │   │   │
│   │   └─────────────────────────────────────────────────────────────┘   │   │
│   └──────────────────────────────┬──────────────────────────────────────┘   │
│                                  │                                          │
│   ┌──────────────────────────────┼──────────────────────────────────────┐   │
│   │                       Kubernetes 集群                               │   │
│   │                                                                      │   │
│   │   ┌─────────── Namespace: production ───────────────────────────┐   │   │
│   │   │                                                              │   │   │
│   │   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │   │   │
│   │   │  │ API Gateway │  │ App Service │  │ Push Service        │  │   │   │
│   │   │  │ (3 replicas)│  │ (5 replicas)│  │ (3 replicas)        │  │   │   │
│   │   │  └─────────────┘  └─────────────┘  └─────────────────────┘  │   │   │
│   │   │                                                              │   │   │
│   │   │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │   │   │
│   │   │  │ Review Svc  │  │ User Svc    │  │ Chain Listener      │  │   │   │
│   │   │  │ (2 replicas)│  │ (3 replicas)│  │ (per chain)         │  │   │   │
│   │   │  └─────────────┘  └─────────────┘  └─────────────────────┘  │   │   │
│   │   │                                                              │   │   │
│   │   └──────────────────────────────────────────────────────────────┘   │   │
│   │                                                                      │   │
│   │   ┌─────────── Namespace: monitoring ───────────────────────────┐   │   │
│   │   │  ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐ │   │   │
│   │   │  │Prometheus │  │ Grafana   │  │ Alertmgr  │  │ Jaeger    │ │   │   │
│   │   │  └───────────┘  └───────────┘  └───────────┘  └───────────┘ │   │   │
│   │   └──────────────────────────────────────────────────────────────┘   │   │
│   │                                                                      │   │
│   └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌──────────────────────────────────────────────────────────────────────┐   │
│   │                            数据层                                     │   │
│   │                                                                      │   │
│   │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐  │   │
│   │  │   PostgreSQL    │  │     Redis       │  │      MinIO          │  │   │
│   │  │   (主从复制)     │  │   (Cluster)     │  │   (APK 存储)        │  │   │
│   │  │                 │  │                 │  │                     │  │   │
│   │  │  Master ──────▶ │  │  ┌───┬───┬───┐  │  │  ┌───────────────┐  │  │   │
│   │  │    │    Slave  │  │  │ M │ M │ M │  │  │  │  Bucket:apks  │  │  │   │
│   │  │    ▼           │  │  └───┴───┴───┘  │  │  │  Bucket:icons │  │  │   │
│   │  │  Slave ─────▶  │  │    6 Nodes     │  │  │  Bucket:screen│  │  │   │
│   │  │         Slave  │  │                 │  │  └───────────────┘  │  │   │
│   │  └─────────────────┘  └─────────────────┘  └─────────────────────┘  │   │
│   │                                                                      │   │
│   │  ┌─────────────────┐  ┌─────────────────┐                           │   │
│   │  │  Elasticsearch  │  │     Kafka       │                           │   │
│   │  │   (3 节点集群)   │  │  (3 brokers)    │                           │   │
│   │  └─────────────────┘  └─────────────────┘                           │   │
│   │                                                                      │   │
│   └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 8.2 Kubernetes 部署配置

```yaml
# deployment.yaml - API 服务部署
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dapp-store-api
  namespace: production
spec:
  replicas: 5
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 2
      maxUnavailable: 1
  selector:
    matchLabels:
      app: dapp-store-api
  template:
    metadata:
      labels:
        app: dapp-store-api
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
    spec:
      containers:
      - name: api
        image: registry.di.xyz/dapp-store-api:${VERSION}
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
        volumeMounts:
        - name: config
          mountPath: /app/config
      volumes:
      - name: config
        configMap:
          name: api-config
---
# HPA 自动扩缩容
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: dapp-store-api-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: dapp-store-api
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Pods
        value: 4
        periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
```

### 8.3 CI/CD 流水线

```yaml
# .github/workflows/deploy.yaml
name: Deploy to Production

on:
  push:
    branches: [main]
    tags: ['v*']

env:
  REGISTRY: registry.di.xyz
  IMAGE_NAME: dapp-store-api

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Tests
        run: ./gradlew test

      - name: Run Security Scan
        uses: snyk/actions/gradle@master
        with:
          args: --severity-threshold=high

  build:
    needs: test
    runs-on: ubuntu-latest
    outputs:
      version: ${{ steps.meta.outputs.version }}
    steps:
      - uses: actions/checkout@v4

      - name: Docker Meta
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=sha,prefix=
            type=ref,event=tag
            type=raw,value=latest,enable=${{ github.ref == 'refs/heads/main' }}

      - name: Build and Push
        uses: docker/build-push-action@v5
        with:
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  deploy-staging:
    needs: build
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - name: Deploy to Staging
        uses: azure/k8s-deploy@v4
        with:
          namespace: staging
          manifests: k8s/staging/
          images: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ needs.build.outputs.version }}

      - name: Run E2E Tests
        run: |
          npm run test:e2e -- --env=staging

  deploy-production:
    needs: [build, deploy-staging]
    runs-on: ubuntu-latest
    environment: production
    steps:
      - name: Deploy to Production
        uses: azure/k8s-deploy@v4
        with:
          namespace: production
          manifests: k8s/production/
          images: |
            ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ needs.build.outputs.version }}
          strategy: canary
          percentage: 20

      - name: Monitor Canary
        run: |
          # 监控 20 分钟，检查错误率
          sleep 1200
          ERROR_RATE=$(curl -s "$PROMETHEUS_URL/api/v1/query?query=rate(http_errors_total[5m])")
          if [ $(echo "$ERROR_RATE > 0.01" | bc) -eq 1 ]; then
            echo "Error rate too high, rolling back"
            kubectl rollout undo deployment/dapp-store-api -n production
            exit 1
          fi

      - name: Promote Canary
        run: |
          kubectl argo rollouts promote dapp-store-api -n production
```

### 8.4 多环境配置

```yaml
# config/application-production.yaml
spring:
  datasource:
    url: jdbc:postgresql://pg-cluster.db:5432/dappstore
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10

  redis:
    cluster:
      nodes:
        - redis-0.redis:6379
        - redis-1.redis:6379
        - redis-2.redis:6379
        - redis-3.redis:6379
        - redis-4.redis:6379
        - redis-5.redis:6379

  kafka:
    bootstrap-servers: kafka-0.kafka:9092,kafka-1.kafka:9092,kafka-2.kafka:9092

# 安全配置
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000  # 24 hours
  rate-limit:
    enabled: true
    requests-per-minute: 100

# 区块链 RPC 配置
blockchain:
  ethereum:
    rpc-urls:
      - https://eth-mainnet.alchemyapi.io/v2/${ALCHEMY_KEY}
      - https://mainnet.infura.io/v3/${INFURA_KEY}
    ws-url: wss://eth-mainnet.alchemyapi.io/v2/${ALCHEMY_KEY}
  solana:
    rpc-urls:
      - https://api.mainnet-beta.solana.com
      - https://solana-mainnet.g.alchemy.com/v2/${ALCHEMY_KEY}
  bsc:
    rpc-urls:
      - https://bsc-dataseed1.binance.org
      - https://bsc-dataseed2.binance.org

# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    tags:
      application: dapp-store
      environment: production
```

---

## 9. 监控告警与日志系统

### 9.1 监控架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             监控告警架构                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌────────────────────────────────────────────────────────────────────┐    │
│   │                         数据采集层                                  │    │
│   │                                                                    │    │
│   │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌───────────┐ │    │
│   │  │ Metrics      │ │ Logs         │ │ Traces       │ │ Events    │ │    │
│   │  │ (Prometheus) │ │ (Fluent Bit) │ │ (Jaeger)     │ │ (Custom)  │ │    │
│   │  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └─────┬─────┘ │    │
│   └─────────┼────────────────┼────────────────┼───────────────┼───────┘    │
│             │                │                │               │            │
│             ▼                ▼                ▼               ▼            │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                         存储层                                       │  │
│   │  ┌────────────────┐ ┌────────────────┐ ┌────────────────────────┐   │  │
│   │  │ Prometheus     │ │ Elasticsearch  │ │ Jaeger Backend         │   │  │
│   │  │ (时序数据)      │ │ (日志数据)      │ │ (链路追踪)              │   │  │
│   │  │ 15d retention  │ │ 30d retention  │ │ 7d retention           │   │  │
│   │  └────────────────┘ └────────────────┘ └────────────────────────┘   │  │
│   └───────────────────────────┬─────────────────────────────────────────┘  │
│                               │                                            │
│                               ▼                                            │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │                        可视化 & 告警层                                │  │
│   │                                                                      │  │
│   │  ┌───────────────────────────────────────────────────────────────┐  │  │
│   │  │                      Grafana Dashboard                         │  │  │
│   │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────┐ │  │  │
│   │  │  │ 系统概览     │ │ API 性能    │ │ 区块链监控  │ │ 业务指标  │ │  │  │
│   │  │  └─────────────┘ └─────────────┘ └─────────────┘ └──────────┘ │  │  │
│   │  └───────────────────────────────────────────────────────────────┘  │  │
│   │                                                                      │  │
│   │  ┌───────────────────────────────────────────────────────────────┐  │  │
│   │  │                    Alertmanager                                │  │  │
│   │  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌──────────┐ │  │  │
│   │  │  │ PagerDuty   │ │ Slack       │ │ 企业微信     │ │ Email    │ │  │  │
│   │  │  └─────────────┘ └─────────────┘ └─────────────┘ └──────────┘ │  │  │
│   │  └───────────────────────────────────────────────────────────────┘  │  │
│   │                                                                      │  │
│   └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 9.2 核心监控指标

```yaml
# prometheus/rules/dapp-store.yaml
groups:
  - name: dapp-store-api
    rules:
      # API 可用性
      - alert: APIHighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
          / sum(rate(http_server_requests_seconds_count[5m])) > 0.01
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "API 错误率过高"
          description: "5xx 错误率超过 1%，当前: {{ $value | humanizePercentage }}"

      # API 延迟
      - alert: APIHighLatency
        expr: |
          histogram_quantile(0.95,
            sum(rate(http_server_requests_seconds_bucket[5m])) by (le, uri)
          ) > 2
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "API 响应延迟过高"
          description: "P95 延迟超过 2 秒: {{ $labels.uri }}"

      # 数据库连接池
      - alert: DatabaseConnectionPoolExhausted
        expr: |
          hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "数据库连接池即将耗尽"
          description: "活跃连接占比: {{ $value | humanizePercentage }}"

  - name: blockchain-monitor
    rules:
      # RPC 节点健康
      - alert: BlockchainRPCUnhealthy
        expr: |
          up{job="blockchain-rpc"} == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "区块链 RPC 节点不可用"
          description: "{{ $labels.chain }} RPC 节点故障"

      # 区块同步延迟
      - alert: BlockSyncLag
        expr: |
          blockchain_latest_block - blockchain_synced_block > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "区块同步延迟"
          description: "{{ $labels.chain }} 同步落后 {{ $value }} 个区块"

  - name: push-notification
    rules:
      # 推送失败率
      - alert: PushNotificationHighFailureRate
        expr: |
          sum(rate(push_notification_failed_total[5m]))
          / sum(rate(push_notification_sent_total[5m])) > 0.05
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "推送失败率过高"
          description: "推送失败率: {{ $value | humanizePercentage }}"

  - name: business-metrics
    rules:
      # 下载量异常
      - alert: AbnormalDownloadPattern
        expr: |
          rate(app_downloads_total[1h]) >
            avg_over_time(rate(app_downloads_total[1h])[7d:1h]) * 3
        for: 30m
        labels:
          severity: warning
        annotations:
          summary: "异常下载模式检测"
          description: "下载量异常增加，可能存在刷量行为"
```

### 9.3 日志收集配置

```yaml
# fluent-bit/fluent-bit.conf
[SERVICE]
    Flush         5
    Daemon        Off
    Log_Level     info
    Parsers_File  parsers.conf

[INPUT]
    Name              tail
    Path              /var/log/containers/dapp-store-*.log
    Parser            docker
    Tag               kube.*
    Mem_Buf_Limit     50MB
    Skip_Long_Lines   On
    Refresh_Interval  10

[FILTER]
    Name                kubernetes
    Match               kube.*
    Kube_URL            https://kubernetes.default.svc:443
    Kube_CA_File        /var/run/secrets/kubernetes.io/serviceaccount/ca.crt
    Kube_Token_File     /var/run/secrets/kubernetes.io/serviceaccount/token
    Merge_Log           On
    K8S-Logging.Parser  On
    K8S-Logging.Exclude On

[FILTER]
    Name    modify
    Match   kube.*
    # 脱敏处理
    Remove  privateKey
    Remove  mnemonic
    Remove  password

[OUTPUT]
    Name            es
    Match           kube.*
    Host            elasticsearch.logging.svc
    Port            9200
    Index           dapp-store-logs
    Type            _doc
    Logstash_Format On
    Logstash_Prefix dapp-store
    Retry_Limit     5
```

---

## 10. 灾备与高可用设计

### 10.1 高可用架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            高可用架构设计                                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                       多可用区部署                                    │   │
│   │                                                                      │   │
│   │     可用区 A (主)                    可用区 B (从)                     │   │
│   │   ┌─────────────────┐             ┌─────────────────┐               │   │
│   │   │  K8s Workers    │             │  K8s Workers    │               │   │
│   │   │  ┌───┐ ┌───┐   │             │  ┌───┐ ┌───┐   │               │   │
│   │   │  │Pod│ │Pod│   │◀───────────▶│  │Pod│ │Pod│   │               │   │
│   │   │  └───┘ └───┘   │   跨 AZ     │  └───┘ └───┘   │               │   │
│   │   │  ┌───┐ ┌───┐   │   负载均衡   │  ┌───┐ ┌───┐   │               │   │
│   │   │  │Pod│ │Pod│   │             │  │Pod│ │Pod│   │               │   │
│   │   │  └───┘ └───┘   │             │  └───┘ └───┘   │               │   │
│   │   └────────┬────────┘             └────────┬────────┘               │   │
│   │            │                               │                         │   │
│   │   ┌────────┴────────┐             ┌────────┴────────┐               │   │
│   │   │  PostgreSQL     │             │  PostgreSQL     │               │   │
│   │   │  Primary        │────同步复制──▶│  Standby        │               │   │
│   │   │  (读写)          │             │  (只读)          │               │   │
│   │   └─────────────────┘             └─────────────────┘               │   │
│   │                                                                      │   │
│   └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌──────────────────────────────────────────────────────────────────────┐  │
│   │                       故障转移策略                                    │  │
│   │                                                                      │  │
│   │   正常状态:                                                          │  │
│   │   ┌─────────┐      ┌─────────┐      ┌─────────┐                     │  │
│   │   │  用户   │ ────▶│   ALB   │ ────▶│  AZ-A   │                     │  │
│   │   └─────────┘      └─────────┘      └─────────┘                     │  │
│   │                          │                                           │  │
│   │                          └─────────▶│  AZ-B   │ (备用)               │  │
│   │                                     └─────────┘                      │  │
│   │                                                                      │  │
│   │   AZ-A 故障:                                                         │  │
│   │   ┌─────────┐      ┌─────────┐      ┌─────────┐                     │  │
│   │   │  用户   │ ────▶│   ALB   │ ────▶│  AZ-B   │ (自动接管)          │  │
│   │   └─────────┘      └─────────┘      └─────────┘                     │  │
│   │                                         │                            │  │
│   │                               PostgreSQL Standby 自动提升为 Primary   │  │
│   │                                                                      │  │
│   └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 10.2 数据备份策略

```yaml
# 备份策略配置
backup:
  database:
    # PostgreSQL 备份
    type: "pg_basebackup + WAL archiving"
    schedule:
      full_backup: "0 2 * * *"          # 每天凌晨 2 点全量备份
      wal_archive: "continuous"          # 持续 WAL 归档
    retention:
      full_backup: 30                    # 保留 30 天
      wal_archive: 7                     # WAL 保留 7 天
    storage:
      primary: "s3://di-backup-primary"
      secondary: "oss://di-backup-dr"    # 异地容灾
    encryption:
      algorithm: "AES-256-GCM"
      key_management: "AWS KMS"

  object_storage:
    # MinIO APK 文件备份
    type: "cross-region-replication"
    source: "minio-primary.di.internal"
    target: "minio-dr.di.internal"
    sync_mode: "async"
    lag_threshold: "5m"

  redis:
    # Redis 集群备份
    type: "RDB + AOF"
    rdb_schedule: "0 */6 * * *"          # 每 6 小时 RDB 快照
    aof_fsync: "everysec"
    retention: 7

  elasticsearch:
    # ES 索引备份
    type: "snapshot"
    repository: "s3://di-backup-es"
    schedule: "0 3 * * *"
    retention: 14
```

### 10.3 灾难恢复流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           灾难恢复 (DR) 流程                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        RTO / RPO 目标                                │   │
│   │                                                                      │   │
│   │   ┌──────────────────┬─────────────────┬─────────────────────────┐  │   │
│   │   │ 服务等级         │      RTO        │         RPO             │  │   │
│   │   ├──────────────────┼─────────────────┼─────────────────────────┤  │   │
│   │   │ API 服务         │    < 5 分钟     │       0 (同步复制)      │  │   │
│   │   │ 数据库           │    < 15 分钟    │      < 1 分钟           │  │   │
│   │   │ 推送服务         │    < 30 分钟    │      < 5 分钟           │  │   │
│   │   │ APK 下载         │    < 1 小时     │       0 (跨区复制)      │  │   │
│   │   │ 搜索服务         │    < 2 小时     │      < 1 小时           │  │   │
│   │   └──────────────────┴─────────────────┴─────────────────────────┘  │   │
│   └──────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│   ┌──────────────────────────────────────────────────────────────────────┐  │
│   │                       DR 切换流程                                     │  │
│   │                                                                      │  │
│   │   1. 检测与告警 (自动)                                                │  │
│   │      │                                                               │  │
│   │      ▼                                                               │  │
│   │   ┌───────────────────────────────────────────────────────────────┐ │  │
│   │   │ 监控系统检测到主区域故障                                        │ │  │
│   │   │ • 连续 3 次健康检查失败                                         │ │  │
│   │   │ • 网络不可达超过 2 分钟                                         │ │  │
│   │   └───────────────────────────────────────────────────────────────┘ │  │
│   │      │                                                               │  │
│   │      ▼                                                               │  │
│   │   2. 自动 DNS 切换                                                   │  │
│   │      │                                                               │  │
│   │      ▼                                                               │  │
│   │   ┌───────────────────────────────────────────────────────────────┐ │  │
│   │   │ Route53/DNS 健康检查触发自动故障转移                            │ │  │
│   │   │ api.di.xyz -> DR Region IP                                    │ │  │
│   │   └───────────────────────────────────────────────────────────────┘ │  │
│   │      │                                                               │  │
│   │      ▼                                                               │  │
│   │   3. 数据库提升 (手动确认)                                            │  │
│   │      │                                                               │  │
│   │      ▼                                                               │  │
│   │   ┌───────────────────────────────────────────────────────────────┐ │  │
│   │   │ pg_ctl promote -D /var/lib/postgresql/data                    │ │  │
│   │   │ # 将 Standby 提升为 Primary                                    │ │  │
│   │   └───────────────────────────────────────────────────────────────┘ │  │
│   │      │                                                               │  │
│   │      ▼                                                               │  │
│   │   4. 服务验证                                                        │  │
│   │      │                                                               │  │
│   │      ▼                                                               │  │
│   │   ┌───────────────────────────────────────────────────────────────┐ │  │
│   │   │ 自动化测试验证:                                                 │ │  │
│   │   │ • API 端点可访问性                                              │ │  │
│   │   │ • 数据完整性检查                                                │ │  │
│   │   │ • 关键业务流程测试                                              │ │  │
│   │   └───────────────────────────────────────────────────────────────┘ │  │
│   │                                                                      │  │
│   └──────────────────────────────────────────────────────────────────────┘  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 11. 合规与隐私保护

### 11.1 数据分类与处理

| 数据类型 | 敏感级别 | 存储策略 | 访问控制 | 保留期限 |
|---------|---------|---------|---------|---------|
| 私钥/助记词 | 最高 | 仅本地 TEE/Keystore | 生物识别 | 永久 (用户控制) |
| 钱包地址 | 高 | 加密存储 | 用户级隔离 | 账户存续期 |
| 交易记录 | 高 | 加密存储 | 用户级隔离 | 5 年 |
| 设备信息 | 中 | 服务端加密 | 内部访问日志 | 1 年 |
| 应用使用数据 | 低 | 匿名化处理 | 聚合统计 | 90 天 |
| 日志数据 | 中 | 脱敏后存储 | 安全团队 | 30 天 |

### 11.2 隐私保护措施

```kotlin
// 隐私数据处理策略
class PrivacyManager {

    // 数据收集最小化原则
    data class CollectedData(
        val deviceId: String,           // 匿名设备标识
        val appUsageStats: UsageStats?, // 可选，用户授权后收集
        val crashReports: CrashData?    // 可选，用户授权后收集
    ) {
        // 不收集: 通讯录、短信、位置、照片等敏感信息
    }

    // 用户数据权利
    interface UserDataRights {
        // 数据访问权 (GDPR Art. 15)
        suspend fun exportUserData(userId: String): UserDataExport

        // 数据删除权 (GDPR Art. 17)
        suspend fun deleteUserData(userId: String): DeletionResult

        // 数据可携带权 (GDPR Art. 20)
        suspend fun portUserData(userId: String, format: ExportFormat): ByteArray

        // 撤回同意权
        suspend fun withdrawConsent(userId: String, consentType: ConsentType)
    }

    // 日志脱敏处理
    fun sanitizeLog(message: String): String {
        return message
            .replace(Regex("0x[a-fA-F0-9]{40}"), "0x****...****")  // 钱包地址
            .replace(Regex("\\b[\\w.-]+@[\\w.-]+\\.\\w+\\b"), "***@***.***")  // 邮箱
            .replace(Regex("\\b\\d{11}\\b"), "***********")  // 手机号
            .replace(Regex("\"(private[Kk]ey|mnemonic|password)\":\"[^\"]+\""),
                     "\"$1\":\"[REDACTED]\"")
    }

    // 链上数据披露提示
    fun showBlockchainDisclosure(): Disclosure {
        return Disclosure(
            title = "区块链数据公开性提示",
            content = """
                您在区块链上的交易记录是公开可查的:
                • 钱包地址和交易历史可被任何人查看
                • 交易一旦确认无法撤销或删除
                • 建议使用独立地址保护隐私
            """.trimIndent()
        )
    }
}
```

### 11.3 合规检查清单

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            合规检查清单                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   GDPR 合规 (欧盟用户)                                                       │
│   ┌───────────────────────────────────────────────────────────────────────┐ │
│   │ [✓] 隐私政策清晰说明数据收集和使用目的                                  │ │
│   │ [✓] 提供数据访问、删除、导出功能                                        │ │
│   │ [✓] 实现用户同意机制 (Consent Management)                              │ │
│   │ [✓] 数据处理记录 (Records of Processing Activities)                    │ │
│   │ [✓] 数据保护影响评估 (DPIA) 已完成                                      │ │
│   │ [✓] 任命数据保护官 (DPO)                                               │ │
│   │ [✓] 72 小时数据泄露通知流程                                            │ │
│   └───────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│   中国网络安全法合规                                                         │
│   ┌───────────────────────────────────────────────────────────────────────┐ │
│   │ [✓] 用户数据存储在境内服务器                                            │ │
│   │ [✓] 实名认证接口 (可选功能)                                            │ │
│   │ [✓] 内容审核机制                                                       │ │
│   │ [✓] 日志保留符合要求 (≥6个月)                                          │ │
│   │ [✓] 安全等级保护评估                                                    │ │
│   └───────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│   加密货币合规考虑                                                           │
│   ┌───────────────────────────────────────────────────────────────────────┐ │
│   │ [!] 不同地区监管政策差异 (需法务评估)                                    │ │
│   │ [!] 反洗钱 (AML) 风险评估                                              │ │
│   │ [!] 可疑交易报告机制 (如需要)                                           │ │
│   │ [ ] KYC 流程 (根据业务需要)                                             │ │
│   └───────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
│   应用商店合规                                                               │
│   ┌───────────────────────────────────────────────────────────────────────┐ │
│   │ [✓] 开发者实名认证                                                      │ │
│   │ [✓] 应用内容审核标准                                                    │ │
│   │ [✓] 用户年龄限制提示                                                    │ │
│   │ [✓] 投诉举报处理机制                                                    │ │
│   └───────────────────────────────────────────────────────────────────────┘ │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 开发计划

### Phase 1: 基础框架 (MVP)

**目标**: 完成最小可用版本

#### 1.1 Android 客户端
- [ ] 项目初始化与架构搭建
- [ ] 应用市场首页 UI
- [ ] 应用列表与详情页
- [ ] APK 下载与安装管理
- [ ] 基础钱包功能 (创建/导入)
- [ ] 单链支持 (Ethereum)

#### 1.2 后端服务
- [ ] Spring Boot 项目初始化
- [ ] 数据库设计与迁移
- [ ] 应用 CRUD API
- [ ] 文件上传与存储
- [ ] 基础认证系统

#### 1.3 交付物
- 可安装的 APK
- 可运行的后端服务
- API 文档

---

### Phase 2: 钱包增强

**目标**: 完善钱包功能，支持多链

#### 2.1 多链钱包
- [ ] 集成 Trust Wallet Core
- [ ] Solana 链支持
- [ ] BSC/Polygon 支持
- [ ] Token 列表管理
- [ ] NFT 展示

#### 2.2 安全增强
- [ ] Seed Vault 集成 (如硬件支持)
- [ ] Android Keystore 加密
- [ ] 生物识别认证
- [ ] 交易确认流程

#### 2.3 交付物
- 多链钱包功能
- 安全存储方案

---

### Phase 3: 生态完善

**目标**: 完善应用市场生态

#### 3.1 开发者平台
- [ ] 开发者注册系统
- [ ] 应用提交流程
- [ ] 审核工作流
- [ ] 开发者控制台

#### 3.2 用户体验
- [ ] 搜索与推荐
- [ ] 评分评论系统
- [ ] 应用更新通知
- [ ] 用户偏好设置

#### 3.3 交付物
- 开发者平台
- 完整的应用市场功能

---

### Phase 4: 系统集成

**目标**: 预装到手机 ROM

#### 4.1 ROM 集成
- [ ] System App 适配
- [ ] 开机引导流程
- [ ] OTA 更新支持
- [ ] 系统权限配置

#### 4.2 性能优化
- [ ] 启动速度优化
- [ ] 内存占用优化
- [ ] 电量消耗优化

#### 4.3 交付物
- ROM 集成包
- 预装应用指南

---

## 附录

### A. 目录结构

```
web3-dapp-store/
├── android/                    # Android 客户端
│   ├── app/
│   │   └── src/main/
│   │       ├── java/com/web3store/
│   │       │   ├── ui/         # UI 层
│   │       │   ├── domain/     # 业务逻辑
│   │       │   ├── data/       # 数据层
│   │       │   ├── di/         # 依赖注入
│   │       │   ├── security/   # 安全模块
│   │       │   └── utils/      # 工具类
│   │       └── res/            # 资源文件
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
├── backend/                    # 后端服务
│   ├── src/main/
│   │   ├── java/com/web3store/server/
│   │   │   ├── controller/     # API 控制器
│   │   │   ├── service/        # 业务服务
│   │   │   ├── repository/     # 数据访问
│   │   │   ├── model/          # 数据模型
│   │   │   ├── config/         # 配置类
│   │   │   └── security/       # 安全配置
│   │   └── resources/
│   └── docker/                 # Docker 配置
│
├── shared/                     # 共享模块
│   ├── proto/                  # Protobuf 定义
│   └── contracts/              # 智能合约 ABI
│
├── docs/                       # 文档
│   └── DESIGN.md              # 本设计文档
│
├── scripts/                    # 脚本工具
│
└── refs/                       # 参考代码库
    ├── neo-store/
    ├── fdroidserver/
    ├── wallet-core/
    ├── solana-mwa/
    ├── seed-vault-sdk/
    ├── walletconnect-kotlin/
    └── sol4k/
```

### B. 环境配置

```bash
# 开发环境要求
- JDK 17+
- Android Studio Hedgehog+
- Kotlin 1.9+
- Docker & Docker Compose
- Node.js 18+ (Web3 工具)

# 启动后端服务
cd backend/docker
docker-compose up -d

# 启动 Android 开发
cd android
./gradlew assembleDebug
```

### C. 参考资源

- [Solana Mobile Documentation](https://docs.solanamobile.com/)
- [Trust Wallet Developer Docs](https://developer.trustwallet.com/)
- [WalletConnect V2 Docs](https://docs.walletconnect.com/)
- [F-Droid Server Documentation](https://f-droid.org/docs/)
- [EIP-1193 Ethereum Provider](https://eips.ethereum.org/EIPS/eip-1193)
