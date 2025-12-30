"use client"

import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Star, Download, Shield, Globe, ExternalLink } from "lucide-react"
import { use } from "react"

const appData: Record<string, any> = {
  "1": {
    name: "Aave",
    tagline: "Decentralized Lending Protocol",
    description:
      "Aave is an open-source and non-custodial liquidity protocol for earning interest on deposits and borrowing assets. The protocol features Flash Loans, the first uncollateralized loan in DeFi.",
    rating: 4.8,
    downloads: "2.5M",
    chains: ["Ethereum", "Polygon", "Avalanche", "Optimism"],
    icon: "/aave-defi-logo.jpg",
    screenshots: ["/aave-dashboard.jpg", "/aave-lending.jpg", "/aave-borrowing.jpg"],
    features: ["Lending & Borrowing", "Flash Loans", "Rate Switching", "Collateral Management"],
    security: "Audited by Trail of Bits, OpenZeppelin, and Consensys Diligence",
    website: "https://aave.com",
  },
  "2": {
    name: "OpenSea",
    tagline: "The World's First & Largest NFT Marketplace",
    description:
      "OpenSea is the first and largest peer-to-peer marketplace for NFTs. Discover, collect, and sell extraordinary NFTs on the world's first and largest NFT marketplace.",
    rating: 4.6,
    downloads: "3.2M",
    chains: ["Ethereum", "Polygon", "Solana", "Klaytn"],
    icon: "/opensea-nft-marketplace.jpg",
    screenshots: ["/opensea-marketplace.jpg", "/opensea-collections.jpg", "/opensea-nft.jpg"],
    features: ["Buy & Sell NFTs", "Create Collections", "Auction System", "Cross-chain Support"],
    security: "Smart contract audited and regularly reviewed",
    website: "https://opensea.io",
  },
  "3": {
    name: "Axie Infinity",
    tagline: "Play to Earn Blockchain Game",
    description:
      "Axie Infinity is a Pokemon-inspired digital pet universe where players battle, raise, and trade creatures called Axies. Players can earn tokens through skilled gameplay and contributions to the ecosystem.",
    rating: 4.7,
    downloads: "2.8M",
    chains: ["Ethereum", "Ronin"],
    icon: "/axie-infinity-blockchain-game.jpg",
    screenshots: ["/axie-infinity-gameplay.jpg", "/axie-battle.jpg", "/axie-marketplace.jpg"],
    features: ["PvP Battles", "Breeding System", "Land Gameplay", "Marketplace"],
    security: "Community-driven with regular security updates",
    website: "https://axieinfinity.com",
  },
  "4": {
    name: "PancakeSwap",
    tagline: "Leading DEX on BNB Chain",
    description:
      "PancakeSwap is a decentralized exchange (DEX) that allows users to trade BEP-20 tokens. Built on BNB Chain, it offers lower fees and faster transactions than Ethereum-based alternatives.",
    rating: 4.9,
    downloads: "3.1M",
    chains: ["BNB Chain", "Ethereum"],
    icon: "/pancakeswap-defi-logo.jpg",
    screenshots: ["/pancakeswap-trading.jpg", "/pancakeswap-farms.jpg", "/pancakeswap-pools.jpg"],
    features: ["Token Swaps", "Yield Farming", "Liquidity Pools", "NFT Marketplace"],
    security: "Audited by CertiK and regularly monitored",
    website: "https://pancakeswap.finance",
  },
  "5": {
    name: "MetaMask",
    tagline: "Your Gateway to Web3",
    description:
      "MetaMask is a cryptocurrency wallet that allows users to interact with the Ethereum blockchain and other EVM-compatible chains. It's available as a browser extension and mobile app.",
    rating: 4.5,
    downloads: "5.2M",
    chains: ["Ethereum", "BNB Chain", "Polygon", "Avalanche", "Arbitrum", "Optimism"],
    icon: "/images/metamask.png",
    screenshots: ["/metamask-wallet.jpg", "/metamask-swap.jpg", "/placeholder.svg?height=400&width=600"],
    features: ["Multi-chain Support", "Token Swaps", "Hardware Wallet Integration", "DApp Browser"],
    security: "Open-source and regularly audited",
    website: "https://metamask.io",
  },
}

