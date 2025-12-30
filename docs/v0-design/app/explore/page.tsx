"use client"

import type React from "react"

import Link from "next/link"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Compass, Home, Search, Wallet, User, TrendingUp, Flame, Sparkles } from "lucide-react"

export default function ExplorePage() {
  const trendingCategories = [
    { name: "DeFi", count: 234, color: "from-primary to-secondary", icon: <TrendingUp className="w-6 h-6" /> },
    { name: "NFT", count: 189, color: "from-secondary to-primary", icon: <Sparkles className="w-6 h-6" /> },
    { name: "GameFi", count: 156, color: "from-primary/80 to-secondary/80", icon: <Flame className="w-6 h-6" /> },
  ]

  const newApps = [
    { id: 101, name: "SushiSwap", category: "DeFi", rating: 4.6, icon: "/placeholder.svg?height=80&width=80" },
    { id: 102, name: "Decentraland", category: "GameFi", rating: 4.4, icon: "/placeholder.svg?height=80&width=80" },
    { id: 103, name: "SuperRare", category: "NFT", rating: 4.7, icon: "/placeholder.svg?height=80&width=80" },
  ]

  return (
    <div className="min-h-screen bg-black text-white pb-20">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-black/95 backdrop-blur-sm border-b border-primary/20 animate-slide-in-down">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
            Explore dApps
          </h1>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-6 space-y-8">
        {/* Trending Categories */}
        <section className="animate-fade-in-up">
          <h2 className="text-xl font-bold mb-4 text-foreground">Trending Categories</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {trendingCategories.map((category, index) => (
              <Link href={`/category/${category.name.toLowerCase()}`} key={category.name}>
                <Card
                  className="bg-card border-primary/30 rounded-2xl p-6 cursor-pointer transition-all duration-300 hover:scale-105 hover:shadow-[0_0_30px_rgba(212,175,55,0.3)] animate-fade-in-up"
                  style={{ animationDelay: `${index * 100}ms` }}
                >
                  <div
                    className={`w-12 h-12 rounded-xl bg-gradient-to-br ${category.color} flex items-center justify-center mb-4`}
                  >
                    {category.icon}
                  </div>
                  <h3 className="text-lg font-bold text-foreground mb-1">{category.name}</h3>
                  <p className="text-sm text-muted-foreground">{category.count} apps</p>
                </Card>
              </Link>
            ))}
          </div>
        </section>

        {/* New & Noteworthy */}
        <section className="animate-fade-in-up" style={{ animationDelay: "300ms" }}>
          <h2 className="text-xl font-bold mb-4 text-foreground">New & Noteworthy</h2>
          <div className="space-y-3">
            {newApps.map((app, index) => (
              <Link href={`/app/${app.id}`} key={app.id}>
                <Card
                  className="bg-card border-primary/20 rounded-2xl p-4 hover:border-primary/50 transition-all duration-300 cursor-pointer hover:scale-[1.02] animate-fade-in-up"
                  style={{ animationDelay: `${300 + index * 80}ms` }}
                >
                  <div className="flex items-center gap-4">
                    <img
                      src={app.icon || "/placeholder.svg"}
                      alt={app.name}
                      className="w-14 h-14 rounded-xl transition-transform duration-300 hover:scale-110"
                    />
                    <div className="flex-1">
                      <h3 className="font-semibold text-lg text-foreground">{app.name}</h3>
                      <p className="text-sm text-muted-foreground">{app.category}</p>
                    </div>
                    <Badge className="bg-primary/20 text-primary border-primary/50">⭐ {app.rating}</Badge>
                  </div>
                </Card>
              </Link>
            ))}
          </div>
        </section>
      </main>

      {/* Bottom Navigation */}
      <nav className="fixed bottom-0 left-0 right-0 bg-card/95 backdrop-blur-sm border-t border-primary/20">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-around py-3">
            <NavButton href="/" icon={<Home className="w-6 h-6" />} label="Home" />
            <NavButton href="/explore" icon={<Compass className="w-6 h-6" />} label="Explore" active />
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
