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
INSERT INTO users (wallet_address, username, role, nonce) VALUES
('0x1234567890abcdef1234567890abcdef12345678', 'TestUser', 'USER', 'test-nonce-123'),
('0xabcdefabcdefabcdefabcdefabcdefabcdefabcd', 'TestDeveloper', 'DEVELOPER', 'dev-nonce-456');

-- 创建测试开发者
INSERT INTO developers (user_id, company_name, contact_email, description, is_verified, verification_status) VALUES
(2, 'DI Labs', 'dev@di.xyz', 'Web3 DApp 开发团队', TRUE, 'VERIFIED');

-- 创建测试应用
INSERT INTO apps (package_name, name, short_description, description, version_name, version_code,
    apk_url, apk_hash, developer_id, category_id, is_web3, blockchain, status, is_featured, download_count, rating_average, rating_count) VALUES
('com.uniswap.mobile', 'Uniswap', '去中心化交易所', 'Uniswap 是以太坊上最流行的去中心化交易所，支持代币兑换、提供流动性等功能。',
    '1.0.0', 1, 'https://example.com/uniswap.apk', 'sha256-hash-1', 1, 1, TRUE, 'ethereum', 'APPROVED', TRUE, 100000, 4.5, 1200),
('io.opensea.mobile', 'OpenSea', 'NFT 市场', 'OpenSea 是全球最大的 NFT 市场，支持购买、出售和探索 NFT。',
    '2.0.0', 2, 'https://example.com/opensea.apk', 'sha256-hash-2', 1, 2, TRUE, 'ethereum', 'APPROVED', TRUE, 80000, 4.3, 980),
('xyz.axie.infinity', 'Axie Infinity', 'Play to Earn 游戏', 'Axie Infinity 是一款基于区块链的养成对战游戏，玩家可以通过游戏赚取代币。',
    '1.5.0', 15, 'https://example.com/axie.apk', 'sha256-hash-3', 1, 3, TRUE, 'ronin', 'APPROVED', FALSE, 50000, 4.2, 750);
