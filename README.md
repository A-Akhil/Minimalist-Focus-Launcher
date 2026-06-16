# Minimalist Focus Launcher

<div align="center">
  <img src="logo_export/mini_launcher_logo.png" alt="Mini Launcher Logo" width="128"/>
  
  **A distraction-free Android launcher focused on simplicity and productivity**
  
  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
  [![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
  [![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
</div>

---

## Overview

A minimalist Android launcher designed to reduce digital noise and promote intentional device usage. With a pure black background and text-only interface, it helps you focus on what matters most.

### Core Philosophy
- **No visual clutter** - Just text, time, and tasks
- **Intentional usage** - Pin only the apps you truly need
- **Productivity first** - Built-in task manager and app locks
- **Battery efficient** - Pure black OLED-optimized design

<div align="center" style="margin: 20px 0;">
  <a href="https://play.google.com/store/apps/details?id=com.minifocus.launcher">
    <img src="https://raw.githubusercontent.com/steverichey/google-play-badge-svg/266d2b2df26f10d3c00b8129a0bd9f6da6b19f00/img/en_get.svg" alt="Get it on Google Play" height="80"/>
  </a>
</div>

---

## Features

### Three-Screen Layout
- **Home Screen** - Clock, date, and pinned apps (text-only)
- **Tasks Screen** - Built-in to-do list with checkboxes
- **All Apps Screen** - Alphabetical list of installed apps

### Key Functionality
- **Swipe Navigation** - Intuitive left/right swipes between screens
- **Universal Search** - Swipe up for fuzzy search across apps
- **Inline Search** - Optional keyboard search in All Apps (toggle in settings)
- **Pinned Apps** - Long-press to pin your most-used apps to home
- **App Management**
  - Hide apps from the main list
  - Lock apps with time-based restrictions
  - Two customizable bottom quick-launch icons
- **Task Manager** - Integrated to-do list accessible via swipe
- **Customization**
  - 12-hour or 24-hour clock format
  - Keyboard auto-open on All Apps swipe (optional)

### Design
- **Pure Black** background (#000000) for OLED screens
- **White Text** (#FFFFFF) for maximum contrast
- **No Icons** - Text-based app list
- **Minimalist UI** - Zero distractions, maximum focus

---

## Screenshots

> *Coming soon*

---

## Promotion Video

[docs/promo-video.mp4](docs/promo-video.mp4)

---

## Technical Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material3
- **Architecture:** MVVM with Repository pattern
- **Database:** Room (for tasks)
- **Preferences:** DataStore
- **Min SDK:** 29 (Android 10)
- **Target SDK:** 34 (Android 14)

### Key Dependencies
- Jetpack Compose
- Material3
- Room Database
- Kotlin Coroutines & Flow
- DataStore Preferences
- Accompanist System UI Controller

---

## Installation

### Build from Source

1. **Clone the repository**
   ```bash
   git clone https://github.com/A-Akhil/mini-launcher.git
   cd mini-launcher
   ```

2. **Open in Android Studio**
   - Use Android Studio Hedgehog (2023.1.1) or later
   - Ensure you have JDK 17+ installed

3. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**
   - Connect your Android device via USB
   - Run the app from Android Studio
   - Set as your default launcher when prompted

---

## Usage

### First Launch
1. Install and open Mini
2. Android will ask you to choose a launcher - select **Mini**
3. Grant necessary permissions (if prompted)

### Navigation
- **Swipe Left** → Tasks screen
- **Swipe Right** → All Apps screen  
- **Swipe Up** → Universal search overlay
- **Long-press app** → Pin/Unpin, Hide, Lock options
- **Tap bottom icons** → Quick launch apps (long-press to customize)

### Settings
- Open **All Apps** screen
- Scroll to bottom and tap **Settings**
- Configure:
  - Clock format (12h/24h)
  - Auto-open keyboard on swipe

---

## Roadmap

- [ ] App usage statistics
- [ ] Custom themes/accent colors
- [ ] Widget support on home screen
- [ ] Gestures customization
- [ ] Backup & restore settings
- [ ] Daily motivational quotes

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the GNU General Public License v3 - see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- Inspired by minimalist launcher philosophies
- Built with modern Android development best practices
- Designed for intentional and mindful device usage

---


<div align="center">

## Please support the development by donating.

[![BuyMeACoffee](https://img.shields.io/badge/Buy%20Me%20a%20Coffee-ffdd00?style=for-the-badge&logo=buy-me-a-coffee&logoColor=black)](https://buymeacoffee.com/aakhil)

</div>

---

<div align="center">
  <sub>Built with focus and simplicity</sub>
</div>
