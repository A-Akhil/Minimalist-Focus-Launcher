# Changelog

## [Unreleased]

## v2.6.1 (Released - Play Store) — 2026-06-28

### Calendar
- Introduced a smooth swipe interaction between months using a `HorizontalPager`.
- Added a `MonthYearPickerDialog` for quick date navigation and applied code optimizations.
- Display adjacent month days in the calendar grid for better context.
- Ensured calendar state resets automatically when navigating back to it.

### Settings
- Simplified the "Daily tasks" text in Home Screen Settings for a cleaner minimalist layout.
- Corrected the double-tap lock warning text to appear in red (error color) when accessibility permissions are missing.
- Fixed the double-tap lock switch state so it visually remains off when permission is denied, while keeping it clickable to easily prompt for required access.
- Tucked away the debug "Replay Onboarding" option behind a compile-time flag.

### Localization
- Replaced remaining hardcoded UI strings across screens, dialogs, and overlays with string resources so language switching applies consistently.

### Repo Hygiene
- Ignored VS Code workspace settings by adding `.vscode/` to `.gitignore`.

### Community & Documentation
- Improved GitHub issue templates to capture build/source, frequency, permission context, and scope/success/privacy details.
- Enhanced the pull request template with scope, risk/impact, and data/permissions sections.
- Added a promotion video link to README and included the rendered video asset under docs.
- Normalized .github and README file timestamps for release consistency.

## v2.1.1 (Release Candidate) — 2026-03-15

### App Time Intention
- Stabilized usage-stat window handling in `TimeIntentionDialog`.
- Kept dialog usage fetch as one-shot on open to avoid repeated polling overhead.
- Updated TODAY calculation to event-session based foreground tracking with improved cross-app transition handling.
- Kept PAST 7 DAYS calculation as one bounded aggregate range over previous 7 full days, strictly excluding today (`end = todayStart - 1`).

## v2.1.0 (Release Candidate) — 2026-03-14

### All Apps Drawer
- Added an alphabetical fast-scroll rail for the app drawer with drag support.
- Improved rail behavior to scroll continuously and feel smoother during interaction.
- Tuned rail positioning so it starts below the "Frequently opened" section when shown.
- Added fallback behavior for missing letters (for example, tapping `B` jumps to the next available letter).
- Reset drawer list position to top when leaving and re-entering the app drawer.

### App Time Intention
- Introduced a 60-second grace window after returning to launcher: reopening the same tracked app within the window now continues the existing timer without re-prompting.
- Fixed stale session bypass by hardening active-session validation in `AppTimeReminderReceiver` (package + expiry timestamp), so expired sessions cannot silently skip the intention dialog.
- Screen-off now force-resets any active app-time session immediately, ensuring the next open asks for time intention again.

## v2.0.1 (Released - Play Store) — 2026-03-09

### Appearance
- **System Theme Support**: Refactored `PermissionScreen` (and `About`, `LogViewer`, `EmergencyUnlock`, `NotificationInbox`, `NotificationFilter` screens earlier) to respect Light Mode/Dark Mode settings by replacing hardcoded colors (`Color.Black`, `Color.White`, `Color(0xFF161616)`) with dynamic `MaterialTheme.colorScheme` tokens.
- **Text Size Customization**: Implemented a global text scaling engine supporting 6 distinct levels (Extra Small to Huge).
    - Introduced `TextSizeProvider` theme wrapper that injects a scaling multiplier via `CompositionLocal`, ensuring consistent sizing across all screens without restarting the activity.
    - Added `TextSize` enum with multipliers ranging from 0.75x to 1.50x.
    - Persisted user preference using DataStore in `SettingsManager` with migration-safe defaults.
- **Settings UI**:
    - Added a new top-level "Appearance" section in the main Settings list.
    - Created `AppearanceSettingsScreen` as a hub for visual customizations.
    - Built `TextSizeSettingsScreen` featuring a live interactive preview that demonstrates the selected scale on Clock, App Names, and Body text in real-time.
- **Backup & Restore**:
    - Implemented a manual backup system that saves current settings (including text size preferences and clock format) to a JSON file in `Documents/.minilauncher/`.
    - Added "Backup Settings" and "Restore Settings" options in the main Settings menu, providing users with a simple way to persist and recover their configuration.
- **Navigation**: extended `LauncherViewModel` and `LauncherApp` to manage the new settings overlay stack, handling back navigation correctly from the nested appearance screens.

### Home Screen
- Added a minimalist timer/stopwatch chip that mirrors active clock notifications via the notification listener; tapping the chip opens the default clock while the UI hides automatically when the source notification disappears.
- Refined the timer indicator styling per feedback: the countdown/stopwatch now displays as a subtle grey badge tucked beneath the clock/date for a cleaner header layout.
- Timer chip now ticks in real time by deriving countdown targets or stopwatch start timestamps from notification metadata, eliminating the stuck-at-00:00 behaviour seen with static snapshots.

