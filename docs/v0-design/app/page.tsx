"use client"

import type React from "react"
import Link from "next/link"
import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Search, Home, Compass, Wallet, User, Star } from "lucide-react"

export default function DAppStorePage() {
  const [selectedCategory, setSelectedCategory] = useState("All")

  const categories = ["All", "DeFi", "NFT", "GameFi", "Social", "DAO", "Tools"]

  const featuredApps = [
    {
      id: 1,
      name: "UniSwap",
      tagline: "Leading decentralized exchange",
      chains: ["ETH"],
      image: "/uniswap-defi-exchange.jpg",
    },
    {
      id: 2,
      name: "OpenSea",
      tagline: "World's first and largest NFT marketplace",
      chains: ["ETH", "SOL"],
      image: "/opensea-nft-marketplace.jpg",
    },
    {
      id: 3,
      name: "Axie Infinity",
      tagline: "Play to earn blockchain game",
      chains: ["ETH"],
      image: "/axie-infinity-blockchain-game.jpg",
    },
  ]

  const popularApps = [
    { id: 1, name: "Aave", rating: 4.8, downloads: "2.5M", chains: ["ETH", "SOL"], icon: "/aave-defi-logo.jpg" },
    {
      id: 2,
      name: "Curve Finance",
      rating: 4.7,
      downloads: "1.8M",
      chains: ["ETH"],
      icon: "/curve-finance-logo.png",
    },
    {
      id: 3,
      name: "Rarible",
      rating: 4.6,
      downloads: "1.2M",
      chains: ["ETH", "SOL"],
      icon: "/rarible-nft-logo.jpg",
    },
    { id: 4, name: "PancakeSwap", rating: 4.9, downloads: "3.1M", chains: ["ETH"], icon: "/pancakeswap-defi-logo.jpg" },
    { id: 5, name: "MetaMask", rating: 4.5, downloads: "5.2M", chains: ["ETH"], icon: "/images/metamask.png" },
  ]

  return (
    <div className="min-h-screen bg-black text-white pb-20">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-black/95 backdrop-blur-sm border-b border-primary/20 animate-slide-in-down">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-2 group">
            <img
              src="https://www.di.xyz/images/di-logo.png"
              alt="DI Store Logo"
              className="h-10 w-auto transition-transform duration-300 group-hover:scale-110"
            />
          </Link>
          <Button
            variant="outline"
            className="text-primary border-primary/50 hover:bg-primary/10 hover:border-primary font-mono text-sm bg-transparent transition-all duration-300 hover:scale-105 hover:shadow-[0_0_20px_rgba(212,175,55,0.3)]"
          >
            0x1a2b...3c4d
          </Button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6 space-y-8">
        <div className="relative animate-fade-in">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground w-5 h-5 transition-colors duration-300" />
          <Input
            placeholder="Search dApps, tokens, NFTs..."
            className="pl-12 h-14 bg-card border-muted focus:border-primary rounded-2xl text-foreground placeholder:text-muted-foreground transition-all duration-300 hover:shadow-[0_0_15px_rgba(212,175,55,0.2)]"
          />
        </div>

        {/* Featured Carousel */}
        <section className="animate-fade-in-up">
          <h2 className="text-2xl font-bold mb-4 text-foreground">Featured</h2>
          <div className="flex gap-4 overflow-x-auto pb-4 scrollbar-hide snap-x snap-mandatory">
            {featuredApps.map((app, index) => (
              <Link href={`/app/${app.id}`} key={app.id}>
                <Card
                  className="min-w-[280px] h-[160px] bg-card border-primary/30 rounded-2xl overflow-hidden relative cursor-pointer gold-glow snap-start transition-all duration-500 hover:scale-105"
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <img
                    src={app.image || "/placeholder.svg"}
                    alt={app.name}
                    className="w-full h-full object-cover transition-transform duration-700 hover:scale-110"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-black via-black/50 to-transparent" />
                  <div className="absolute bottom-0 left-0 right-0 p-4 transform transition-transform duration-300 hover:translate-y-[-4px]">
                    <h3 className="text-lg font-bold text-white mb-1">{app.name}</h3>
                    <p className="text-sm text-muted-foreground mb-2">{app.tagline}</p>
                    <div className="flex gap-2">
                      {app.chains.map((chain) => (
                        <Badge key={chain} className="bg-primary/20 text-primary border-primary/50 text-xs">
                          {chain}
                        </Badge>
                      ))}
                    </div>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        </section>

        {/* Category Chips */}
        <section className="animate-fade-in-up" style={{ animationDelay: "200ms" }}>
          <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide">
            {categories.map((category) => (
              <Link href={category === "All" ? "/" : `/category/${category.toLowerCase()}`} key={category}>
                <Button
                  onClick={() => setSelectedCategory(category)}
                  className={`rounded-full px-6 h-10 whitespace-nowrap transition-all duration-300 hover:scale-105 ${
                    selectedCategory === category
                      ? "bg-primary text-black hover:bg-primary/90 font-semibold shadow-[0_0_20px_rgba(212,175,55,0.4)]"
                      : "bg-muted text-foreground hover:bg-muted/80 border border-muted-foreground/20 hover:border-primary/50"
                  }`}
                >
                  {category}
                </Button>
              </Link>
            ))}
          </div>
        </section>

        {/* Popular Apps List */}
        <section className="animate-fade-in-up" style={{ animationDelay: "300ms" }}>
          <h2 className="text-2xl font-bold mb-4 text-foreground">Popular Apps</h2>
          <div className="space-y-3">
            {popularApps.map((app, index) => (
              <Link href={`/app/${app.id}`} key={app.id}>
                <Card
                  className="bg-card border-primary/20 rounded-2xl p-4 hover:border-primary/50 transition-all duration-300 cursor-pointer hover:scale-[1.02] hover:shadow-[0_0_25px_rgba(212,175,55,0.2)]"
                  style={{ animationDelay: `${300 + index * 50}ms` }}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-4 flex-1">
                      <img
                        src={app.icon || "/placeholder.svg"}
                        alt={app.name}
                        className="w-14 h-14 rounded-xl transition-transform duration-300 hover:scale-110 hover:rotate-3"
                      />
                      <div className="flex-1">
                        <h3 className="font-semibold text-lg text-foreground mb-1">{app.name}</h3>
                        <div className="flex items-center gap-3">
                          <div className="flex items-center gap-1">
                            <Star className="w-4 h-4 fill-primary text-primary animate-pulse-subtle" />
                            <span className="text-sm text-primary font-medium">{app.rating}</span>
                          </div>
                          <span className="text-sm text-muted-foreground">{app.downloads} downloads</span>
                          <div className="flex gap-1.5">
                            {app.chains.map((chain) => (
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

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-card/95 backdrop-blur-sm border-t border-primary/20 animate-slide-in-up">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-around py-3">
            <NavButton href="/" icon={<Home className="w-6 h-6" />} label="Home" active />
            <NavButton href="/explore" icon={<Compass className="w-6 h-6" />} label="Explore" />
            <NavButton href="/search" icon={<Search className="w-6 h-6" />} label="Search" />
            <NavButton href="/wallet" icon={<Wallet className="w-6 h-6" />} label="Wallet" />
            <NavButton href="/profile" icon={<User className="w-6 h-6" />} label="Profile" />
          </div>
        </div>
      </nav>
    </div>
  )
}

function NavButton({
  icon,
  label,
  active = false,
  href = "/",
}: { icon: React.ReactNode; label: string; active?: boolean; href?: string }) {
  return (
    <Link href={href} className="flex flex-col items-center gap-1 min-w-[60px] group">
      <div
        className={`${active ? "text-primary" : "text-muted-foreground"} transition-all duration-300 group-hover:text-primary group-hover:scale-110`}
      >
        {icon}
      </div>
      <span
        className={`text-xs ${active ? "text-primary font-medium" : "text-muted-foreground"} transition-colors duration-300 group-hover:text-primary`}
      >
        {label}
      </span>
      {active && <div className="w-1 h-1 rounded-full bg-primary mt-0.5 animate-pulse" />}
    </Link>
  )
}
