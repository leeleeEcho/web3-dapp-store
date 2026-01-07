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

-- 创建测试应用 (使用官方应用信息)
INSERT INTO apps (package_name, name, short_description, description, version_name, version_code,
    apk_url, apk_hash, apk_size, icon_url, website_url, developer_id, category_id, is_web3, blockchain, status, is_featured, download_count, rating_average, rating_count) VALUES
-- Uniswap Wallet: 官方自托管钱包和 DEX
('com.uniswap.mobile', 'Uniswap Wallet', '自托管加密钱包与去中心化交易',
    'Uniswap Wallet 是一款自托管加密钱包，让您轻松进行代币兑换、购买和出售 NFT，并与各种去中心化应用交互。支持以太坊、Polygon、Arbitrum、Optimism 等多链网络。核心功能包括：即时代币兑换、NFT 浏览与交易、多链资产管理、WalletConnect 支持。',
    '1.42', 142, 'http://192.168.3.104:8888/metamask.apk', '3cb654ac20b8c2e6ef6b3e11facab2e3fc40615e2edbefb4a0f4d93862254443', 214025059,
    'http://192.168.3.104:8888/uniswap_icon.png',
    'https://uniswap.org', 1, 1, TRUE, 'ethereum,polygon,arbitrum,optimism', 'APPROVED', TRUE, 1000000, 4.6, 12580),

-- MetaMask: 最流行的以太坊钱包
('io.metamask', 'MetaMask', '以太坊钱包与 Web3 浏览器',
    'MetaMask 是全球最受欢迎的自托管加密钱包，已有超过 3000 万用户信任。安全存储和管理您的数字资产，探索区块链应用。功能亮点：安全密钥管理、多链支持（以太坊及 EVM 兼容链）、内置 Swap 功能、NFT 收藏管理、与数千个 DApps 无缝连接。',
    '7.61.5', 3341, 'http://192.168.3.104:8888/metamask.apk', '3cb654ac20b8c2e6ef6b3e11facab2e3fc40615e2edbefb4a0f4d93862254443', 214025059,
    'http://192.168.3.104:8888/metamask_icon.png',
    'https://metamask.io', 1, 6, TRUE, 'ethereum,polygon,bsc,avalanche', 'APPROVED', TRUE, 5000000, 4.5, 89650),

-- OpenSea: 全球最大 NFT 市场
('io.opensea.mobile', 'OpenSea', 'NFT 市场领导者',
    'OpenSea 是全球首创且最大的 NFT 市场，汇集数字艺术、收藏品、游戏物品、域名等各类 NFT。平台支持以太坊、Polygon、Solana 等多链 NFT 交易。主要功能：浏览和购买 NFT、创建和出售作品、管理您的 NFT 收藏、实时价格追踪、稀有度分析。',
    '2.3.0', 230, 'http://192.168.3.104:8888/metamask.apk', '3cb654ac20b8c2e6ef6b3e11facab2e3fc40615e2edbefb4a0f4d93862254443', 214025059,
    'http://192.168.3.104:8888/opensea_icon.png',
    'https://opensea.io', 1, 2, TRUE, 'ethereum,polygon,solana', 'APPROVED', TRUE, 2500000, 4.3, 35890),

-- Axie Infinity: 知名链游
('com.axieinfinity.origin', 'Axie Infinity', 'Play-to-Earn 区块链游戏',
    'Axie Infinity 是一款基于区块链的数字宠物养成对战游戏。玩家可以收集、繁殖、战斗和交易被称为 Axies 的幻想生物。游戏运行在 Ronin 侧链上，提供低成本高速交易。特色功能：策略回合制战斗、Axie 繁殖系统、土地系统、SLP 和 AXS 代币奖励、竞技排名赛。',
    '2.1.5', 215, 'http://192.168.3.104:8888/metamask.apk', '3cb654ac20b8c2e6ef6b3e11facab2e3fc40615e2edbefb4a0f4d93862254443', 214025059,
    'http://192.168.3.104:8888/axie_icon.png',
    'https://axieinfinity.com', 1, 3, TRUE, 'ronin', 'APPROVED', TRUE, 800000, 4.2, 28750);
