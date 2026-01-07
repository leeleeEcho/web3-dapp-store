package com.web3store.ui.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3store.data.repository.AppRepository
import com.web3store.domain.model.AppDetail
import com.web3store.download.DownloadRepository
import com.web3store.download.DownloadState
import com.web3store.installer.ApkInstaller
import com.web3store.installer.InstallState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private const val TAG = "AppDetailViewModel"

/**
 * Button state for download/install actions
 */
sealed class ActionButtonState {
    object Download : ActionButtonState()
    data class Downloading(val progress: Float) : ActionButtonState()
    object Install : ActionButtonState()
    data class Installing(val progress: Float) : ActionButtonState()
    object Open : ActionButtonState()
    object Update : ActionButtonState()
}

/**
 * UI state for app detail screen
 */
data class AppDetailUiState(
    val isLoading: Boolean = true,
    val appDetail: AppDetail? = null,
    val downloadState: DownloadState = DownloadState.Idle,
    val installState: InstallState = InstallState.Idle,
    val buttonState: ActionButtonState = ActionButtonState.Download,
    val downloadedApkFile: File? = null,
    val needsInstallPermission: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for app detail screen
 */
@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val downloadRepository: DownloadRepository,
    private val apkInstaller: ApkInstaller,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get appId from navigation arguments (supports both String and Long)
    private val appId: Long = savedStateHandle.get<String>("appId")?.toLongOrNull()
        ?: savedStateHandle.get<Long>("appId")
        ?: 0L

    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    init {
        if (appId > 0) {
            loadAppDetail(appId)
        }
    }

    /**
     * Load app detail by string ID
     */
    fun loadAppDetailByStringId(id: String) {
        id.toLongOrNull()?.let { loadAppDetail(it) }
    }

    /**
     * Load app detail from API
     */
    fun loadAppDetail(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            appRepository.getAppById(id).fold(
                onSuccess = { appDetail ->
                    _uiState.update { it.copy(isLoading = false, appDetail = appDetail) }
                    checkAppState(appDetail)
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, error = exception.message ?: "Failed to load")
                    }
                }
            )
        }
    }

    /**
     * Check current app state (installed, downloaded, etc.)
     */
    private fun checkAppState(appDetail: AppDetail) {
        val installedVersion = apkInstaller.getInstalledVersion(appDetail.packageName)
        Log.i(TAG, "checkAppState: packageName=${appDetail.packageName}, installedVersion=$installedVersion, dbVersion=${appDetail.versionName}")

        val buttonState = when {
            installedVersion != null && installedVersion == appDetail.versionName -> {
                Log.i(TAG, "checkAppState: versions match, setting Open state")
                ActionButtonState.Open
            }
            installedVersion != null && installedVersion != appDetail.versionName -> {
                Log.i(TAG, "checkAppState: versions differ, setting Update state")
                ActionButtonState.Update
            }
            downloadRepository.hasDownloadedApk(appDetail.packageName, appDetail.versionName) -> {
                val file = downloadRepository.getDownloadedApk(appDetail.packageName, appDetail.versionName)
                _uiState.update { it.copy(downloadedApkFile = file) }
                Log.i(TAG, "checkAppState: APK downloaded, setting Install state")
                ActionButtonState.Install
            }
            else -> {
                Log.i(TAG, "checkAppState: nothing found, setting Download state")
                ActionButtonState.Download
            }
        }

        Log.i(TAG, "checkAppState: final buttonState=$buttonState")
        _uiState.update { it.copy(buttonState = buttonState) }
    }

    /**
     * Handle primary button click
     */
    fun onPrimaryButtonClick() {
        Log.i(TAG, "onPrimaryButtonClick: buttonState=${_uiState.value.buttonState}")
        when (_uiState.value.buttonState) {
            is ActionButtonState.Download, is ActionButtonState.Update -> startDownload()
            is ActionButtonState.Install -> installApk()
            is ActionButtonState.Open -> openApp()
            is ActionButtonState.Downloading, is ActionButtonState.Installing -> { /* Do nothing */ }
        }
    }

    /**
     * Start downloading the APK
     */
    fun startDownload() {
        val appDetail = _uiState.value.appDetail ?: return
        Log.d(TAG, "startDownload: appId=${appDetail.id}, apkUrl=${appDetail.apkUrl}")

        viewModelScope.launch {
            // Check if already downloaded
            if (downloadRepository.hasDownloadedApk(appDetail.packageName, appDetail.versionName)) {
                val file = downloadRepository.getDownloadedApk(appDetail.packageName, appDetail.versionName)
                _uiState.update {
                    it.copy(
                        downloadState = DownloadState.Completed(file!!),
                        downloadedApkFile = file,
                        buttonState = ActionButtonState.Install
                    )
                }
                return@launch
            }

            // Record download on backend
            appRepository.recordDownload(appDetail.id)

            // Start download
            _uiState.update {
                it.copy(
                    downloadState = DownloadState.Pending,
                    buttonState = ActionButtonState.Downloading(0f)
                )
            }

            val workId = downloadRepository.startDownload(appDetail)
            Log.d(TAG, "Download work enqueued: workId=$workId")

            // Observe download progress
            downloadRepository.observeDownloadState(appDetail.id, workId).collect { state ->
                Log.d(TAG, "Download state changed: $state")
                _uiState.update { uiState ->
                    val buttonState = when (state) {
                        is DownloadState.Downloading -> ActionButtonState.Downloading(state.progress)
                        is DownloadState.Completed -> ActionButtonState.Install
                        is DownloadState.Failed -> ActionButtonState.Download
                        else -> uiState.buttonState
                    }

                    val downloadedFile = if (state is DownloadState.Completed) state.file else null

                    uiState.copy(
                        downloadState = state,
                        buttonState = buttonState,
                        downloadedApkFile = downloadedFile ?: uiState.downloadedApkFile,
                        error = if (state is DownloadState.Failed) state.error.getMessage() else null
                    )
                }
            }
        }
    }

    /**
     * Cancel current download
     */
    fun cancelDownload() {
        val appDetail = _uiState.value.appDetail ?: return
        downloadRepository.cancelDownload(appDetail.id)
        _uiState.update {
            it.copy(
                downloadState = DownloadState.Idle,
                buttonState = ActionButtonState.Download
            )
        }
    }

    /**
     * Install the downloaded APK
     */
    fun installApk() {
        Log.d(TAG, "installApk called")
        val appDetail = _uiState.value.appDetail
        if (appDetail == null) {
            Log.e(TAG, "installApk: appDetail is null")
            return
        }

        val apkFile = _uiState.value.downloadedApkFile
        if (apkFile == null) {
            Log.e(TAG, "installApk: downloadedApkFile is null")
            // Try to get the downloaded file from cache
            val cachedFile = downloadRepository.getDownloadedApk(appDetail.packageName, appDetail.versionName)
            if (cachedFile != null && cachedFile.exists()) {
                Log.d(TAG, "installApk: Found cached file: ${cachedFile.absolutePath}")
                _uiState.update { it.copy(downloadedApkFile = cachedFile) }
            } else {
                Log.e(TAG, "installApk: No cached file found either, need to download first")
                return
            }
        } else {
            Log.d(TAG, "installApk: apkFile=${apkFile.absolutePath}, exists=${apkFile.exists()}")
        }

        val fileToInstall = _uiState.value.downloadedApkFile ?: return
        Log.d(TAG, "installApk: Installing file: ${fileToInstall.absolutePath}")

        // Check install permission
        val hasPermission = apkInstaller.hasInstallPermission()
        Log.d(TAG, "installApk: hasInstallPermission=$hasPermission")
        if (!hasPermission) {
            _uiState.update { it.copy(needsInstallPermission = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    installState = InstallState.Pending,
                    buttonState = ActionButtonState.Installing(0f)
                )
            }

            Log.d(TAG, "installApk: Starting install...")
            apkInstaller.install(fileToInstall, appDetail.packageName).fold(
                onSuccess = {
                    Log.d(TAG, "installApk: Install success")
                    _uiState.update {
                        it.copy(
                            installState = InstallState.Success,
                            buttonState = ActionButtonState.Open
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "installApk: Install failed: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            installState = InstallState.Failed(
                                com.web3store.installer.InstallError.Unknown(error.message ?: "Installation failed")
                            ),
                            buttonState = ActionButtonState.Install,
                            error = error.message
                        )
                    }
                }
            )
        }

        // Observe install state
        viewModelScope.launch {
            apkInstaller.installStates.collect { states ->
                val state = states[appDetail.packageName] ?: return@collect

                _uiState.update { uiState ->
                    val buttonState = when (state) {
                        is InstallState.Installing -> ActionButtonState.Installing(state.progress)
                        is InstallState.Success -> ActionButtonState.Open
                        is InstallState.Failed -> ActionButtonState.Install
                        else -> uiState.buttonState
                    }

                    uiState.copy(
                        installState = state,
                        buttonState = buttonState,
                        error = if (state is InstallState.Failed) state.error.getMessage() else null
                    )
                }
            }
        }
    }

    /**
     * Request install permission
     */
    fun requestInstallPermission(activity: Activity) {
        apkInstaller.requestInstallPermission(activity)
    }

    /**
     * Called when returning from permission settings
     */
    fun onPermissionResult() {
        _uiState.update { it.copy(needsInstallPermission = false) }

        // If permission was granted, try install again
        if (apkInstaller.hasInstallPermission()) {
            installApk()
        }
    }

    /**
     * Open the installed app
     */
    fun openApp() {
        Log.i(TAG, "openApp called, buttonState=${_uiState.value.buttonState}")
        val appDetail = _uiState.value.appDetail
        if (appDetail == null) {
            Log.e(TAG, "openApp: appDetail is null")
            return
        }
        Log.i(TAG, "openApp: launching ${appDetail.packageName}")
        val result = apkInstaller.launchApp(appDetail.packageName)
        Log.i(TAG, "openApp: result=$result")
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // Legacy properties for backward compatibility
    val isDownloading: Boolean
        get() = _uiState.value.downloadState is DownloadState.Downloading

    val downloadProgress: Float
        get() = when (val state = _uiState.value.downloadState) {
            is DownloadState.Downloading -> state.progress
            is DownloadState.Completed -> 1f
            else -> 0f
        }
}
