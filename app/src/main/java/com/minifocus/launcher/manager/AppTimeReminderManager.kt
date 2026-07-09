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

import android.content.Context
import android.content.pm.PackageManager
import com.minifocus.launcher.data.dao.AppTimeReminderDao
import com.minifocus.launcher.data.entity.AppTimeReminderEntity
import com.minifocus.launcher.model.ExpiryAction
import kotlinx.coroutines.flow.Flow

class AppTimeReminderManager(
    private val dao: AppTimeReminderDao
) {
    fun observeTrackedApps(): Flow<List<AppTimeReminderEntity>> = dao.observeAll()

    suspend fun isTracked(packageName: String): Boolean =
        dao.isTracked(packageName) > 0

    suspend fun getTrackedApp(packageName: String): AppTimeReminderEntity? =
        dao.getByPackage(packageName)

    suspend fun addTrackedApp(
        packageName: String,
        appLabel: String,
        defaultDurationMinutes: Int? = null,
        expiryAction: ExpiryAction = ExpiryAction.NOTIFICATION
    ) {
        dao.upsert(
            AppTimeReminderEntity(
                packageName = packageName,
                appLabel = appLabel,
                defaultDurationMinutes = defaultDurationMinutes,
                expiryAction = expiryAction.name
            )
        )
    }

    suspend fun updateTrackedApp(
        packageName: String,
        appLabel: String,
        defaultDurationMinutes: Int?,
        expiryAction: ExpiryAction
    ) {
        dao.upsert(
            AppTimeReminderEntity(
                packageName = packageName,
                appLabel = appLabel,
                defaultDurationMinutes = defaultDurationMinutes,
                expiryAction = expiryAction.name
            )
        )
    }

    suspend fun updateExpiryAction(packageName: String, expiryAction: ExpiryAction) {
        dao.updateExpiryAction(packageName, expiryAction.name)
    }

    suspend fun removeTrackedApp(packageName: String) {
        dao.delete(packageName)
    }

    companion object {
        // Social media and messaging apps
        private val DEFAULT_TRACKED_APPS = listOf(
            "com.instagram.android" to "Instagram",
            "com.twitter.android" to "X",
            "com.reddit.frontpage" to "Reddit",
            "com.discord" to "Discord",
            "com.snapchat.android" to "Snapchat",
            "com.facebook.katana" to "Facebook",
            "com.zhiliaoapp.musically" to "TikTok",
            "com.linkedin.android" to "LinkedIn",
            "org.telegram.messenger" to "Telegram",
            "com.pinterest" to "Pinterest",
            "com.tumblr" to "Tumblr",
            // Video streaming
            "com.google.android.youtube" to "YouTube",
            "com.netflix.mediaclient" to "Netflix",
            "in.startv.hotstar" to "Hotstar",
            "com.amazon.avod.thirdpartyclient" to "Prime Video",
            // Games
            "com.supercell.clashofclans" to "Clash of Clans",
            "com.supercell.clashroyale" to "Clash Royale",
            "com.supercell.brawlstars" to "Brawl Stars",
            "com.kiloo.subwaysurf" to "Subway Surfers",
            "com.king.candycrushsaga" to "Candy Crush",
            "com.activision.callofduty.shooter" to "Call of Duty Mobile",
            "com.pubg.imobile" to "BGMI",
            "com.dts.freefireth" to "Free Fire",
            "com.mojang.minecraftpe" to "Minecraft",
            "com.innersloth.spacemafia" to "Among Us",
        )
    }
}
