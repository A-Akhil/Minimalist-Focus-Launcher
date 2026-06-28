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

@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.minifocus.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.minifocus.launcher.model.AppEntry
import com.minifocus.launcher.model.BottomIconSlot
import com.minifocus.launcher.model.ClockFormat
import com.minifocus.launcher.model.LauncherTheme
import com.minifocus.launcher.model.SearchResult
import com.minifocus.launcher.model.DailyTaskItem
import com.minifocus.launcher.model.DailyTaskRepeatMode
import com.minifocus.launcher.model.TaskItem
import com.minifocus.launcher.model.patternLabel
import com.minifocus.launcher.model.AppUsageEventSource
import com.minifocus.launcher.model.SmartSuggestion
import com.minifocus.launcher.viewmodel.LauncherUiState
import com.minifocus.launcher.viewmodel.NotificationFilterViewModel.FilterUiState
import com.minifocus.launcher.viewmodel.NotificationFilterViewModel.NotificationFilterItem
import com.minifocus.launcher.viewmodel.NotificationInboxViewModel.NotificationInboxUiState
import com.minifocus.launcher.permissions.PermissionsState
import com.minifocus.launcher.ui.components.AppContextMenu
import com.minifocus.launcher.R
import com.minifocus.launcher.ui.components.MinimalCheckbox
import com.minifocus.launcher.ui.components.ScreenHeader
import com.minifocus.launcher.ui.screens.AboutScreen
import com.minifocus.launcher.ui.screens.CalendarScreen
import com.minifocus.launcher.ui.screens.CalendarSettingsScreen
import com.minifocus.launcher.ui.screens.AppearanceSettingsScreen
import com.minifocus.launcher.ui.screens.EmergencyUnlockScreen
import com.minifocus.launcher.ui.screens.LogViewerScreen
import com.minifocus.launcher.ui.screens.NotificationFilterScreen
import com.minifocus.launcher.ui.screens.NotificationInboxScreen
import com.minifocus.launcher.ui.screens.NotificationSettingsScreen
import androidx.compose.runtime.CompositionLocalProvider
import com.minifocus.launcher.ui.screens.TextSizeSettingsScreen
import com.minifocus.launcher.ui.screens.AppTimeReminderSettingsScreen
import com.minifocus.launcher.ui.screens.LanguageSettingsScreen
import com.minifocus.launcher.model.ExpiryAction
import com.minifocus.launcher.data.entity.AppTimeReminderEntity
import com.minifocus.launcher.service.AppTimeReminderReceiver
import com.minifocus.launcher.ui.theme.LocalTextMultiplier
import com.minifocus.launcher.ui.theme.TextSizeProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlin.math.roundToInt

