package com.web3store.installer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log

/**
 * BroadcastReceiver for handling PackageInstaller callbacks
 */
class InstallerReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "InstallerReceiver"

        // Listeners for install results
        private val listeners = mutableMapOf<String, InstallResultListener>()

        fun registerListener(packageName: String, listener: InstallResultListener) {
            listeners[packageName] = listener
        }

        fun unregisterListener(packageName: String) {
            listeners.remove(packageName)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        Log.d(TAG, "Install result for $packageName: status=$status, message=$message")

        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                Log.i(TAG, "Installation successful for $packageName")
                packageName?.let { listeners[it]?.onSuccess(it) }
            }

            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // User needs to confirm installation
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirmIntent != null) {
                    Log.d(TAG, "User action required for $packageName")
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmIntent)
                }
            }

            PackageInstaller.STATUS_FAILURE -> {
                Log.e(TAG, "Installation failed for $packageName: $message")
                packageName?.let {
                    listeners[it]?.onFailure(it, InstallError.Unknown(message ?: "Installation failed"))
                }
            }

            PackageInstaller.STATUS_FAILURE_BLOCKED -> {
                Log.e(TAG, "Installation blocked for $packageName")
                packageName?.let {
                    listeners[it]?.onFailure(it, InstallError.Blocked())
                }
            }

            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                Log.w(TAG, "Installation cancelled for $packageName")
                packageName?.let {
                    listeners[it]?.onFailure(it, InstallError.UserCancelled())
                }
            }

            PackageInstaller.STATUS_FAILURE_INVALID -> {
                Log.e(TAG, "Invalid APK for $packageName")
                packageName?.let {
                    listeners[it]?.onFailure(it, InstallError.InvalidApk())
                }
            }

            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> {
                Log.e(TAG, "Incompatible APK for $packageName")
                packageName?.let {
                    listeners[it]?.onFailure(it, InstallError.Incompatible())
                }
            }

            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                Log.e(TAG, "Insufficient storage for $packageName")
                packageName?.let {
                    listeners[it]?.onFailure(it, InstallError.InsufficientStorage())
                }
            }

            else -> {
                Log.e(TAG, "Unknown status $status for $packageName")
                packageName?.let {
                    listeners[it]?.onFailure(it, InstallError.Unknown("Unknown error: $status"))
                }
            }
        }
    }

    /**
     * Listener interface for installation results
     */
    interface InstallResultListener {
        fun onSuccess(packageName: String)
        fun onFailure(packageName: String, error: InstallError)
    }
}
