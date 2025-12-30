# Web3 DApp Store 技术设计文档

## 项目概述

### 愿景
构建一款类似 Solana Phone dApp Store 的 Web3 应用市场，预装于自研手机中，为用户提供安全、便捷的去中心化应用分发和管理服务。

### 核心特性
- **应用市场**: 中心化审核的 Web3 应用分发
- **内置钱包**: 多链钱包支持 (Ethereum, Solana, BSC 等)
- **安全存储**: 硬件级密钥保护 (类似 Saga Seed Vault)
- **dApp 浏览器**: 内置 Web3 浏览器，支持 dApp 直接交互
- **预装集成**: 作为系统应用预装到手机 ROM

---

## 系统架构

### 整体架构图

```
┌────────────────────────────────────────────────────────────────────────┐
│                           手机端 (Android)                              │
├────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐    │
│  │                  │  │                  │  │                    │    │
│  │   DApp Store     │  │   内置钱包        │  │    dApp Browser    │    │
│  │   Application    │  │   Application    │  │    (WebView)       │    │
│  │                  │  │                  │  │                    │    │
│  └────────┬─────────┘  └────────┬─────────┘  └─────────┬──────────┘    │
│           │                     │                      │               │
│  ┌────────┴─────────────────────┴──────────────────────┴──────────┐    │
│  │                      Core SDK Layer                             │    │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌───────────┐ │    │
│  │  │ Trust       │ │ WalletCon-  │ │ Solana MWA  │ │ Web3      │ │    │
│  │  │ Wallet Core │ │ nect SDK    │ │ Protocol    │ │ Provider  │ │    │
│  │  └─────────────┘ └─────────────┘ └─────────────┘ └───────────┘ │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                      Security Layer                              │    │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────┐  │    │
│  │  │   Seed Vault     │  │  Android Keystore │  │   Biometric   │  │    │
│  │  │   (TEE/SE)       │  │  (Encrypted)      │  │   Auth        │  │    │
│  │  └──────────────────┘  └──────────────────┘  └───────────────┘  │    │
│  └─────────────────────────────────────────────────────────────────┘    │
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

### 3. dApp 浏览器

#### 3.1 架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      dApp Browser                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Browser UI                             │   │
│  │  ┌─────────┐ ┌─────────────────────────┐ ┌─────────────┐ │   │
│  │  │URL Bar  │ │       WebView           │ │ Wallet Conn │ │   │
│  │  └─────────┘ └─────────────────────────┘ └─────────────┘ │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                   │
│  ┌──────────────────────────┴───────────────────────────────┐   │
│  │              Web3 Provider Injection                      │   │
│  │                                                           │   │
│  │   window.ethereum = {                                     │   │
│  │     isMetaMask: true,                                     │   │
│  │     chainId: '0x1',                                       │   │
│  │     request: async (args) => { ... },                     │   │
│  │     on: (event, callback) => { ... },                     │   │
│  │     // EIP-1193 compliant                                 │   │
│  │   }                                                       │   │
│  │                                                           │   │
│  │   window.solana = {                                       │   │
│  │     isPhantom: true,                                      │   │
│  │     connect: async () => { ... },                         │   │
│  │     signTransaction: async (tx) => { ... },               │   │
│  │   }                                                       │   │
│  │                                                           │   │
│  └───────────────────────────────────────────────────────────┘   │
│                              │                                   │
│  ┌──────────────────────────┴───────────────────────────────┐   │
│  │              JavaScript Bridge                            │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────────────┐│   │
│  │  │ RPC Handler│ │Event System│ │   Permission Manager   ││   │
│  │  └────────────┘ └────────────┘ └────────────────────────┘│   │
│  └───────────────────────────────────────────────────────────┘   │
│                              │                                   │
│  ┌──────────────────────────┴───────────────────────────────┐   │
│  │                  Native Wallet                            │   │
│  └───────────────────────────────────────────────────────────┘   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 3.2 支持的 RPC 方法

**Ethereum (EIP-1193)**:
- `eth_requestAccounts` - 请求连接
- `eth_accounts` - 获取账户
- `eth_chainId` - 获取链 ID
- `eth_sendTransaction` - 发送交易
- `eth_signTransaction` - 签名交易
- `personal_sign` - 个人签名
- `eth_signTypedData_v4` - EIP-712 类型签名
- `wallet_switchEthereumChain` - 切换网络
- `wallet_addEthereumChain` - 添加网络

**Solana**:
- `connect` - 连接钱包
- `disconnect` - 断开连接
- `signTransaction` - 签名交易
- `signAllTransactions` - 批量签名
- `signMessage` - 签名消息

---

### 4. 后端服务

#### 4.1 API 设计

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

#### 4.2 数据库设计

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

### 5. 安全设计

#### 5.1 密钥安全

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

#### 5.2 应用安全

| 安全措施 | 描述 |
|----------|------|
| APK 签名验证 | 所有上架应用必须提供开发者签名 |
| SHA-256 完整性校验 | 下载后验证 APK 哈希 |
| 权限审查 | 敏感权限需要人工审核 |
| 恶意代码扫描 | 自动化安全扫描 |
| 证书透明度 | 公开开发者证书信息 |

#### 5.3 通信安全

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

| 技术 | 用途 |
|------|------|
| Java 17 / Kotlin | 开发语言 |
| Spring Boot 3 | Web 框架 |
| PostgreSQL | 主数据库 |
| Redis | 缓存 |
| MinIO | APK 文件存储 |
| Elasticsearch | 搜索引擎 |
| Docker | 容器化部署 |

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

### Phase 3: dApp 浏览器

**目标**: 实现 Web3 dApp 访问能力

#### 3.1 浏览器实现
- [ ] WebView 配置
- [ ] Web3 Provider 注入 (EIP-1193)
- [ ] Solana Provider 注入
- [ ] JavaScript Bridge

#### 3.2 连接协议
- [ ] WalletConnect V2 集成
- [ ] Solana MWA 集成
- [ ] dApp 权限管理

#### 3.3 交付物
- 可访问 dApp 的浏览器
- WalletConnect 支持

---

### Phase 4: 生态完善

**目标**: 完善应用市场生态

#### 4.1 开发者平台
- [ ] 开发者注册系统
- [ ] 应用提交流程
- [ ] 审核工作流
- [ ] 开发者控制台

#### 4.2 用户体验
- [ ] 搜索与推荐
- [ ] 评分评论系统
- [ ] 应用更新通知
- [ ] 用户偏好设置

#### 4.3 交付物
- 开发者平台
- 完整的应用市场功能

---

### Phase 5: 系统集成

**目标**: 预装到手机 ROM

#### 5.1 ROM 集成
- [ ] System App 适配
- [ ] 开机引导流程
- [ ] OTA 更新支持
- [ ] 系统权限配置

#### 5.2 性能优化
- [ ] 启动速度优化
- [ ] 内存占用优化
- [ ] 电量消耗优化

#### 5.3 交付物
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
