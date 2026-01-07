package com.web3store.installer

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * APK installer using PackageInstaller API
 */
@Singleton
class ApkInstaller @Inject constructor(
    @ApplicationContext private val context: Context
) : InstallerReceiver.InstallResultListener {

    companion object {
        private const val TAG = "ApkInstaller"
    }

    private val packageInstaller = context.packageManager.packageInstaller

    // Track install states by package name
    private val _installStates = MutableStateFlow<Map<String, InstallState>>(emptyMap())
    val installStates: StateFlow<Map<String, InstallState>> = _installStates.asStateFlow()

    /**
     * Check if the app has permission to install packages
     */
    fun hasInstallPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    /**
     * Open settings to request install permission
     */
    fun requestInstallPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(intent)
        }
    }

    /**
     * Install an APK file
     */
    suspend fun install(apkFile: File, packageName: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!hasInstallPermission()) {
            updateState(packageName, InstallState.Failed(InstallError.PermissionDenied()))
            return@withContext Result.failure(Exception("Install permission not granted"))
        }

        if (!apkFile.exists()) {
            updateState(packageName, InstallState.Failed(InstallError.InvalidApk("APK file not found")))
            return@withContext Result.failure(Exception("APK file not found"))
        }

        updateState(packageName, InstallState.Pending)

        try {
            installWithSession(apkFile, packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Session install failed, trying legacy install", e)
            try {
                installWithIntent(apkFile)
                Result.success(Unit)
            } catch (e2: Exception) {
                updateState(packageName, InstallState.Failed(InstallError.Unknown(e2.message ?: "Installation failed")))
                Result.failure(e2)
            }
        }
    }

    /**
     * Install using PackageInstaller Session API
     */
    private suspend fun installWithSession(apkFile: File, packageName: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            try {
                // Register listener for result
                InstallerReceiver.registerListener(packageName, object : InstallerReceiver.InstallResultListener {
                    override fun onSuccess(pkg: String) {
                        InstallerReceiver.unregisterListener(pkg)
                        updateState(pkg, InstallState.Success)
                        if (continuation.isActive) {
                            continuation.resume(Result.success(Unit))
                        }
                    }

                    override fun onFailure(pkg: String, error: InstallError) {
                        InstallerReceiver.unregisterListener(pkg)
                        updateState(pkg, InstallState.Failed(error))
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception(error.getMessage())))
                        }
                    }
                })

                // Create session params
                val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                    setAppPackageName(packageName)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setInstallReason(PackageManager.INSTALL_REASON_USER)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
                    }
                }

                // Create session
                val sessionId = packageInstaller.createSession(params)
                val session = packageInstaller.openSession(sessionId)

                updateState(packageName, InstallState.Installing(0f))

                // Write APK to session
                session.openWrite("base.apk", 0, apkFile.length()).use { output ->
                    FileInputStream(apkFile).use { input ->
                        val buffer = ByteArray(65536)
                        var bytesWritten = 0L
                        var bytesRead: Int

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            bytesWritten += bytesRead

                            val progress = bytesWritten.toFloat() / apkFile.length()
                            updateState(packageName, InstallState.Installing(progress))
                        }

                        session.fsync(output)
                    }
                }

                // Create pending intent for result
                val intent = Intent(context, InstallerReceiver::class.java).apply {
                    action = "com.web3store.INSTALL_RESULT"
                    putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, packageName)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    sessionId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                // Commit session
                session.commit(pendingIntent.intentSender)

                Log.d(TAG, "Session committed for $packageName")

            } catch (e: Exception) {
                Log.e(TAG, "Session install failed", e)
                InstallerReceiver.unregisterListener(packageName)
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }

            continuation.invokeOnCancellation {
                InstallerReceiver.unregisterListener(packageName)
            }
        }

    /**
     * Install using legacy Intent method (fallback)
     */
    private fun installWithIntent(apkFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
    }

    /**
     * Check if an app is installed
     */
    fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Get installed app version
     */
    fun getInstalledVersion(packageName: String): String? {
        return try {
            val info = context.packageManager.getPackageInfo(packageName, 0)
            info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * Launch an installed app
     */
    fun launchApp(packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } else {
            false
        }
    }

    override fun onSuccess(packageName: String) {
        updateState(packageName, InstallState.Success)
    }

    override fun onFailure(packageName: String, error: InstallError) {
        updateState(packageName, InstallState.Failed(error))
    }

    private fun updateState(packageName: String, state: InstallState) {
        _installStates.value = _installStates.value.toMutableMap().apply {
            put(packageName, state)
        }
    }
}
