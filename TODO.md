# TODO - Feature Backlog

## 0. Critical Fixes for Google Play Rejection (2026-02-23)
**Status:** Completed (Released in v1.2.2) - Production Access Received (2026-02-25)

### Challenge
The app was rejected due to "Missing description in Play Listing" and "Prominent disclosure Non-compliant design" regarding the `AccessibilityService` API used for the double-tap-to-lock feature.

### Required Actions
1.  **Update Play Store Description**:
    - Add explicit disclosure: "This app uses the AccessibilityService API (LockScreenAccessibilityService) to allow users to lock their device screen with a double-tap gesture. It does not collect or read any personal data."

2.  **Implement Prominent Disclosure Dialog**:
    - **Current Behavior**: Tapping "Grant" in `PermissionScreen` immediately launches system settings. This violates policy.
    - **Required Behavior**: Show a modal dialog *before* redirecting.
    - **Dialog Content**:
        - Title: "Accessibility Permission Needed"
        - Body: "Mini Launcher uses the Accessibility Service solely to lock your screen when you double-tap. No data is collected. If you decline, this feature will be disabled."
        - Buttons: Two clear options (e.g., "Not Now" and "Go to Settings").
    - **Implementation**:
        - Update `PermissionScreen.kt` (or `MainActivity.kt` logic) to show this `AlertDialog` when the user taps the lock accessibility permission item.
        - Only launch the intent if the positive button is clicked.

---

## 1. Double Tap to Turn Off Screen
**Status:** Completed (Released in v1.0.1)

### Description
Implement double-tap gesture on home screen to turn off the device screen.

### Known Challenges
- **Permission Issues:** Turning off the screen requires special permissions
  - `DEVICE_ADMIN` permission or Device Administrator policy
  - May require user to grant Device Admin privileges in Settings
  - Global permission onboarding flow (2025-10-27) now surfaces the device admin request at app launch; gesture logic still pending.
  - Exact alarm capability has been added to onboarding to support timely reminders once gesture-triggered locks interact with scheduling.

### Technical Approach
1. Implement device admin receiver for screen-off capability
2. Add double-tap gesture detector on home screen
3. Request device admin permission on first use
4. Handle permission denial gracefully with user guidance

### Dependencies
- Android DevicePolicyManager
- Device Admin manifest declarations

---

## 2. Configurable Text Size
**Status:** Completed (Released in v1.1.0)

### Description
Allow users to customize font sizes across the launcher interface.

### Scope
- Task list text
- App drawer labels
- Home screen clock
- Settings page text
- Dialog text

### Technical Approach
1. Add font size preference to SettingsManager (DataStore)
2. Create font size selector in settings page (e.g., Small, Medium, Large, Extra Large)
3. Define scaling factors in theme or composable constants
4. Apply dynamic text size modifiers throughout UI
5. Persist user preference

### UI Location
Settings page → "Text Size" option with preview

---

## 3. Customizable Home Screen Bottom Icons
**Status:** Completed

### Description
Allow users to customize both left and right bottom icons on home screen (currently Phone and Camera).

### Requirements
- Let user pick any installed app for left icon
- Let user pick any installed app for right icon
- Provide clear UI to change assignments
- Persist selections in database
---

## 4. Overview/Recents Black Background
**Status:** Not Started

### Description
Ensure the system Overview (Recents) screen shows the launcher's intended pure black background instead of the device's default wallpaper.

### Considerations
- Current behavior: overview preview displays system wallpaper when multitasking.
- Desired behavior: maintain minimalist solid black aesthetic across launcher and system overview.
- May require adjustments to window/task snapshot configuration in `MainActivity` or theme.


---

Here is the new feature added to your backlog as item #5:

---

## 4.1. Enhanced Hidden Apps UX
**Status:** Completed (2025-12-16)
**Priority:** High

### Description
Provide a secure, gesture-based access method for hidden apps without passcodes or biometrics. Users can hide apps from All Apps/Search and access them only through a dedicated gated screen in App Drawer Settings.

### Implementation Summary
- Removed inline "Hidden Apps" section from All Apps screen
- Added dedicated "Hidden Apps" entry in App Drawer Settings
- Gate entry with 10-second tap-and-hold gesture with circular progress animation
- Progress resets immediately if user releases early
- Once unlocked, users can view and unhide apps from the dedicated screen
- Hidden apps completely removed from All Apps and Search results
- Clear user guidance during unlock process ("Unlocking hidden apps…")