### Gestures
- Added double-tap to lock screen feature: users can now enable a toggle in Home Screen Settings that lets them lock the device by double-tapping empty space on the home screen (powered by an accessibility service so biometrics stay available).
- Removed the old device-admin dependency for locking; the launcher now guides users to enable an accessibility shortcut instead.

### Notification Inbox
- Persisted notification content and action intents in Room, rebuilt cache hydration for cold starts, added stored action execution, purged associated data when packages are filtered or skipped, and now dispatch stored intents using the application context so inbox items reliably reopen their source apps even after process restarts.
- Simple interception logic: if app is in the filter list (user enabled interception), notification is archived and cancelled unless it's from a system app; if app is NOT in the filter list, notification stays visible.
- Timer parsing now prefers the notification's chronometer base (when provided) to derive countdown targets/stopwatch start times via `SystemClock.elapsedRealtime()`, falling back to textual or `when` metadata only as needed; if the platform still reports `00:00`, we parse the displayed string and synthesize a reference so the home chip keeps counting down from the user-facing time.
- Log Viewer now consumes a combined feed: `NotificationInboxManager` parses the JSON log archives (up to 5,000 lines), converts them into `NotificationItem`s, deduplicates against the Room inbox snapshot using `(key, timestamp)` pairs so repeated notifications stay visible, and exposes the merged list through `NotificationInboxViewModel`, ensuring the viewer shows the entire historical log while the inbox continues to reflect the short-retention dataset.
- Added a Notification Logs viewer inside Notification Settings that reads the retention files, filters to intercepted notifications, and renders them with the exact Notification Inbox layout (app name, timestamp, title, content) while ensuring swipe-to-home clears the viewer state.

### Build
- Renamed Room migration parameters to align with the `Migration` base class, tightened notification label resolution, and dropped the deprecated swipe threshold override so `./gradlew assembleDebug` runs without Kotlin warnings (only the Gradle 9 deprecation notice remains).
- Declared an explicit `androidx.compose.foundation:foundation-layout` dependency and dropped the obsolete direct imports so Compose can resolve `weight`/`stickyHeader` through their scope receivers, unblocking the All Apps fast-scroll build.
- Fixed a hidden-apps compile error by importing Compose's `setValue` delegate helper in `LauncherApp.kt`, allowing `HiddenAppsScreen`'s long-press state to compile and restoring a green `./gradlew assembleDebug` run.
- Added the missing Compose `getValue` delegate import in `LogViewerScreen.kt`, resolving the new Room-backed log viewer's build failure and restoring a successful `./gradlew assembleDebug` run.

### Hardlock Variant
- Created the experimental `HardlockDeviceAdminReceiver`, hooked it into the manifest with the required `BIND_DEVICE_ADMIN` permission, and added `res/xml/device_admin_rules.xml` so ADB-installed builds can register as a device admin during testing without affecting the Play-distributed flavor.
- **POLICY WARNING**: Added `HardlockAccessibilityService` that intercepts Settings windows (app info, device admin, default apps) and forces users back to the launcher home screen. This VIOLATES Google Play's "Mobile Unwanted Software" policy and MUST NEVER be shipped in any Play-associated build. Hardlock branch only, sideload-only, voids all Play eligibility. Users can disable via: Settings > Accessibility > Hardlock Guard > Turn Off, or `adb shell settings put secure enabled_accessibility_services null`.
- Implemented external backup system: `HardlockBackupManager` persists locked apps, hidden apps, and settings to `Documents/.minilauncher/hardlock_state.json` so restrictions survive "clear data" and device reboots. Added `HardlockBootReceiver` that restores state on `BOOT_COMPLETED`. Requires BOOT_COMPLETED and storage permissions. **WARNING**: This is part of the hardlock sideload variant only and must never be included in Play builds.

### Community & Documentation
- Added a comprehensive `CONTRIBUTING.md` outlining setup prerequisites, workflow, testing expectations, and overall contribution guidelines.
- Published a `CODE_OF_CONDUCT.md` based on Contributor Covenant v2.1 so contributors share a clear set of behavioral standards.
- Introduced GitHub issue templates (bug report, feature request, and config) plus a pull request template to capture reproducible details, enforce build/lint checks, and standardize labeling.
- Trimmed program-specific references from the contributor docs/templates so they remain agnostic while still explaining the general expectations.

### App Locks
- Fixed critical bug where locking an already-locked app would replace the lock time: now only updates if the new duration extends the existing lock, preventing accidental shortening of lock periods.

### Pinned Apps
- Enforced maximum of 5 pinned apps with clear user feedback when limit is reached.
- Implemented automatic position gap filling: unpinned app positions are now reused when pinning new apps, preventing unbounded position growth.
- Added startup cleanup that compacts any legacy position gaps into a clean 0-4 sequence.

