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

import android.util.Log
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Compares the latest GitHub release tag (e.g. "v2.7.0") with the installed versionName.
 * Every GitHub release is also published on Play, so a newer tag means Play has an update.
 */
object GitHubReleaseChecker {

    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/A-Akhil/Minimalist-Focus-Launcher/releases/latest"

    /** Throws [IOException] on network errors. */
    suspend fun isNewerReleaseAvailable(installedVersionName: String): Boolean = withContext(Dispatchers.IO) {
        val connection = (URL(LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        try {
            // 404: no published release yet.
            if (connection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) return@withContext false
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            val tag = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
                .optString("tag_name")
            isNewer(tag, installedVersionName).also {
                Log.i("RemotePatch", "Latest release $tag, installed $installedVersionName, update available: $it")
            }
        } finally {
            connection.disconnect()
        }
    }

    /** "v2.10.0" vs "2.9.1": numeric, part by part; missing parts count as 0. */
    internal fun isNewer(tag: String, installed: String): Boolean {
        val latest = parts(tag)
        val current = parts(installed)
        if (latest.isEmpty() || current.isEmpty()) return false
        for (i in 0 until maxOf(latest.size, current.size)) {
            val a = latest.getOrElse(i) { 0 }
            val b = current.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

    private fun parts(version: String): List<Int> =
        version.trimStart('v', 'V').split('.')
            .map { part -> part.takeWhile { it.isDigit() } }
            .takeWhile { it.isNotEmpty() }
            .map { it.toInt() }
}
