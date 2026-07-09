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

import com.minifocus.launcher.model.AppEntry
import com.minifocus.launcher.model.SearchResult
import com.minifocus.launcher.model.TaskItem
import kotlinx.coroutines.flow.first

class SearchManager(
    private val appsManager: AppsManager,
    private val tasksManager: TasksManager,
    private val appUsageStatsManager: AppUsageStatsManager
) {

    suspend fun search(query: String): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        return when {
            trimmed.startsWith(":hidden") -> buildHiddenAppsResult()
            trimmed.startsWith("@") -> buildHiddenAppLaunch(trimmed.removePrefix("@"))
            else -> combineSearch(trimmed)
        }
    }

    fun isCommand(query: String): Boolean {
        val trimmed = query.trim()
        return trimmed.startsWith(":") || trimmed.startsWith("@")
    }

    private suspend fun buildHiddenAppsResult(): List<SearchResult> {
    val hiddenApps = appsManager.observeHiddenApps().first()
    return hiddenApps.map { entry -> SearchResult.App(entry.copy(isHidden = true)) }
    }

    private suspend fun buildHiddenAppLaunch(appName: String): List<SearchResult> {
        if (appName.isBlank()) return emptyList()
        val hiddenApps = appsManager.observeHiddenApps().first()
        val matches = hiddenApps.filter { it.label.contains(appName, ignoreCase = true) || getAcronym(it.label).startsWith(appName, ignoreCase = true) }
        return matches.map { SearchResult.App(it.copy(isHidden = true)) }
    }

    private suspend fun combineSearch(query: String): List<SearchResult> {
        val apps = appsManager.observeAllApps().first()
        val tasks = tasksManager.observeTasks().first()
        val hidden = appsManager.observeHiddenApps().first()
        val stats = appUsageStatsManager.observeStats().value

        val results = mutableListOf<SearchResult>()

        results += apps.filter { it.label.contains(query, ignoreCase = true) || getAcronym(it.label).startsWith(query, ignoreCase = true) }
            .sortedByDescending { app ->
                val usage = stats[app.packageName]?.totalScore ?: 0.0
                val multiplier = calculateMatchMultiplier(app.label, query)
                (usage + 0.1) * multiplier
            }
            .map { SearchResult.App(it) }
            
        results += hidden.filter { it.label.contains(query, ignoreCase = true) || getAcronym(it.label).startsWith(query, ignoreCase = true) }
            .map { SearchResult.App(it) }
        results += tasks.filter { it.title.contains(query, ignoreCase = true) }
            .map { SearchResult.Task(it) }

        return results
    }

    private fun calculateMatchMultiplier(label: String, query: String): Double {
        // Tier 1: Exact Match (Learning Rate: 100x)
        if (label.equals(query, ignoreCase = true)) {
            return 100.0
        }

        // Tier 2: Acronym Match (Learning Rate: 50x)
        val acronym = getAcronym(label)
        if (acronym.startsWith(query, ignoreCase = true)) {
            return 50.0
        }

        // Tier 3: Prefix Match (Learning Rate: 30x)
        if (label.startsWith(query, ignoreCase = true)) {
            return 30.0
        }

        // Tier 4: Word Start Match (Learning Rate: 10x)
        var index = label.indexOf(query, ignoreCase = true)
        while (index >= 0) {
            if (index > 0 && !Character.isLetterOrDigit(label[index - 1])) {
                return 10.0
            }
            index = label.indexOf(query, index + 1, ignoreCase = true)
        }

        // Tier 5: Infix Match (Learning Rate: 1x)
        return 1.0
    }

    private fun getAcronym(label: String): String {
        val builder = java.lang.StringBuilder()
        var isWordStart = true
        for (char in label) {
            if (char.isLetterOrDigit()) {
                if (isWordStart) {
                    builder.append(char)
                    isWordStart = false
                }
            } else {
                isWordStart = true
            }
        }
        return builder.toString()
    }
}
