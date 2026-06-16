/*
 * Minimalist Focus Launcher
 * Copyright (C) 2025 A-Akhil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.minifocus.launcher

import android.Manifest
import android.app.AlarmManager
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.provider.AlarmClock
import android.provider.Settings
import android.content.ComponentName
import android.widget.Toast
import android.service.notification.NotificationListenerService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableStateFlow
import com.minifocus.launcher.R
import com.minifocus.launcher.ui.LauncherApp
import com.minifocus.launcher.ui.PermissionScreen
import com.minifocus.launcher.ui.theme.MinimalistFocusTheme
import com.minifocus.launcher.viewmodel.LauncherViewModel
import com.minifocus.launcher.viewmodel.LauncherViewModelFactory
import com.minifocus.launcher.viewmodel.NotificationFilterViewModel
import com.minifocus.launcher.viewmodel.NotificationFilterViewModelFactory
import com.minifocus.launcher.viewmodel.NotificationInboxViewModel
import com.minifocus.launcher.viewmodel.NotificationInboxViewModelFactory
import com.minifocus.launcher.permissions.PermissionsState
import com.minifocus.launcher.permissions.PermissionsEvaluator
import com.minifocus.launcher.permissions.NotificationInboxListenerService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import com.minifocus.launcher.manager.SettingsManager
import com.minifocus.launcher.service.LockScreenAccessibilityService
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.graphics.Color as AndroidColor
import com.minifocus.launcher.model.LauncherTheme

class MainActivity : ComponentActivity() {

    private fun updateSystemBars(theme: LauncherTheme) {
        val isLight = theme == LauncherTheme.LIGHT
        enableEdgeToEdge(
            statusBarStyle = if (isLight) {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            },
            navigationBarStyle = if (isLight) {
                SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
            } else {
                SystemBarStyle.dark(AndroidColor.TRANSPARENT)
            }
        )
    }

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                com.minifocus.launcher.service.AppTimeReminderReceiver.forceResetActiveSession(this@MainActivity)
            }
        }
    }

    private val settingsManager: SettingsManager by lazy {
        val app = application as LauncherApplication
        app.container.settingsManager
    }

    private val requestHomeRoleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // No-op; user choice handled by system UI.
    }

    private val requestPostNotificationsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        updatePermissionsState()
    }

    private val notificationAccessLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updatePermissionsState()
    }

    private val accessibilitySettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updatePermissionsState()
    }

    private val exactAlarmLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updatePermissionsState()
    }

    private val usageStatsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updatePermissionsState()
    }

    private val overlayLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updatePermissionsState()
    }

    private val exportBackupLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.backupSettings(uri, contentResolver)
        }
    }

    private val importBackupLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.restoreSettings(uri, contentResolver)
        }
    }

    private val permissionsState = MutableStateFlow(
        PermissionsState(
            notificationsGranted = false,
            notificationListenerGranted = false,
            lockAccessibilityGranted = false,
            exactAlarmsGranted = false,
            usageStatsGranted = false,
            overlayGranted = false
        )
    )

    private val permissionManagerVisible = MutableStateFlow(false)
    private val allSetScreenVisible = MutableStateFlow(false)

    private val notificationRestrictionHint = MutableStateFlow(false)
    private var notificationAccessRequested = false

    private val viewModel: LauncherViewModel by viewModels {
        val app = application as LauncherApplication
        LauncherViewModelFactory(
            appsManager = app.container.appsManager,
            tasksManager = app.container.tasksManager,
            dailyTasksManager = app.container.dailyTasksManager,
            hiddenAppsManager = app.container.hiddenAppsManager,
            lockManager = app.container.lockManager,
            settingsManager = app.container.settingsManager,
            settingsBackupManager = app.container.settingsBackupManager,
            searchManager = app.container.searchManager,
            appUsageStatsManager = app.container.appUsageStatsManager,
            appTimeReminderManager = app.container.appTimeReminderManager,
            calendarManager = app.container.calendarManager
        )
    }

    private val notificationInboxViewModel: NotificationInboxViewModel by viewModels {
        val app = application as LauncherApplication
        NotificationInboxViewModelFactory(
            inboxManager = app.container.notificationInboxManager,
            settingsManager = app.container.settingsManager
        )
    }

    private val notificationFilterViewModel: NotificationFilterViewModel by viewModels {
        val app = application as LauncherApplication
        NotificationFilterViewModelFactory(
            notificationInboxManager = app.container.notificationInboxManager,
            appsManager = app.container.appsManager
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initial edge-to-edge with AMOLED default; will be updated by LaunchedEffect
        updateSystemBars(LauncherTheme.AMOLED)
        super.onCreate(savedInstanceState)
        
        val initialPermissionsAcknowledged = runBlocking {
            settingsManager.getPermissionOnboardingAcknowledged()
        }
        val initialOnboardingComplete = runBlocking {
            settingsManager.getOnboardingComplete()
        }

        // Enforce default launcher requirement - app cannot be used otherwise
        if (!isDefaultLauncher()) {
            showDefaultLauncherPrompt()
            return
        }

        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
        
        notificationRestrictionHint.value = !isFromTrustedStore()
        updatePermissionsState()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            
            // Update system bars when theme changes
            LaunchedEffect(state.theme) {
                updateSystemBars(state.theme)
            }
            
            MinimalistFocusTheme(theme = state.theme) {
                val inboxState by notificationInboxViewModel.uiState.collectAsStateWithLifecycle()
                val filterState by notificationFilterViewModel.uiState.collectAsStateWithLifecycle()
                val permissions by permissionsState.collectAsStateWithLifecycle()
                val manualPermissionManagerVisible by permissionManagerVisible.collectAsStateWithLifecycle()
                val isAllSetScreenVisible by allSetScreenVisible.collectAsStateWithLifecycle()
                val permissionsAcknowledged by settingsManager
                    .observePermissionOnboardingAcknowledged()
                    .collectAsStateWithLifecycle(initialPermissionsAcknowledged)
                val onboardingComplete by settingsManager
                    .observeOnboardingComplete()
                    .collectAsStateWithLifecycle(initialOnboardingComplete)
                val restrictedHint by notificationRestrictionHint.collectAsStateWithLifecycle()
                
                val showPermissionScreen = manualPermissionManagerVisible || (!permissionsAcknowledged && !isAllSetScreenVisible)

                LaunchedEffect(permissions.requiredGranted) {
                    if (!permissions.requiredGranted) {
                        settingsManager.setPermissionOnboardingAcknowledged(false)
                    }
                }

                if (!onboardingComplete) {
                    // First-launch: render real LauncherApp with onboarding overlay
                    LauncherApp(
                        state = state,
                        notificationInboxState = inboxState,
                        notificationFilterState = filterState,
                        permissionsState = permissions,
                        onToggleTask = viewModel::toggleTask,
                        onAddTask = viewModel::addTask,
                        onDeleteTask = viewModel::deleteTask,
                        onAddDailyTask = viewModel::addDailyTask,
                        onUpdateDailyTask = viewModel::updateDailyTask,
                        onDeleteDailyTask = viewModel::deleteDailyTask,
                        onDailyTaskEnabledChange = viewModel::setDailyTaskEnabled,
                        onDailyTaskCompleted = viewModel::markDailyTaskCompleted,
                        onDailyTaskReset = viewModel::resetDailyTaskForToday,
                        onPinApp = viewModel::pinApp,
                        onUnpinApp = viewModel::unpinApp,
                        onHideApp = viewModel::hideApp,
                        onUnhideApp = viewModel::unhideApp,
                        onLockApp = ::handleLockRequest,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSearchVisibilityChange = viewModel::setSearchVisibility,
                        onBottomIconChange = viewModel::setBottomIcon,
                        onSettingsVisibilityChange = viewModel::setSettingsVisibility,
                        onHomeSettingsVisibilityChange = viewModel::setHomeSettingsVisibility,
                        onClockSettingsVisibilityChange = viewModel::setClockSettingsVisibility,
                        onAppDrawerSettingsVisibilityChange = viewModel::setAppDrawerSettingsVisibility,
                        onAppearanceSettingsVisibilityChange = viewModel::setAppearanceSettingsVisibility,
                        onTextSizeSettingsVisibilityChange = viewModel::setTextSizeSettingsVisibility,
                        onHiddenAppsVisibilityChange = viewModel::setHiddenAppsVisibility,
                        onHistoryVisibilityChange = viewModel::setHistoryVisibility,
                        onAboutVisibilityChange = viewModel::setAboutVisibility,
                        onEmergencyUnlockVisibilityChange = viewModel::setEmergencyUnlockVisibility,
                        onClockFormatChange = viewModel::setClockFormat,
                        onTextSizeChange = viewModel::setTextSize,
                        onKeyboardSearchOnSwipeChange = viewModel::setKeyboardSearchOnSwipe,
                        onSmartSuggestionsToggle = viewModel::setSmartSuggestionsEnabled,
                        onResetSmartSuggestions = viewModel::resetSmartSuggestions,
                        onShowSecondsChange = viewModel::setShowSeconds,
                        onShowDailyTasksOnHomeChange = viewModel::setShowDailyTasksOnHome,
                        onDoubleTapLockScreenChange = viewModel::setDoubleTapLockScreen,
                        onNotificationInboxEnabledChange = ::handleNotificationInboxToggle,
                        onNotificationInboxVisibilityChange = viewModel::setNotificationInboxVisibility,
                        onNotificationSettingsVisibilityChange = viewModel::setNotificationSettingsVisibility,
                        onLogViewerVisibilityChange = viewModel::setLogViewerVisibility,
                        onNotificationFilterVisibilityChange = viewModel::setNotificationFilterVisibility,
                        onNotificationRetentionSelected = notificationInboxViewModel::setNotificationRetentionDays,
                        onLogRetentionSelected = notificationInboxViewModel::setLogRetentionDays,
                        onNotificationDelete = notificationInboxViewModel::deleteNotification,
                        onNotificationMarkAllRead = notificationInboxViewModel::markAllAsRead,
                        onNotificationFilterQueryChange = notificationFilterViewModel::updateSearchQuery,
                        onNotificationFilterToggle = notificationFilterViewModel::toggle,
                        onNotificationFilterToggleAll = notificationFilterViewModel::setAll,
                        onOpenPermissionManager = {
                            updatePermissionsState()
                            permissionManagerVisible.value = true
                        },
                        onOpenDeviceSettings = ::openDeviceSettings,
                        onRequestLockAccessibility = ::requestLockAccessibility,
                        onLockDevice = ::lockDevice,
                        onRecordAppUsage = viewModel::recordAppUsage,
                        canLaunch = viewModel::canLaunch,
                        onLaunchApp = { packageName -> launchPackage(packageName) },
                        onOpenClock = { openClockApp() },
                        lockManager = (application as LauncherApplication).container.lockManager,
                        onBackupSettingsVisibilityChange = viewModel::setBackupSettingsVisibility,
                        onBackupSettings = {
                            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                            exportBackupLauncher.launch("minilauncher_backup_$timestamp.json")
                        },
                        onRestoreSettings = {
                            importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        onAppTimeReminderSettingsVisibilityChange = viewModel::setAppTimeReminderSettingsVisibility,
                        onAddTrackedReminderApp = viewModel::addTrackedReminderApp,
                        onRemoveTrackedReminderApp = viewModel::removeTrackedReminderApp,
                        onSetPendingTimeIntention = viewModel::setPendingTimeIntention,
                        onUpdateExpiryAction = viewModel::updateExpiryAction,
                        onThemeChange = viewModel::setTheme,
                        onLanguageSettingsVisibilityChange = viewModel::setLanguageSettingsVisibility,
                        onCalendarSettingsVisibilityChange = viewModel::setCalendarSettingsVisibility,
                        onSelectCalendar = viewModel::setSelectedCalendar,
                        onToggleShowTasksInCalendar = viewModel::setShowTasksInCalendar,
                        onToggleSyncTasksWithCalendar = viewModel::setSyncTasksWithCalendar,
                        onToggleSyncTasksWithDate = viewModel::setSyncTasksWithDate,
                        onToggleSyncDailyReminders = viewModel::setSyncDailyReminders,
                        isOnboarding = true,
                        onOnboardingComplete = {
                            lifecycleScope.launch {
                                settingsManager.setOnboardingComplete(true)
                            }
                        },
                    )
                } else if (isAllSetScreenVisible) {
                    com.minifocus.launcher.ui.AllSetScreen(
                        onBegin = {
                            lifecycleScope.launch {
                                settingsManager.setPermissionOnboardingAcknowledged(true)
                                allSetScreenVisible.value = false
                            }
                        }
                    )
                } else if (!showPermissionScreen) {
                    LauncherApp(
                        state = state,
                        notificationInboxState = inboxState,
                        notificationFilterState = filterState,
                        permissionsState = permissions,
                        onToggleTask = viewModel::toggleTask,
                        onAddTask = viewModel::addTask,
                        onDeleteTask = viewModel::deleteTask,
                        onAddDailyTask = viewModel::addDailyTask,
                        onUpdateDailyTask = viewModel::updateDailyTask,
                        onDeleteDailyTask = viewModel::deleteDailyTask,
                        onDailyTaskEnabledChange = viewModel::setDailyTaskEnabled,
                        onDailyTaskCompleted = viewModel::markDailyTaskCompleted,
                        onDailyTaskReset = viewModel::resetDailyTaskForToday,
                        onPinApp = viewModel::pinApp,
                        onUnpinApp = viewModel::unpinApp,
                        onHideApp = viewModel::hideApp,
                        onUnhideApp = viewModel::unhideApp,
                        onLockApp = ::handleLockRequest,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSearchVisibilityChange = viewModel::setSearchVisibility,
                        onBottomIconChange = viewModel::setBottomIcon,
                        onSettingsVisibilityChange = viewModel::setSettingsVisibility,
                        onHomeSettingsVisibilityChange = viewModel::setHomeSettingsVisibility,
                        onClockSettingsVisibilityChange = viewModel::setClockSettingsVisibility,
                        onAppDrawerSettingsVisibilityChange = viewModel::setAppDrawerSettingsVisibility,
                        onAppearanceSettingsVisibilityChange = viewModel::setAppearanceSettingsVisibility,
                        onTextSizeSettingsVisibilityChange = viewModel::setTextSizeSettingsVisibility,
                        onHiddenAppsVisibilityChange = viewModel::setHiddenAppsVisibility,
                        onHistoryVisibilityChange = viewModel::setHistoryVisibility,
                        onAboutVisibilityChange = viewModel::setAboutVisibility,
                        onEmergencyUnlockVisibilityChange = viewModel::setEmergencyUnlockVisibility,
                        onClockFormatChange = viewModel::setClockFormat,
                        onTextSizeChange = viewModel::setTextSize,
                        onKeyboardSearchOnSwipeChange = viewModel::setKeyboardSearchOnSwipe,
                        onSmartSuggestionsToggle = viewModel::setSmartSuggestionsEnabled,
                        onResetSmartSuggestions = viewModel::resetSmartSuggestions,
                        onShowSecondsChange = viewModel::setShowSeconds,
                        onShowDailyTasksOnHomeChange = viewModel::setShowDailyTasksOnHome,
                        onDoubleTapLockScreenChange = viewModel::setDoubleTapLockScreen,
                        onNotificationInboxEnabledChange = ::handleNotificationInboxToggle,
                        onNotificationInboxVisibilityChange = viewModel::setNotificationInboxVisibility,
                        onNotificationSettingsVisibilityChange = viewModel::setNotificationSettingsVisibility,
                        onLogViewerVisibilityChange = viewModel::setLogViewerVisibility,
                        onNotificationFilterVisibilityChange = viewModel::setNotificationFilterVisibility,
                        onNotificationRetentionSelected = notificationInboxViewModel::setNotificationRetentionDays,
                        onLogRetentionSelected = notificationInboxViewModel::setLogRetentionDays,
                        onNotificationDelete = notificationInboxViewModel::deleteNotification,
                        onNotificationMarkAllRead = notificationInboxViewModel::markAllAsRead,
                        onNotificationFilterQueryChange = notificationFilterViewModel::updateSearchQuery,
                        onNotificationFilterToggle = notificationFilterViewModel::toggle,
                        onNotificationFilterToggleAll = notificationFilterViewModel::setAll,
                        onOpenPermissionManager = {
                            updatePermissionsState()
                            permissionManagerVisible.value = true
                        },
                        onOpenDeviceSettings = ::openDeviceSettings,
                        onRequestLockAccessibility = ::requestLockAccessibility,
                        onLockDevice = ::lockDevice,
                        onRecordAppUsage = viewModel::recordAppUsage,
                        canLaunch = viewModel::canLaunch,
                        onLaunchApp = { packageName -> launchPackage(packageName) },
                        onOpenClock = { openClockApp() },
                        lockManager = (application as LauncherApplication).container.lockManager,
                        onBackupSettingsVisibilityChange = viewModel::setBackupSettingsVisibility,
                        onBackupSettings = {
                            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                            exportBackupLauncher.launch("minilauncher_backup_$timestamp.json")
                        },
                        onRestoreSettings = {
                            importBackupLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        onAppTimeReminderSettingsVisibilityChange = viewModel::setAppTimeReminderSettingsVisibility,
                        onAddTrackedReminderApp = viewModel::addTrackedReminderApp,
                        onRemoveTrackedReminderApp = viewModel::removeTrackedReminderApp,
                        onSetPendingTimeIntention = viewModel::setPendingTimeIntention,
                        onUpdateExpiryAction = viewModel::updateExpiryAction,
                        onThemeChange = viewModel::setTheme,
                        onLanguageSettingsVisibilityChange = viewModel::setLanguageSettingsVisibility,
                        onCalendarSettingsVisibilityChange = viewModel::setCalendarSettingsVisibility,
                        onSelectCalendar = viewModel::setSelectedCalendar,
                        onToggleShowTasksInCalendar = viewModel::setShowTasksInCalendar,
                        onToggleSyncTasksWithCalendar = viewModel::setSyncTasksWithCalendar,
                        onToggleSyncTasksWithDate = viewModel::setSyncTasksWithDate,
                        onToggleSyncDailyReminders = viewModel::setSyncDailyReminders,
                        onResetOnboarding = {
                            lifecycleScope.launch {
                                settingsManager.setOnboardingComplete(false)
                                settingsManager.setPermissionOnboardingAcknowledged(false)
                            }
                        },
                    )
                } else {
                    PermissionScreen(
                        state = permissions,
                        allowDismiss = manualPermissionManagerVisible,
                        onRequestNotifications = ::requestPostNotificationsPermission,
                        onRequestNotificationListener = ::requestNotificationAccess,
                        onRequestExactAlarms = ::requestExactAlarmPermission,
                        onRequestLockAccessibility = ::requestLockAccessibility,
                        onRequestUsageStats = ::requestUsageStatsPermission,
                        onRequestOverlay = ::requestOverlayPermission,
                        showRestrictedNotificationHint = restrictedHint,
                        onOpenRestrictedSettings = ::openRestrictedSettings,
                        onClose = {
                            if (permissionManagerVisible.value) {
                                permissionManagerVisible.value = false
                            }
                        },
                        onContinue = {
                            if (permissionManagerVisible.value) {
                                permissionManagerVisible.value = false
                            } else {
                                allSetScreenVisible.value = true
                            }
                        },
                        onRefreshPermissions = ::updatePermissionsState
                    )
                }
            }
        }

        if (intent?.action == ACTION_OPEN_INBOX) {
            viewModel.setNotificationInboxVisibility(true)
            intent.action = Intent.ACTION_MAIN
        }
    }

    // TODO: Replace these manual permissionManagerVisible resets with a single navigation
    // owner once the permission overlay manages lifecycle transitions (home, back,
    // leave-hint) without workarounds in onStop/onUserLeaveHint/onNewIntent.
    override fun onStop() {
        super.onStop()
        if (permissionManagerVisible.value) {
            permissionManagerVisible.value = false
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (permissionManagerVisible.value) {
            permissionManagerVisible.value = false
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    private fun requestPostNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!permissionsState.value.notificationsGranted) {
                requestPostNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            updatePermissionsState()
        }
    }

    private fun requestNotificationAccess() {
        if (permissionsState.value.notificationListenerGranted) {
            updatePermissionsState()
            return
        }
        notificationAccessRequested = true
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        notificationAccessLauncher.launch(intent)
    }

    private fun requestLockAccessibility() {
        if (permissionsState.value.lockAccessibilityGranted) {
            updatePermissionsState()
            return
        }

        val detailIntent = Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").apply {
            setData(Uri.parse("package:$packageName"))
        }
        val targetIntent = if (detailIntent.resolveActivity(packageManager) != null) {
            detailIntent
        } else {
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        }

        accessibilitySettingsLauncher.launch(targetIntent)
    }

    /**
     * Locks the device using accessibility service GLOBAL_ACTION_LOCK_SCREEN.
     *
     * WHY NOT DEVICE ADMIN:
     * DevicePolicyManager.lockNow() was tried first but it disables biometric unlock
     * and forces PIN-only authentication. Accessibility service approach preserves
     * fingerprint/face unlock just like the hardware power button.
     */
    private fun lockDevice() {
        val locked = LockScreenAccessibilityService.lockDevice()
        if (!locked) {
            Toast.makeText(
                this,
                getString(R.string.lock_accessibility_service_toast),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            updatePermissionsState()
            return
        }

        if (permissionsState.value.exactAlarmsGranted) {
            updatePermissionsState()
            return
        }

        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        if (intent.resolveActivity(packageManager) != null) {
            exactAlarmLauncher.launch(intent)
        } else {
            openRestrictedSettings()
        }
    }

    private fun requestUsageStatsPermission() {
        if (permissionsState.value.usageStatsGranted) {
            updatePermissionsState()
            return
        }
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        usageStatsLauncher.launch(intent)
    }

    private fun requestOverlayPermission() {
        if (permissionsState.value.overlayGranted) {
            updatePermissionsState()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayLauncher.launch(intent)
        }
    }

    private fun updatePermissionsState() {
        val state = PermissionsEvaluator.currentState(this)
        permissionsState.value = state
        // android.util.Log.d(
        //     "PermissionsState",
    //     "notifications=${state.notificationsGranted}, listener=${state.notificationListenerGranted}, " +
    //         "lockAccessibility=${state.lockAccessibilityGranted}, exact=${state.exactAlarmsGranted}, usage=${state.usageStatsGranted}, overlay=${state.overlayGranted}"
        // )
        if (notificationAccessRequested && !state.notificationListenerGranted) {
            notificationRestrictionHint.value = !isFromTrustedStore()
        } else if (state.notificationListenerGranted) {
            notificationAccessRequested = false
            notificationRestrictionHint.value = false
            try {
                NotificationListenerService.requestRebind(
                    ComponentName(this, NotificationInboxListenerService::class.java)
                )
            } catch (_: Exception) {
                // Ignore; system will bind when possible.
            }
        }
        
        if (state.allGranted) {
            startAppLockMonitorService()
        }
    }

    private fun startAppLockMonitorService() {
        try {
            val serviceIntent = Intent(this, com.minifocus.launcher.service.AppLockMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    override fun onResume() {
        super.onResume()
        // Re-check default launcher status to prevent bypass
        // Note: This may cause issues after phone calls - monitor behavior
        if (!isDefaultLauncher()) {
            showDefaultLauncherPrompt()
            return
        }
        updatePermissionsState()

        // Start a 60s grace window when user returns to launcher.
        // If the app is reopened within this window, we keep the same timer session.
        // If not reopened, the session is reset after the grace period.
        val activePkg = com.minifocus.launcher.service.AppTimeReminderReceiver.activeReminderPackage
        if (activePkg != null) {
            com.minifocus.launcher.service.AppTimeReminderReceiver.scheduleGraceReset(this, activePkg)
        }
    }

    private fun openRestrictedSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun handleNotificationInboxToggle(enabled: Boolean) {
        if (enabled) {
            var missingPermission = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionsState.value.notificationsGranted) {
                requestPostNotificationsPermission()
                missingPermission = true
            }
            if (!permissionsState.value.notificationListenerGranted) {
                requestNotificationAccess()
                missingPermission = true
            }
            if (missingPermission) {
                Toast.makeText(this, "Grant notification permissions first", Toast.LENGTH_SHORT).show()
                return
            }
        }
        viewModel.setNotificationInboxEnabled(enabled)
    }

    private fun handleLockRequest(packageName: String, durationMinutes: Long) {
        if (!permissionsState.value.usageStatsGranted) {
            requestUsageStatsPermission()
            Toast.makeText(this, "Grant usage access to lock apps", Toast.LENGTH_SHORT).show()
            return
        }
        if (!permissionsState.value.overlayGranted) {
            requestOverlayPermission()
            Toast.makeText(this, "Grant overlay permission to lock apps", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.lockApp(packageName, durationMinutes)
    }

    private fun openDeviceSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Unable to open device settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isFromTrustedStore(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName == "com.android.vending"
            } else {
                @Suppress("DEPRECATION")
                val installer = packageManager.getInstallerPackageName(packageName)
                installer == "com.android.vending"
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun showDefaultLauncherPrompt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                requestHomeRoleLauncher.launch(intent)
            }
        } else {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
        finish()
    }

    private fun launchPackage(packageName: String) {
        // Check if app is locked first (using runBlocking since we need to check before launching)
        val container = (application as LauncherApplication).container
        val lockInfo = runBlocking { container.lockManager.getLockInfo(packageName) }
        
        if (lockInfo != null && lockInfo.lockedUntil > System.currentTimeMillis()) {
            // App is locked - show overlay instead of launching
            val overlayIntent = Intent(this, com.minifocus.launcher.service.AppLockOverlayActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(com.minifocus.launcher.service.AppLockMonitorService.EXTRA_PACKAGE_NAME, packageName)
                putExtra(com.minifocus.launcher.service.AppLockMonitorService.EXTRA_LOCKED_UNTIL, lockInfo.lockedUntil)
            }
            startActivity(overlayIntent)
            return
        }
        
        // App is not locked, launch normally
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        }
    }

    private fun openClockApp() {
        val pm = packageManager

        // Level 1: The "Official" Intent (Best Method)
        val standardIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (standardIntent.resolveActivity(pm) != null) {
            try {
                startActivity(standardIntent)
                return
            } catch (e: Exception) {
                // Could still fail on some devices
            }
        }

        // Level 2: Hardcoded List (Good Fallback)
        val knownClockPackages = listOf(
            // Google Pixel / AOSP
            "com.google.android.deskclock",
            "com.android.deskclock",

            // Samsung
            "com.samsung.android.app.clockpackage",
            "com.sec.android.app.clockpackage",

            // OnePlus
            "com.oneplus.deskclock",

            // Oppo (ColorOS)
            "com.coloros.alarmclock",

            // Vivo (Funtouch OS / OriginOS)
            "com.vivo.alarmclock",
            "com.bbk.alarmclock",

            // Huawei (EMUI / HarmonyOS)
            "com.huawei.android.deskclock",
            "com.android.util",

            // LG
            "com.lge.clock",
            "com.lge.alarmclock",

            // Motorola
            "com.motorola.blur.alarmclock",
            "com.motorola.timeweatherwidget",

            // Sony
            "com.sonyericsson.organizer",
            "com.sonymobile.tocca.clock",

            // Asus
            "com.asus.deskclock",

            // HTC
            "com.htc.android.worldclock"
        )

        for (pkg in knownClockPackages) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                try {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launchIntent)
                    return
                } catch (e: Exception) {
                    // Ignore and try next package
                }
            }
        }

        // Level 3: The "Best-Guess" Dynamic Search (Last Resort)
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            val appList = pm.queryIntentActivities(mainIntent, 0)

            for (info in appList) {
                val pkgName = info.activityInfo.packageName.lowercase()
                val appLabel = info.loadLabel(pm).toString().lowercase()

                if (pkgName.contains("clock") || appLabel.contains("clock") ||
                    pkgName.contains("alarm") || appLabel.contains("alarm")) {
                    
                    val launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(launchIntent)
                        return
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Level 4: Failed
        Toast.makeText(this, "Could not find clock app", Toast.LENGTH_SHORT).show()
    }

    private fun isDefaultLauncher(): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        val resolveInfo = packageManager.resolveActivity(intent, 0) ?: return false
        return resolveInfo.activityInfo?.packageName == packageName
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        when (intent.action) {
            ACTION_OPEN_INBOX -> {
                viewModel.setNotificationInboxVisibility(true)
            }
            Intent.ACTION_MAIN -> {
                if (intent.hasCategory(Intent.CATEGORY_HOME)) {
                    if (permissionManagerVisible.value) {
                        permissionManagerVisible.value = false
                    }
                    viewModel.resetToHome()
                }
            }
        }
        intent.action = Intent.ACTION_MAIN
    }

    companion object {
        private const val PREFS_NAME = "launcher_preferences"
        private const val KEY_PROMPTED_HOME_ROLE = "prompted_home_role"
        const val ACTION_OPEN_INBOX = "com.minifocus.launcher.action.OPEN_INBOX"
    }
}
