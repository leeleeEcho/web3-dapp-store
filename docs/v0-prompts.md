# V0.app 提示词集合

## Web3 DApp Store 首页设计

### 提示词 1: 完整首页 (推荐)

```
Design a mobile Web3 dApp Store home page with a premium black and gold luxury theme.

## Color Scheme (Black & Gold - DI Brand Style)
- Background: #000000 (pure black)
- Surface/Cards: #0D0D0D with subtle gold border (#1A1A1A)
- Primary accent: #D4AF37 (classic gold)
- Secondary accent: #F5A623 (bright gold for highlights)
- Text primary: #FFFFFF
- Text secondary: #A0A0A0
- Success: #4ADE80
- Border: linear-gradient(135deg, #D4AF37, #F5A623)

## Layout Structure

### Top Section (Header)
- Status bar area (time, battery icons)
- App title "DI Store" with gold accent logo on left
- Wallet connect button (gold outline) showing "0x1a2b...3c4d" or "Connect"
- Search icon button

### Search Bar (Below Header)
- Full-width search input with rounded corners
- Placeholder: "Search dApps, tokens, NFTs..."
- Gold accent border on focus
- Microphone icon on right

### Featured Section
- Section title "Featured" with "See all" link in gold
- Horizontal scrollable carousel
- Large cards (280x160px) with:
  - Background gradient (dark with subtle gold glow)
  - dApp icon (48px, rounded)
  - dApp name (bold, white)
  - Short tagline
  - "Featured" badge with gold background
  - Chain icons (ETH, SOL small badges)

### Category Chips
- Horizontal scrollable row
- Chips: "All", "DeFi", "NFT", "GameFi", "Social", "DAO", "Tools"
- Selected chip: gold background (#D4AF37), black text
- Unselected: transparent with gold border

### Popular dApps Section
- Section title "Popular" with filter dropdown
- Vertical list of app cards
- Each card contains:
  - App icon (56px, rounded-xl)
  - App name (bold)
  - Developer name (gray, smaller)
  - Star rating (gold stars) + review count
  - Download count with icon
  - "GET" button (gold gradient background)
  - Supported chains as small icons

### Trending Section
- Section title "Trending Now" 🔥
- Numbered list (1, 2, 3 in gold circles)
- Compact horizontal layout
- Show rank change indicator (↑↓)

### Bottom Navigation Bar
- 5 items with icons and labels
- Home (house icon) - selected, gold color
- Explore (compass icon) - gray
- Search (magnifying glass) - gray
- Wallet (wallet icon) - gray
- Profile (user icon) - gray
- Selected item has gold indicator dot above

## Design Details
- Border radius: 16px for cards, 12px for buttons, 24px for chips
- Subtle gold glow/shadow on interactive elements
- Glass morphism effect on cards (subtle)
- Smooth transitions and micro-interactions
- Premium, luxurious feel matching high-end crypto aesthetic
- Dark mode only, no light theme

## Typography
- Font: Inter or SF Pro Display
- Headings: Bold, slightly larger
- Body: Regular weight
- Numbers: Tabular figures for alignment

## Spacing
- Page padding: 16px horizontal
- Section gap: 24px
- Card gap: 12px
- Component internal padding: 16px
```

---

### 提示词 2: 应用列表组件

```
Design a dApp list item component for a Web3 app store with black and gold theme.

## Component: Expandable App Card

### Collapsed State
Layout: Horizontal, full width
- Left: App icon (56px, rounded-2xl, subtle gold ring border)
- Center:
  - Row 1: App name (white, bold) + Verified badge (gold checkmark)
  - Row 2: Short description (gray, max 2 lines, ellipsis)
  - Row 3: Rating stars (gold) + "4.8" + "(2.3k)" + Chain icons
- Right: Version badge or "NEW" tag

### Expanded State (on tap)
- Smooth height animation
- Show additional info:
  - Full description
  - Screenshots preview (3 thumbnails)
  - Action buttons row:
    - "Add to Favorites" (heart icon, gold outline)
    - "Install" button (gold gradient, prominent)
    - "Open" if installed (outlined style)

## Colors
- Card background: #0A0A0A
- Border: 1px solid #1A1A1A
- On hover/focus: border becomes gold gradient
- Icon: white, gold accent for selected states
- Install button: linear-gradient(135deg, #D4AF37, #B8860B)

## States
- Default: dark card
- Pressed: slight scale down (0.98)
- Installing: progress bar with gold fill
- Installed: green checkmark, "Open" button
- Update available: gold "Update" badge
```

---

### 提示词 3: 分类浏览页

