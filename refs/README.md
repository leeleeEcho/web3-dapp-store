# 参考代码库说明

本目录包含 Web3 DApp Store 项目的参考代码库。

## 已克隆的仓库

| 目录 | 项目 | 用途 |
|------|------|------|
| `neo-store` | [Neo Store](https://github.com/NeoApplications/Neo-Store) | 应用市场 UI 参考 |
| `fdroidserver` | [F-Droid Server](https://github.com/f-droid/fdroidserver) | 应用仓库后端参考 |
| `wallet-core` | [Trust Wallet Core](https://github.com/trustwallet/wallet-core) | 多链钱包核心库 |
| `solana-mwa` | [Solana Mobile Wallet Adapter](https://github.com/solana-mobile/mobile-wallet-adapter) | Solana 钱包协议 |
| `seed-vault-sdk` | [Seed Vault SDK](https://github.com/solana-mobile/seed-vault-sdk) | 安全密钥存储 |
| `walletconnect-kotlin` | [WalletConnect Kotlin V2](https://github.com/WalletConnect/WalletConnectKotlinV2) | dApp 连接协议 |
| `sol4k` | [Sol4k](https://github.com/sol4k/sol4k) | Kotlin Solana 客户端 |

## 需要手动克隆的仓库

以下仓库需要安装 `git-lfs` 后手动克隆：

```bash
# 安装 git-lfs (macOS)
brew install git-lfs
git lfs install

# 克隆 trust-web3-provider
git clone https://github.com/trustwallet/trust-web3-provider.git trust-web3-provider
```

## 关键文件导航

### Neo Store (应用市场 UI)
- `neo-store/src/main/kotlin/com/machiav3lli/fdroid/` - 主要源码
- `neo-store/src/main/kotlin/com/machiav3lli/fdroid/ui/` - UI 组件

### F-Droid Server (后端)
- `fdroidserver/fdroidserver/` - Python 服务端代码
- `fdroidserver/fdroidserver/index.py` - 仓库索引生成

### Trust Wallet Core (钱包)
- `wallet-core/src/` - C++ 核心代码
- `wallet-core/android/` - Android 绑定

### Solana Mobile
- `solana-mwa/android/` - Android 实现
- `seed-vault-sdk/` - 安全存储 API