### All Apps Drawer
- Rebuilt the All Apps experience with alphabetical sections: when search is idle, apps render under sticky letter headers backed by memoized letter→index maps for fast navigation.
- Added a minimalist fast-scroll rail with drag handling, letter scaling, haptic pulses, and a centered bubble overlay plus blur animation to keep context while scrubbing through the list.
- Tuned the fast-scroll animations with spring-based easing so letter scales and the preview bubble have a gentle overshoot that matches the requested “sleek Contacts-style” behavior.
- Smart suggestions row is now enabled by default on fresh installs so new users immediately benefit from the adaptive ordering while retaining the ability to toggle it off in App Drawer settings.
- Removed the "Smart picks" header and horizontal card row; adaptive suggestions now appear as the first entries in the regular All Apps list without extra labels, keeping the drawer visually uniform while still tracking smart launches.
- Added subtle "Frequently opened" / "All apps" dividers so adaptive entries stay visually separated from the full list while retaining the minimalist single-column layout and copy without the old "Great around now" phrasing.
- Hidden apps now live behind a dedicated App Drawer settings entry: the inline All Apps section is gone, and opening “Hidden apps” launches a gated screen that requires a continuous 10-second press with a circular progress indicator before revealing the unhide controls.

## v2.0.0 (Released - Play Store)

## 2025-11-08

### Permissions
- Restored the onboarding acknowledgement flag so the permission screen only closes after the user taps Continue even when POST_NOTIFICATIONS is granted, and reset it automatically if required permissions are revoked.
- Updated permission overlay gating so it remains visible until the user explicitly taps Continue, even after the last required permission is granted.

### Notification Inbox
- Added gating for the inbox toggle: enabling now verifies notification and listener permissions, prompting the user when missing instead of silently flipping the switch.

### Notification Filters
- Added "Enable all" / "Disable all" controls to the filter screen and wired them through `LauncherApp`/`MainActivity` so users can bulk-toggle capture state without flipping each app individually.
- Flattened the Notification Filter screen to match the launcher's minimal aesthetic: removed descriptive copy and counts, switched bulk actions into a three-dot overflow menu, and kept the list to simple rows (app name + toggle) with tight spacing.

### App Locks
- App locking now checks for usage stats and overlay permissions before scheduling a lock, prompting only one system screen at a time when access is missing.

### App Discovery
- Reworked app visibility logic to include any system package that exposes a launcher activity in addition to intent/category matches, ensuring OEM utilities (calculator, camera, etc.) remain visible without hardcoded package IDs.
- Quick-launch defaults for dialer and camera now resolve via PackageManager intents and the system default dialer API instead of vendor-specific lists, so the home shortcuts adapt automatically to each device.

### Release
- Bumped versionCode to 3 and versionName to 1.0.1 in preparation for the next release build.

## 2025-11-07

### Permissions
- Split the onboarding screen into required versus optional permissions, keeping notifications as the sole requirement and surfacing contextual descriptions for optional capabilities.
- Added a continue button that unlocks the launcher as soon as required permissions are granted, while still offering quick actions to enable optional features.

### Notification Inbox
- Hooked `LauncherApplication` into the `notificationInboxEnabled` preference so the manager toggles capture state reactively without requiring a restart.
- Taught `NotificationInboxManager` to skip interception when disabled, clear stored entries, cancel the summary notification, and keep the banner persistent by dropping the auto-cancel flag.
- Short-circuited `NotificationInboxListenerService` when the inbox is off to avoid unnecessary work and log noise.
- Simplified the permission gate in `MainActivity` so the launcher only shows the onboarding screen while required permissions remain ungranted, eliminating the acknowledged-state flash when launching from the inbox summary.

### Settings
- Added a "Device Settings" shortcut to the Settings landing page (above About) that launches the system Settings app so users can jump to phone configuration directly from the launcher.

## 2025-11-06

### Tasks
- Made the add-task dialog scroll-aware so that scrolling away from the text field clears focus and automatically dismisses the keyboard, keeping reminder configuration uncluttered without affecting keyboard behavior elsewhere.
- Clearing focus now also kicks in whenever users toggle reminder switches, tap repeat selectors, or open date pickers so the blinking cursor disappears as soon as they move beyond the task title field.
- Updated the focus helper to hide the software keyboard alongside clearing focus, guaranteeing the keyboard collapses even when scrolling or interacting with controls that do not steal focus themselves.
- Added a hidden focus anchor inside the dialog and redirect focus to it before hiding the keyboard so the task title field cannot immediately reclaim focus while users adjust reminder settings.

## 2025-11-05

### Daily Tasks Feature
- Added Room-backed daily tasks pipeline end-to-end: ViewModel now exposes CRUD APIs, active-window gating, and 10-second completion hold for the home module.
- Introduced reusable `DailyTaskEditorDialog` with title, enable toggle, optional date range, and delete action for managing recurring reminders.
- Surfaced daily tasks on the Home screen beneath the clock (checkbox-driven, auto-hide after completion) and created a management section on the Tasks page with enable/disable switches and edit/delete controls.
- Extended Settings with a dedicated Home Screen Settings page housing the “Show daily tasks on home” toggle and contextual messaging.
- Registered daily reminder hold state before persisting completion so reminders remain visible for the full 10-second buffer without flicker.
- Pruned unused undo/message/unlock callbacks from `LauncherApp` and `MainActivity`, and simplified `handleAppLaunch`, clearing Kotlin's unused-parameter warnings during the debug build.
- Aligned `TasksScreen` callbacks with recurrence metadata and surfaced the concise repeat pattern label next to daily reminder status for quick scanning.

