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

import android.app.Application
import com.minifocus.launcher.data.AppDatabase
import com.minifocus.launcher.data.datastore.launcherSettingsDataStore
import com.minifocus.launcher.manager.AppsManager
import com.minifocus.launcher.manager.DailyTasksManager
import com.minifocus.launcher.manager.HiddenAppsManager
import com.minifocus.launcher.manager.InboxLogger
import com.minifocus.launcher.manager.LockManager
import com.minifocus.launcher.manager.NotificationInboxManager
import com.minifocus.launcher.manager.SearchManager
import com.minifocus.launcher.manager.SettingsBackupManager
import com.minifocus.launcher.manager.SettingsManager
import com.minifocus.launcher.manager.TasksManager
import com.minifocus.launcher.manager.AppUsageStatsManager
import com.minifocus.launcher.manager.AppTimeReminderManager
import com.minifocus.launcher.manager.CalendarManager
import com.minifocus.launcher.service.AppLockMonitorService
import com.minifocus.launcher.update.RemotePatchManager
import com.minifocus.launcher.worker.NotificationMaintenanceWorker
import com.minifocus.launcher.worker.RemotePatchWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LauncherApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val database = AppDatabase.build(this)
        val settingsManager = SettingsManager(launcherSettingsDataStore)
        val inboxLogger = InboxLogger(
            context = this,
            onRotation = { timestamp ->
                appScope.launch { settingsManager.setLastLogRotation(timestamp) }
            }
        )
        val tasksManager = TasksManager(database.taskDao(), this)
        val dailyTasksManager = DailyTasksManager(database.dailyTaskDao())
        val appUsageStatsManager = AppUsageStatsManager(database.appUsageStatsDao(), appScope)
        val appTimeReminderManager = AppTimeReminderManager(database.appTimeReminderDao())
        val calendarManager = CalendarManager(this)
        val hiddenManager = HiddenAppsManager(database.hiddenAppDao())
        val lockManager = LockManager(database.appLockDao())
        val appsManager = AppsManager(
            context = this,
            pinnedAppDao = database.pinnedAppDao(),
            hiddenAppsManager = hiddenManager,
            lockManager = lockManager,
            scope = appScope
        )
        val searchManager = SearchManager(appsManager, tasksManager, appUsageStatsManager)
        val notificationInboxManager = NotificationInboxManager(
            context = this,
            notificationDao = database.notificationDao(),
            notificationFilterDao = database.notificationFilterDao(),
            logger = inboxLogger
        )
        val settingsBackupManager = SettingsBackupManager()
        val remotePatchManager = RemotePatchManager(
            context = this,
            appVersionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode
        )
        container = AppContainer(
            tasksManager = tasksManager,
            hiddenAppsManager = hiddenManager,
            lockManager = lockManager,
            appsManager = appsManager,
            settingsManager = settingsManager,
            settingsBackupManager = settingsBackupManager,
            searchManager = searchManager,
            dailyTasksManager = dailyTasksManager,
            notificationInboxManager = notificationInboxManager,
            inboxLogger = inboxLogger,
            applicationScope = appScope,
            appUsageStatsManager = appUsageStatsManager,
            appTimeReminderManager = appTimeReminderManager,
            calendarManager = calendarManager,
            remotePatchManager = remotePatchManager
        )



        appScope.launch {
            settingsManager.observeNotificationInboxEnabled().collectLatest { enabled ->
                notificationInboxManager.setEnabled(enabled)
            }
        }

        appScope.launch {
            settingsManager.observeNotificationRetentionDays().collectLatest { days ->
                notificationInboxManager.updateRetention(days)
            }
        }

        appScope.launch {
            settingsManager.observeLogRetentionDays().collectLatest { days ->
                notificationInboxManager.updateLogRetention(days)
            }
        }

        appScope.launch {
            notificationInboxManager.trimExpired()
        }

        // Compact pinned app positions on startup
        appScope.launch {
            appsManager.compactPinnedPositions()
        }

        // Monitor lock state and start/stop monitoring service
        appScope.launch {
            lockManager.observeLocks().collectLatest { locks ->
                val hasActiveLocks = locks.any { it.lockedUntil > System.currentTimeMillis() }
                if (hasActiveLocks) {
                    AppLockMonitorService.start(this@LauncherApplication)
                } else {
                    AppLockMonitorService.stop(this@LauncherApplication)
                }
            }
        }

        appScope.launch {
            remotePatchManager.loadApplied()
        }

        NotificationMaintenanceWorker.schedule(this)
        if (remotePatchManager.isConfigured) {
            RemotePatchWorker.schedule(this)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        container.appsManager.cleanup()
    }
}

class AppContainer(
    val tasksManager: TasksManager,
    val dailyTasksManager: DailyTasksManager,
    val hiddenAppsManager: HiddenAppsManager,
    val lockManager: LockManager,
    val appsManager: AppsManager,
    val settingsManager: SettingsManager,
    val settingsBackupManager: SettingsBackupManager,
    val searchManager: SearchManager,
    val notificationInboxManager: NotificationInboxManager,
    val inboxLogger: InboxLogger,
    val applicationScope: CoroutineScope,
    val appUsageStatsManager: AppUsageStatsManager,
    val appTimeReminderManager: AppTimeReminderManager,
    val calendarManager: CalendarManager,
    val remotePatchManager: RemotePatchManager
)
