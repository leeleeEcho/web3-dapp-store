# Web3 DApp Store

一款类似 Solana Phone dApp Store 的 Web3 应用市场，支持预装到自研手机 ROM 中。

## 项目概述

Web3 DApp Store 是一个面向 Web3 生态的应用分发平台，集成了：

- **应用市场** - 中心化审核的 dApp 分发
- **内置钱包** - 多链钱包支持 (ETH/SOL/BSC/Polygon)
- **dApp 浏览器** - 内置 Web3 浏览器
- **安全存储** - 硬件级密钥保护

## 快速开始

### 环境要求

- JDK 17+
- Android Studio Hedgehog (2023.1.1)+
- Docker & Docker Compose
- Git LFS (用于某些依赖库)

### 克隆项目

```bash
git clone https://github.com/your-org/web3-dapp-store.git
cd web3-dapp-store
```

### 启动后端服务

```bash
cd backend/docker
docker-compose up -d
```

### 构建 Android 应用

```bash
cd android
./gradlew assembleDebug
```

## 项目结构

```
web3-dapp-store/
├── android/          # Android 客户端
├── backend/          # 后端服务 (Spring Boot)
├── shared/           # 共享模块 (Proto/Contracts)
├── docs/             # 设计文档
├── scripts/          # 辅助脚本
└── refs/             # 参考代码库
```

## 文档

- [设计文档](docs/DESIGN.md) - 详细的技术设计和开发计划
- [参考代码说明](refs/README.md) - 参考项目导航

## 技术栈

### Android
- Kotlin + Jetpack Compose
- Hilt (DI) + Room (DB)
- Trust Wallet Core
- WalletConnect V2
- Solana Mobile Wallet Adapter

### Backend
- Spring Boot 3 + Java 17
- PostgreSQL + Redis
- MinIO (文件存储)
- Docker

## 开发计划

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | 基础框架 (MVP) | 计划中 |
| Phase 2 | 钱包增强 | 计划中 |
| Phase 3 | dApp 浏览器 | 计划中 |
| Phase 4 | 生态完善 | 计划中 |
| Phase 5 | 系统集成 | 计划中 |

## 参考项目

本项目参考了以下优秀的开源项目：

- [Neo Store](https://github.com/NeoApplications/Neo-Store) - 应用市场 UI
- [F-Droid Server](https://github.com/f-droid/fdroidserver) - 应用仓库管理
- [Trust Wallet Core](https://github.com/trustwallet/wallet-core) - 多链钱包
- [Solana Mobile MWA](https://github.com/solana-mobile/mobile-wallet-adapter) - Solana 钱包协议
- [Seed Vault SDK](https://github.com/solana-mobile/seed-vault-sdk) - 安全存储
- [WalletConnect Kotlin V2](https://github.com/WalletConnect/WalletConnectKotlinV2) - dApp 连接

## License

[MIT License](LICENSE)
