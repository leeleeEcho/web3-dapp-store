"use client"

import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Star, TrendingUp } from "lucide-react"
import { use } from "react"

const categoryData: Record<string, any> = {
  defi: {
    name: "DeFi",
    description: "Decentralized Finance Applications",
    color: "#d4af37",
    apps: [
      {
        id: 1,
        name: "Aave",
        rating: 4.8,
        downloads: "2.5M",
        chains: ["ETH", "SOL"],
        icon: "/aave-defi-logo.jpg",
        description: "Lending and borrowing platform",
      },
      {
        id: 2,
        name: "Curve Finance",
        rating: 4.7,
        downloads: "1.8M",
        chains: ["ETH"],
        icon: "/curve-finance-logo.png",
        description: "Stablecoin exchange",
      },
      {
        id: 4,
        name: "PancakeSwap",
        rating: 4.9,
        downloads: "3.1M",
        chains: ["ETH"],
        icon: "/pancakeswap-defi-logo.jpg",
        description: "AMM DEX on BNB Chain",
      },
      {
        id: 1,
        name: "UniSwap",
        rating: 4.9,
        downloads: "5.2M",
        chains: ["ETH"],
        icon: "/uniswap-defi-exchange.jpg",
        description: "Leading decentralized exchange",
      },
    ],
  },
  nft: {
    name: "NFT",
    description: "NFT Marketplaces & Collections",
    color: "#f5a623",
    apps: [
      {
        id: 2,
        name: "OpenSea",
        rating: 4.6,
        downloads: "3.2M",
        chains: ["ETH", "SOL"],
        icon: "/opensea-nft-marketplace.jpg",
        description: "World's largest NFT marketplace",
      },
      {
        id: 3,
        name: "Rarible",
        rating: 4.6,
        downloads: "1.2M",
        chains: ["ETH", "SOL"],
        icon: "/rarible-nft-logo.jpg",
        description: "Community-owned NFT marketplace",
      },
    ],
  },
  gamefi: {
    name: "GameFi",
    description: "Blockchain Gaming & Play-to-Earn",
    color: "#9945ff",
    apps: [
      {
        id: 3,
        name: "Axie Infinity",
        rating: 4.7,
        downloads: "2.8M",
        chains: ["ETH"],
        icon: "/axie-infinity-blockchain-game.jpg",
        description: "Play to earn blockchain game",
      },
    ],
  },
  social: {
    name: "Social",
    description: "Web3 Social Networks",
    color: "#14f195",
    apps: [
      {
        id: 10,
        name: "Lens Protocol",
        rating: 4.5,
        downloads: "800K",
        chains: ["ETH"],
        icon: "/lens-protocol-logo.png",
        description: "Decentralized social graph",
      },
    ],
  },
  dao: {
    name: "DAO",
    description: "Decentralized Autonomous Organizations",
    color: "#ff6b9d",
    apps: [
      {
        id: 11,
        name: "Snapshot",
        rating: 4.7,
        downloads: "1.5M",
        chains: ["ETH"],
        icon: "/snapshot-dao-logo.jpg",
        description: "Off-chain voting platform",
      },
    ],
  },
  tools: {
    name: "Tools",
    description: "Web3 Developer Tools & Utilities",
    color: "#00d4ff",
    apps: [
      {
        id: 5,
        name: "MetaMask",
        rating: 4.5,
        downloads: "5.2M",
        chains: ["ETH"],
        icon: "/images/metamask.png",
        description: "Cryptocurrency wallet & gateway",
      },
    ],
  },
}

export default function CategoryPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params)
  const category = categoryData[slug] || categoryData.defi

  return (
    <div className="min-h-screen bg-black text-white pb-20">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-black/95 backdrop-blur-sm border-b border-primary/20 animate-slide-in-down">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center gap-4">
          <Link href="/">
            <Button
              variant="ghost"
              size="icon"
              className="text-primary hover:bg-primary/10 transition-all duration-300 hover:scale-110"
            >
              <ArrowLeft className="w-6 h-6" />
            </Button>
          </Link>
          <div>
            <h1 className="text-2xl font-bold" style={{ color: category.color }}>
              {category.name}
            </h1>
            <p className="text-sm text-muted-foreground">{category.description}</p>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6 space-y-6">
        {/* Featured in Category */}
        <section className="animate-fade-in-up">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp className="w-5 h-5 text-primary" />
            <h2 className="text-xl font-bold text-foreground">Trending in {category.name}</h2>
          </div>

          <div className="space-y-3">
            {category.apps.map((app: any, index: number) => (
              <Link href={`/app/${app.id}`} key={app.id}>
                <Card
                  className="bg-card border-primary/20 rounded-2xl p-4 hover:border-primary/50 transition-all duration-300 cursor-pointer hover:scale-[1.02] hover:shadow-[0_0_25px_rgba(212,175,55,0.2)] animate-fade-in-up"
                  style={{ animationDelay: `${index * 80}ms` }}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4 flex-1">
                      <img
                        src={app.icon || "/placeholder.svg"}
                        alt={app.name}
                        className="w-16 h-16 rounded-xl transition-transform duration-300 hover:scale-110 hover:rotate-3"
                      />
                      <div className="flex-1">
                        <h3 className="font-semibold text-lg text-foreground mb-1">{app.name}</h3>
                        <p className="text-sm text-muted-foreground mb-2">{app.description}</p>
                        <div className="flex items-center gap-3">
                          <div className="flex items-center gap-1">
                            <Star className="w-4 h-4 fill-primary text-primary" />
                            <span className="text-sm text-primary font-medium">{app.rating}</span>
                          </div>
                          <span className="text-sm text-muted-foreground">{app.downloads} downloads</span>
                          <div className="flex gap-1.5">
                            {app.chains.map((chain: string) => (
                              <Badge key={chain} variant="outline" className="border-primary/30 text-primary text-xs">
                                {chain}
                              </Badge>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                    <Button className="bg-primary text-black hover:bg-secondary font-semibold px-6 rounded-xl h-10 transition-all duration-300 hover:scale-110 hover:shadow-[0_0_25px_rgba(212,175,55,0.5)]">
                      GET
                    </Button>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        </section>
      </main>
    </div>
  )
}