@Composable
fun LauncherApp(
    state: LauncherUiState,
    notificationInboxState: NotificationInboxUiState,
    notificationFilterState: FilterUiState,
    permissionsState: PermissionsState,
    onToggleTask: (Long) -> Unit,
    onAddTask: (String, Long?) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onAddDailyTask: (String, Long?, Long?, Boolean, DailyTaskRepeatMode, Int, Int) -> Unit,
    onUpdateDailyTask: (Long, String, Long?, Long?, Boolean, DailyTaskRepeatMode, Int, Int) -> Unit,
    onDeleteDailyTask: (Long) -> Unit,
    onDailyTaskEnabledChange: (Long, Boolean) -> Unit,
    onDailyTaskCompleted: (Long) -> Unit,
    onDailyTaskReset: (Long) -> Unit,
    onPinApp: (String) -> Unit,
    onUnpinApp: (String) -> Unit,
    onHideApp: (String) -> Unit,
    onUnhideApp: (String) -> Unit,
    onLockApp: (String, Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchVisibilityChange: (Boolean) -> Unit,
    onBottomIconChange: (BottomIconSlot, String) -> Unit,
    onSettingsVisibilityChange: (Boolean) -> Unit,
    onHomeSettingsVisibilityChange: (Boolean) -> Unit,
    onClockSettingsVisibilityChange: (Boolean) -> Unit,
    onAppDrawerSettingsVisibilityChange: (Boolean) -> Unit,
    onAppearanceSettingsVisibilityChange: (Boolean) -> Unit,
    onTextSizeSettingsVisibilityChange: (Boolean) -> Unit,
    onHiddenAppsVisibilityChange: (Boolean) -> Unit,
    onAboutVisibilityChange: (Boolean) -> Unit,
    onEmergencyUnlockVisibilityChange: (Boolean) -> Unit,
    onHistoryVisibilityChange: (Boolean) -> Unit,
    onClockFormatChange: (ClockFormat) -> Unit,
    onTextSizeChange: (com.minifocus.launcher.model.TextSize) -> Unit,
    onKeyboardSearchOnSwipeChange: (Boolean) -> Unit,
    onSmartSuggestionsToggle: (Boolean) -> Unit,
    onResetSmartSuggestions: () -> Unit,
    onShowSecondsChange: (Boolean) -> Unit,
    onShowDailyTasksOnHomeChange: (Boolean) -> Unit,
    onDoubleTapLockScreenChange: (Boolean) -> Unit,
    onNotificationInboxEnabledChange: (Boolean) -> Unit,
    onNotificationInboxVisibilityChange: (Boolean) -> Unit,
    onNotificationSettingsVisibilityChange: (Boolean) -> Unit,
    onLogViewerVisibilityChange: (Boolean) -> Unit,
    onNotificationFilterVisibilityChange: (Boolean) -> Unit,
    onOpenPermissionManager: () -> Unit,
    onOpenDeviceSettings: () -> Unit,
    onRequestLockAccessibility: () -> Unit,
    onLockDevice: () -> Unit,
    onNotificationRetentionSelected: (Int) -> Unit,
    onLogRetentionSelected: (Int) -> Unit,
    onNotificationDelete: (Long) -> Unit,
    onNotificationMarkAllRead: () -> Unit,
    onNotificationFilterQueryChange: (String) -> Unit,
    onNotificationFilterToggle: (NotificationFilterItem) -> Unit,
    onNotificationFilterToggleAll: (Boolean) -> Unit,
    onRecordAppUsage: (String, AppUsageEventSource) -> Unit,
    canLaunch: suspend (String) -> Boolean,
    onLaunchApp: (String) -> Unit,
    onOpenClock: () -> Unit,
    lockManager: com.minifocus.launcher.manager.LockManager,
    onBackupSettingsVisibilityChange: (Boolean) -> Unit,
    onBackupSettings: () -> Unit,
    onRestoreSettings: () -> Unit,
    onLanguageSettingsVisibilityChange: (Boolean) -> Unit,
    onAppTimeReminderSettingsVisibilityChange: (Boolean) -> Unit,
    onCalendarSettingsVisibilityChange: (Boolean) -> Unit,
    onSelectCalendar: (Long, String) -> Unit,
    onToggleShowTasksInCalendar: (Boolean) -> Unit,
    onToggleSyncTasksWithCalendar: (Boolean) -> Unit,
    onToggleSyncTasksWithDate: (Boolean) -> Unit,
    onToggleSyncDailyReminders: (Boolean) -> Unit,
    onAddTrackedReminderApp: (String, String) -> Unit,
    onRemoveTrackedReminderApp: (String) -> Unit,
    onSetPendingTimeIntention: (AppEntry?) -> Unit,
    onUpdateExpiryAction: (String, ExpiryAction) -> Unit,
    onThemeChange: (com.minifocus.launcher.model.LauncherTheme) -> Unit,
    onResetOnboarding: () -> Unit = {},
    isOnboarding: Boolean = false,
    onOnboardingComplete: () -> Unit = {},
    onRootBack: () -> Unit = {},
    onConsumeMessage: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = 2, pageCount = { 4 })

    // -- Onboarding step state machine --
    val onboardingStep = remember { mutableStateOf(OnboardingSteps.WELCOME) }
    val onboardingPinDone = remember { mutableStateOf(false) }

    // Reset onboarding step if screen turns off (ON_STOP)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    if (isOnboarding) {
        androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                    onboardingStep.value = OnboardingSteps.WELCOME
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    // Advance onboarding steps based on pager page changes
    if (isOnboarding) {
        LaunchedEffect(pagerState.settledPage, onboardingStep.value) {
            when (onboardingStep.value) {
                // WELCOME does not track pager, advanced via tap callback
                OnboardingSteps.SWIPE_TO_TASKS -> {
                    if (pagerState.settledPage == 1) {
                        onboardingStep.value = OnboardingSteps.SWIPE_TO_CALENDAR
                    }
                }
                OnboardingSteps.SWIPE_TO_CALENDAR -> {
                    if (pagerState.settledPage == 0) {
                        onboardingStep.value = OnboardingSteps.SWIPE_BACK_HOME
                    }
                }
                OnboardingSteps.SWIPE_BACK_HOME -> {
                    if (pagerState.settledPage == 2) {
                        onboardingStep.value = OnboardingSteps.SWIPE_TO_DRAWER
                    }
                }
                OnboardingSteps.SWIPE_TO_DRAWER -> {
                    if (pagerState.settledPage == 3) {
                        onboardingStep.value = OnboardingSteps.LONG_PRESS_TO_PIN
                    }
                }
                else -> {}
            }
        }
    }

    // Intercept Android back button during onboarding
    if (isOnboarding) {
        BackHandler {
            when (onboardingStep.value) {
                OnboardingSteps.WELCOME -> onOnboardingComplete()
                OnboardingSteps.SWIPE_TO_TASKS -> onboardingStep.value = OnboardingSteps.WELCOME
                OnboardingSteps.SWIPE_TO_CALENDAR -> onboardingStep.value = OnboardingSteps.SWIPE_TO_TASKS
                OnboardingSteps.SWIPE_BACK_HOME -> onboardingStep.value = OnboardingSteps.SWIPE_TO_CALENDAR
                OnboardingSteps.SWIPE_TO_DRAWER -> onboardingStep.value = OnboardingSteps.SWIPE_BACK_HOME
                OnboardingSteps.LONG_PRESS_TO_PIN -> onboardingStep.value = OnboardingSteps.SWIPE_TO_DRAWER
                OnboardingSteps.TAP_PIN -> onboardingStep.value = OnboardingSteps.LONG_PRESS_TO_PIN
                OnboardingSteps.COMPLETE -> onboardingStep.value = OnboardingSteps.TAP_PIN
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val bottomIconPickerSlot = remember { mutableStateOf<BottomIconSlot?>(null) }
    val searchVisible = state.isSearchVisible
    val shouldShowInlineSearch = state.isKeyboardSearchOnSwipe
    val shouldFocusInlineSearch = shouldShowInlineSearch && pagerState.currentPage == 3
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val showNotificationRetentionDialog = remember { mutableStateOf(false) }
    val showLogRetentionDialog = remember { mutableStateOf(false) }
    val filterBackTarget = remember { mutableStateOf(FilterBackTarget.None) }
    val inboxBackTarget = remember { mutableStateOf(InboxBackTarget.None) }
    val notifSettingsBackTarget = remember { mutableStateOf(NotifSettingsBackTarget.None) }
    val logViewerBackTarget = remember { mutableStateOf(LogViewerBackTarget.None) }
    val homeSettingsBackTarget = remember { mutableStateOf(HomeSettingsBackTarget.None) }
    val clockSettingsBackTarget = remember { mutableStateOf(ClockSettingsBackTarget.None) }
    val appDrawerSettingsBackTarget = remember { mutableStateOf(AppDrawerSettingsBackTarget.None) }
    val appearanceSettingsBackTarget = remember { mutableStateOf(AppearanceSettingsBackTarget.None) }
    val hiddenAppsBackTarget = remember { mutableStateOf(HiddenAppsBackTarget.None) }
    val appTimeReminderSettingsBackTarget = remember { mutableStateOf(AppTimeReminderSettingsBackTarget.None) }
    val trackedPackages = remember(state.trackedReminderApps) {
        state.trackedReminderApps.map { it.packageName }.toSet()
    }

    val shouldSnapToHome = state.isSettingsVisible ||
        state.isHomeSettingsVisible ||
        state.isClockSettingsVisible ||
        state.isAppearanceSettingsVisible ||
        state.isNotificationSettingsVisible ||
        state.isNotificationFilterVisible ||
        state.isNotificationInboxVisible ||
        state.isAboutVisible ||
        state.isEmergencyUnlockVisible ||
        state.isHistoryVisible ||
        state.isSearchVisible ||
        state.isHiddenAppsVisible ||
        state.isAppTimeReminderSettingsVisible ||
        state.isLanguageSettingsVisible ||
        state.isCalendarSettingsVisible

    LaunchedEffect(state.homeResetTick) {
        if (pagerState.currentPage != 2) {
            pagerState.scrollToPage(2)
        }
    }

    LaunchedEffect(shouldSnapToHome) {
        if (shouldSnapToHome && pagerState.currentPage != 2) {
            pagerState.scrollToPage(2)
        }
    }

    // Show transient UI messages (from ViewModel) as a Toast and notify consumer
    val _context = LocalContext.current
    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        Toast.makeText(_context, msg, Toast.LENGTH_SHORT).show()
        onConsumeMessage()
    }

    fun openInbox(from: InboxBackTarget) {
        inboxBackTarget.value = from
        if (from == InboxBackTarget.Settings) {
            onSettingsVisibilityChange(false)
        }
        onNotificationInboxVisibilityChange(true)
    }

    fun closeInbox() {
        onNotificationInboxVisibilityChange(false)
        when (inboxBackTarget.value) {
            InboxBackTarget.Settings -> onSettingsVisibilityChange(true)
            InboxBackTarget.None -> Unit
        }
        inboxBackTarget.value = InboxBackTarget.None
    }

    fun openNotifSettings(from: NotifSettingsBackTarget) {
        notifSettingsBackTarget.value = from
        if (from == NotifSettingsBackTarget.Settings) {
            onSettingsVisibilityChange(false)
        }
        onNotificationSettingsVisibilityChange(true)
    }

    fun closeNotifSettings() {
        onNotificationSettingsVisibilityChange(false)
        when (notifSettingsBackTarget.value) {
            NotifSettingsBackTarget.Settings -> onSettingsVisibilityChange(true)
            NotifSettingsBackTarget.None -> Unit
        }
        notifSettingsBackTarget.value = NotifSettingsBackTarget.None
    }

    fun openLogViewer() {
        logViewerBackTarget.value = LogViewerBackTarget.NotifSettings
        onNotificationSettingsVisibilityChange(false)
        onLogViewerVisibilityChange(true)
    }

    fun closeLogViewer() {
        onLogViewerVisibilityChange(false)
        when (logViewerBackTarget.value) {
            LogViewerBackTarget.NotifSettings -> onNotificationSettingsVisibilityChange(true)
            LogViewerBackTarget.None -> Unit
        }
        logViewerBackTarget.value = LogViewerBackTarget.None
    }

    fun openHomeSettings(from: HomeSettingsBackTarget) {
        homeSettingsBackTarget.value = from
        if (from == HomeSettingsBackTarget.Settings) {
            onSettingsVisibilityChange(false)
        }
        onHomeSettingsVisibilityChange(true)
    }

    fun closeHomeSettings() {
        onHomeSettingsVisibilityChange(false)
        when (homeSettingsBackTarget.value) {
            HomeSettingsBackTarget.Settings -> onSettingsVisibilityChange(true)
            HomeSettingsBackTarget.None -> Unit
        }
        homeSettingsBackTarget.value = HomeSettingsBackTarget.None
    }

    fun openClockSettings(from: ClockSettingsBackTarget) {
        clockSettingsBackTarget.value = from
        if (from == ClockSettingsBackTarget.Settings) {
            onSettingsVisibilityChange(false)
        }
        onClockSettingsVisibilityChange(true)
    }

    fun closeClockSettings() {
        onClockSettingsVisibilityChange(false)
        when (clockSettingsBackTarget.value) {
            ClockSettingsBackTarget.Settings -> onSettingsVisibilityChange(true)
            ClockSettingsBackTarget.None -> Unit
        }
        clockSettingsBackTarget.value = ClockSettingsBackTarget.None
    }

    fun openAppDrawerSettings(from: AppDrawerSettingsBackTarget) {
        appDrawerSettingsBackTarget.value = from
        if (from == AppDrawerSettingsBackTarget.Settings) {
            onSettingsVisibilityChange(false)
        }
        onAppDrawerSettingsVisibilityChange(true)
    }

    fun closeAppDrawerSettings() {
        onAppDrawerSettingsVisibilityChange(false)
        when (appDrawerSettingsBackTarget.value) {
            AppDrawerSettingsBackTarget.Settings -> onSettingsVisibilityChange(true)
            AppDrawerSettingsBackTarget.None -> Unit
        }
        appDrawerSettingsBackTarget.value = AppDrawerSettingsBackTarget.None
    }

    fun openAppearanceSettings(from: AppearanceSettingsBackTarget) {
        appearanceSettingsBackTarget.value = from
        if (from == AppearanceSettingsBackTarget.Settings) {
            onSettingsVisibilityChange(false)
        }
        onAppearanceSettingsVisibilityChange(true)
    }

    fun closeAppearanceSettings() {
        onAppearanceSettingsVisibilityChange(false)
        when (appearanceSettingsBackTarget.value) {
            AppearanceSettingsBackTarget.Settings -> onSettingsVisibilityChange(true)
            AppearanceSettingsBackTarget.None -> Unit
        }
        appearanceSettingsBackTarget.value = AppearanceSettingsBackTarget.None
    }

    fun openTextSizeSettings() {
        onAppearanceSettingsVisibilityChange(false)
        onTextSizeSettingsVisibilityChange(true)
    }

    fun closeTextSizeSettings() {
        onTextSizeSettingsVisibilityChange(false)
        onAppearanceSettingsVisibilityChange(true)
    }

    fun openHiddenApps(from: HiddenAppsBackTarget) {
        hiddenAppsBackTarget.value = from
        when (from) {
            HiddenAppsBackTarget.Settings -> onSettingsVisibilityChange(false)
            HiddenAppsBackTarget.AppDrawerSettings -> onAppDrawerSettingsVisibilityChange(false)
            HiddenAppsBackTarget.None -> Unit
        }
        onHiddenAppsVisibilityChange(true)
    }

    fun closeHiddenApps() {
        onHiddenAppsVisibilityChange(false)
        when (hiddenAppsBackTarget.value) {
            HiddenAppsBackTarget.Settings -> onSettingsVisibilityChange(true)
            HiddenAppsBackTarget.AppDrawerSettings -> onAppDrawerSettingsVisibilityChange(true)
            HiddenAppsBackTarget.None -> Unit
        }
        hiddenAppsBackTarget.value = HiddenAppsBackTarget.None
    }

    fun openAppTimeReminderSettings(from: AppTimeReminderSettingsBackTarget) {
        appTimeReminderSettingsBackTarget.value = from
        when (from) {
            AppTimeReminderSettingsBackTarget.Settings -> onSettingsVisibilityChange(false)
            AppTimeReminderSettingsBackTarget.None -> Unit
        }
        onAppTimeReminderSettingsVisibilityChange(true)
    }

    fun closeAppTimeReminderSettings() {
        onAppTimeReminderSettingsVisibilityChange(false)
        when (appTimeReminderSettingsBackTarget.value) {
            AppTimeReminderSettingsBackTarget.Settings -> onSettingsVisibilityChange(true)
            AppTimeReminderSettingsBackTarget.None -> Unit
        }
        appTimeReminderSettingsBackTarget.value = AppTimeReminderSettingsBackTarget.None
    }

    fun openFilters(from: FilterBackTarget) {
        filterBackTarget.value = from
        when (from) {
            FilterBackTarget.Settings -> onSettingsVisibilityChange(false)
            FilterBackTarget.Inbox -> onNotificationInboxVisibilityChange(false)
            FilterBackTarget.NotifSettings -> onNotificationSettingsVisibilityChange(false)
            FilterBackTarget.None -> Unit
        }
        onNotificationFilterVisibilityChange(true)
    }

    fun closeFilters() {
        onNotificationFilterVisibilityChange(false)
        when (filterBackTarget.value) {
            FilterBackTarget.Settings -> onSettingsVisibilityChange(true)
            FilterBackTarget.Inbox -> onNotificationInboxVisibilityChange(true)
            FilterBackTarget.NotifSettings -> onNotificationSettingsVisibilityChange(true)
            FilterBackTarget.None -> Unit
        }
        filterBackTarget.value = FilterBackTarget.None
    }

    BackHandler {
        when {
            state.pendingTimeIntention != null -> onSetPendingTimeIntention(null)
            state.isNotificationFilterVisible -> closeFilters()
            state.isNotificationSettingsVisible -> closeNotifSettings()
            state.isClockSettingsVisible -> closeClockSettings()
            state.isAppDrawerSettingsVisible -> closeAppDrawerSettings()
            state.isTextSizeSettingsVisible -> closeTextSizeSettings()
            state.isAppearanceSettingsVisible -> closeAppearanceSettings()
            state.isHomeSettingsVisible -> closeHomeSettings()
            state.isAppTimeReminderSettingsVisible -> closeAppTimeReminderSettings()
            state.isLanguageSettingsVisible -> onLanguageSettingsVisibilityChange(false)
            state.isNotificationInboxVisible -> closeInbox()
            state.isSettingsVisible -> onSettingsVisibilityChange(false)
            state.isHistoryVisible -> onHistoryVisibilityChange(false)
            state.isAboutVisible -> onAboutVisibilityChange(false)
            state.isEmergencyUnlockVisible -> onEmergencyUnlockVisibilityChange(false)
            state.isHiddenAppsVisible -> closeHiddenApps()
            state.isCalendarSettingsVisible -> {
                onCalendarSettingsVisibilityChange(false)
                onSettingsVisibilityChange(true)
            }
            searchVisible -> onSearchVisibilityChange(false)
            pagerState.currentPage != 2 -> {
                coroutineScope.launch { pagerState.scrollToPage(2) }
            }
            else -> onRootBack()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 3) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            if (state.searchQuery.isNotEmpty()) {
                onSearchQueryChange("")
            }
        }
    }

    LaunchedEffect(shouldShowInlineSearch, searchVisible, pagerState.currentPage) {
        if (shouldShowInlineSearch && pagerState.currentPage == 3 && searchVisible) {
            onSearchVisibilityChange(false)
        }
    }

    LaunchedEffect(shouldShowInlineSearch) {
        if (!shouldShowInlineSearch && state.searchQuery.isNotEmpty()) {
            onSearchQueryChange("")
        }
    }

    LaunchedEffect(pagerState.currentPage, shouldShowInlineSearch) {
        if (pagerState.currentPage != 3 && shouldShowInlineSearch && state.searchQuery.isNotEmpty()) {
            onSearchQueryChange("")
        }
    }

    TextSizeProvider(state.textSize.multiplier) {
        Scaffold(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when {
                state.isNotificationFilterVisible -> {
                    NotificationFilterScreen(
                        state = notificationFilterState,
                        onBack = { closeFilters() },
                        onQueryChange = onNotificationFilterQueryChange,
                        onToggle = onNotificationFilterToggle,
                        onToggleAll = onNotificationFilterToggleAll
                    )
                }
                state.isSettingsVisible -> {
                    SettingsScreen(
                        clockFormat = state.clockFormat,
                        showSeconds = state.showSeconds,
                        textSize = state.textSize,
                        theme = state.theme,
                        showDailyTasksOnHome = state.showDailyTasksOnHome,
                        bottomLeftApp = state.bottomLeft,
                        bottomRightApp = state.bottomRight,
                        keyboardOnSwipe = state.isKeyboardSearchOnSwipe,
                        smartSuggestionsEnabled = state.smartSuggestionsEnabled,
                        notificationInboxEnabled = state.notificationInboxEnabled,
                        notificationRetentionDays = state.notificationRetentionDays,
                        logRetentionDays = state.logRetentionDays,
                        permissionsState = permissionsState,
                        onOpenHomeSettings = { openHomeSettings(HomeSettingsBackTarget.Settings) },
                        onOpenClockSettings = { openClockSettings(ClockSettingsBackTarget.Settings) },
                        onOpenAppearanceSettings = { openAppearanceSettings(AppearanceSettingsBackTarget.Settings) },
                        onOpenAppDrawerSettings = { openAppDrawerSettings(AppDrawerSettingsBackTarget.Settings) },
                        onOpenNotificationSettings = { openNotifSettings(NotifSettingsBackTarget.Settings) },
                        onOpenPermissionManager = onOpenPermissionManager,
                        onOpenDeviceSettings = onOpenDeviceSettings,
                        onOpenAbout = { onAboutVisibilityChange(true) },
                        onOpenBackupSettings = { onBackupSettingsVisibilityChange(true) },
                        onOpenAppTimeReminderSettings = { openAppTimeReminderSettings(AppTimeReminderSettingsBackTarget.Settings) },
                        onOpenCalendarSettings = {
                            onSettingsVisibilityChange(false)
                            onCalendarSettingsVisibilityChange(true)
                        },
                        onOpenLanguageSettings = { onLanguageSettingsVisibilityChange(true) },
                        selectedCalendarAccountName = state.selectedCalendarAccountName,
                        trackedReminderAppsCount = state.trackedReminderApps.size,
                        onResetOnboarding = onResetOnboarding,
                        onBack = { onSettingsVisibilityChange(false) }
                    )
                }
                state.isBackupSettingsVisible -> {
                    com.minifocus.launcher.ui.screens.BackupSettingsScreen(
                        onBackup = onBackupSettings,
                        onRestore = onRestoreSettings,
                        onBack = { onBackupSettingsVisibilityChange(false) }
                    )
                }
                state.isLanguageSettingsVisible -> {
                    LanguageSettingsScreen(
                        onBack = { onLanguageSettingsVisibilityChange(false) }
                    )
                }
                state.isCalendarSettingsVisible -> {
                    CalendarSettingsScreen(
                        selectedCalendarId = state.selectedCalendarId,
                        showTasksInCalendar = state.showTasksInCalendar,
                        syncTasksWithCalendar = state.syncTasksWithCalendar,
                        syncTasksWithDate = state.syncTasksWithDate,
                        syncDailyReminders = state.syncDailyReminders,
                        onSelectCalendar = onSelectCalendar,
                        onToggleShowTasksInCalendar = onToggleShowTasksInCalendar,
                        onToggleSyncTasksWithCalendar = onToggleSyncTasksWithCalendar,
                        onToggleSyncTasksWithDate = onToggleSyncTasksWithDate,
                        onToggleSyncDailyReminders = onToggleSyncDailyReminders,
                        onBack = { 
                            onCalendarSettingsVisibilityChange(false)
                            onSettingsVisibilityChange(true)
                        }
                    )
                }
                state.isAppTimeReminderSettingsVisible -> {
                    AppTimeReminderSettingsScreen(
                        trackedApps = state.trackedReminderApps,
                        allApps = state.allApps,
                        onAddApp = onAddTrackedReminderApp,
                        onRemoveApp = onRemoveTrackedReminderApp,
                        onBack = { closeAppTimeReminderSettings() }
                    )
                }
                state.isHomeSettingsVisible -> {
                    HomeSettingsScreen(
                        showDailyTasksOnHome = state.showDailyTasksOnHome,
                        doubleTapLockScreen = state.doubleTapLockScreen,
                        lockAccessibilityGranted = permissionsState.lockAccessibilityGranted,
                        bottomLeftApp = state.bottomLeft,
                        bottomRightApp = state.bottomRight,
                        onToggleDailyTasksOnHome = onShowDailyTasksOnHomeChange,
                        onToggleDoubleTapLockScreen = onDoubleTapLockScreenChange,
                        onRequestLockAccessibility = onRequestLockAccessibility,
                        onBottomIconClick = { slot -> bottomIconPickerSlot.value = slot },
                        onBack = { closeHomeSettings() }
                    )
                }
                state.isClockSettingsVisible -> {
                    ClockSettingsScreen(
                        clockFormat = state.clockFormat,
                        showSeconds = state.showSeconds,
                        onClockFormatChange = onClockFormatChange,
                        onShowSecondsToggle = onShowSecondsChange,
                        onBack = { closeClockSettings() }
                    )
                }
                state.isAppDrawerSettingsVisible -> {
                    AppDrawerSettingsScreen(
                        keyboardOnSwipe = state.isKeyboardSearchOnSwipe,
                        smartSuggestionsEnabled = state.smartSuggestionsEnabled,
                        hiddenAppsCount = state.hiddenApps.size,
                        onKeyboardToggle = onKeyboardSearchOnSwipeChange,
                        onSmartSuggestionsToggle = onSmartSuggestionsToggle,
                        onResetSmartSuggestions = onResetSmartSuggestions,
                        onOpenHiddenApps = { openHiddenApps(HiddenAppsBackTarget.AppDrawerSettings) },
                        onBack = { closeAppDrawerSettings() }
                    )
                }
                state.isAppearanceSettingsVisible -> {
                    AppearanceSettingsScreen(
                        textSize = state.textSize,
                        theme = state.theme,
                        onOpenTextSize = { openTextSizeSettings() },
                        onThemeChange = onThemeChange,
                        onBack = { closeAppearanceSettings() }
                    )
                }
                state.isTextSizeSettingsVisible -> {
                    TextSizeSettingsScreen(
                        textSize = state.textSize,
                        onTextSizeChange = onTextSizeChange,
                        onBack = { closeTextSizeSettings() }
                    )
                }
                state.isNotificationSettingsVisible -> {
                    NotificationSettingsScreen(
                        notificationInboxEnabled = state.notificationInboxEnabled,
                        notificationRetentionDays = state.notificationRetentionDays,
                        logRetentionDays = state.logRetentionDays,
                        onBack = { closeNotifSettings() },
                        onOpenAppFilters = { openFilters(FilterBackTarget.NotifSettings) },
                        onOpenNotificationRetention = { showNotificationRetentionDialog.value = true },
                        onOpenLogRetention = { showLogRetentionDialog.value = true },
                        onOpenLogViewer = { openLogViewer() },
                        onNotificationInboxToggle = onNotificationInboxEnabledChange
                    )
                }
                state.isLogViewerVisible -> {
                    LogViewerScreen(
                        notifications = notificationInboxState.logEntries,
                        onBack = { closeLogViewer() }
                    )
                }
                state.isNotificationInboxVisible -> {
                    NotificationInboxScreen(
                        state = notificationInboxState,
                        onBack = { closeInbox() },
                        onMarkAllRead = onNotificationMarkAllRead,
                        onDelete = onNotificationDelete
                    )
                }
                state.isHistoryVisible -> {
                    HistoryScreen(
                        historyTasks = state.historyTasks,
                        onBack = { onHistoryVisibilityChange(false) },
                        onToggleTask = onToggleTask,
                        onDeleteTask = onDeleteTask
                    )
                }
                state.isAboutVisible -> {
                    AboutScreen(
                        onBack = { onAboutVisibilityChange(false) },
                        onNavigateToEmergencyUnlock = { onEmergencyUnlockVisibilityChange(true) }
                    )
                }
                state.isEmergencyUnlockVisible -> {
                    EmergencyUnlockScreen(
                        lockManager = lockManager,
                        onBack = { onEmergencyUnlockVisibilityChange(false) },
                        onUnlockApp = lockManager::unlockApp
                    )
                }
                state.isHiddenAppsVisible -> {
                    HiddenAppsScreen(
                        hiddenApps = state.hiddenApps,
                        onUnhideApp = onUnhideApp,
                        onBack = { closeHiddenApps() }
                    )
                }
                else -> {
                    val currentOnboardingStep = onboardingStep.value
                    
                    val nestedScrollConnection = remember(isOnboarding, currentOnboardingStep) {
                        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                                if (!isOnboarding) return androidx.compose.ui.geometry.Offset.Zero
                                
                                val sourceStr = source.toString()
                                if (sourceStr != "Drag" && sourceStr != "UserInput") {
                                    return androidx.compose.ui.geometry.Offset.Zero
                                }
                                
                                val isSwipingRight = available.x > 0
                                val isSwipingLeft = available.x < 0
                                
                                val canSwipeRight = currentOnboardingStep == OnboardingSteps.SWIPE_TO_TASKS || currentOnboardingStep == OnboardingSteps.SWIPE_TO_CALENDAR
                                val canSwipeLeft = currentOnboardingStep == OnboardingSteps.SWIPE_BACK_HOME || currentOnboardingStep == OnboardingSteps.SWIPE_TO_DRAWER
                                
                                if (isSwipingRight && !canSwipeRight) {
                                    return available // consume
                                }
                                if (isSwipingLeft && !canSwipeLeft) {
                                    return available // consume
                                }
                                return androidx.compose.ui.geometry.Offset.Zero
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
                        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        when (page) {
                            0 -> CalendarScreen(
                                selectedCalendarId = state.selectedCalendarId,
                                isActive = pagerState.currentPage == 0
                            )
                            1 -> TasksScreen(
                                tasks = state.tasks,
                                dailyTasks = state.dailyTasks,
                                heldDailyTaskIds = state.heldDailyTaskIds,
                                onToggleTask = onToggleTask,
                                onAddTask = onAddTask,
                                onDeleteTask = onDeleteTask,
                                onOpenHistory = { onHistoryVisibilityChange(true) },
                                onAddDailyTask = onAddDailyTask,
                                onUpdateDailyTask = onUpdateDailyTask,
                                onDeleteDailyTask = onDeleteDailyTask,
                                onDailyTaskEnabledChange = onDailyTaskEnabledChange,
                                onDailyTaskCompleted = onDailyTaskCompleted,
                                onDailyTaskReset = onDailyTaskReset
                            )
                            2 -> HomeScreen(
                                state = state,
                                onLaunchApp = { entry ->
                                    if (!isOnboarding) {
                                        onRecordAppUsage(entry.packageName, AppUsageEventSource.HOME)
                                        handleAppLaunch(
                                            entry,
                                            coroutineScope,
                                            onLaunchApp,
                                            navigateToHome = { /* Already on home */ },
                                            trackedPackages = trackedPackages,
                                            onSetPendingTimeIntention = onSetPendingTimeIntention
                                        )
                                    }
                                },
                                bottomIconPickerSlot = bottomIconPickerSlot,
                                onUnpinApp = onUnpinApp,
                                onHideApp = onHideApp,
                                onLockApp = onLockApp,
                                onOpenClock = onOpenClock,
                                onDailyTaskCompleted = onDailyTaskCompleted,
                                onDailyTaskReset = onDailyTaskReset,
                                onLockDevice = onLockDevice,
                                trackedPackages = trackedPackages,
                                onAddTrackedReminderApp = onAddTrackedReminderApp,
                                onRemoveTrackedReminderApp = onRemoveTrackedReminderApp
                            )
                            3 -> AllAppsScreen(
                                apps = state.allApps,
                                isDrawerVisible = pagerState.currentPage == 3,
                                keyboardOnSwipe = if (isOnboarding) false else shouldShowInlineSearch,
                                searchQuery = state.searchQuery,
                                shouldFocusSearch = if (isOnboarding) false else shouldFocusInlineSearch,
                                unreadCount = notificationInboxState.unreadCount,
                                smartSuggestionsEnabled = state.smartSuggestionsEnabled,
                                smartSuggestions = state.smartSuggestions,
                                usageWeights = state.smartSuggestionWeights,
                                onQueryChange = onSearchQueryChange,
                                onAppSelected = { entry, source, _ ->
                                    if (!isOnboarding) {
                                        onRecordAppUsage(entry.packageName, source)
                                        handleAppLaunch(
                                            entry,
                                            coroutineScope,
                                            onLaunchApp,
                                            navigateToHome = {
                                                coroutineScope.launch {
                                                    pagerState.scrollToPage(2)
                                                }
                                            },
                                            trackedPackages = trackedPackages,
                                            onSetPendingTimeIntention = onSetPendingTimeIntention
                                        )
                                    }
                                },
                                onPinApp = { pkg ->
                                    onPinApp(pkg)
                                    if (isOnboarding) {
                                        onboardingPinDone.value = true
                                        onboardingStep.value = OnboardingSteps.COMPLETE
                                    }
                                },
                                onUnpinApp = onUnpinApp,
                                onHideApp = onHideApp,
                                onLockApp = onLockApp,
                                onOpenNotificationInbox = { if (!isOnboarding) openInbox(InboxBackTarget.None) },
                                onOpenSettings = { if (!isOnboarding) onSettingsVisibilityChange(true) },
                                trackedPackages = trackedPackages,
                                onAddTrackedReminderApp = onAddTrackedReminderApp,
                                onRemoveTrackedReminderApp = onRemoveTrackedReminderApp,
                                onboardingPinOnly = isOnboarding && onboardingStep.value in listOf(
                                    OnboardingSteps.LONG_PRESS_TO_PIN,
                                    OnboardingSteps.TAP_PIN
                                ),
                                onOnboardingContextMenuShown = {
                                    if (isOnboarding && onboardingStep.value == OnboardingSteps.LONG_PRESS_TO_PIN) {
                                        onboardingStep.value = OnboardingSteps.TAP_PIN
                                    }
                                }
                            )
                        }
                    }

                    // Onboarding overlay on top of the pager
                    if (isOnboarding) {
                        OnboardingOverlay(
                            step = onboardingStep.value,
                            onFinish = onOnboardingComplete,
                            onWelcomeDismiss = {
                                onboardingStep.value = OnboardingSteps.SWIPE_TO_TASKS
                            },
                            onSkip = onOnboardingComplete
                        )
                    }
                    } // end Box
                }
            }

            AnimatedVisibility(
                visible = searchVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SearchOverlay(
                    query = state.searchQuery,
                    results = state.searchResults,
                    onQueryChange = onSearchQueryChange,
                    autoFocus = state.isSearchVisible,
                    onResultClick = { entry ->
                        coroutineScope.launch {
                            if (canLaunch(entry.packageName)) {
                                onSearchVisibilityChange(false)
                                onRecordAppUsage(entry.packageName, AppUsageEventSource.SEARCH_OVERLAY)
                                onLaunchApp(entry.packageName)
                            }
                            // If app is locked, simply don't launch (no snackbar message)
                        }
                    }
                )
            }

            if (showNotificationRetentionDialog.value) {
                RetentionPickerDialog(
                    title = stringResource(R.string.notif_settings_notif_retention),
                    options = listOf(1, 2, 7, 14, 30),
                    selected = state.notificationRetentionDays,
                    onSelect = { days ->
                        onNotificationRetentionSelected(days)
                        showNotificationRetentionDialog.value = false
                    },
                    onDismiss = { showNotificationRetentionDialog.value = false }
                )
            }

            if (showLogRetentionDialog.value) {
                RetentionPickerDialog(
                    title = stringResource(R.string.notif_settings_log_retention),
                    options = listOf(7, 30, 60, 90),
                    selected = state.logRetentionDays,
                    onSelect = { days ->
                        onLogRetentionSelected(days)
                        showLogRetentionDialog.value = false
                    },
                    onDismiss = { showLogRetentionDialog.value = false }
                )
            }

            BottomIconPickerDialog(
                slot = bottomIconPickerSlot.value,
                apps = state.allApps,
                onDismiss = { bottomIconPickerSlot.value = null },
                onSelect = { slot, packageName ->
                    onBottomIconChange(slot, packageName)
                    bottomIconPickerSlot.value = null
                }
            )

            // Time intention dialog for tracked apps
            val pendingApp = state.pendingTimeIntention
            if (pendingApp != null) {
                val trackedInfo = state.trackedReminderApps.find { it.packageName == pendingApp.packageName }
                val defaultAction = trackedInfo?.expiryAction?.let {
                    runCatching { ExpiryAction.valueOf(it) }.getOrNull()
                } ?: ExpiryAction.NOTIFICATION
                TimeIntentionDialog(
                    app = pendingApp,
                    defaultDurationMinutes = trackedInfo?.defaultDurationMinutes,
                    defaultExpiryAction = defaultAction,
                    onConfirm = { durationMinutes, expiryAction ->
                        onSetPendingTimeIntention(null)
                        // Save chosen expiry action for next time
                        onUpdateExpiryAction(pendingApp.packageName, expiryAction)
                        // Track this package for alarm cancellation on return
                        AppTimeReminderReceiver.activeReminderPackage = pendingApp.packageName
                        AppTimeReminderReceiver.schedule(
                            _context,
                            pendingApp.packageName,
                            pendingApp.label,
                            durationMinutes,
                            expiryAction.name
                        )
                        onLaunchApp(pendingApp.packageName)
                    },
                    onDismiss = {
                        onSetPendingTimeIntention(null)
                    }
                )
            }
        }
    }
    }
}

