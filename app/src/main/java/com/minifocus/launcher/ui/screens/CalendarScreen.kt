/*
 * Minimalist Focus Launcher
 * Copyright (C) 2025 A-Akhil
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.minifocus.launcher.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import java.time.Month
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.minifocus.launcher.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.minifocus.launcher.manager.CalendarManager
import com.minifocus.launcher.model.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(selectedCalendarId: Long = -1L, isActive: Boolean = true) {
    val context = LocalContext.current
    val calendarManager = remember { CalendarManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Permission state
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions[Manifest.permission.READ_CALENDAR] == true
    }

    // Calendar state
    val initialMonth = remember { YearMonth.now() }
    val START_PAGE = 50000
    val pagerState = rememberPagerState(initialPage = START_PAGE) { 100000 }
    val currentMonth = remember(pagerState.currentPage) {
        initialMonth.plusMonths((pagerState.currentPage - START_PAGE).toLong())
    }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var daysWithEvents by remember { mutableStateOf<Map<YearMonth, Set<Int>>>(emptyMap()) }
    var selectedDateEvents by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var deleteConfirmEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    // Refresh counter to trigger reloads after mutations
    var refreshTick by remember { mutableIntStateOf(0) }

    // Reset state when screen becomes active again
    LaunchedEffect(isActive) {
        if (isActive) {
            selectedDate = LocalDate.now()
            if (pagerState.currentPage != START_PAGE) {
                pagerState.scrollToPage(START_PAGE)
            }
        }
    }

    // Request permissions on first render if not granted
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR
                )
            )
        }
    }

    // Load days-with-events for the current month and adjacent months
    LaunchedEffect(currentMonth, hasPermission, refreshTick) {
        if (hasPermission) {
            withContext(Dispatchers.IO) {
                val prevMonth = currentMonth.minusMonths(1)
                val nextMonth = currentMonth.plusMonths(1)
                
                val newMap = mutableMapOf<YearMonth, Set<Int>>()
                newMap[prevMonth] = calendarManager.getDaysWithEvents(prevMonth.year, prevMonth.monthValue)
                newMap[currentMonth] = calendarManager.getDaysWithEvents(currentMonth.year, currentMonth.monthValue)
                newMap[nextMonth] = calendarManager.getDaysWithEvents(nextMonth.year, nextMonth.monthValue)
                
                daysWithEvents = newMap
            }
        }
    }

    // Load events for the selected date
    LaunchedEffect(selectedDate, hasPermission, refreshTick) {
        if (hasPermission) {
            selectedDateEvents = withContext(Dispatchers.IO) {
                calendarManager.getEventsForDate(
                    selectedDate.year,
                    selectedDate.monthValue,
                    selectedDate.dayOfMonth
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!hasPermission) {
            // Permission not granted view
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.title_calendar),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Calendar permission is needed to view and manage your events.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR
                        )
                    )
                }) {
                    Text("Grant Permission")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 36.dp)
            ) {
                // Title
                Text(
                    text = stringResource(R.string.title_calendar),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Month navigation header
                MonthHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    onNextMonth = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    onTapMonth = {
                        showMonthPicker = true
                    },
                    onLongPressMonth = {
                        // Reset to today
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(START_PAGE)
                        }
                        selectedDate = LocalDate.now()
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Weekday header row
                WeekdayHeader()
                Spacer(modifier = Modifier.height(4.dp))

                // Calendar grid pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val pageMonth = remember(page) {
                        initialMonth.plusMonths((page - START_PAGE).toLong())
                    }
                    CalendarGrid(
                        currentMonth = pageMonth,
                        selectedDate = selectedDate,
                        daysWithEvents = daysWithEvents[pageMonth] ?: emptySet(),
                        onDateSelected = { date ->
                            selectedDate = date
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Selected date label
                Text(
                    text = formatSelectedDate(selectedDate),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Events list for selected date
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (selectedDateEvents.isEmpty()) {
                        Text(
                            text = stringResource(R.string.msg_no_events),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(selectedDateEvents, key = { "${it.id}-${it.startMillis}" }) { event ->
                                EventRow(
                                    event = event,
                                    is24Hour = DateFormat.is24HourFormat(context),
                                    onDelete = { deleteConfirmEvent = event }
                                )
                            }
                        }
                    }
                }
            }

            // FAB for adding events
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Event")
            }
        }
    }

    // Add event dialog
    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = currentMonth,
            onDismiss = { showMonthPicker = false },
            onMonthYearSelected = { targetYearMonth ->
                showMonthPicker = false
                val diffMonths = (targetYearMonth.year - initialMonth.year) * 12 + (targetYearMonth.monthValue - initialMonth.monthValue)
                coroutineScope.launch {
                    pagerState.scrollToPage(START_PAGE + diffMonths)
                }
            }
        )
    }

    if (showAddDialog) {
        AddEventDialog(
            selectedDate = selectedDate,
            onDismiss = { showAddDialog = false },
            onAdd = { title, hour, minute, durationMinutes ->
                coroutineScope.launch {
                    val startMillis = java.time.LocalDateTime.of(selectedDate, java.time.LocalTime.of(hour, minute))
                        .atZone(java.time.ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    val endMillis = startMillis + durationMinutes * 60 * 1000L
                    withContext(Dispatchers.IO) {
                        val targetCalendar = calendarManager.getWritableCalendar(selectedCalendarId)
                        if (targetCalendar != null) {
                            calendarManager.createEvent(
                                title = title,
                                startMillis = startMillis,
                                endMillis = endMillis,
                                calendarId = targetCalendar.id
                            )
                        }
                    }
                    showAddDialog = false
                    refreshTick++
                }
            }
        )
    }

    // Delete confirmation dialog
    deleteConfirmEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { deleteConfirmEvent = null },
            title = { Text("Delete Event") },
            text = { Text("Remove \"${event.title}\" from your calendar?") },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            calendarManager.deleteEvent(event.id)
                        }
                        deleteConfirmEvent = null
                        refreshTick++
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmEvent = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Sub-components ---

@Composable
private fun MonthHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTapMonth: () -> Unit,
    onLongPressMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.combinedClickable(
                onClick = onTapMonth,
                onLongClick = onLongPressMonth
            )
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val daysOfWeek = remember {
        listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    daysWithEvents: Set<Int>,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstOfMonth = currentMonth.atDay(1)
    // Monday=1 ... Sunday=7; offset so Monday is column 0
    val startOffset = (firstOfMonth.dayOfWeek.value - 1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7
    val prevMonthDays = currentMonth.minusMonths(1).lengthOfMonth()

    Column(modifier = Modifier.fillMaxWidth()) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1
                    if (dayNumber < 1) {
                        val prevDay = prevMonthDays + dayNumber
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prevDay.toString(),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                fontSize = 14.sp
                            )
                        }
                    } else if (dayNumber > daysInMonth) {
                        val nextDay = dayNumber - daysInMonth
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = nextDay.toString(),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        val date = currentMonth.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val hasEvent = daysWithEvents.contains(dayNumber)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.onBackground,
                                            CircleShape
                                        )
                                    } else if (isToday) {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                                            CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.background
                                        isToday -> MaterialTheme.colorScheme.onBackground
                                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                // Event dot indicator
                                if (hasEvent && !isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(
                                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                                CircleShape
                                            )
                                    )
                                } else if (hasEvent && isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                                                CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(
    event: CalendarEvent,
    is24Hour: Boolean,
    onDelete: () -> Unit
) {
    val timeFormatter = remember(is24Hour) {
        if (is24Hour) {
            java.time.format.DateTimeFormatter.ofPattern("HH:mm")
        } else {
            java.time.format.DateTimeFormatter.ofPattern("h:mm a")
        }
    }
    val zoneId = remember { java.time.ZoneId.systemDefault() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator from calendar
        Box(
            modifier = Modifier
                .size(4.dp, 32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (event.eventColor != 0) {
                        androidx.compose.ui.graphics.Color(event.eventColor)
                    } else if (event.calendarColor != 0) {
                        androidx.compose.ui.graphics.Color(event.calendarColor)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title.ifEmpty { "(No title)" },
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val timeText = if (event.allDay) {
                "All day"
            } else {
                val start = java.time.Instant.ofEpochMilli(event.startMillis)
                    .atZone(zoneId).toLocalTime().format(timeFormatter)
                val end = java.time.Instant.ofEpochMilli(event.endMillis)
                    .atZone(zoneId).toLocalTime().format(timeFormatter)
                "$start - $end"
            }
            Text(
                text = timeText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete event",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AddEventDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onAdd: (title: String, hour: Int, minute: Int, durationMinutes: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var hour by remember { mutableIntStateOf(java.time.LocalTime.now().hour) }
    var minute by remember { mutableIntStateOf(0) }
    var durationMinutes by remember { mutableIntStateOf(60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "New Event",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = formatSelectedDate(selectedDate),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = MaterialTheme.colorScheme.onBackground,
                        focusedLabelColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                var showTimePicker by remember { mutableStateOf(false) }

                // Time picker row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.label_time),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(
                            text = "%02d:%02d".format(hour, minute),
                            fontSize = 14.sp
                        )
                    }
                }

                if (showTimePicker) {
                    com.minifocus.launcher.ui.TimePickerDialog(
                        onDismiss = { showTimePicker = false },
                        onConfirm = { h, m ->
                            hour = h
                            minute = m
                            showTimePicker = false
                        }
                    )
                }
                // Duration selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_duration),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    listOf(30, 60, 120).forEach { mins ->
                        val label = if (mins < 60) "${mins}m" else "${mins / 60}h"
                        val isChosen = durationMinutes == mins
                        Text(
                            text = label,
                            color = if (isChosen) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                            fontSize = 13.sp,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isChosen) MaterialTheme.colorScheme.onBackground
                                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                                )
                                .clickable { durationMinutes = mins }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), hour, minute, durationMinutes)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add", color = MaterialTheme.colorScheme.onBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatSelectedDate(date: LocalDate): String {
    val today = LocalDate.now()
    val dayLabel = when (date) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
    val month = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "$dayLabel, $month ${date.dayOfMonth}"
}

@Composable
private fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onMonthYearSelected: (YearMonth) -> Unit
) {
    var startYear by remember { mutableIntStateOf(currentMonth.year - 4) } 
    var selectedYear by remember { mutableIntStateOf(currentMonth.year) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Year Header with Arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { startYear -= 9 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Previous Years",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "$startYear - ${startYear + 8}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { startYear += 9 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Next Years",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3x3 Year Grid
                for (row in 0 until 3) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 3) {
                            val year = startYear + row * 3 + col
                            val isSelected = year == selectedYear
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent
                                    )
                                    .clickable { selectedYear = year }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = year.toString(),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(16.dp))

                // 4x3 Month Grid
                val months = remember { Month.values() }
                for (row in 0 until 4) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 3) {
                            val month = months[row * 3 + col]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { 
                                        onMonthYearSelected(YearMonth.of(selectedYear, month))
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
