#!/bin/bash

# Web3 DApp Store - 开发环境配置脚本

set -e

echo "======================================"
echo "Web3 DApp Store 开发环境配置"
echo "======================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查必要工具
check_tool() {
    if command -v $1 &> /dev/null; then
        echo -e "${GREEN}✓${NC} $1 已安装"
        return 0
    else
        echo -e "${RED}✗${NC} $1 未安装"
        return 1
    fi
}

echo ""
echo "检查开发环境..."
echo "----------------"

check_tool "java" || { echo "请安装 JDK 17+"; exit 1; }
check_tool "docker" || { echo "请安装 Docker"; exit 1; }
check_tool "docker-compose" || check_tool "docker" # docker compose v2
check_tool "git" || { echo "请安装 Git"; exit 1; }

# 检查 Java 版本
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -ge 17 ]; then
    echo -e "${GREEN}✓${NC} Java 版本: $JAVA_VERSION"
else
    echo -e "${RED}✗${NC} 需要 Java 17+, 当前版本: $JAVA_VERSION"
    exit 1
fi

# 检查 Git LFS
if check_tool "git-lfs"; then
    echo "  Git LFS 已配置"
else
    echo -e "${YELLOW}!${NC} Git LFS 未安装，部分依赖库可能无法完整克隆"
    echo "  安装命令: brew install git-lfs && git lfs install"
fi

echo ""
echo "配置参考代码库..."
echo "------------------"

REFS_DIR="$(dirname "$0")/../refs"

# 克隆缺失的参考库
clone_if_missing() {
    local dir=$1
    local url=$2
    if [ ! -d "$REFS_DIR/$dir" ]; then
        echo "克隆 $dir..."
        git clone --depth 1 "$url" "$REFS_DIR/$dir"
    else
        echo -e "${GREEN}✓${NC} $dir 已存在"
    fi
}

clone_if_missing "neo-store" "https://github.com/NeoApplications/Neo-Store.git"
clone_if_missing "fdroidserver" "https://github.com/f-droid/fdroidserver.git"
clone_if_missing "wallet-core" "https://github.com/trustwallet/wallet-core.git"
clone_if_missing "solana-mwa" "https://github.com/solana-mobile/mobile-wallet-adapter.git"
clone_if_missing "seed-vault-sdk" "https://github.com/solana-mobile/seed-vault-sdk.git"
clone_if_missing "walletconnect-kotlin" "https://github.com/WalletConnect/WalletConnectKotlinV2.git"
clone_if_missing "sol4k" "https://github.com/sol4k/sol4k.git"

# trust-web3-provider 需要 git-lfs
if [ ! -d "$REFS_DIR/trust-web3-provider" ]; then
    if command -v git-lfs &> /dev/null; then
        echo "克隆 trust-web3-provider..."
        git clone --depth 1 "https://github.com/trustwallet/trust-web3-provider.git" "$REFS_DIR/trust-web3-provider"
    else
        echo -e "${YELLOW}!${NC} 跳过 trust-web3-provider (需要 git-lfs)"
    fi
else
    echo -e "${GREEN}✓${NC} trust-web3-provider 已存在"
fi

echo ""
echo "启动后端服务..."
echo "----------------"

cd "$(dirname "$0")/../backend/docker"

if docker-compose ps | grep -q "Up"; then
    echo -e "${GREEN}✓${NC} Docker 服务已在运行"
else
    echo "启动 Docker 容器..."
    docker-compose up -d
    echo -e "${GREEN}✓${NC} Docker 服务已启动"
fi

echo ""
echo "======================================"
echo -e "${GREEN}开发环境配置完成!${NC}"
echo "======================================"
echo ""
echo "下一步:"
echo "  1. 打开 Android Studio"
echo "  2. 导入 android/ 目录"
echo "  3. 运行应用"
echo ""
echo "后端服务地址:"
echo "  - API Server: http://localhost:8080"
echo "  - MinIO Console: http://localhost:9001"
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
echo ""
