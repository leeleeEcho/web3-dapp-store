package com.web3store.di

import android.content.Context
import androidx.work.WorkManager
import com.web3store.download.ApkDownloader
import com.web3store.download.DownloadRepository
import com.web3store.installer.ApkInstaller
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadModule {

    @Provides
    @Singleton
    fun provideApkDownloader(okHttpClient: OkHttpClient): ApkDownloader {
        return ApkDownloader(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideDownloadRepository(
        @ApplicationContext context: Context
    ): DownloadRepository {
        return DownloadRepository(context)
    }

    @Provides
    @Singleton
    fun provideApkInstaller(
        @ApplicationContext context: Context
    ): ApkInstaller {
        return ApkInstaller(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}