### Build & Install
- Ran `./gradlew assembleDebug` to confirm the latest daily task fixes compile cleanly and deployed the debug APK to device 41311JEHN10268 via `adb install -r` for on-device validation.
- Streamlined the Tasks screen per feedback: the original `+` button now drives both regular and daily reminders, the daily manager card lost its separate add icon (long-press rows to edit/delete), and the combined dialog captures optional daily ranges while keeping the minimalist aesthetic.
- Refined home screen balance: daily reminders (when enabled) share a centered stack with pinned apps, pinned app count adapts (3 with reminders, 5 without), and the quick shortcuts stay anchored at the bottom for a calmer layout.

### Settings Navigation
- Rebuilt the Settings landing page around grouped entries for Home Screen, Notification Bar, and App Drawer, each showing a concise summary instead of exposing every toggle at once.
- Refined the Home Screen subpage to focus on daily task visibility and bottom quick-launch selectors while clock controls live in their own screen.
- Moved the notification inbox master toggle into the Notification Bar page; retention durations and app filter controls now appear only when the inbox is enabled.
- Added a dedicated App Drawer page focused on the keyboard-on-swipe preference with clear guidance for both behaviors.
- Introduced a standalone Clock settings page with its own navigation route, pulled clock controls off the Home Screen page, and added a top-level Clock summary row so the landing page now lists four focused groups.
- Fixed task auto-archive timing so completed items drop into History once the three-second grace window expires, restoring the intended “tap → undo opportunity → archive” flow.
- Synced the undo buffer with UX feedback: both regular tasks and daily reminders now wait 10 seconds before leaving the active lists, and daily reminders disappear from the home/tasks surfaces once that buffer lapses.

## 2025-11-04

### Compliance Documentation
- Added `docs/data-safety.md` summarizing Google Play Data Safety responses: confirms no data collection or sharing, details local-only storage for tasks/locks/notifications, clarifies permission usage, and outlines retention/encryption behavior for current release.
- Refined `docs/privacy-policy.md`: clarified children’s privacy scope given the absence of data-entry surfaces, added forward-looking note about introducing anonymized crash reports with prior disclosure, and removed the redundant consent/uninstall clause.

## v1.2.x (Released - Play Store)

## 2025-10-30

### Enhanced Context Menu UI
- **Redesigned App Context Menu**: Replaced basic DropdownMenu with modern AppContextMenu component
  - Material3 dark surface design (0xFF1A1A1A background) with rounded corners (16.dp)
  - Each menu item now has an icon (Material Icons) for better visual recognition
  - Cleaner spacing and typography (20.sp app name header, 16.sp menu items)
  - Smooth animations and transitions
  - Red color for destructive action (Uninstall)
  - Applied to all long-press contexts: Home (pinned apps), All Apps (regular and hidden)

