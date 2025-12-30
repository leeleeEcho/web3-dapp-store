// Top-level build file for Web3 DApp Store
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.protobuf") version "0.9.4" apply false
}

buildscript {
    extra.apply {
        set("compileSdk", 34)
        set("minSdk", 26)
        set("targetSdk", 34)
    }
}
