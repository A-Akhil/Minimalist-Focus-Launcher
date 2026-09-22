# Privacy Policy

**Last updated:** 25 September 2026

Minimalist Focus ("the App") is developed by A-Akhil ("we", "us", or "our"). We are committed to protecting your privacy. This Privacy Policy explains what information the App collects, how it is used, and the choices you have.

## 1. Information We Do **Not** Collect

- No account is required to use the App.
- We do not collect, transmit, or store any personal data on our servers.
- The App does not use analytics, crash reporting, or advertising SDKs.
- We do not share any user data with third parties.
- The only network access is downloading a signed settings file and, when you ask, checking GitHub for a new release (see section 2).

All preferences, tasks, hidden-app lists, and notification inbox data are stored **locally on your device** using Android's encrypted storage mechanisms (Room database and DataStore). This data never leaves your device unless you back it up with your own tools (e.g., cloud backup services).

## 2. Optional Permissions and Their Purpose

The App requests the following permissions to deliver core functionality:

| Permission | Usage | Required? |
|------------|-------|-----------|
| `INTERNET` | Download the signed settings file that lets us adjust layout or switch features off without a full update, and check GitHub for a new release when you tap Check for updates. Nothing is uploaded. | Yes |
| `QUERY_ALL_PACKAGES` | Discover installed apps so they can be shown in the launcher, hidden, pinned, or locked. | Yes |
| `PACKAGE_USAGE_STATS` | Monitor app launches to enforce the lock timer when you have locked an app. | Yes |
| `SYSTEM_ALERT_WINDOW` | Display the lock overlay when a locked app is launched. | Yes |
| `POST_NOTIFICATIONS` | Allow the App to display reminders about locked apps or inbox updates (only when enabled). | Optional |
| Notification Listener (`BIND_NOTIFICATION_LISTENER_SERVICE`) | Needed only if you opt in to the notification inbox feature; allows the App to read and organize notifications locally. | Optional |

The App periodically downloads a small, signed settings file from GitHub (`raw.githubusercontent.com`) that can adjust layout or switch features off. When you tap **Check for updates** in About, it also asks GitHub (`api.github.com`) for the latest release number to tell you if a new version is on the Play Store. These requests contain no personal data, settings, or identifiers. As with any web request, GitHub receives your IP address and the request time, handled under [GitHub's Privacy Statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement).

You may revoke optional permissions at any time via Android system settings. If revoked, the related feature (e.g., notification inbox) will be disabled.

## 3. Children’s Privacy

The App is **not directed to children under 13** and does not knowingly collect personal information from anyone. The experience has no forms, accounts, or other fields where children could enter data. If you are a parent or guardian and believe your child has provided personal information through device-level features outside the App or via shared backups, please contact us so we can take appropriate action.

## 4. Diagnostic Data (Crash Reports)

We currently do not capture crash reports or other diagnostic telemetry. If we introduce an anonymized crash-reporting solution in the future, it will collect only non-identifying technical details needed to resolve stability issues. We will update this Privacy Policy and clearly disclose the change before enabling such collection.

## 5. Data Security

All data is stored locally on your device. We rely on Android’s built-in security features (sandboxing, encrypted storage on modern devices) to protect your information. Because no user data leaves your device, there is no remote storage to secure. The downloaded settings file is signed, and the App ignores any file that fails signature verification.

## 6. Changes to This Privacy Policy

We may update this Privacy Policy from time to time. When we do, we will update the "Last updated" date at the top. Material changes will be communicated via the project repository and release notes.

## 7. Contact

If you have any questions about this Privacy Policy or our data practices, please contact:

```
A-Akhil
Email: akhilarul324@gmail.com
GitHub: https://github.com/A-Akhil
```