```
Design a category browsing page for Web3 dApp store with black gold theme.

## Layout

### Header
- Back button (gold icon)
- Category title (e.g., "DeFi Applications")
- Filter button (gold)
- Sort dropdown

### Filter Bar
- Horizontal scroll chips
- Options: "All Chains", "Ethereum", "Solana", "BSC", "Polygon"
- Selected: gold fill
- Additional filters: "Top Rated", "Most Downloaded", "New"

### Category Grid (Top Section)
- 2x3 grid of category cards
- Each card:
  - Icon representing category (in gold)
  - Category name
  - App count badge
  - Subtle gradient overlay
- Categories: DeFi, NFT, GameFi, Social, DAO, Tools, Wallet, Analytics

### App List
- Vertical scrolling list
- Same card style as home page
- Infinite scroll with loading skeleton

### Empty State
- Illustration (gold line art)
- "No apps found" message
- "Try different filters" suggestion

## Visual Effects
- Category icons with gold glow
- Smooth parallax on scroll
- Staggered animation on load
```

---

### 提示词 4: 钱包连接弹窗

```
Design a wallet connection modal for Web3 dApp store, black and gold luxury theme.

## Modal Design

### Overlay
- Background: rgba(0, 0, 0, 0.8)
- Blur effect on content behind

### Modal Container
- Background: #0A0A0A
- Border: 1px solid with gold gradient
- Border radius: 24px
- Max width: 360px
- Padding: 24px

### Content

#### Header
- Title: "Connect Wallet" (white, bold)
- Subtitle: "Choose your preferred wallet" (gray)
- Close button (X) top right, gold color

#### Wallet Options List
Each option is a button:
- MetaMask: Fox icon + "MetaMask" + "Popular" badge
- Phantom: Ghost icon + "Phantom" + "Solana"
- WalletConnect: Icon + "WalletConnect" + "Multi-chain"
- Coinbase Wallet: Icon + "Coinbase Wallet"
- Trust Wallet: Icon + "Trust Wallet"

Button style:
- Full width, height 56px
- Dark background #141414
- Gold border on hover
- Icon (32px) on left
- Wallet name in center
- Badge/tag on right
- Chevron right arrow

#### Divider
- "or" with lines
- Gold color

#### New to Web3 Section
- "New to crypto?" text
- "Create a new wallet" link (gold underline)

### Footer
- Security note with lock icon
- "Your keys, your crypto" tagline
- Terms of service link

## Animations
- Modal slides up from bottom
- Fade in overlay
- Staggered button animations
- Pulse effect on popular option
```

---

### 提示词 5: 应用详情页

```
Design a dApp detail page for Web3 app store with black and gold premium theme.

## Layout

### Hero Section
- Full width header image/banner (gradient with app colors)
- Floating app icon (80px, overlapping banner)
- Install/Open button (gold gradient, large)
- Share button

### App Info Header
- App name (large, white, bold)
- Developer name (gold link)
- Verified badge (gold checkmark)
- Category tag
- Rating: 4.8 ⭐ (2,345 reviews) - gold stars

### Quick Stats Row
- Downloads: "1.2M+"
- Size: "45 MB"
- Last updated: "2 days ago"
- Age rating: "17+"
- Chains: ETH, SOL icons

### Action Buttons
- Primary: "Install" (gold gradient, full width)
- Secondary row: "Add to Wishlist" ♡ | "Share" | "Report"

### Screenshots Section
- Horizontal carousel
- Phone mockup frames
- Dots indicator (gold active)

### Description
- Expandable text
- "Read more" link in gold
- App highlights with check icons

### Blockchain Info Card
- Contract addresses (copyable)
- Chain icons
- "View on Explorer" links
- Audit status badge

### Ratings & Reviews
- Rating breakdown (5 bars, gold fill)
- "Write a Review" button
- Review cards:
  - User avatar + name + rating
  - Review text
  - Helpful button
  - Date

### Similar Apps
- Horizontal scroll
- Compact app cards

### Developer Info
- Developer card
- Other apps by developer
- Contact/support links

## Bottom Bar (Sticky)
- Price: "Free" or token amount
- Install button (prominent, gold)
```

---

## 使用说明

1. 复制以上任一提示词到 v0.app
2. 生成后截图发送给我
3. 我将转换为 Jetpack Compose 代码

## 颜色参考值

```css
/* DI Black & Gold Theme */
--bg-primary: #000000;
--bg-secondary: #0A0A0A;
--bg-card: #0D0D0D;
--bg-elevated: #141414;
--border-default: #1A1A1A;
--border-gold: #D4AF37;

--gold-primary: #D4AF37;
--gold-bright: #F5A623;
--gold-dark: #B8860B;
--gold-gradient: linear-gradient(135deg, #D4AF37 0%, #F5A623 50%, #B8860B 100%);

--text-primary: #FFFFFF;
--text-secondary: #A0A0A0;
--text-tertiary: #666666;

--success: #4ADE80;
--warning: #FBBF24;
--error: #EF4444;

/* Chain Colors */
--chain-ethereum: #627EEA;
--chain-solana: #14F195;
--chain-bsc: #F0B90B;
--chain-polygon: #8247E5;
```

## 设计参考

- Neo Store: 应用列表可展开卡片、分类浏览、底部导航
- Solana dApp Store: Web3 特色、链标识、钱包集成
- DI.xyz: 黑金配色、科技感、高端质感