private fun handleAppLaunch(
    entry: AppEntry,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onLaunchApp: (String) -> Unit,
    navigateToHome: () -> Unit,
    trackedPackages: Set<String> = emptySet(),
    onSetPendingTimeIntention: ((AppEntry?) -> Unit)? = null
) {
    // If this app is tracked for time reminders, show intention dialog first
    if (trackedPackages.contains(entry.packageName) && onSetPendingTimeIntention != null) {
        if (AppTimeReminderReceiver.hasActiveSession(entry.packageName)) {
            AppTimeReminderReceiver.consumeGraceIfActive(entry.packageName)
            coroutineScope.launch {
                onLaunchApp(entry.packageName)
                kotlinx.coroutines.delay(50)
                navigateToHome()
            }
            return
        }
        onSetPendingTimeIntention(entry)
        navigateToHome()
        return
    }
    coroutineScope.launch {
        // Always call onLaunchApp - it will handle locked apps by showing overlay
        onLaunchApp(entry.packageName)
        // Small delay to allow UI to settle before navigation
        kotlinx.coroutines.delay(50)
        navigateToHome()  // Navigate back to home after launching app
    }
}

private fun unreadCountLabel(count: Int): String = if (count > 99) "99+" else count.toString()

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
        Text(
            text = "→",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 20.sp
        )
    }
}

