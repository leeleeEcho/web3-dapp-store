# Web3 DApp Store - 部署指南

## 目录

1. [快速启动](#快速启动)
2. [环境要求](#环境要求)
3. [配置说明](#配置说明)
4. [生产部署](#生产部署)
5. [服务监控](#服务监控)
6. [常见问题](#常见问题)

---

## 快速启动

### 开发环境 (5分钟)

```bash
# 1. 进入 docker 目录
cd backend/docker

# 2. 复制环境变量文件
cp .env.example .env

# 3. 启动所有服务
docker-compose up -d

# 4. 查看服务状态
docker-compose ps

# 5. 查看日志
docker-compose logs -f web3store-server
```

### 访问服务

| 服务 | 地址 |
|------|------|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |
| MinIO Console | http://localhost:9001 |
| Elasticsearch | http://localhost:9200 |

---

## 环境要求

### 系统要求

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2 核 | 4 核 |
| 内存 | 4 GB | 8 GB |
| 磁盘 | 20 GB | 50 GB SSD |

### 软件要求

- Docker 20.10+
- Docker Compose 2.0+

### 端口使用

| 端口 | 服务 |
|------|------|
| 5432 | PostgreSQL |
| 6379 | Redis |
| 8080 | Backend API |
| 9000 | MinIO API |
| 9001 | MinIO Console |
| 9200 | Elasticsearch |
| 9300 | Elasticsearch (内部) |

---

## 配置说明

### 环境变量

复制 `.env.example` 为 `.env` 并修改以下变量：

```bash
# 数据库 (必须修改)
DB_PASSWORD=your_strong_password

# MinIO (必须修改)
MINIO_ROOT_PASSWORD=your_minio_password
MINIO_SECRET_KEY=your_minio_password

# JWT (必须修改)
JWT_SECRET=your_32_char_or_longer_secret_key
```

### 生产环境必须修改

| 变量 | 说明 | 建议 |
|------|------|------|
| `DB_PASSWORD` | 数据库密码 | 使用强密码 (16+ 字符) |
| `MINIO_ROOT_PASSWORD` | MinIO 密码 | 使用强密码 |
| `JWT_SECRET` | JWT 签名密钥 | 32+ 字符随机字符串 |
| `SPRING_PROFILES_ACTIVE` | Spring 配置文件 | 设置为 `prod` |

### 生成安全密钥

```bash
# 生成 JWT_SECRET
openssl rand -base64 32

# 生成密码
openssl rand -base64 24
```

---

## 生产部署

### 1. 准备环境

```bash
# 克隆代码
git clone https://github.com/your-org/web3-dapp-store.git
cd web3-dapp-store/backend/docker

# 配置环境变量
cp .env.example .env
vim .env  # 修改密码和密钥
```

### 2. 构建并启动

```bash
# 构建镜像
docker-compose build

# 启动服务 (后台运行)
docker-compose up -d

# 查看启动日志
docker-compose logs -f
```

### 3. 验证部署

```bash
# 检查服务健康状态
curl http://localhost:8080/actuator/health

# 应返回:
# {"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}

# 检查 API
curl http://localhost:8080/api/v1/categories
```

### 4. 初始化 MinIO 存储桶

首次部署需创建存储桶：

```bash
# 访问 MinIO Console: http://localhost:9001
# 登录后创建以下 Buckets:
# - dappstore-apks
# - dappstore-icons
# - dappstore-screenshots
```

---

## 服务监控

### 健康检查端点

```bash
# 整体健康状态
curl http://localhost:8080/actuator/health

# 详细健康信息 (需认证)
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/redis
```

### 查看日志

```bash
# 所有服务日志
docker-compose logs -f

# 特定服务日志
docker-compose logs -f web3store-server
docker-compose logs -f postgres
docker-compose logs -f elasticsearch
```

### 资源监控

```bash
# 查看容器资源使用
docker stats

# 查看容器详情
docker-compose ps
```

### Prometheus 指标

```bash
# 获取 Prometheus 指标
curl http://localhost:8080/actuator/prometheus
```

---

## 运维操作

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启单个服务
docker-compose restart web3store-server
```

### 更新部署

```bash
# 拉取最新代码
git pull

# 重新构建并启动
docker-compose up -d --build
```

### 数据备份

```bash
# 备份 PostgreSQL
docker exec web3store-postgres pg_dump -U postgres web3store > backup.sql

# 备份 Redis
docker exec web3store-redis redis-cli BGSAVE

# 备份 MinIO 数据
docker cp web3store-minio:/data ./minio-backup
```

### 数据恢复

```bash
# 恢复 PostgreSQL
cat backup.sql | docker exec -i web3store-postgres psql -U postgres web3store
```

### 清理数据

```bash
# 停止并删除容器
docker-compose down

# 删除数据卷 (警告: 会删除所有数据!)
docker-compose down -v
```

---

## 常见问题

### Q: 服务启动失败

**检查步骤:**
1. 查看日志: `docker-compose logs -f`
2. 检查端口占用: `lsof -i :8080`
3. 检查 Docker 内存: `docker system df`

### Q: 数据库连接失败

**解决方案:**
```bash
# 确认 PostgreSQL 启动
docker-compose ps postgres

# 检查连接
docker exec -it web3store-postgres psql -U postgres -c "\l"
```

### Q: Elasticsearch 启动慢

Elasticsearch 首次启动需要 1-2 分钟初始化，请耐心等待健康检查通过。

**检查状态:**
```bash
curl http://localhost:9200/_cluster/health?pretty
```

### Q: MinIO 无法访问

**解决方案:**
1. 确认端口 9000/9001 未被占用
2. 检查 MinIO 日志: `docker-compose logs minio`
3. 确认存储桶已创建

### Q: 内存不足

**解决方案:**
1. 增加 Docker 内存配额
2. 调整 JVM 参数: 修改 Dockerfile 中的 `JAVA_OPTS`
3. 减少 ES 内存: 修改 `docker-compose.yml` 中 `ES_JAVA_OPTS`

---

## 架构图

```
                    ┌─────────────────┐
                    │   Load Balancer │
                    │    (optional)   │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  Web3Store API  │
                    │   (Port 8080)   │
                    └────────┬────────┘
                             │
         ┌───────────────────┼───────────────────┐
         │                   │                   │
┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐
│   PostgreSQL    │ │     Redis       │ │  Elasticsearch  │
│   (Port 5432)   │ │   (Port 6379)   │ │   (Port 9200)   │
└─────────────────┘ └─────────────────┘ └─────────────────┘
                             │
                    ┌────────▼────────┐
                    │     MinIO       │
                    │ (Port 9000/9001)│
                    └─────────────────┘
```

---

## 联系支持

如遇问题，请提交 Issue 或联系开发团队。