### New Context Menu Actions
- **App Info**: Opens Android system settings for the selected app (Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
- **Uninstall**: Launches system uninstall dialog (Intent.ACTION_DELETE with package URI)
- **Block (Lock)**: Existing lock functionality with duration picker, now with proper icon
- **Hide/Unhide**: Improved visibility toggle with proper icons (VisibilityOff/Visibility)
- **Pin/Unpin**: Quick pinning to home screen with PushPin icon
- All actions properly integrated with system intents for native Android behavior

## 2025-10-29

### Notification Inbox Toggle
- **Enable/Disable Notification Management**: Added toggle in Settings to control entire notification inbox system
  - New "Notification Inbox" section in Settings with master toggle switch
  - When disabled (default): notifications pass through to system shade normally
  - When enabled: notifications are intercepted and routed to inbox
  - "Notification Settings" row only appears when toggle is enabled
  - Added `notificationInboxEnabled` preference in DataStore (defaults to false)
  - Updated LauncherViewModel to expose and manage enabled state
  - Gives users full control over whether they want inbox functionality

### Navigation & Modal Screen Fixes
- **Fixed Modal Navigation Bug**: Modal screens (Settings, About, Notification Filters) no longer incorrectly show All Apps drawer after swipe-up gestures
  - Added `homeResetTick` signal in LauncherViewModel that increments on reset
  - Implemented automatic pager snap to home page (index 1) when any overlay becomes visible
  - System swipe gestures now properly return to home screen instead of stranding users in All Apps
  - Ensures consistent navigation experience across all modal screens

### Consistent Navigation & Header UI
- **Unified Screen Headers**: Created centralized `ScreenHeader` composable component
  - Standardized back button: Material Icons ArrowBack in IconButton (white on black)
  - Consistent title styling: 28.sp, SemiBold, White color
  - Optional content slot for custom layouts (used by NotificationInbox for unread badge)
  - Single source of truth for all header styling
- **Critical Bug Fix - SettingsScreen**: Fixed major visual inconsistency
  - **Before**: Used plain text "←" arrow at 32.sp with `combinedClickable`
  - **After**: Uses `ScreenHeader` with proper IconButton and Material icon
  - Eliminated jarring difference between Settings and other screens
- **Refactored 7 Screens**: Applied `ScreenHeader` component consistently
  1. NotificationFilterScreen.kt
  2. NotificationSettingsScreen.kt
  3. AboutScreen.kt
  4. EmergencyUnlockScreen.kt (also fixed: 24.sp/Light → 28.sp/SemiBold)
  5. TaskDialogs.kt - CompletedTasksHistoryScreen (removed extra title padding)
  6. NotificationInboxScreen.kt (uses content slot for unread count badge)
  7. LauncherApp.kt - SettingsScreen (replaced text arrow with IconButton)
- **Code Quality Improvements**:
  - Removed 180+ lines of duplicate header code
  - Centralized header logic in `/ui/components/ScreenHeader.kt`
  - All screens now import and use `ScreenHeader(title, onBack)`
  - Future header changes only require editing one file
- **Visual Consistency**: All screens now have identical navigation experience
  - Same back arrow icon (Material ArrowBack)
  - Same arrow size and positioning
  - Same title typography
  - Seamless transitions between screens

### Emergency Unlock System
- **Hidden Emergency Unlock Access**: Implemented secret 20-click sequence on "A Akhil" in About screen to access emergency unlock functionality
  - Click counter resets when navigating away from About screen
  - After 20 clicks, automatically navigates to Emergency Unlock screen
- **Emergency Unlock Screen**: New dedicated screen showing all currently locked apps with unlock times
  - Displays list of locked apps fetched from `LockManager.observeLocks()`
  - Shows app package name and unlock time for each locked app
  - Each app has an "Unlock" button to initiate emergency unlock process
- **60-Second Countdown Protection**: Multi-layered friction before unlocking
  - Clicking unlock shows full-screen loading overlay
  - Rotating circular progress indicator (white on black)
  - Countdown timer from 60 to 0 seconds
  - Cannot be cancelled - user must wait full minute
  - After 60 seconds, shows confirmation dialog
- **Confirmation Dialog**: Final safeguard against impulsive unlocking
  - Message: "Do you really wanna stop?"
  - "Yes" button unlocks the app via `LockManager.unlockApp()`
  - "No" button returns to emergency unlock screen without unlocking
  - Provides moment of reflection before breaking digital detox
- **Complete Lock Enforcement**: Removed all easy unlock paths
  - Removed unlock button from lock screen overlay (`AppLockOverlayActivity`)
  - Removed unlock option from all long-press context menus (home screen pinned apps, all apps list, hidden apps list)
  - Lock screen now only displays: app name, "Locked" label, countdown timer, unlock time, and motivational message
  - Only way to unlock: emergency process (20 clicks → 60s wait → confirmation) or wait for timer to expire
- **Digital Detox Philosophy**: Design enforces intentional usage
  - Multi-step process creates significant friction
  - Prevents impulsive app unlocking
  - Respects user's commitment to focus
  - Aligns with minimalist, distraction-free mission

### Technical Implementation
- Created `EmergencyUnlockScreen.kt` composable with Material3 components
- Added `isEmergencyUnlockVisible` state flow to `LauncherViewModel`
- Added `setEmergencyUnlockVisibility()` function to manage screen visibility
- Updated `LauncherUiState` data class with emergency unlock visibility property
- Integrated emergency unlock screen into `LauncherApp` navigation system
- Passed `lockManager` to `LauncherApp` for emergency unlock operations
- Used `collectAsState()` to observe locked apps reactively
- Coroutine-based countdown timer with `LaunchedEffect` and `delay(1000)`
- Proper state management with `rememberCoroutineScope()` for async operations

### App Lock System (Continued)
- **Enhanced Lock Screen Design**: Minimalist black and white interface
  - Digital detox message: "You chose to focus / Respect your decision"
  - Clean countdown timer (hours, minutes, seconds format)
  - Unlock time display (e.g., "Unlocks at Oct 29, 03:30 PM")
  - Light font weights (FontWeight.Light) for subtle appearance
  - 50-60% opacity on secondary text
- **Service Persistence**: `AppLockMonitorService` runs reliably
  - Foreground service with notification
  - Auto-starts when locks are active
  - Auto-stops when no locks remain
  - Handler-based monitoring (1-second intervals)
  - Works across all launchers system-wide

## 2025-10-28
- **Notification Settings Overhaul**: Moved every notification preference into the new `NotificationSettingsScreen`, leaving the inbox for message triage only.
  - Added hierarchical navigation (Settings → Notification Settings → Auto-clear / Log retention / App filters).
  - Simplified the main Settings screen to a single "Notification Settings" row and removed inline retention/filter chips from the inbox header.
- **Inbox Readability Upgrades**: Reworked notification rows to be compact yet expandable.
  - Tap any notification to expand and view the full title/body inline (no dialogs required).
  - Reduced padding, font sizes, and spacing so more items are visible without scrolling.
  - Added a 70% swipe threshold and gentler background animation to prevent accidental deletions.
- **Snackbar Removal**: Eliminated all snackbar usage across the launcher for a cleaner UI.
  - No more "Notification removed" or "App locked" pop-ups; actions apply immediately.
  - Removed `SnackbarHost` plumbing and related dependencies from `LauncherApp` and inbox components.
- **Notification Bell Badge Fix**: Replaced the old `BadgedBox` with a custom badge layout so the red unread indicator renders completely within the icon button.
  - Badge now uses explicit sizing, offset, and color styling to avoid clipping on all densities.

## 2025-10-29
- **Notification Inbox Polish**: Simplified inbox row interactions by removing the redundant "Mark read" button and relying entirely on swipe-to-dismiss gestures.
  - Tightened card padding and list spacing so notifications occupy less vertical space while retaining readability.
  - Pruned the unused mark-read callback wiring from `LauncherApp` and `MainActivity`, keeping the ViewModel API focused on mark-all-read and swipe actions.
- **System Notification Handling**: Added dynamic detection for system and essential packages so core services (Phone, Clock/Alarm, Camera, Settings, SMS/default dialer, Android System) stay in the system shade and never surface in the inbox.
  - Combines `ApplicationInfo` flag checks with runtime discovery of essential packages (dialer, SMS, camera, alarm, settings, Telecom default dialer) and purges any legacy entries/filters for those packages.
- **Filter List Cleanup**: Filtered out system apps from the notification filter list, removed raw package-name subtitles, and hid packages without friendly labels so the list only shows user-facing apps.
- **WhatsApp Compatibility**: Relaxed the group-summary guard to skip only aggregate summaries, ensuring single-thread notifications (e.g., WhatsApp chats) continue to land in the inbox.
- **Shade Cleanup**: Notification listener now cancels WhatsApp-style group summaries even when they are skipped from storage, so intercepted apps no longer leave residual notifications in the system shade.
- **Enhanced App Lock System**: Implemented persistent app lock overlay that blocks locked apps even when switching launchers.
  - Created `AppLockMonitorService` foreground service using `UsageStatsManager` to detect app launches without infinite loops (checks every 1 second).
  - Built full-screen blocking overlay (`AppLockOverlayActivity`) with countdown timer, motivational message ("You're doing great! Stay on track..."), and unlock time display.
  - Added PACKAGE_USAGE_STATS permission to onboarding flow—required for monitoring app usage.
  - Overlay intercepts locked app launches system-wide and forces user to return home until lock expires.
  - Service starts automatically when all permissions are granted and runs as foreground service for reliability.

## 2025-10-27
- **Permission Onboarding**: Added dedicated permission screen shown before launcher UI
  - Integrated `PermissionScreen` composable prompting for POST_NOTIFICATIONS, notification listener access, and device admin privileges
  - Wired `MainActivity` to manage permission request launchers and block UI until all permissions granted
  - Added placeholder notification listener service and device admin receiver (with XML policy) so permissions can be requested upfront
  - Manifest updated with new receiver/service registrations and rationale string for future double-tap lock feature
  - Ensured notification listener access can be granted by exporting the service and requesting a rebind after permission is toggled
  - Sideloaded builds now surface clear instructions (with App Info shortcut) explaining how to enable "Allow restricted settings" before toggling notification access
- **Exact Alarm Support**: Permission flow now requests `SCHEDULE_EXACT_ALARM` so reminders fire on time; added intent handling for settings fallback.
- **UI Polish**: Replaced Material checkbox with custom minimalist square checkbox in tasks/history lists and restyled permission-screen buttons with outlined treatment to match the monochrome theme.
- **Code Cleanup**: Removed all unused parameters and variables from codebase
  - Removed unused `pinnedMap` variable in `AppsManager.kt`
  - Removed unused `onDismiss` parameter from `SearchOverlay` composable in `LauncherApp.kt`
  - Removed unused `showTimePicker` variable from `FancyAddTaskDialog` in `TaskDialogs.kt`
  - Build now compiles without any "never used" warnings

## 2025-10-24
- Scaffolded Android project (`settings.gradle.kts`, `build.gradle.kts`, Gradle wrapper scripts) for the Minimalist Focus Launcher MVP.
- Implemented Room entities/DAOs and managers for tasks, pinned apps, hidden apps, and lock persistence.
- Added DataStore-backed settings manager and search manager to support universal search and customization hooks.
- Built Jetpack Compose UI for home, tasks, and all apps screens with pager navigation, universal search overlay, and bottom icon assignment workflow.
- Created `LauncherViewModel`, app container, and application class wiring managers into the UI layer.
- Added placeholder adaptive launcher icons and Material3 theme aligned with minimalist design guidelines.
- Added Material Components dependency (`com.google.android.material:material:1.12.0`) to satisfy `Theme.Material3.Dark.NoActionBar` style requirements during builds.
- Refactored search/view model flows to align hidden app data snapshots and updated Compose gesture handling to use file-level experimental opt-in and `consumePositionChange` for compatibility with latest Compose APIs.
- Reordered file-level opt-in annotation in `LauncherApp.kt` to comply with Kotlin top-level declaration rules and restore the build.
- Imported `SearchManager` into `LauncherApplication` and migrated swipe detection to `detectVerticalDragGestures` to align with updated Compose foundation APIs.
- Added default-launcher prompt flow leveraging `RoleManager` (API 29+) and legacy home settings intents, ensuring users can set Minimalist Focus as the system home screen.
- Relaxed app filtering, declared `QUERY_ALL_PACKAGES`, and expanded manifest `<queries>` so system dialer/camera apps appear in the launcher.
- Wrapped Room task mutations in `Dispatchers.IO` and introduced bottom icon fallbacks for dialer and camera packages to improve first-run usability.
- Corrected `detectVerticalDragGestures` usage in `LauncherApp.kt` to rely on supported `onDragEnd`/`onDragCancel` callbacks with trailing lambda handling, clearing recent compile errors.
- Forced system status/navigation bars to pure black via `MainActivity` window styling so the launcher honors the intended AMOLED appearance.
- Swapped the All Apps refresh text for a settings icon backed by a new in-app settings dialog (theme and clock format) and added Compose material icons support.
- Updated task insertion to return Room row IDs and emit snackbar feedback, fixing the unresponsive Add button behavior.
- Eliminated the stacked overlay when "keyboard on swipe" is enabled by introducing an inline All Apps search bar with focus/keyboard management, filtered results messaging, and overlay auto-dismiss logic limited to the All Apps page.
- Polished the Tasks entry affordance with a styled single-line field and placeholder copy so users can immediately understand where to type new items.
- Reconciled build-time regressions by removing the unused theme callback and correcting pointer gesture handling to remove the manual consumption call (Compose 1.6 gesture detector already handles event routing).
- Made the All Apps inline search box always visible so users can manually type queries even when the keyboard-on-swipe preference is disabled; the toggle now only controls auto-focus behavior on page navigation.
- Removed swipe-up gesture from home screen to prevent accidental search overlay activation.
- Enforced launcher-only mode: app now checks if it is the default launcher on create and resume; if not, it prompts the user to set it as default and immediately finishes, preventing any usage until the user makes it the system home screen.
- Battery optimizations: removed the infinite lock cleanup coroutine and now clean locks on-demand during `isLocked` checks; `refreshInstalledApps()` runs only on init since app list is static unless packages are installed/removed externally.
- Performance fix: changed `uiState` flow to `SharingStarted.Eagerly` and removed `stateIn` wrapper from `timeFlow` to keep all flows hot during pager navigation, eliminating lag when swiping between home/tasks/apps screens (acceptable battery trade-off since launcher enforces default-only mode and is always foreground when active).
- Auto-hide keyboard when leaving All Apps screen: added pager listener that dismisses keyboard and clears search query whenever user swipes away from the app drawer to home or tasks.
- Fixed All Apps initial load lag: moved `refreshInstalledApps()` to `Dispatchers.IO` and added a one-time-only guard so the expensive PackageManager scan runs in the background without blocking the UI thread; further optimized with `buildList`, early-continue logic, and inline sorting to reduce allocations (user-provided micro-optimization).
- Added package change listener: `BroadcastReceiver` in `AppsManager` automatically refreshes app list cache when apps are installed/removed/updated, ensuring All Apps stays in sync without manual refresh or polling.
- Completely reworked Tasks screen UX: replaced inline text field + Add button with FloatingActionButton (FAB) with + icon at bottom-right; clicking FAB opens AlertDialog with OutlinedTextField for task input; long press on any task opens edit dialog with rename/delete options; removed easy-to-accidentally-click "×" delete button.
- Changed FAB to white background with black icon for minimalist black/white theme consistency.
- Upgraded All Apps search from exact substring matching to fuzzy search algorithm: matches apps even when query chars are scattered (e.g., "what's" finds "WhatsApp", "gm" finds "Gmail").
- Added keyboard Done/Enter action in All Apps search: pressing Done launches the first matching app with a 3-step blink animation (150ms fade cycles) for visual feedback before opening.
- Fixed blink animation to properly complete 3 cycles and launch app; improved fuzzy search to strip punctuation from queries for better matching.
- Replaced settings dialog with full-screen settings page: clicking settings icon navigates to a dedicated screen with back arrow, larger fonts, and cleaner black/white layout for all settings options.
- Added lock duration picker dialog: long-pressing any app and selecting "Lock" now shows a slider with preset durations (30min to 31 days) plus custom hour input field for flexible app locking.
- Re-enforced default launcher requirement in both onCreate and onResume to prevent app usage unless set as system home screen (may require phone call lifecycle monitoring).
- Made home screen clock clickable: tapping the time opens the system clock/alarm app using multi-level fallback (AlarmClock intent → OEM-specific packages for 15+ manufacturers → dynamic search → error toast).
- Fixed app launch navigation: opening an app from All Apps screen now automatically returns to home screen when the app is closed, preventing users from being stuck in the app drawer.
- Fixed critical crash on app launch: clearing focus, hiding the keyboard, and resetting the All Apps search query before launching; retained a short 50ms settle delay and switched pager navigation to `scrollToPage(1)` to avoid the buggy animated scroll path that previously threw `CursorAnchorInfoController.updateCursorAnchorInfo` NPEs.
- Dismiss the keyboard whenever leaving the All Apps page (including via system back) by clearing focus during pager transitions so the IME no longer lingers on the home screen.

## 2025-10-26
- **Task History & Enhanced UI**: Implemented comprehensive task history feature with completed task tracking
- Added History icon in Tasks screen header to access full-page history view showing all completed tasks with timestamps
- Created FancyAddTaskDialog with spacious black/white design: multi-line task input, optional reminder toggle, proper calendar date picker and time picker
- Database schema updated to version 2: added `completedAt`, `scheduledFor`, `notificationId` fields to TaskEntity
- Task completion with 3-second undo grace period: when checked, tasks stay visible (grayed out) for 3 seconds allowing immediate undo by unchecking; automatically moved to history after grace period expires
- History screen now allows unchecking tasks to bring them back to active task list (checkbox added)
- Replaced "Remove" text button with trash icon (Icons.Filled.Delete) in history screen for cleaner UI
- Tasks automatically sorted by nearest scheduled time first, then by creation time (oldest first)
- EditTaskDialog now allows editing both task name AND scheduled time (reminder toggle + date/time picker)
- Task list now displays scheduled time below task title (formatted as "Today at HH:mm", "Tomorrow at HH:mm", "MMM dd at HH:mm", with ⚠ indicator for overdue tasks)
- **Notification System Implemented**: Added WorkManager-based notification scheduling for task reminders
  - Notifications automatically scheduled when adding/editing tasks with reminder times
  - Notifications cancelled when tasks are completed or deleted
  - POST_NOTIFICATIONS and SCHEDULE_EXACT_ALARM permissions added to manifest
  - Notification channel "Task Reminders" created for high-priority alerts
  - Tapping notification opens launcher app
- **Improved All Apps Search Algorithm**: Smart ranking system prioritizes better matches
  - Priority 1: Exact matches (e.g., "settings" → Settings)
  - Priority 2: Starts with query (e.g., "sett" → Settings)
  - Priority 3: Word boundary matches (e.g., "link" → LinkedIn, Family Link)
  - Priority 4: Substring matches (e.g., "link" → Blinkit)
  - Priority 5: Fuzzy scattered matches (last resort)
  - Results now sorted by match quality, then alphabetically
  - Eliminates false positives like "Live Transcribe" appearing for "sett" query
- All Apps drawer now hides background-only system utilities via dynamic detection: essential apps (dialer, camera, clock, settings, etc.) are kept by resolving core intents, while other preloaded tools stay hidden without hard-coded package lists.
- Hardened `proguard-rules.pro` with explicit keeps for coroutines dispatchers, Jetpack Compose runtime classes, Room entities/DAO/database, and WorkManager workers to support shrinked release builds without breaking reflection-driven components.
- **Show Seconds Toggle**: Added "Show seconds in clock" setting in Settings screen; home screen clock now conditionally displays seconds (HH:mm:ss / hh:mm:ss) based on user preference, persisted via DataStore.
- **Customizable Bottom Quick Launch Icons**: Implemented full bottom icon customization feature allowing users to assign any installed app to the left and right quick launch icons
  - Added "Bottom Quick Launch Icons" section in Settings screen showing current app assignments with clickable rows
  - Long-pressing bottom icons on home screen or clicking them in Settings opens app picker dialog
  - Full app list dialog shows all installed apps for selection
  - Icon assignments persist via DataStore (`bottomIconLeft` and `bottomIconRight` preferences)
  - Default fallback to Phone (left) and Camera (right) apps when no custom app is assigned
  - TODO item #3 from TODO.md completed
- Replaced slider-based time picker with Material3 DatePicker (calendar view) and TimePicker (clock dial) for professional reminder scheduling UX
- Fixed LazyColumn items() syntax error in HistoryScreen and overlaySnapshot combine lambda for Kotlin Flow compatibility
- Build successful: all compilation errors resolved, ready for device testing
- HistoryScreen shows all completed tasks with completion timestamps and remove option
- Updated EditTaskDialog with improved styling matching minimalist theme
- Task manager now separates active tasks from history, exposing both lists in UI state