private fun pluralizeDays(days: Int): String = if (days == 1) "1 day" else "$days days"

private enum class FilterBackTarget { None, Settings, Inbox, NotifSettings }
private enum class InboxBackTarget { None, Settings }
private enum class NotifSettingsBackTarget { None, Settings }
private enum class LogViewerBackTarget { None, NotifSettings }
private enum class HomeSettingsBackTarget { None, Settings }
private enum class ClockSettingsBackTarget { None, Settings }
private enum class AppDrawerSettingsBackTarget { None, Settings }
private enum class AppearanceSettingsBackTarget { None, Settings }
private enum class HiddenAppsBackTarget { None, Settings, AppDrawerSettings }
private enum class AppTimeReminderSettingsBackTarget { None, Settings }

@Composable
private fun RetentionPickerDialog(
    title: String,
    options: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    TextButton(
                        onClick = { onSelect(option) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = pluralizeDays(option),
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun HomeScreen(
    state: LauncherUiState,
    onLaunchApp: (AppEntry) -> Unit,
    bottomIconPickerSlot: MutableState<BottomIconSlot?>,
    onUnpinApp: (String) -> Unit,
    onHideApp: (String) -> Unit,
    onLockApp: (String, Long) -> Unit,
    onOpenClock: () -> Unit,
    onDailyTaskCompleted: (Long) -> Unit,
    onDailyTaskReset: (Long) -> Unit,
    onLockDevice: () -> Unit,
    trackedPackages: Set<String>,
    onAddTrackedReminderApp: (String, String) -> Unit,
    onRemoveTrackedReminderApp: (String) -> Unit
) {
    val expandedPinned = remember { mutableStateOf<String?>(null) }
    val lockDialogApp = remember { mutableStateOf<AppEntry?>(null) }
    val lastTapTime = remember { mutableStateOf(0L) }
    val doubleTapThresholdMs = 300L
    
    val activeDailyTasks = remember(state.dailyTasks, state.heldDailyTaskIds) {
        state.dailyTasks.filter { task ->
            task.isActiveToday && task.isEnabled && (!task.isCompletedToday || task.id in state.heldDailyTaskIds)
        }
    }
    val hasDailyTasksForHome = state.showDailyTasksHomeSection && activeDailyTasks.isNotEmpty()
    val pinnedLimit = if (hasDailyTasksForHome) 3 else 5
    val pinnedToShow = remember(state.pinnedApps, pinnedLimit) {
        state.pinnedApps.take(pinnedLimit)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(state.doubleTapLockScreen) {
                if (state.doubleTapLockScreen) {
                    detectTapGestures(
                        onTap = {
                            val currentTime = System.currentTimeMillis()
                            val timeSinceLastTap = currentTime - lastTapTime.value
                            if (timeSinceLastTap < doubleTapThresholdMs) {
                                onLockDevice()
                                lastTapTime.value = 0L
                            } else {
                                lastTapTime.value = currentTime
                            }
                        }
                    )
                }
            }
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = state.timeFormatted,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.combinedClickable(
                    onClick = { onOpenClock() }
                )
            )
            Text(
                text = state.dateFormatted,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically)
            ) {
                if (hasDailyTasksForHome) {
                    DailyTasksHomeSection(
                        dailyTasks = activeDailyTasks,
                        onDailyTaskCompleted = onDailyTaskCompleted,
                        onDailyTaskReset = onDailyTaskReset
                    )
                }

                if (pinnedToShow.isNotEmpty()) {
                    PinnedAppsHomeSection(
                        pinnedApps = pinnedToShow,
                        onLaunchApp = onLaunchApp,
                        onLongPress = { app -> expandedPinned.value = app.packageName }
                    )
                }
            }
        }

        BottomIconRow(
            left = state.bottomLeft,
            right = state.bottomRight,
            onLaunch = onLaunchApp,
            onLongPress = { slot -> bottomIconPickerSlot.value = slot }
        )
    }
    
    // Context menu for pinned apps
    expandedPinned.value?.let { pkgName ->
        state.pinnedApps.find { it.packageName == pkgName }?.let { app ->
            val isTracked = pkgName in trackedPackages
            AppContextMenu(
                app = app,
                onDismiss = { expandedPinned.value = null },
                onUnpin = { onUnpinApp(app.packageName) },
                onHide = { onHideApp(app.packageName) },
                onLock = { lockDialogApp.value = app },
                onAddTimeReminder = if (!isTracked) ({
                    onAddTrackedReminderApp(app.packageName, app.label)
                }) else null,
                onRemoveTimeReminder = if (isTracked) ({
                    onRemoveTrackedReminderApp(app.packageName)
                }) else null
            )
        }
    }
    
    // Lock duration dialog
    lockDialogApp.value?.let { app ->
        LockDurationDialog(
            appName = app.label,
            onDismiss = { lockDialogApp.value = null },
            onLock = { minutes ->
                onLockApp(app.packageName, minutes)
                lockDialogApp.value = null
            }
        )
    }
}

