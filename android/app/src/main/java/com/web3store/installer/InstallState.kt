package com.web3store.installer

/**
 * Installation state sealed class
 */
sealed class InstallState {
    object Idle : InstallState()
    object Pending : InstallState()
    data class Installing(val progress: Float = 0f) : InstallState()
    object Success : InstallState()
    data class Failed(val error: InstallError) : InstallState()
}

/**
 * Installation error types
 */
sealed class InstallError {
    abstract val errorMessage: String

    data class UserCancelled(override val errorMessage: String = "Installation cancelled by user") : InstallError()
    data class Incompatible(override val errorMessage: String = "App is incompatible with this device") : InstallError()
    data class InsufficientStorage(override val errorMessage: String = "Not enough storage space") : InstallError()
    data class Blocked(override val errorMessage: String = "Installation blocked by system") : InstallError()
    data class InvalidApk(override val errorMessage: String = "Invalid or corrupted APK") : InstallError()
    data class PermissionDenied(override val errorMessage: String = "Install permission not granted") : InstallError()
    data class Unknown(override val errorMessage: String) : InstallError()

    fun getMessage(): String = errorMessage
}