### Technical Details
- Extended `LauncherViewModel` with `isHiddenAppsVisible` overlay flag
- Created `HiddenAppsScreen` composable with press-hold progress tracking (10s duration)
- Centered hold button UI with 200dp circular progress indicator
- Migrated `CircularProgressIndicator` to lambda-based progress API
- All unhide actions moved exclusively to gated screen
- Integrated back-target handling in `MainActivity`/`LauncherApp`
- Fixed 4 Kotlin warnings: deprecated CircularProgressIndicator, unused parameters, deprecated Icons.Filled.ArrowBack
- Removed all "Tip" sections from settings screens (Home Settings, Clock Settings, App Drawer Settings)

**Build Status:** Zero Kotlin warnings (only Gradle 9 deprecation notice remains)

---

## 5. Notification Filter & Inbox
**Status:** Completed (Released in v1.1.0)

### Description
Implement a notification filtering system that blocks all incoming system notifications. All intercepted notifications will be stored in a notification history/inbox within the launcher, which the user must manually open to view.

### Known Challenges
- **Permission Issues:** This feature requires "Notification Access" (`NotificationListenerService`), which is a sensitive permission. The user must grant this manually through a specific system settings page.
- **Service Reliability:** The `NotificationListenerService` can be stopped by the system and needs to be robust.
- **Data Storage:** Storing a history of all notifications can lead to a large database. A data retention and purging strategy will be needed.
  - Permission onboarding (2025-10-27) already directs users to enable notification access; service implementation must leverage the granted access when delivered.
  - Permission screen informs sideload users how to enable "Allow restricted settings" and links to App Info for convenience.
  - Exact alarm privilege is now requested alongside other permissions so scheduled reminders can alert at precise times.

### Technical Approach
1. Create a class that extends `NotificationListenerService`.
2. In the `onNotificationPosted(StatusBarNotification sbn)` method:
   - Extract relevant data (app icon, app name, title, text, time) from the `sbn` object.
   - Store this data in a local database (e.g., Room).
   - Call `cancelNotification(sbn.getKey())` to immediately dismiss the notification so the user doesn't see it.
3. Create a new UI ("Notification Inbox") in the launcher to display the stored notification history from the database.
4. On first use, check for permission. If not granted, provide a clear explanation and a button that sends an `Intent` for `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS` to guide the user to the correct settings page.

### Dependencies
- `NotificationListenerService` API
- `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` manifest declaration
- Room Database for storing notification history

---

## 6. Fix Notification Badge Clipping in All Apps Header
**Status:** Completed (2026-03-01)
**Priority:** High

### Description
The red notification badge (unread count indicator) on the notification bell icon in the All Apps header appears clipped or invisible. Multiple layout approaches have been attempted (BadgedBox, Box with offset, Box with padding), but the badge consistently renders with its boundary cut off.

### Known Issues
- Badge rendering issue persists across different Compose layout strategies
- Problem may be device/density-specific or related to parent Box constraints
- Current implementation uses inner Box with padding (3.dp top/end) but user reports badge still not fully visible
---

## 7. Long Click to Uninstall in App Options
**Status:** Completed (Released in v1.0.0)

### Description
Add "Uninstall" as a long-click option alongside existing actions (e.g., lock, hide, pin) in the app options menu for each app in the launcher.

### Technical Approach
1. Add "Uninstall" to the long-press menu for app entries.
2. Use the standard uninstall intent for the selected package.
3. Handle uninstall result to refresh app list and update UI.

---

## 8. Enhanced App Lock (Persistent Overlay)
**Status:** Completed

### Description
App lock should prevent launching a locked app until the unlock time, even if the user switches to another launcher and returns. When a locked app is attempted to be opened, show a persistent overlay with a motivational message and unlock time.

### Requirements
- Overlay must appear when launching a locked app from our launcher.
- Overlay must also appear if the user tries to open the locked app from another launcher (as long as our launcher is still installed).
- Overlay screen should display:
  - App name (e.g., "App X is locked for 30 min")
  - Motivational message (static, e.g., "You chose to focus / Respect your decision")
  - Countdown timer showing remaining time
  - Unlock time
- Overlay must block interaction with the app until the lock expires.
- No unlock button - enforces digital detox.
- Emergency unlock only accessible through hidden 20-click sequence in About screen.

