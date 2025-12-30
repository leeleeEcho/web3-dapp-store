package com.web3store.ui.theme

import androidx.compose.ui.graphics.Color

// DI Brand - Black & Gold Theme
object DIColors {
    // Background colors
    val Background = Color(0xFF000000)
    val Surface = Color(0xFF0A0A0A)
    val Card = Color(0xFF0D0D0D)
    val CardElevated = Color(0xFF141414)

    // Gold accent colors
    val Primary = Color(0xFFD4AF37)      // Classic gold
    val Secondary = Color(0xFFF5A623)    // Bright gold
    val PrimaryDark = Color(0xFFB8860B)  // Dark gold

    // Text colors
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA0A0A0)
    val TextTertiary = Color(0xFF666666)

    // Border colors
    val Border = Color(0xFF1A1A1A)
    val BorderGold = Color(0xFFD4AF37)

    // Muted colors
    val Muted = Color(0xFF1A1A1A)
    val MutedForeground = Color(0xFFA0A0A0)

    // Status colors
    val Success = Color(0xFF4ADE80)
    val Warning = Color(0xFFFBBF24)
    val Error = Color(0xFFEF4444)

    // Chain colors
    val ChainEthereum = Color(0xFF627EEA)
    val ChainSolana = Color(0xFF14F195)
    val ChainBSC = Color(0xFFF0B90B)
    val ChainPolygon = Color(0xFF8247E5)
    val ChainArbitrum = Color(0xFF2D374B)
    val ChainOptimism = Color(0xFFFF0420)
    val ChainAvalanche = Color(0xFFE84142)

    // Category colors
    val CategoryDeFi = Color(0xFFD4AF37)
    val CategoryNFT = Color(0xFFF5A623)
    val CategoryGameFi = Color(0xFF9945FF)
    val CategorySocial = Color(0xFF14F195)
    val CategoryDAO = Color(0xFFFF6B9D)
    val CategoryTools = Color(0xFF00D4FF)
}

// Gold gradient for buttons and highlights
val GoldGradient = listOf(
    Color(0xFFD4AF37),
    Color(0xFFF5A623),
    Color(0xFFB8860B)
)
