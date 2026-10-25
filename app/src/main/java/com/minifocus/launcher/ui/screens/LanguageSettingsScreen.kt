/*
 * Minimalist Focus Launcher
 * Copyright (C) 2025 A-Akhil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.minifocus.launcher.ui.screens

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.minifocus.launcher.R
import com.minifocus.launcher.ui.components.ScreenHeader
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

private data class LanguageOption(val tag: String, @StringRes val labelRes: Int)

private val LANGUAGES = listOf(
    LanguageOption("", R.string.language_system_default),
    LanguageOption("en", R.string.language_english),
    LanguageOption("de", R.string.language_german),
    LanguageOption("es", R.string.language_spanish),
    LanguageOption("pt", R.string.language_portuguese),
    LanguageOption("fr", R.string.language_french),
    LanguageOption("ru", R.string.language_russian),
    LanguageOption("hi", R.string.language_hindi),
    LanguageOption("ta", R.string.language_tamil),
    LanguageOption("ja", R.string.language_japanese),
    LanguageOption("id", R.string.language_indonesian),
    LanguageOption("tr", R.string.language_turkish),
    LanguageOption("da", R.string.language_danish),
)

@Composable
fun LanguageSettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var currentTag by remember {
        mutableStateOf(currentLanguageTag(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        ScreenHeader(title = stringResource(id = R.string.language_settings_title), onBack = onBack)

        Spacer(modifier = Modifier.height(32.dp))

        LANGUAGES.forEach { option ->
            val isSelected = currentTag == option.tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        currentTag = option.tag
                        applyLanguage(context, option.tag)
                    }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = option.labelRes),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun currentLanguageTag(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        localeManager?.applicationLocales?.toLanguageTags()?.split(",")?.firstOrNull()?.trim().orEmpty()
    } else {
        AppCompatDelegate.getApplicationLocales().toLanguageTags()
            .split(",")
            .firstOrNull()
            ?.trim()
            .orEmpty()
    }
}

private fun applyLanguage(context: Context, languageTag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val locales = if (languageTag.isEmpty()) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(languageTag)
        }
        localeManager?.applicationLocales = locales
    } else {
        val locales = if (languageTag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    val app = context.applicationContext as? com.minifocus.launcher.LauncherApplication
    app?.let { launcherApp ->
        launcherApp.container.applicationScope.launch {
            launcherApp.container.notificationInboxManager.refreshSummaryNotification()
            val locks = launcherApp.container.lockManager.observeLocks().firstOrNull()
            if (locks?.any { lock -> lock.lockedUntil > System.currentTimeMillis() } == true) {
                com.minifocus.launcher.service.AppLockMonitorService.start(context)
            }
        }
    }

    (context as? Activity)?.recreate()
}