### Implementation Status
Created `AppLockMonitorService` - Foreground service using `UsageStatsManager` to detect app launches
Created `AppLockOverlayActivity` - Black screen with motivational message, countdown timer, and unlock time
Added `PACKAGE_USAGE_STATS` permission to manifest
Added `SYSTEM_ALERT_WINDOW` (overlay) permission to permission flow
Added usage stats permission to `PermissionScreen` and permission flow
Added overlay permission to `PermissionScreen` and permission flow
Service automatically starts/stops based on active lock state
Monitoring uses Handler.postDelayed (no infinite loop, checks every 1 second)
Works across launchers - intercepts app launches system-wide when our launcher is installed
**Removed "Go to Home" button - overlay auto-dismisses when timer expires**
**Back button completely blocked - user cannot escape the lock screen**
**Removed unlock button from lock screen overlay**
**Removed unlock option from long-press context menus**
**Emergency unlock system implemented:**
---

## 9. Consistent Back Button & Header Behavior
**Status:** Completed (2025-10-29)

### Description
Unify the back button and header UI across all screens. The back button should always return to the previous screen (not always home), and header styling should be consistent (icon, placement, text size, etc.).

---

---

## 10. Clock Widget Sync
**Status:** Not Started

### Description
Display the phone's default clock app data directly on the launcher home screen (e.g., next alarm time, active timers).

### Technical Approach
- Query `AlarmManager.getNextAlarmClock()` for next scheduled alarm
- Show formatted alarm time on home screen below the main clock
- Auto-refresh when alarm changes (broadcast receiver)
- Handle case where no alarm is set

### Dependencies
- Android AlarmManager API
- Broadcast receiver for alarm updates

---

## 11. Focus Mode (Lockdown)
**Status:** Not Started

### Description
Create configurable "Focus Mode" profiles where user selects a set of apps to block. When a profile is activated, all selected apps become inaccessible. Emergency access available via the existing unlock screen.

### Features
- Multiple named profiles (e.g., "Work", "Sleep", "Study")
- Per-profile app selection (checkboxes in all apps list)
- Quick toggle to activate/deactivate profiles
- Use existing app lock overlay for blocked apps
- Emergency unlock screen (20-tap sequence) to override

### UI Flow
1. Settings → Focus Mode → Create/Edit Profiles
2. Select profile, choose apps to block
3. Home screen widget/shortcut to activate profile
4. When profile is active, selected apps trigger lock overlay

### Technical Approach
- Store profiles in Room (profile name, list of package names)
- On profile activation, lock all selected apps with a special "focus mode" flag
- Reuse `AppLockOverlayActivity` with custom message ("Focus Mode Active")
- Profile timer (optional: auto-disable after X hours)

---

## 12. App Time Intention Dialog
**Status:** Completed (2026-03-01)

### Description
Before opening an app, show a dialog asking "How long do you want to use this app?" User selects a duration (5, 10, 15, 30 min or custom). After the time expires, show a reminder or auto-close the app.

### Features
- Dialog with preset durations: 5, 10, 15, 30 minutes + custom input
- Show current screen time for that app (today + past 7 days)
- Select behavior when time expires (in same dialog):
  - Show popup reminder (dismissible)
  - Force close app
  - Return to launcher home screen
- Clicking any time duration immediately opens the app (no Start button needed)

### UI Design
```
┌─────────────────────────────┐
│  How long do you want to    │
│  use [App Name]?            │
│                             │
│  Today: 24 min  │  7d: 3.2h │
│                             │
│  ○ 5 min   ○ 10 min         │
│  ○ 15 min  ○ 30 min         │
│                             │
│  Custom: [__] minutes       │
│                             │
│  After time expires:        │
│  ○ Show reminder            │
│  ○ Close app                │
│  ○ Return home              │
└─────────────────────────────┘
```

### Technical Approach
- Intercept app launch, show dialog before starting activity
- Store session start time and duration
- Background service checks elapsed time
- Show notification or overlay when time limit reached
- Query `UsageStatsManager` for daily screen time

### Settings
- Enable/disable per app
- Default action when time expires
- Snooze duration for reminders

---

## 13. 20-20-20 Rule Reminder
**Status:** Not Started

### Description
Implement the 20-20-20 eye health rule: Every 20 minutes of screen time, remind user to look at something 20 feet away for 20 seconds. Screen blurs and shows reminder overlay.

### Features
- Tracks total screen-on time (across all apps)
- Every 20 minutes, overlay activates:
  - Screen content blurs behind overlay
  - Message: "Take a break! Look 20 feet away for 20 seconds"
  - 20-second countdown timer
  - Cannot dismiss until timer completes
- Snooze option (skip once, resume tracking)
- Settings to enable/disable and customize intervals

### UI Design
```
┌─────────────────────────────┐
│      [Blurred screen]       │
│                             │
│       Eye Break Time!       │
│                             │
│  Look at something 20 feet  │
│  away for 20 seconds        │
│                             │
│        0:18 remaining       │
│                             │
│   [ Snooze 5 min ]          │
└─────────────────────────────┘
```