export default function AppDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const app = appData[id] || appData["1"]

  return (
    <div className="min-h-screen bg-black text-white pb-20">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-black/95 backdrop-blur-sm border-b border-primary/20 animate-slide-in-down">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <Link href="/">
            <Button
              variant="ghost"
              size="icon"
              className="text-primary hover:bg-primary/10 transition-all duration-300 hover:scale-110"
            >
              <ArrowLeft className="w-6 h-6" />
            </Button>
          </Link>
          <Button
            variant="outline"
            className="text-primary border-primary/50 hover:bg-primary/10 hover:border-primary font-mono text-sm bg-transparent transition-all duration-300"
          >
            Share
          </Button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6 space-y-8">
        {/* App Header */}
        <section className="animate-fade-in-up">
          <div className="flex gap-6">
            <img
              src={app.icon || "/placeholder.svg"}
              alt={app.name}
              className="w-24 h-24 rounded-3xl shadow-lg transition-transform duration-300 hover:scale-110"
            />
            <div className="flex-1">
              <h1 className="text-3xl font-bold text-foreground mb-2">{app.name}</h1>
              <p className="text-lg text-muted-foreground mb-4">{app.tagline}</p>
              <div className="flex items-center gap-4 flex-wrap">
                <div className="flex items-center gap-2">
                  <Star className="w-5 h-5 fill-primary text-primary" />
                  <span className="text-lg text-primary font-semibold">{app.rating}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Download className="w-5 h-5 text-muted-foreground" />
                  <span className="text-muted-foreground">{app.downloads}</span>
                </div>
                <a
                  href={app.website}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 text-primary hover:text-secondary transition-colors duration-300"
                >
                  <Globe className="w-5 h-5" />
                  <span className="text-sm">Website</span>
                  <ExternalLink className="w-4 h-4" />
                </a>
              </div>
            </div>
          </div>

          <Button className="w-full mt-6 bg-primary text-black hover:bg-secondary font-bold text-lg py-6 rounded-2xl transition-all duration-300 hover:scale-105 hover:shadow-[0_0_30px_rgba(212,175,55,0.5)]">
            Install App
          </Button>
        </section>

        {/* Screenshots */}
        <section className="animate-fade-in-up" style={{ animationDelay: "100ms" }}>
          <h2 className="text-xl font-bold mb-4 text-foreground">Screenshots</h2>
          <div className="flex gap-4 overflow-x-auto pb-4 scrollbar-hide snap-x snap-mandatory">
            {app.screenshots.map((screenshot: string, index: number) => (
              <img
                key={index}
                src={screenshot || "/placeholder.svg"}
                alt={`Screenshot ${index + 1}`}
                className="h-64 w-auto rounded-2xl border border-primary/30 snap-start transition-all duration-300 hover:scale-105 hover:border-primary/60 cursor-pointer"
              />
            ))}
          </div>
        </section>

        {/* Description */}
        <section className="animate-fade-in-up" style={{ animationDelay: "200ms" }}>
          <h2 className="text-xl font-bold mb-3 text-foreground">About</h2>
          <p className="text-muted-foreground leading-relaxed">{app.description}</p>
        </section>

        {/* Features */}
        <section className="animate-fade-in-up" style={{ animationDelay: "300ms" }}>
          <h2 className="text-xl font-bold mb-4 text-foreground">Features</h2>
          <div className="grid grid-cols-2 gap-3">
            {app.features.map((feature: string, index: number) => (
              <Card
                key={index}
                className="bg-card border-primary/20 p-4 rounded-xl hover:border-primary/50 transition-all duration-300 hover:scale-105 hover:shadow-[0_0_20px_rgba(212,175,55,0.2)]"
              >
                <p className="text-foreground font-medium">{feature}</p>
              </Card>
            ))}
          </div>
        </section>

        {/* Supported Chains */}
        <section className="animate-fade-in-up" style={{ animationDelay: "400ms" }}>
          <h2 className="text-xl font-bold mb-4 text-foreground">Supported Chains</h2>
          <div className="flex flex-wrap gap-2">
            {app.chains.map((chain: string) => (
              <Badge
                key={chain}
                className="bg-primary/20 text-primary border-primary/50 px-4 py-2 text-sm transition-all duration-300 hover:bg-primary/30 hover:scale-110"
              >
                {chain}
              </Badge>
            ))}
          </div>
        </section>

        {/* Security */}
        <section className="animate-fade-in-up" style={{ animationDelay: "500ms" }}>
          <Card className="bg-card border-primary/30 rounded-2xl p-6">
            <div className="flex items-start gap-4">
              <Shield className="w-6 h-6 text-primary mt-1 flex-shrink-0" />
              <div>
                <h2 className="text-xl font-bold mb-2 text-foreground">Security</h2>
                <p className="text-muted-foreground leading-relaxed">{app.security}</p>
              </div>
            </div>
          </Card>
        </section>
      </main>
    </div>
  )
}
