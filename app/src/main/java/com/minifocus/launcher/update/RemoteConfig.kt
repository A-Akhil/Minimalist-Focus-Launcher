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

package com.minifocus.launcher.update

import com.minifocus.launcher.viewmodel.LauncherUiState
import org.json.JSONArray

/** Swipeable pages of the launcher's home pager. */
enum class HomePage(val id: String) {
    CALENDAR("calendar"),
    TASKS("tasks"),
    HOME("home"),
    APPS("apps");

    companion object {
        val DEFAULT = listOf(CALENDAR, TASKS, HOME, APPS)
    }
}

/**
 * The typed view of remote patch values. Every key the app understands is listed here;
 * unknown keys are ignored and missing/invalid ones fall back to the built-in default.
 *
 * Keys:
 *   "home.pages"                 ["calendar","tasks","home","apps"]  order; omit an id to hide it.
 *                                "home" and "apps" are mandatory, otherwise the default is used.
 *   "kill.smartSuggestions"      true disables smart suggestions in the app drawer.
 *   "kill.keyboardSearchOnSwipe" true disables opening the keyboard on swipe to the drawer.
 *   "about.appName"              replaces the app name shown in About (not translated).
 */
data class RemoteConfig(
    val homePages: List<HomePage> = HomePage.DEFAULT,
    val killSmartSuggestions: Boolean = false,
    val killKeyboardSearchOnSwipe: Boolean = false,
    val aboutAppName: String? = null
) {
    /** Applies kill switches on top of the user's own settings. */
    fun applyTo(state: LauncherUiState): LauncherUiState {
        if (!killSmartSuggestions && !killKeyboardSearchOnSwipe) return state
        return state.copy(
            smartSuggestionsEnabled = state.smartSuggestionsEnabled && !killSmartSuggestions,
            isKeyboardSearchOnSwipe = state.isKeyboardSearchOnSwipe && !killKeyboardSearchOnSwipe
        )
    }

    companion object {
        fun from(values: Map<String, Any?>): RemoteConfig = RemoteConfig(
            homePages = parsePages(values["home.pages"]),
            killSmartSuggestions = values["kill.smartSuggestions"] as? Boolean ?: false,
            killKeyboardSearchOnSwipe = values["kill.keyboardSearchOnSwipe"] as? Boolean ?: false,
            aboutAppName = (values["about.appName"] as? String)?.takeIf { it.isNotBlank() }
        )

        private fun parsePages(raw: Any?): List<HomePage> {
            val array = raw as? JSONArray ?: return HomePage.DEFAULT
            val pages = (0 until array.length())
                .mapNotNull { i -> HomePage.entries.firstOrNull { it.id == array.optString(i) } }
                .distinct()
            return if (HomePage.HOME in pages && HomePage.APPS in pages) pages else HomePage.DEFAULT
        }
    }
}
