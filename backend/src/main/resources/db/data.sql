-- 初始化分类数据
INSERT INTO categories (name, display_name, description, icon_name, sort_order) VALUES
('defi', 'DeFi', '去中心化金融应用', 'account_balance', 1),
('nft', 'NFT', 'NFT 市场和收藏品', 'collections', 2),
('games', '游戏', 'Web3 游戏和 GameFi', 'sports_esports', 3),
('social', '社交', '去中心化社交应用', 'people', 4),
('dao', 'DAO', '去中心化自治组织工具', 'groups', 5),
('tools', '工具', '钱包和开发工具', 'build', 6),
('exchange', '交易所', 'DEX 和交易平台', 'swap_horiz', 7),
('other', '其他', '其他 Web3 应用', 'more_horiz', 8);

-- 创建测试用户
INSERT INTO users (wallet_address, auth_provider, username, role, nonce) VALUES
('0x1234567890abcdef1234567890abcdef12345678', 'WALLET', 'TestUser', 'USER', 'test-nonce-123'),
('0xabcdefabcdefabcdefabcdefabcdefabcdefabcd', 'WALLET', 'TestDeveloper', 'DEVELOPER', 'dev-nonce-456'),
('0xadmin0000000000000000000000000000000000', 'WALLET', 'TestAdmin', 'ADMIN', 'admin-nonce-789');

-- 创建测试开发者
INSERT INTO developers (user_id, company_name, contact_email, description, is_verified, verification_status) VALUES
(2, 'DI Labs', 'dev@di.xyz', 'Web3 DApp 开发团队', TRUE, 'VERIFIED');

-- 创建测试应用 (使用真实 APK)
INSERT INTO apps (package_name, name, short_description, description, version_name, version_code,
    apk_url, apk_hash, apk_size, icon_url, website_url, developer_id, category_id, is_web3, blockchain, status, is_featured, download_count, rating_average, rating_count) VALUES

-- TokenPocket: 多链钱包 (真实 APK)
('vip.mytokenpocket', 'TokenPocket', '多链加密钱包',
    'TokenPocket 是一款多链加密货币钱包，支持比特币、以太坊、EOS、TRON、BSC 等众多主流公链。功能包括：多链资产管理、DApp 浏览器、代币交换、NFT 收藏、Staking 质押等。安全可靠，全球数百万用户信赖。',
    '2.13.0', 2130, 'http://192.168.3.104:9102/dappstore-apks/tokenpocket.apk', 'd0487a3ef698dfc1ef3f3836f423e353bc5c419ec26e89522c5408768c36dd0c', 106256869,
    'http://192.168.3.104:9102/dappstore-icons/tokenpocket_icon.png',
    'https://tokenpocket.pro', 1, 6, TRUE, 'ethereum,bsc,tron,eos,polygon', 'APPROVED', TRUE, 5000000, 4.7, 125800),

-- Trust Wallet: 官方 Binance 钱包 (真实 APK)
('com.wallet.crypto.trustapp', 'Trust Wallet', '安全的多链加密钱包',
    'Trust Wallet 是一款安全、开源的多链加密货币钱包，由 Binance 支持。支持 100+ 区块链，包括以太坊、BSC、Polygon、Solana 等。功能包括：安全存储私钥、DApp 浏览器、代币交换、NFT 收藏、Staking 质押。全球超过 7000 万用户信赖。',
    '8.78.1', 8781, 'http://192.168.3.104:9102/dappstore-apks/trustwallet.apk', 'b4dce55a9073bc5730b39132fb8650a6e38cf210c1b59046adc018220338ae80', 194855428,
    'http://192.168.3.104:9102/dappstore-icons/trustwallet_icon.png',
    'https://trustwallet.com', 1, 6, TRUE, 'ethereum,bsc,polygon,solana,arbitrum', 'APPROVED', TRUE, 70000000, 4.6, 890000),

-- Telegram: 通讯应用 (真实 APK)
('org.telegram.messenger.web', 'Telegram', '安全即时通讯应用',
    'Telegram 是一款专注于速度和安全的即时通讯应用。支持端到端加密、自动销毁消息、无限云存储。在 Web3 生态中，Telegram 是加密社区最重要的通讯工具，支持 TON 钱包和 Mini Apps。',
    '12.3.1', 12031, 'http://192.168.3.104:9102/dappstore-apks/telegram.apk', '4d3400b9330b2a7d93683fd03ddda41aa144c65f74eb27e3a1786b21397ec7d1', 83083680,
    'http://192.168.3.104:9102/dappstore-icons/telegram_icon.png',
    'https://telegram.org', 1, 4, TRUE, 'ton', 'APPROVED', TRUE, 100000000, 4.8, 2500000),