@Composable
private fun DailyTasksHomeSection(
    dailyTasks: List<DailyTaskItem>,
    onDailyTaskCompleted: (Long) -> Unit,
    onDailyTaskReset: (Long) -> Unit
) {
    val activeTasks = remember(dailyTasks) {
        dailyTasks
            .filter { it.isActiveToday && it.isEnabled }
            .sortedBy { it.createdAt }
    }

    if (activeTasks.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        activeTasks.forEach { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .combinedClickable(
                        onClick = {
                            if (task.isCompletedToday) {
                                onDailyTaskReset(task.id)
                            } else {
                                onDailyTaskCompleted(task.id)
                            }
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MinimalCheckbox(
                    checked = task.isCompletedToday,
                    onCheckedChange = { checked ->
                        if (checked) {
                            onDailyTaskCompleted(task.id)
                        } else {
                            onDailyTaskReset(task.id)
                        }
                    }
                )
                Text(
                    text = task.title,
                    color = if (task.isCompletedToday) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PinnedAppsHomeSection(
    pinnedApps: List<AppEntry>,
    onLaunchApp: (AppEntry) -> Unit,
    onLongPress: (AppEntry) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        pinnedApps.forEach { app ->
            Text(
                text = app.label,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongPress(app) }
                    ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DailyTaskManagerRow(
    task: DailyTaskItem,
    todayEpochDay: Long,
    formatter: DateTimeFormatter,
    onEditDailyTask: (DailyTaskItem) -> Unit,
    onDailyTaskEnabledChange: (Long, Boolean) -> Unit,
    onDailyTaskCompleted: (Long) -> Unit,
    onDailyTaskReset: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .combinedClickable(
                onClick = {
                    if (!task.isActiveToday || !task.isEnabled) return@combinedClickable
                    if (task.isCompletedToday) {
                        onDailyTaskReset(task.id)
                    } else {
                        onDailyTaskCompleted(task.id)
                    }
                },
                onLongClick = { onEditDailyTask(task) }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MinimalCheckbox(
            checked = task.isCompletedToday,
            onCheckedChange = { checked ->
                if (!task.isActiveToday || !task.isEnabled) return@MinimalCheckbox
                if (checked) {
                    onDailyTaskCompleted(task.id)
                } else {
                    onDailyTaskReset(task.id)
                }
            },
            enabled = task.isActiveToday && task.isEnabled
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = task.title,
                color = when {
                    !task.isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                    task.isCompletedToday -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onBackground
                },
                fontSize = 16.sp
            )
            val status = dailyTaskStatusText(task, todayEpochDay, formatter)
            val pattern = task.patternLabel()
            Text(
                text = status,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                text = pattern,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = task.isEnabled,
            onCheckedChange = { enabled -> onDailyTaskEnabledChange(task.id, enabled) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onBackground,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
    }
}

@Composable
private fun BottomIconRow(
    left: AppEntry?,
    right: AppEntry?,
    onLaunch: (AppEntry) -> Unit,
    onLongPress: (BottomIconSlot) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BottomIconButton(
            label = left?.label ?: stringResource(R.string.home_default_left_label),
            onClick = { left?.let(onLaunch) },
            onLongPress = { onLongPress(BottomIconSlot.LEFT) }
        )
        BottomIconButton(
            label = right?.label ?: stringResource(R.string.home_default_right_label),
            onClick = { right?.let(onLaunch) },
            onLongPress = { onLongPress(BottomIconSlot.RIGHT) }
        )
    }
}

@Composable
private fun BottomIconButton(
    label: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Text(
        text = label.uppercase(),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .padding(16.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
    )
}

@Composable
private fun TasksScreen(
    dailyTasks: List<DailyTaskItem>,
    tasks: List<TaskItem>,
    heldDailyTaskIds: Set<Long>,
    onToggleTask: (Long) -> Unit,
    onAddTask: (String, Long?) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onAddDailyTask: (String, Long?, Long?, Boolean, DailyTaskRepeatMode, Int, Int) -> Unit,
    onUpdateDailyTask: (Long, String, Long?, Long?, Boolean, DailyTaskRepeatMode, Int, Int) -> Unit,
    onDeleteDailyTask: (Long) -> Unit,
    onDailyTaskEnabledChange: (Long, Boolean) -> Unit,
    onDailyTaskCompleted: (Long) -> Unit,
    onDailyTaskReset: (Long) -> Unit
) {
    val showAddDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf<TaskItem?>(null) }
    val showEditDailyDialog = remember { mutableStateOf<DailyTaskItem?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 36.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.nav_tasks),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onOpenHistory) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = stringResource(R.string.task_history_icon),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            val enableDailyTask = onDailyTaskEnabledChange
            val completeDailyTask = onDailyTaskCompleted
            val resetDailyTask = onDailyTaskReset
            val todayEpochDay = remember { LocalDate.now().toEpochDay() }
            val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d") }
            val sortedDailyTasks = remember(dailyTasks, heldDailyTaskIds) {
                dailyTasks
                    .filter { task -> !task.isCompletedToday || task.id in heldDailyTaskIds }
                    .sortedWith(
                        compareByDescending<DailyTaskItem> { it.isActiveToday }
                            .thenByDescending { it.isEnabled }
                            .thenBy { it.createdAt }
                    )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                if (sortedDailyTasks.isNotEmpty()) {
                    items(sortedDailyTasks, key = { "daily-${it.id}" }) { task ->
                        DailyTaskManagerRow(
                            task = task,
                            todayEpochDay = todayEpochDay,
                            formatter = dateFormatter,
                            onEditDailyTask = { showEditDailyDialog.value = it },
                            onDailyTaskEnabledChange = { taskId, enabled -> enableDailyTask(taskId, enabled) },
                            onDailyTaskCompleted = { taskId -> completeDailyTask(taskId) },
                            onDailyTaskReset = { taskId -> resetDailyTask(taskId) }
                        )
                    }
                    if (tasks.isNotEmpty()) {
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }
                }
                items(tasks, key = { "task-${it.id}" }) { task ->
                    TaskItemRow(
                        task = task,
                        onToggle = { onToggleTask(task.id) },
                        onLongClick = { showEditDialog.value = task }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog.value = true },
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.task_add_icon))
        }
    }

    // Fancy Add Task Dialog
    if (showAddDialog.value) {
        FancyAddTaskDialog(
            onDismiss = { showAddDialog.value = false },
            onAddTask = { title, scheduledTime ->
                onAddTask(title, scheduledTime)
                showAddDialog.value = false
            },
            onAddDailyTask = { title, start, end, enabled, repeatMode, intervalDays, daysMask ->
                onAddDailyTask(title, start, end, enabled, repeatMode, intervalDays, daysMask)
                showAddDialog.value = false
            }
        )
    }

    // Edit/Delete Task Dialog
    showEditDialog.value?.let { task ->
        EditTaskDialog(
            task = task,
            onDismiss = { showEditDialog.value = null },
            onSave = { newTitle, scheduledTime ->
                onDeleteTask(task.id)
                onAddTask(newTitle, scheduledTime)
                showEditDialog.value = null
            },
            onDelete = {
                onDeleteTask(task.id)
                showEditDialog.value = null
            }
        )
    }

    showEditDailyDialog.value?.let { task ->
        DailyTaskEditorDialog(
            title = stringResource(R.string.task_edit_daily_title),
            initialTitle = task.title,
            initialStartEpochDay = task.startEpochDay,
            initialEndEpochDay = task.endEpochDay,
            initialEnabled = task.isEnabled,
            initialRepeatMode = task.repeatMode,
            initialIntervalDays = task.intervalDays,
            initialDaysOfWeekMask = task.daysOfWeekMask,
            onDismiss = { showEditDailyDialog.value = null },
            onConfirm = { updatedTitle, start, end, enabled, repeatMode, intervalDays, daysMask ->
                onUpdateDailyTask(task.id, updatedTitle, start, end, enabled, repeatMode, intervalDays, daysMask)
                if (!enabled && task.isCompletedToday) {
                    onDailyTaskReset(task.id)
                }
                showEditDailyDialog.value = null
            },
            onDelete = {
                onDeleteDailyTask(task.id)
                showEditDailyDialog.value = null
            }
        )
    }
}

@Composable
private fun TaskItemRow(
    task: TaskItem,
    onToggle: () -> Unit,
    onLongClick: () -> Unit
) {
    val isCompleted = task.isCompleted
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCompleted) 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) 
                else 
                    Color.Transparent
            )
            .combinedClickable(
                onClick = onToggle,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MinimalCheckbox(
            checked = isCompleted,
            onCheckedChange = { onToggle() }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = task.title,
                color = if (isCompleted) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            task.scheduledFor?.let { timestamp ->
                val dateTime = java.time.LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(timestamp),
                    java.time.ZoneId.systemDefault()
                )
                val today = java.time.LocalDate.now()
                val taskDate = dateTime.toLocalDate()
                val scheduledText = when {
                    taskDate == today -> stringResource(R.string.task_today_at, dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                    taskDate == today.plusDays(1) -> stringResource(R.string.task_tomorrow_at, dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                    timestamp < System.currentTimeMillis() -> stringResource(
                        R.string.task_overdue_at,
                        dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd 'at' HH:mm"))
                    )
                    else -> dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd 'at' HH:mm"))
                }
                Text(
                    text = scheduledText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (isCompleted) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(R.string.task_completed_icon),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun dailyTaskStatusText(
    task: DailyTaskItem,
    todayEpochDay: Long,
    formatter: DateTimeFormatter
): String {
    val today = LocalDate.ofEpochDay(todayEpochDay)
    val startDate = task.startEpochDay?.let { LocalDate.ofEpochDay(it) }
    val endDate = task.endEpochDay?.let { LocalDate.ofEpochDay(it) }

    return when {
        !task.isEnabled -> stringResource(R.string.task_status_paused)
        task.isActiveToday && task.isCompletedToday -> stringResource(R.string.task_status_completed_today)
        task.isActiveToday -> when {
            endDate != null -> stringResource(R.string.task_status_active_ends, endDate.format(formatter))
            startDate != null -> stringResource(R.string.task_status_active_since, startDate.format(formatter))
            else -> stringResource(R.string.task_status_active_every_day)
        }
        startDate != null && today.isBefore(startDate) -> stringResource(R.string.task_status_starts, startDate.format(formatter))
        endDate != null && today.isAfter(endDate) -> stringResource(R.string.task_status_ended, endDate.format(formatter))
        endDate != null -> stringResource(R.string.task_status_inactive_ended, endDate.format(formatter))
        else -> stringResource(R.string.task_status_inactive_today)
    }
}

// Smart search: prioritizes better matches
// 1. Starts with query (highest priority)
// 2. Word boundary match (query starts a word)
// 3. Contains as substring
// 4. Fuzzy match (scattered chars)
private fun fuzzyMatch(label: String, query: String): Boolean {
    if (query.isEmpty()) return true
    val lowerLabel = label.lowercase()
    val lowerQuery = query.lowercase().filter { it.isLetterOrDigit() }
    
    if (lowerQuery.isEmpty()) return true
    
    // 1. Exact prefix match
    if (lowerLabel.startsWith(lowerQuery)) return true
    
    // 2. Word boundary match (query starts a word)
    val words = lowerLabel.split(" ", "-", "_", ".", "&")
    if (words.any { it.startsWith(lowerQuery) }) return true
    
    // 3. Substring match
    if (lowerLabel.contains(lowerQuery)) return true
    
    // 4. Fuzzy scattered match (last resort)
    var queryIndex = 0
    for (char in lowerLabel) {
        if (queryIndex < lowerQuery.length && char == lowerQuery[queryIndex]) {
            queryIndex++
        }
    }
    
    return queryIndex == lowerQuery.length
}

// Calculate match score (lower is better)
// Used to sort search results by relevance
private fun matchScore(label: String, query: String): Int {
    val lowerLabel = label.lowercase()
    val lowerQuery = query.lowercase().filter { it.isLetterOrDigit() }
    
    if (lowerQuery.isEmpty()) return 999
    
    // 1. Exact match (score 0)
    if (lowerLabel == lowerQuery) return 0
    
    // 2. Starts with query (score 1)
    if (lowerLabel.startsWith(lowerQuery)) return 1
    
    // 3. Word boundary match (score 2)
    val words = lowerLabel.split(" ", "-", "_", ".", "&")
    if (words.any { it.startsWith(lowerQuery) }) return 2
    
    // 4. Contains as substring (score 3)
    if (lowerLabel.contains(lowerQuery)) return 3
    
    // 5. Fuzzy match (score 4)
    return 4
}

// New multiplier logic consistent with SearchManager
private fun calculateHybridMatchMultiplier(label: String, query: String): Double {
    val lowerLabel = label.lowercase()
    val isSingleChar = query.length == 1
    
    // Priority 1: Starts with query
    if (lowerLabel.startsWith(query)) {
        return if (isSingleChar) 1000.0 else 4.0
    }

    // Priority 2: Word starts with query (Check boundaries)
    // We already lowercased query and label for comparison
    var index = lowerLabel.indexOf(query)
    while (index >= 0) {
        if (index > 0 && !Character.isLetterOrDigit(lowerLabel[index - 1])) {
            return if (isSingleChar) 500.0 else 3.0
        }
        index = lowerLabel.indexOf(query, index + 1)
    }

    // Priority 3: Just contains
    return 1.0
}

@Composable
private fun AllAppsScreen(
    apps: List<AppEntry>,
    isDrawerVisible: Boolean,
    keyboardOnSwipe: Boolean,
    searchQuery: String,
    shouldFocusSearch: Boolean,
    unreadCount: Int,
    smartSuggestionsEnabled: Boolean,
    smartSuggestions: List<SmartSuggestion>,
    usageWeights: Map<String, Double>,
    onQueryChange: (String) -> Unit,
    onAppSelected: (AppEntry, AppUsageEventSource, String?) -> Unit,
    onPinApp: (String) -> Unit,
    onUnpinApp: (String) -> Unit,
    onHideApp: (String) -> Unit,
    onLockApp: (String, Long) -> Unit,
    onOpenNotificationInbox: () -> Unit,
    onOpenSettings: () -> Unit,
    trackedPackages: Set<String>,
    onAddTrackedReminderApp: (String, String) -> Unit,
    onRemoveTrackedReminderApp: (String) -> Unit,
    onboardingPinOnly: Boolean = false,
    onOnboardingContextMenuShown: () -> Unit = {}
) {
    val expandedApp = remember { mutableStateOf<String?>(null) }
    val lockDialogApp = remember { mutableStateOf<AppEntry?>(null) }
    val blinkingApp = remember { mutableStateOf<String?>(null) }
    val appToLaunch = remember { mutableStateOf<AppEntry?>(null) }
    val appLaunchSource = remember { mutableStateOf(AppUsageEventSource.ALL_APPS_SEARCH) }
    val searchActive = searchQuery.isNotBlank()
    val filteredApps = remember(apps, searchQuery, usageWeights) {
        if (searchActive) {
            val query = searchQuery.lowercase().filter { it.isLetterOrDigit() }
            if (query.isEmpty()) return@remember apps

            apps.filter { fuzzyMatch(it.label, searchQuery) }
                .sortedByDescending { app ->
                    val usage = usageWeights[app.packageName] ?: 0.0
                    val multiplier = calculateHybridMatchMultiplier(app.label, query)
                    (usage + 0.1) * multiplier
                }
        } else {
            apps
        }
    }
    val suggestionEntries = remember(smartSuggestions, searchActive, smartSuggestionsEnabled) {
        if (!searchActive && smartSuggestionsEnabled && smartSuggestions.isNotEmpty()) {
            smartSuggestions.take(3).map { it.app }
        } else {
            emptyList()
        }
    }
    val suggestionPackages = remember(suggestionEntries) {
        suggestionEntries.map { it.packageName }.toSet()
    }
    // Show all apps in the alphabetical list even if they appear in suggestions
    val remainingApps = filteredApps
    val listState = rememberLazyListState()
    val alphabet = remember { listOf('#') + ('A'..'Z').toList() }
    var fastScrollLetter by remember { mutableStateOf<Char?>(null) }
    var scrollJob by remember { mutableStateOf<Job?>(null) }
    var lastFastScrollTargetIndex by remember { mutableStateOf(-1) }
    val showFastScroll = !searchActive && remainingApps.isNotEmpty()
    val letterPositions = remember(searchActive, suggestionEntries, remainingApps) {
        if (searchActive || remainingApps.isEmpty()) {
            emptyMap()
        } else {
            val beforeRemainingCount = 1 + if (suggestionEntries.isNotEmpty()) {
                suggestionEntries.size + 2 // Frequent section + divider + All apps section + divider
            } else {
                0
            }

            val map = mutableMapOf<Char, Int>()
            // # maps to the very top (search box + frequently opened)
            map['#'] = 0
            remainingApps.forEachIndexed { index, app ->
                val letter = app.label.firstOrNull()?.uppercaseChar()?.takeIf { it.isLetter() } ?: '#'
                if (!map.containsKey(letter)) {
                    map[letter] = beforeRemainingCount + index
                }
            }
            map
        }
    }
    val availableLetters = remember(letterPositions) { letterPositions.keys }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var wasDrawerVisible by remember { mutableStateOf(isDrawerVisible) }

    LaunchedEffect(keyboardOnSwipe) {
        if (!keyboardOnSwipe) {
            keyboardController?.hide()
        }
    }

    LaunchedEffect(shouldFocusSearch, keyboardOnSwipe) {
        if (keyboardOnSwipe && shouldFocusSearch) {
            // Small delay to ensure focus target is attached to composition
            kotlinx.coroutines.delay(50)
            try {
                focusRequester.requestFocus()
                keyboardController?.show()
            } catch (e: IllegalStateException) {
                // Focus requester not ready yet, ignore
            }
        } else if (!shouldFocusSearch) {
            keyboardController?.hide()
        }
    }

    LaunchedEffect(isDrawerVisible) {
        if (wasDrawerVisible && !isDrawerVisible) {
            scrollJob?.cancel()
            scrollJob = null
            fastScrollLetter = null
            listState.scrollToItem(0)
        }
        wasDrawerVisible = isDrawerVisible
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.nav_all_apps),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                // Notification bell with unread badge.
                // Badge is outside IconButton to avoid clipping by the
                // button's bounded layout (IconButton clips children).
                Box(contentAlignment = Alignment.Center) {
                    IconButton(onClick = onOpenNotificationInbox) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = stringResource(R.string.notif_inbox_title),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 6.dp)
                                .size(18.dp)
                                .background(MaterialTheme.colorScheme.error, CircleShape)
                                .wrapContentSize(Alignment.Center),
                        ) {
                            Text(
                                text = unreadCountLabel(unreadCount),
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (searchQuery.isBlank()) {
                    Text(
                        text = stringResource(R.string.label_search_apps),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.53f)
                    )
                }
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val firstApp = filteredApps.firstOrNull()
                            if (firstApp != null) {
                                appLaunchSource.value = AppUsageEventSource.ALL_APPS_SEARCH
                                appToLaunch.value = firstApp
                                blinkingApp.value = firstApp.packageName
                                keyboardController?.hide()
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
            }
            
            // Blink effect and launch
            LaunchedEffect(appToLaunch.value) {
                appToLaunch.value?.let { app ->
                    // Blink 3 times
                    blinkingApp.value = app.packageName
                    kotlinx.coroutines.delay(150)
                    blinkingApp.value = null
                    kotlinx.coroutines.delay(150)
                    blinkingApp.value = app.packageName
                    kotlinx.coroutines.delay(150)
                    blinkingApp.value = null
                    kotlinx.coroutines.delay(150)
                    blinkingApp.value = app.packageName
                    kotlinx.coroutines.delay(150)
                    blinkingApp.value = null
                    
                    // Launch the app
                    val queryContext = searchQuery.takeIf { it.isNotBlank() }
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                    onQueryChange("")
                    onAppSelected(app, appLaunchSource.value, queryContext)
                    appLaunchSource.value = AppUsageEventSource.ALL_APPS_SEARCH
                    appToLaunch.value = null
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (filteredApps.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.home_no_apps_match),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            if (suggestionEntries.isNotEmpty()) {
                item { SectionDivider(label = stringResource(R.string.home_frequently_opened)) }
                items(suggestionEntries) { app ->
                    val isBlinking = blinkingApp.value == app.packageName
                    Text(
                        text = app.label,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .alpha(if (isBlinking) 0.3f else 1f)
                            .combinedClickable(
                                onClick = {
                                    val queryContext = if (searchActive) searchQuery else null
                                    onAppSelected(app, AppUsageEventSource.SMART_SUGGESTION, queryContext)
                                },
                                onLongClick = { expandedApp.value = app.packageName }
                            )
                    )
                }
                item { SectionDivider(label = stringResource(R.string.home_all_apps_section)) }
            }

            items(remainingApps) { app ->
                val isBlinking = blinkingApp.value == app.packageName
                Text(
                    text = app.label,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .alpha(if (isBlinking) 0.3f else 1f)
                        .combinedClickable(
                            onClick = {
                                val queryContext = if (searchActive) searchQuery else null
                                onAppSelected(app, AppUsageEventSource.ALL_APPS_LIST, queryContext)
                            },
                            onLongClick = { expandedApp.value = app.packageName }
                        )
                )
            }
        }
    }

    if (showFastScroll) {
        // Feedback bubble: shows active letter in a large circle to the left of the rail
        if (fastScrollLetter != null) {
            val activeIdx = alphabet.indexOf(fastScrollLetter!!)
            val railTopPadding = if (suggestionEntries.isNotEmpty()) 192.dp else 160.dp
            if (activeIdx >= 0) {
                val fraction = activeIdx.toFloat() / (alphabet.size - 1).coerceAtLeast(1)
                androidx.compose.foundation.layout.BoxWithConstraints(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxHeight()
                        .padding(end = 44.dp, top = railTopPadding, bottom = 24.dp)
                ) {
                    val railHeightDp = maxHeight
                    val bubbleOffset = railHeightDp * fraction - 21.dp // center the 42dp bubble
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(y = bubbleOffset.coerceAtLeast(0.dp))
                            .size(42.dp)
                            .background(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                CircleShape
                            )
                    ) {
                        Text(
                            text = fastScrollLetter.toString(),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        FastScrollRail(
            letters = alphabet,
            availableLetters = availableLetters,
            activeLetter = fastScrollLetter,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                .padding(end = 2.dp, top = if (suggestionEntries.isNotEmpty()) 192.dp else 160.dp, bottom = 24.dp),
            onDragPosition = { fraction, letter ->
                fastScrollLetter = letter
                focusManager.clearFocus(force = true)
                keyboardController?.hide()

                // Use letterPositions to find the exact index for this letter
                // or the next available letter
                var targetIndex: Int? = letterPositions[letter]
                
                // If letter not found, find the next available letter
                if (targetIndex == null) {
                    for (nextLetter in (alphabet.indexOf(letter) + 1) until alphabet.size) {
                        val candidate = alphabet[nextLetter]
                        targetIndex = letterPositions[candidate]
                        if (targetIndex != null) break
                    }
                }
                
                // Fallback to proportional position if no letter found
                if (targetIndex == null) {
                    val remainingStartIndex = 1 + if (suggestionEntries.isNotEmpty()) {
                        suggestionEntries.size + 2
                    } else {
                        0
                    }
                    val targetWithinRemaining =
                        ((remainingApps.size - 1) * fraction).roundToInt().coerceIn(0, remainingApps.lastIndex)
                    targetIndex = remainingStartIndex + targetWithinRemaining
                }

                if (targetIndex == lastFastScrollTargetIndex) return@FastScrollRail
                lastFastScrollTargetIndex = targetIndex

                scrollJob?.cancel()
                scrollJob = null
                scrollJob = coroutineScope.launch {
                    // For large jumps (e.g., Z to A), animateScrollToItem causes massive lag
                    // as Compose tries to measure all intermediate items. We use instant scroll
                    // for large distances, and smooth animation for small scrubs.
                    if (kotlin.math.abs(listState.firstVisibleItemIndex - targetIndex) > 15) {
                        listState.scrollToItem(targetIndex, scrollOffset = 0)
                    } else {
                        listState.animateScrollToItem(targetIndex, scrollOffset = 0)
                    }
                }
            },
            onInteractionEnd = {
                fastScrollLetter = null
                lastFastScrollTargetIndex = -1
                scrollJob?.cancel()
                scrollJob = null
            }
        )
    }
        
    // Context menu for regular apps
    expandedApp.value?.let { pkgName ->
        filteredApps.find { it.packageName == pkgName }?.let { app ->
            // Notify parent that context menu is shown during onboarding
            if (onboardingPinOnly) {
                LaunchedEffect(pkgName) {
                    onOnboardingContextMenuShown()
                }
            }

            val isTracked = pkgName in trackedPackages
            AppContextMenu(
                app = app,
                onDismiss = { expandedApp.value = null },
                onPin = if (!app.isPinned) ({ onPinApp(app.packageName) }) else null,
                onUnpin = if (!onboardingPinOnly && app.isPinned) ({ onUnpinApp(app.packageName) }) else null,
                onHide = if (!onboardingPinOnly) ({ onHideApp(app.packageName) }) else null,
                onLock = if (!onboardingPinOnly) ({ lockDialogApp.value = app }) else null,
                onAddTimeReminder = if (!onboardingPinOnly && !isTracked) ({
                    onAddTrackedReminderApp(app.packageName, app.label)
                }) else null,
                onRemoveTimeReminder = if (!onboardingPinOnly && isTracked) ({
                    onRemoveTrackedReminderApp(app.packageName)
                }) else null
            )
        }
    }
        
        // Lock duration dialog
        lockDialogApp.value?.let { app ->
            LockDurationDialog(
                appName = app.label,
                onDismiss = { lockDialogApp.value = null },
                onLock = { minutes ->
                    onLockApp(app.packageName, minutes)
                    lockDialogApp.value = null
                }
            )
        }
    }
}

@Composable
private fun SectionDivider(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FastScrollRail(
    modifier: Modifier = Modifier,
    letters: List<Char>,
    availableLetters: Set<Char>,
    activeLetter: Char?,
    onDragPosition: (fraction: Float, letter: Char) -> Unit,
    onInteractionEnd: () -> Unit
) {
    if (letters.isEmpty()) return
    var railHeight by remember { mutableStateOf(0f) }

    Column(
        modifier = modifier
            .width(32.dp)
            .onGloballyPositioned { railHeight = it.size.height.toFloat() }
            .pointerInput(letters) {
                detectVerticalDragGestures(
                    onDragStart = { offset ->
                        val letter = letterForOffset(offset.y, railHeight, letters) ?: return@detectVerticalDragGestures
                        val fraction = fractionForOffset(offset.y, railHeight)
                        onDragPosition(fraction, letter)
                    },
                    onVerticalDrag = { change, _ ->
                        val letter = letterForOffset(change.position.y, railHeight, letters) ?: return@detectVerticalDragGestures
                        val fraction = fractionForOffset(change.position.y, railHeight)
                        onDragPosition(fraction, letter)
                    },
                    onDragEnd = onInteractionEnd,
                    onDragCancel = onInteractionEnd
                )
            },
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val activeIndex = if (activeLetter != null) letters.indexOf(activeLetter) else -1

        letters.forEachIndexed { index, letter ->
            // Distance from the active letter (used for the curve)
            val distance = if (activeIndex >= 0) kotlin.math.abs(index - activeIndex) else Int.MAX_VALUE

            val targetBulge = if (activeIndex >= 0) {
                val maxBulge = -50f  // max shift for the selected letter
                val curveRadius = 5  // how many letters participate in the curve
                if (distance <= curveRadius) {
                    // Cosine curve for smooth falloff
                    val t = distance.toFloat() / curveRadius
                    maxBulge * kotlin.math.cos(t * (Math.PI / 2.0)).toFloat()
                } else {
                    0f
                }
            } else {
                0f
            }

            val animatedBulge by animateFloatAsState(
                targetValue = targetBulge,
                animationSpec = tween(durationMillis = 150),
                label = "bulge"
            )

            val isActive = distance == 0 && activeIndex >= 0
            val isNear = distance in 1..2 && activeIndex >= 0
            val isAvailable = availableLetters.contains(letter)

            val color = when {
                isActive -> MaterialTheme.colorScheme.onBackground
                isNear && isAvailable -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                isAvailable -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
            }
            val fontSize = when {
                isActive -> 16.sp
                isNear -> 12.sp
                else -> 11.sp
            }
            val fontWeight = when {
                isActive -> FontWeight.Bold
                isNear -> FontWeight.Medium
                else -> FontWeight.Normal
            }

            Text(
                text = letter.toString(),
                color = color,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    translationX = animatedBulge
                }
            )
        }
    }
}

private fun letterForOffset(y: Float, height: Float, letters: List<Char>): Char? {
    if (height <= 0f || letters.isEmpty()) return null
    val index = ((y / height) * letters.size).toInt().coerceIn(0, letters.lastIndex)
    return letters.getOrNull(index)
}

private fun fractionForOffset(y: Float, height: Float): Float {
    if (height <= 0f) return 0f
    return (y / height).coerceIn(0f, 1f)
}

@Composable
private fun SearchOverlay(
    query: String,
    results: List<SearchResult>,
    onQueryChange: (String) -> Unit,
    autoFocus: Boolean = false,
    onResultClick: (AppEntry) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.9f))
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.align(Alignment.TopCenter)) {
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            LaunchedEffect(autoFocus) {
                if (autoFocus) {
                    // Small delay to ensure focus target is attached to composition
                    kotlinx.coroutines.delay(50)
                    try {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    } catch (e: IllegalStateException) {
                        // Focus requester not ready yet, ignore
                    }
                } else {
                    keyboardController?.hide()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(results) { result ->
                    when (result) {
                        is SearchResult.App -> Text(
                            text = result.entry.label,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .combinedClickable(
                                    onClick = { onResultClick(result.entry) },
                                    onLongClick = {}
                                )
                        )
                        is SearchResult.Task -> Text(
                            text = result.item.title,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                        is SearchResult.Command -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomIconPickerDialog(
    slot: BottomIconSlot?,
    apps: List<AppEntry>,
    onDismiss: () -> Unit,
    onSelect: (BottomIconSlot, String) -> Unit
) {
    if (slot == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.home_assign_app)) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                items(apps) { app ->
                    Text(
                        text = app.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .combinedClickable(
                                onClick = { onSelect(slot, app.packageName) },
                                onLongClick = { onSelect(slot, app.packageName) }
                            )
                    )
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun SettingsScreen(
    clockFormat: ClockFormat,
    showSeconds: Boolean,
    textSize: com.minifocus.launcher.model.TextSize,
    theme: LauncherTheme,
    showDailyTasksOnHome: Boolean,
    bottomLeftApp: AppEntry?,
    bottomRightApp: AppEntry?,
    keyboardOnSwipe: Boolean,
    smartSuggestionsEnabled: Boolean,
    notificationInboxEnabled: Boolean,
    notificationRetentionDays: Int,
    logRetentionDays: Int,
    permissionsState: PermissionsState,
    onOpenHomeSettings: () -> Unit,
    onOpenClockSettings: () -> Unit,
    onOpenAppearanceSettings: () -> Unit,
    onOpenAppDrawerSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenPermissionManager: () -> Unit,
    onOpenDeviceSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenBackupSettings: () -> Unit,
    onOpenAppTimeReminderSettings: () -> Unit,
    onOpenCalendarSettings: () -> Unit,
    onOpenLanguageSettings: () -> Unit,
    selectedCalendarAccountName: String,
    trackedReminderAppsCount: Int,
    onResetOnboarding: () -> Unit = {},
    onBack: () -> Unit
) {
    val homeSummary = buildString {
        val statusLabel = if (showDailyTasksOnHome) {
            stringResource(R.string.home_daily_tasks_visible)
        } else {
            stringResource(R.string.home_daily_tasks_hidden)
        }
        val leftLabel = bottomLeftApp?.label ?: stringResource(R.string.label_none)
        val rightLabel = bottomRightApp?.label ?: stringResource(R.string.label_none)
        val bottomSummary = stringResource(R.string.home_bottom_summary_format, leftLabel, rightLabel)
        append(stringResource(R.string.home_summary_format, statusLabel, bottomSummary))
    }

    val clockSummary = buildString {
        val formatLabel = if (clockFormat == ClockFormat.H24) {
            stringResource(R.string.clock_format_24)
        } else {
            stringResource(R.string.clock_format_12)
        }
        append(formatLabel)
        append(if (showSeconds) stringResource(R.string.clock_seconds_on) else stringResource(R.string.clock_seconds_off))
    }

    val notificationSummary = if (notificationInboxEnabled) {
        val notifLabel = pluralStringResource(R.plurals.label_day_count, notificationRetentionDays, notificationRetentionDays)
        val logLabel = pluralStringResource(R.plurals.label_day_count, logRetentionDays, logRetentionDays)
        stringResource(R.string.summary_inbox_on_format, notifLabel, logLabel)
    } else {
        stringResource(R.string.summary_inbox_disabled)
    }

    val optionalMissing = buildList {
        if (!permissionsState.notificationListenerGranted) add(stringResource(R.string.permissions_notification_access_label))
        if (!permissionsState.exactAlarmsGranted) add(stringResource(R.string.permissions_exact_alarms_label))
        if (!permissionsState.lockAccessibilityGranted) add(stringResource(R.string.permissions_lock_service_label))
        if (!permissionsState.usageStatsGranted) add(stringResource(R.string.permissions_usage_access_label))
        if (!permissionsState.overlayGranted) add(stringResource(R.string.permissions_overlay_label))
    }

    val permissionSummary = when {
        !permissionsState.notificationsGranted -> stringResource(R.string.permissions_notifications_required)
        optionalMissing.isEmpty() -> stringResource(R.string.permissions_all_granted)
        else -> stringResource(R.string.permissions_missing, optionalMissing.joinToString(", "))
    }

    val appDrawerSummary = buildString {
        append(
            if (keyboardOnSwipe) {
                stringResource(R.string.settings_keyboard_opens_with_swipe)
            } else {
                stringResource(R.string.settings_keyboard_opens_manually)
            }
        )
        append(
            if (smartSuggestionsEnabled) {
                " · ${stringResource(R.string.settings_smart_suggestions_on)}"
            } else {
                " · ${stringResource(R.string.settings_smart_suggestions_off)}"
            }
        )
    }

    val themeName = when (theme) {
        LauncherTheme.AMOLED -> stringResource(R.string.appearance_theme_dark)
        LauncherTheme.LIGHT -> stringResource(R.string.appearance_theme_light)
    }
    val appearanceSummary = stringResource(R.string.settings_theme_summary_format, themeName, textSize.label)

    val deviceSettingsSummary = stringResource(R.string.settings_device_settings_summary)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        SettingsRow(
            title = stringResource(R.string.settings_home_screen),
            subtitle = homeSummary,
            onClick = onOpenHomeSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRow(
            title = stringResource(R.string.settings_clock),
            subtitle = clockSummary,
            onClick = onOpenClockSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRow(
            title = stringResource(R.string.settings_notification_bar),
            subtitle = notificationSummary,
            onClick = onOpenNotificationSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRow(
            title = stringResource(R.string.settings_app_drawer),
            subtitle = appDrawerSummary,
            onClick = onOpenAppDrawerSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        val reminderSummary = if (trackedReminderAppsCount > 0) {
            stringResource(
                R.string.label_apps_tracked_count,
                trackedReminderAppsCount,
                if (trackedReminderAppsCount != 1) stringResource(R.string.label_plural_suffix) else ""
            )
        } else {
            stringResource(R.string.settings_no_apps_tracked)
        }

        SettingsRow(
            title = stringResource(R.string.settings_app_reminders),
            subtitle = reminderSummary,
            onClick = onOpenAppTimeReminderSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRow(
            title = stringResource(R.string.settings_appearance),
            subtitle = appearanceSummary,
            onClick = onOpenAppearanceSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        val calendarSummary = if (selectedCalendarAccountName.isNotEmpty()) {
            selectedCalendarAccountName
        } else {
            "Auto-detect"
        }
        SettingsRow(
            title = "Calendar",
            subtitle = calendarSummary,
            onClick = onOpenCalendarSettings
        )

        Spacer(modifier = Modifier.height(32.dp))

        SettingsRow(
            title = stringResource(R.string.settings_device_settings),
            subtitle = deviceSettingsSummary,
            onClick = onOpenDeviceSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRow(
            title = stringResource(R.string.settings_permissions),
            subtitle = permissionSummary,
            onClick = onOpenPermissionManager
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        SettingsRow(
            title = stringResource(R.string.settings_backup_restore),
            subtitle = stringResource(R.string.settings_backup_restore_subtitle),
            onClick = onOpenBackupSettings
        )
        
        Spacer(modifier = Modifier.height(20.dp))

        SettingsRow(
            title = stringResource(R.string.language_settings_title),
            subtitle = stringResource(R.string.language_settings_subtitle),
            onClick = onOpenLanguageSettings
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRow(
            title = stringResource(R.string.settings_about),
            subtitle = stringResource(R.string.settings_about_subtitle),
            onClick = onOpenAbout
        )

        val showDebugOptions = false
        if (showDebugOptions) {
            Spacer(modifier = Modifier.height(32.dp))

            // Dev/debug: replay onboarding
            SettingsRow(
                title = stringResource(R.string.onboarding_replay),
                subtitle = null,
                onClick = onResetOnboarding
            )
        }
    }
}

@Composable
private fun HomeSettingsScreen(
    showDailyTasksOnHome: Boolean,
    doubleTapLockScreen: Boolean,
    lockAccessibilityGranted: Boolean,
    bottomLeftApp: AppEntry?,
    bottomRightApp: AppEntry?,
    onToggleDailyTasksOnHome: (Boolean) -> Unit,
    onToggleDoubleTapLockScreen: (Boolean) -> Unit,
    onRequestLockAccessibility: () -> Unit,
    onBottomIconClick: (BottomIconSlot) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.home_settings_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.home_settings_daily_tasks_module),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_settings_show_daily_tasks),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                val description = stringResource(R.string.home_settings_daily_tasks_hint)
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = showDailyTasksOnHome,
                onCheckedChange = onToggleDailyTasksOnHome
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.home_settings_gestures),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_settings_double_tap_lock),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                val description = if (!lockAccessibilityGranted) {
                    stringResource(R.string.home_settings_lock_service_disabled)
                } else {
                    stringResource(R.string.home_settings_lock_service_enabled)
                }
                Text(
                    text = description,
                    color = if (!lockAccessibilityGranted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = doubleTapLockScreen && lockAccessibilityGranted,
                onCheckedChange = { enabled ->
                    if (enabled && !lockAccessibilityGranted) {
                        onRequestLockAccessibility()
                    } else {
                        onToggleDoubleTapLockScreen(enabled)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.home_bottom_quick_launch),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsRow(
            title = stringResource(R.string.home_left_icon),
            subtitle = bottomLeftApp?.label ?: stringResource(R.string.label_none),
            onClick = { onBottomIconClick(BottomIconSlot.LEFT) }
        )

        SettingsRow(
            title = stringResource(R.string.home_right_icon),
            subtitle = bottomRightApp?.label ?: stringResource(R.string.label_none),
            onClick = { onBottomIconClick(BottomIconSlot.RIGHT) }
        )
    }
}

@Composable
private fun ClockSettingsScreen(
    clockFormat: ClockFormat,
    showSeconds: Boolean,
    onClockFormatChange: (ClockFormat) -> Unit,
    onShowSecondsToggle: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.clock_settings_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.clock_settings_time_format),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        ClockFormat.values().forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .combinedClickable(onClick = { onClockFormatChange(option) })
            ) {
                RadioButton(
                    selected = clockFormat == option,
                    onClick = { onClockFormatChange(option) }
                )
                Text(
                    text = when (option) {
                        ClockFormat.H24 -> stringResource(R.string.clock_format_24)
                        ClockFormat.H12 -> stringResource(R.string.clock_format_12)
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.clock_settings_seconds),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.clock_settings_show_seconds),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.clock_settings_seconds_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = showSeconds,
                onCheckedChange = onShowSecondsToggle
            )
        }
    }
}

@Composable
private fun HiddenAppsScreen(
    hiddenApps: List<AppEntry>,
    onUnhideApp: (String) -> Unit,
    onBack: () -> Unit
) {
    val holdDurationMs = 10_000L
    var unlocked by remember { mutableStateOf(false) }
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isHolding, unlocked) {
        if (isHolding && !unlocked) {
            val start = System.currentTimeMillis()
            while (isHolding && !unlocked) {
                val elapsed = System.currentTimeMillis() - start
                val progress = (elapsed / holdDurationMs.toFloat()).coerceIn(0f, 1f)
                holdProgress = progress
                if (progress >= 1f) {
                    unlocked = true
                    holdProgress = 1f
                    isHolding = false
                }
                kotlinx.coroutines.delay(16)
            }
            if (!unlocked && !isHolding) {
                holdProgress = 0f
            }
        } else if (!unlocked) {
            holdProgress = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.drawer_settings_hidden_apps),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!unlocked) {
            Text(
                text = stringResource(R.string.drawer_settings_hidden_apps_reveal),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 16.sp
            )
            Text(
                text = stringResource(R.string.drawer_settings_releasing_resets),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.13f))
                        .pointerInput(unlocked) {
                            if (unlocked) return@pointerInput
                            detectTapGestures(
                                onPress = {
                                    if (unlocked) return@detectTapGestures
                                    isHolding = true
                                    try {
                                        tryAwaitRelease()
                                    } finally {
                                        if (!unlocked) {
                                            isHolding = false
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { holdProgress },
                        strokeWidth = 6.dp,
                        color = MaterialTheme.colorScheme.onBackground,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp)
                    )
                Text(
                    text = when {
                        unlocked -> stringResource(R.string.drawer_settings_unlocked)
                        isHolding -> stringResource(R.string.drawer_settings_unlocking)
                        holdProgress > 0f -> stringResource(R.string.drawer_settings_keep_holding)
                        else -> stringResource(R.string.drawer_settings_hold)
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.drawer_settings_hidden_apps_gate),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        } else {
            Text(
                text = stringResource(R.string.drawer_settings_hidden_apps_unlocked),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.drawer_settings_hidden_apps_tap_unhide),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (hiddenApps.isEmpty()) {
                Text(
                    text = stringResource(R.string.drawer_settings_no_hidden_apps),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            } else {
                hiddenApps.forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = app.label,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { onUnhideApp(app.packageName) }) {
                            Text(text = stringResource(R.string.action_unhide))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppDrawerSettingsScreen(
    keyboardOnSwipe: Boolean,
    smartSuggestionsEnabled: Boolean,
    hiddenAppsCount: Int,
    onKeyboardToggle: (Boolean) -> Unit,
    onSmartSuggestionsToggle: (Boolean) -> Unit,
    onResetSmartSuggestions: () -> Unit,
    onOpenHiddenApps: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.settings_app_drawer),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.drawer_settings_keyboard),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.drawer_settings_open_keyboard_on_swipe),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.drawer_settings_keyboard_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(checked = keyboardOnSwipe, onCheckedChange = onKeyboardToggle)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.home_settings_smart_suggestions),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_settings_show_adaptive_row),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
            }
            Switch(
                checked = smartSuggestionsEnabled,
                onCheckedChange = onSmartSuggestionsToggle
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onResetSmartSuggestions,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
        ) {
            Text(text = stringResource(R.string.home_settings_reset_learning))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.drawer_settings_hidden_apps),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        SettingsRow(
            title = stringResource(R.string.drawer_settings_hidden_apps),
            subtitle = if (hiddenAppsCount == 0) {
                stringResource(R.string.drawer_settings_no_apps_hidden)
            } else {
                stringResource(R.string.drawer_settings_hidden_count, hiddenAppsCount)
            },
            onClick = onOpenHiddenApps
        )
        Text(
            text = stringResource(R.string.drawer_settings_hidden_apps_manage),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun LockDurationDialog(
    appName: String,
    onDismiss: () -> Unit,
    onLock: (Long) -> Unit
) {
    val predefinedDurations = listOf(
        stringResource(R.string.label_minutes_format, 30) to 30L,
        stringResource(R.string.label_hours_format, 1) to 60L,
        stringResource(R.string.label_hours_format_plural, 2) to 120L,
        stringResource(R.string.label_hours_format_plural, 4) to 240L,
        stringResource(R.string.label_hours_format_plural, 8) to 480L,
        stringResource(R.string.label_hours_format_plural, 12) to 720L,
        pluralStringResource(R.plurals.label_day_count, 1, 1) to 1440L,
        pluralStringResource(R.plurals.label_day_count, 3, 3) to 4320L,
        pluralStringResource(R.plurals.label_week_count, 1, 1) to 10080L
    )
    
    val sliderPosition = remember { mutableFloatStateOf(0f) }
    val customHours = remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Color.Transparent,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.time_intention_lock_app, appName),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Text(
                    text = predefinedDurations[sliderPosition.floatValue.toInt()].first,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Slider(
                    value = sliderPosition.floatValue,
                    onValueChange = { sliderPosition.floatValue = it },
                    valueRange = 0f..(predefinedDurations.size - 1).toFloat(),
                    steps = predefinedDurations.size - 2,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.onBackground,
                        activeTrackColor = MaterialTheme.colorScheme.onBackground,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = stringResource(R.string.clock_settings_custom_duration),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    if (customHours.value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.label_example_hours),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                    BasicTextField(
                        value = customHours.value,
                        onValueChange = { 
                            if (it.isEmpty() || (it.all { char -> char.isDigit() } && it.length <= 4)) {
                                customHours.value = it
                            }
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = onDismiss)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.action_cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.onBackground)
                            .clickable {
                                val minutes = if (customHours.value.isNotEmpty()) {
                                    customHours.value.toLongOrNull()?.times(60) ?: 0L
                                } else {
                                    predefinedDurations[sliderPosition.floatValue.toInt()].second
                                }
                                if (minutes > 0) {
                                    onLock(minutes)
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.time_intention_lock),
                            color = MaterialTheme.colorScheme.background,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