### Technical Approach
- Foreground service tracks screen-on time via `UsageStatsManager`
- Show fullscreen overlay activity every 20 minutes
- Blur background using `Modifier.blur()` or screenshot + blur
- Countdown timer (20 seconds)
- Store last break time to resume tracking after app restart

### Settings
- Enable/disable 20-20-20 rule
- Customize interval (default: 20 minutes)
- Customize break duration (default: 20 seconds)
- Snooze duration (default: 5 minutes)

---

## 14. Alphabetical Scroll Bar with Animations
**Status:** Completed (Released in v2.4.0)

### Description
Add a polished alphabetical fast-scroll side bar to the All Apps drawer, similar to Contacts app, with smooth animations and haptic feedback.

### Features
- Vertical A-Z strip on right edge of All Apps screen
- Section headers for each letter (only letters with apps)
- Touch/drag side bar to jump to sections
- Large centered bubble shows current letter while dragging
- Smooth spring animations when scrolling
- Haptic feedback on letter change (Vibration can be controlled in settings as it takes lots of battery)
- Letters scale up when finger is near
- Blur background slightly during fast-scroll

### Animation Details
- Bubble: scale from 0.8 → 1.0 with fade in/out
- Scroll: spring animation with slight overshoot
- Side bar: letters scale 1.0 → 1.2x when touched
- Touch ripple effect follows finger vertically

### Technical Approach
- Group apps by first letter in ViewModel
- `LazyColumn` with section headers
- Custom side bar overlay (Box with drag gesture)
- `animateScrollToItem` with spring spec
- `HapticFeedback.performHapticFeedback()`
- `AnimatedVisibility` for letter bubble
- `Modifier.blur()` for background during scroll

---

## 15. Universal Search (Dangling State)
**Status:** Incomplete / Evaluate

### Description
I added `SearchOverlay` and `SearchManager` when I started the project thinking I'd need a "search everything" screen. Now I'm not sure if I want it. It's sitting in the code but you can't actually open it right now.

- It searches tasks and hidden stuff too, which is cool.
- But... does a "minimalist" launcher really need two different search screens? Might just be extra bloat.
- Need to decide: either wire it up to a gesture or just delete the code later.

---

## 16. Custom Lock Screen Replacement
**Status:** Tentative / On Hold

### Description
Replace the system lock screen with a custom minimalist lock screen to ensure focus from the moment the device wakes up.

### Context
- **Play Store Approval Dependency:** I am currently holding off on this until the app passes Google Play Store review. I already got rejected once, so I need to be careful with permissions.
- **Implementation:** I'm not sure if I'll be able to code this right now. I'll see if I can add it later once the app is live.
- **Technical Challenges:** Custom lock screens require sensitive permissions and complex handling of system overlays/keyguard dismissal.

---

## 17. About Screen Polish & Play Store Integration
**Status:** Completed (Released in v1.0.0)

### Description
Enhance the About screen to make the app feel production-ready and encourage user engagement.

### Requirements
- **Dynamic Versioning:** Display `VersionName` and `VersionCode` from `BuildConfig` automatically.
- **Rate App:** Button to open the app's Play Store listing for reviews (`market://details?id=...`).
- **Share App:** Button to share the app link with friends via system share sheet.

### Technical Approach
- Use `BuildConfig` fields.
- Android Intents (`ACTION_VIEW`, `ACTION_SEND`).

---

## 18. Smart Empty States (Zero State Polish)
**Status:** Not Started

### Description
Replace blank screens with motivational or aesthetic empty state components when there is no content (e.g., cleared inbox, no tasks).

### Requirements
- **Visuals:** Minimalist iconography or illustrations for "No Content" states.
- **Dynamic Content:** Fetch daily motivational quotes or minimalist images from public APIs (e.g., ZenQuotes) to reward the user for clearing their tasks/inbox.
- **Offline Fallback:** Ensure it degrades gracefully to local assets if offline.

### Locations
- Daily Tasks (All done for today)
- Notification Inbox (Zero distractions)
- Search Results (No matches)

---

## 19. Global App Localization
**Status:** Completed (Released in v2.3.0)

### Description
Support multiple languages (Portuguese, French, Russian, Hindi, Tamil, Japanese, Indonesian, Turkish) via standard Android string resources and dynamic per-app language selection.

---

## Future Enhancements
- [ ] Widget support on home screen (But the thing is we need in black and white theme)
- [ ] Gesture customization (swipe actions)
- [ ] Theme color customization
- [x] Backup/restore settings and data (Completed in v2.0.1)
- [ ] App usage statistics integration
