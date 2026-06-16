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

package com.minifocus.launcher.manager

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.minifocus.launcher.model.BottomIconSlot
import com.minifocus.launcher.model.ClockFormat
import com.minifocus.launcher.model.LauncherTheme
import com.minifocus.launcher.model.TextSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class SettingsManager(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val clockFormat = stringPreferencesKey("clock_format")
        val fontChoice = stringPreferencesKey("font_choice")
        val textSize = stringPreferencesKey("text_size")
        val bottomLeft = stringPreferencesKey("bottom_left_package")
        val bottomRight = stringPreferencesKey("bottom_right_package")
        val keyboardSearchOnSwipe = intPreferencesKey("keyboard_search_on_swipe")
        val showSeconds = intPreferencesKey("show_seconds")
        val notificationInboxEnabled = intPreferencesKey("notification_inbox_enabled")
        val notificationRetentionDays = intPreferencesKey("notification_retention_days")
        val logRetentionDays = intPreferencesKey("log_retention_days")
        val lastLogRotationAt = longPreferencesKey("last_log_rotation_at")
        val showDailyTasksOnHome = intPreferencesKey("show_daily_tasks_on_home")
        val permissionOnboardingAcknowledged = intPreferencesKey("permission_onboarding_acknowledged")
        val doubleTapLockScreen = intPreferencesKey("double_tap_lock_screen")
        val smartSuggestionsEnabled = intPreferencesKey("smart_suggestions_enabled")
        val selectedCalendarId = longPreferencesKey("selected_calendar_id")
        val selectedCalendarAccountName = stringPreferencesKey("selected_calendar_account_name")
        val syncTasksWithCalendar = intPreferencesKey("sync_tasks_with_calendar")
        val syncTasksWithDate = intPreferencesKey("sync_tasks_with_date")
        val syncDailyReminders = intPreferencesKey("sync_daily_reminders")
        val showTasksInCalendar = intPreferencesKey("show_tasks_in_calendar")
        val onboardingComplete = intPreferencesKey("onboarding_complete")
    }

    fun observeTheme(): Flow<LauncherTheme> = dataStore.data.map { prefs ->
        prefs[Keys.theme]?.let { runCatching { LauncherTheme.valueOf(it) }.getOrNull() } ?: LauncherTheme.AMOLED
    }

    fun observeClockFormat(): Flow<ClockFormat> = dataStore.data.map { prefs ->
        prefs[Keys.clockFormat]?.let { runCatching { ClockFormat.valueOf(it) }.getOrNull() } ?: ClockFormat.H24
    }

    fun observeTextSize(): Flow<TextSize> = dataStore.data.map { prefs ->
        prefs[Keys.textSize]?.let { runCatching { TextSize.valueOf(it) }.getOrNull() } ?: TextSize.MEDIUM
    }

    fun observeBottomIcon(slot: BottomIconSlot): Flow<String?> = dataStore.data.map { prefs ->
        when (slot) {
            BottomIconSlot.LEFT -> prefs[Keys.bottomLeft]
            BottomIconSlot.RIGHT -> prefs[Keys.bottomRight]
        }
    }

    fun observeKeyboardSearchOnSwipe(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.keyboardSearchOnSwipe]?.let { it == 1 } ?: false
    }

    fun observeShowSeconds(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.showSeconds]?.let { it == 1 } ?: false
    }

    fun observeShowDailyTasksOnHome(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.showDailyTasksOnHome]?.let { it == 1 } ?: true
    }

    fun observeSmartSuggestionsEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.smartSuggestionsEnabled]?.let { it == 1 } ?: true
    }

    fun observeNotificationInboxEnabled(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.notificationInboxEnabled]?.let { it == 1 } ?: false
    }

    fun observePermissionOnboardingAcknowledged(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.permissionOnboardingAcknowledged]?.let { it == 1 } ?: false
    }

    fun observeDoubleTapLockScreen(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.doubleTapLockScreen]?.let { it == 1 } ?: false
    }

    fun observeOnboardingComplete(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.onboardingComplete]?.let { it == 1 } ?: false
    }

    fun observeNotificationRetentionDays(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.notificationRetentionDays] ?: DEFAULT_NOTIFICATION_RETENTION_DAYS
    }

    fun observeLogRetentionDays(): Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.logRetentionDays] ?: DEFAULT_LOG_RETENTION_DAYS
    }

    fun observeLastLogRotation(): Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.lastLogRotationAt] ?: 0L
    }

    suspend fun setTheme(theme: LauncherTheme) {
        dataStore.edit { prefs -> prefs[Keys.theme] = theme.name }
    }

    suspend fun setClockFormat(format: ClockFormat) {
        dataStore.edit { prefs -> prefs[Keys.clockFormat] = format.name }
    }
    suspend fun setTextSize(size: TextSize) {
        dataStore.edit { prefs -> prefs[Keys.textSize] = size.name }
    }
    suspend fun setBottomIcon(slot: BottomIconSlot, packageName: String) {
        dataStore.edit { prefs ->
            when (slot) {
                BottomIconSlot.LEFT -> prefs[Keys.bottomLeft] = packageName
                BottomIconSlot.RIGHT -> prefs[Keys.bottomRight] = packageName
            }
        }
    }

    suspend fun setKeyboardSearchOnSwipe(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.keyboardSearchOnSwipe] = if (enabled) 1 else 0
        }
    }

    suspend fun setShowSeconds(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.showSeconds] = if (enabled) 1 else 0
        }
    }

    suspend fun setShowDailyTasksOnHome(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.showDailyTasksOnHome] = if (enabled) 1 else 0
        }
    }

    suspend fun setSmartSuggestionsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.smartSuggestionsEnabled] = if (enabled) 1 else 0
        }
    }

    suspend fun setNotificationInboxEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.notificationInboxEnabled] = if (enabled) 1 else 0
        }
    }

    suspend fun setPermissionOnboardingAcknowledged(acknowledged: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.permissionOnboardingAcknowledged] = if (acknowledged) 1 else 0
        }
    }

    suspend fun getPermissionOnboardingAcknowledged(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[Keys.permissionOnboardingAcknowledged]?.let { it == 1 } ?: false
    }

    suspend fun setNotificationRetentionDays(days: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.notificationRetentionDays] = days
        }
    }

    suspend fun setLogRetentionDays(days: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.logRetentionDays] = days
        }
    }

    suspend fun setLastLogRotation(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.lastLogRotationAt] = timestamp
        }
    }

    suspend fun setDoubleTapLockScreen(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.doubleTapLockScreen] = if (enabled) 1 else 0
        }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.onboardingComplete] = if (complete) 1 else 0
        }
    }

    suspend fun getOnboardingComplete(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[Keys.onboardingComplete]?.let { it == 1 } ?: false
    }

    fun observeSelectedCalendarId(): Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.selectedCalendarId] ?: -1L
    }

    fun observeSelectedCalendarAccountName(): Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.selectedCalendarAccountName] ?: ""
    }

    suspend fun setSelectedCalendar(calendarId: Long, accountName: String) {
        dataStore.edit { prefs ->
            prefs[Keys.selectedCalendarId] = calendarId
            prefs[Keys.selectedCalendarAccountName] = accountName
        }
    }

    fun observeShowTasksInCalendar(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.showTasksInCalendar] ?: 1 == 1 // default true
    }

    suspend fun setShowTasksInCalendar(show: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.showTasksInCalendar] = if (show) 1 else 0
        }
    }

    fun observeSyncTasksWithCalendar(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.syncTasksWithCalendar] ?: 1 == 1 // default true
    }

    suspend fun setSyncTasksWithCalendar(sync: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.syncTasksWithCalendar] = if (sync) 1 else 0
        }
    }

    fun observeSyncTasksWithDate(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.syncTasksWithDate] ?: 1 == 1 // default true
    }

    suspend fun setSyncTasksWithDate(sync: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.syncTasksWithDate] = if (sync) 1 else 0
        }
    }

    fun observeSyncDailyReminders(): Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.syncDailyReminders] == 1 // default false
    }

    suspend fun setSyncDailyReminders(sync: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.syncDailyReminders] = if (sync) 1 else 0
        }
    }

    companion object {
        private const val DEFAULT_NOTIFICATION_RETENTION_DAYS = 2
        private const val DEFAULT_LOG_RETENTION_DAYS = 30
    }
}
