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

package com.minifocus.launcher.ui.screens

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minifocus.launcher.R
import com.minifocus.launcher.ui.components.ScreenHeader

@Composable
fun NotificationSettingsScreen(
    notificationInboxEnabled: Boolean,
    notificationRetentionDays: Int,
    logRetentionDays: Int,
    onBack: () -> Unit,
    onOpenNotificationRetention: () -> Unit,
    onOpenLogRetention: () -> Unit,
    onOpenAppFilters: () -> Unit,
    onOpenLogViewer: () -> Unit,
    onNotificationInboxToggle: (Boolean) -> Unit,
    notificationListenerGranted: Boolean,
    onRequestNotificationListener: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        ScreenHeader(
            title = stringResource(R.string.notif_settings_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.notif_settings_inbox_section),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.notif_settings_capture),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                Text(
                    text = if (notificationListenerGranted) stringResource(R.string.notif_settings_capture_hint) else stringResource(R.string.summary_inbox_disabled),
                    color = if (notificationListenerGranted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = notificationInboxEnabled && notificationListenerGranted,
                onCheckedChange = { enabled ->
                    if (enabled && !notificationListenerGranted) {
                        onRequestNotificationListener()
                    } else {
                        onNotificationInboxToggle(enabled)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (notificationInboxEnabled) {
            NotificationSettingsRow(
                title = stringResource(R.string.notif_settings_auto_clear),
                value = pluralStringResource(R.plurals.label_day_count, notificationRetentionDays, notificationRetentionDays),
                onClick = onOpenNotificationRetention
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotificationSettingsRow(
                title = stringResource(R.string.notif_settings_log_retention),
                value = pluralStringResource(R.plurals.label_day_count, logRetentionDays, logRetentionDays),
                onClick = onOpenLogRetention
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotificationSettingsRow(
                title = stringResource(R.string.notif_settings_app_filters),
                value = "",
                onClick = onOpenAppFilters
            )

            Spacer(modifier = Modifier.height(16.dp))

            NotificationSettingsRow(
                title = stringResource(R.string.notif_settings_view_logs),
                value = "",
                onClick = onOpenLogViewer
            )
        } else {
            Text(
                text = stringResource(R.string.notif_settings_inbox_off_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun NotificationSettingsRow(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            if (value.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
