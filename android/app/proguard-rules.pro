# Add project specific ProGuard rules here.

# Keep Web3 related classes
-keep class org.web3j.** { *; }
-keep class com.walletconnect.** { *; }

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Crypto
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