-- MetaMask: 最流行的以太坊钱包 (真实 APK - GitHub Release)
('io.metamask', 'MetaMask', '以太坊钱包与 Web3 浏览器',
    'MetaMask 是全球最受欢迎的自托管加密钱包，已有超过 3000 万用户信任。安全存储和管理您的数字资产，探索区块链应用。功能亮点：安全密钥管理、多链支持（以太坊及 EVM 兼容链）、内置 Swap 功能、NFT 收藏管理、与数千个 DApps 无缝连接。',
    '7.61.5', 3341, 'http://192.168.3.104:9102/dappstore-apks/metamask.apk', '3cb654ac20b8c2e6ef6b3e11facab2e3fc40615e2edbefb4a0f4d93862254443', 214025059,
    'http://192.168.3.104:9102/dappstore-icons/metamask_icon.png',
    'https://metamask.io', 1, 6, TRUE, 'ethereum,polygon,bsc,avalanche', 'APPROVED', TRUE, 5000000, 4.5, 89650),

-- OKX: 加密货币交易所 (真实 APK)
('com.okinc.okex.gp', 'OKX', '加密货币交易平台',
    'OKX 是全球领先的加密货币交易平台，提供现货、合约、期权等多种交易服务。内置 Web3 钱包支持多链资产管理和 DApp 浏览。功能包括：一站式交易、理财产品、NFT 市场、跨链桥。',
    '6.145.0', 61450, 'http://192.168.3.104:9102/dappstore-apks/okx.apk', '1f6a795bc25fe3c88a59b03888da6d6618b7490e68e68c179bd23d7737c58f5c', 355542448,
    'http://192.168.3.104:9102/dappstore-icons/okx_icon.png',
    'https://okx.com', 1, 7, TRUE, 'ethereum,bsc,polygon,okc', 'APPROVED', TRUE, 10000000, 4.4, 150000),

-- Binance: 全球最大加密货币交易所 (真实 APK)
('com.binance.dev', 'Binance', '全球最大加密货币交易所',
    'Binance 是全球交易量最大的加密货币交易所。提供现货、合约、杠杆、理财等全面服务。功能包括：安全存储资产、即时交易、价格提醒、NFT 市场、Launchpad 新币发行。超过 3 亿用户选择 Binance。',
    '3.8.4', 384, 'http://192.168.3.104:9102/dappstore-apks/binance.apk', '399bfe5014db8a688f031eb222d721d680a089b8b1a92b3edef9652b9b7aad59', 292635057,
    'http://192.168.3.104:9102/dappstore-icons/binance_icon.png',
    'https://binance.com', 1, 7, TRUE, 'ethereum,bsc', 'APPROVED', TRUE, 300000000, 4.5, 1500000),

-- X (Twitter): 社交媒体 (真实 APK)
('com.twitter.android', 'X (Twitter)', '社交媒体平台',
    'X（原 Twitter）是全球最流行的社交媒体平台之一。在 Web3 生态中是加密社区交流、项目宣传的重要渠道。支持 NFT 头像认证、加密货币打赏等功能。',
    '11.54.0', 115400, 'http://192.168.3.104:9102/dappstore-apks/x.apk', 'dd368a36c6474bd60486409350fc89849fc848d762e161c1aad470f7b6174aca', 150953383,
    'http://192.168.3.104:9102/dappstore-icons/x_icon.png',
    'https://x.com', 1, 4, FALSE, NULL, 'APPROVED', TRUE, 500000000, 4.3, 5000000),

-- ave.ai: Web3 交易钱包 (真实 APK)
('ai.ave.platform', 'ave.ai', 'Web3 交易钱包',
    'ave.ai 是综合性链上交易平台，集成 130+ 区块链和 300+ DEX。提供专业的代币分析、智能交易信号、地址监控等功能。支持 Meme 币发现、Copy Trading、止盈止损等高级功能。',
    '2.3.01', 2301, 'http://192.168.3.104:9102/dappstore-apks/aveai.apk', '482d3decd44377661d9d3f53c70910269be0a0ecff6d6aa2012b25d642b73af5', 95628416,
    'http://192.168.3.104:9102/dappstore-icons/aveai_icon.png',
    'https://ave.ai', 1, 6, TRUE, 'ethereum,solana,bsc,base,ton', 'APPROVED', TRUE, 7000000, 4.5, 35000);
