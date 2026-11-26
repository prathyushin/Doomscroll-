# Doom Scroll

**A calmer, privacy-first way to use Instagram and Threads on Android.**

Doom Scroll combines Instagram and Threads into a single lightweight Android app built with Kotlin and Jetpack Compose. Instead of cutting you off from social media, it preserves useful social connections—DMs, Stories, profiles, posts, comments, likes, and replies—while reducing addictive scrolling, algorithmic recommendations, and mindless consumption.

## Latest Release — `v2.0.0` (Zen)

- **[Download Doom Scroll v2.0.0 APK](https://github.com/prathyushin/Doomscroll-/releases/download/v2.0.0/app-debug.apk)**
- [View v2.0.0 Release Notes](https://github.com/prathyushin/Doomscroll-/releases/tag/v2.0.0)
- [All Releases](https://github.com/prathyushin/Doomscroll-/releases)

> **Direct-Install Compatibility:** This release omits system-wide Accessibility and Notification Listener manifest declarations so Google Play Protect Enhanced Fraud Protection does not block installation when sideloading the APK. All core controls—Feed Firewall, Behavioral Intervention Engine, Smart Focus Schedule, Local Story Controls, Privacy Dashboard, and Floating Glass Dock—run locally inside Doom Scroll with zero sensitive system permissions.

## Key Features

- **Instagram & Threads in One Controlled App:** Persistent WebViews for both platforms with shared Meta cookie persistence, instant tab switching without unnecessary reloads, fullscreen media support, file/media picker integration, and automatic renderer crash recovery.
- **Instagram Feed Firewall:** Preserves posts, Stories, profiles, DMs, comments, and likes while blocking Explore, the Reels feed, and suggested recommendation cards. Allows viewing a single Reel intentionally opened from DMs while preventing endless next-Reel consumption.
- **Threads Integration:** Full Threads environment with redirect-loop protection, Following-tab preference, and session controls.
- **Behavioral Intervention Engine:** Centralized state-driven policy engine (`IDLE → ACTIVE → GENTLE_PAUSE → COOLDOWN`) with configurable session limits, scroll and reopen detection, calm non-punitive pause prompts, cooldowns, and session recovery.
- **Smart Focus Schedule:** Custom start/end times, weekday rules, overnight wrap-around handling, live timezone and system-clock change awareness, and strict Reels blocking during active focus windows.
- **Local Story Controls:** Mute or unmute individual Story accounts locally (via long-press or the Story Controls screen) without changing your Instagram account settings.
- **Zen Dark Interface & Floating Glass Dock:** Charcoal (`#101114`), soft panels (`#1B1A1F`), mint accent (`#B8F2D0`), liquid-glass surfaces, context-aware auto-hiding dock, reduced-motion & accessibility support, and a two-color Zen Enso Aperture icon.
- **Privacy Dashboard & Just-in-Time Permissions:** Full local transparency over permissions, on-device usage totals, and one-tap data deletion. Camera and microphone permissions are requested only when you initiate capture on a trusted Instagram or Threads page.

## Privacy & Security

- **No Doom Scroll account** and no cloud sync.
- **No analytics, telemetry, or advertising profiles.**
- **No storage of DM content** or Meta passwords—authentication happens directly on Meta's official web pages inside the WebView.
- **Minimal permissions:** Only network access plus optional just-in-time camera, microphone, and notification permissions. WebView file/content access is disabled and mixed content is blocked.

## Important Limitations

Doom Scroll is an independent WebView shell around the official Instagram and Threads websites. It is not affiliated with, endorsed by, or sponsored by Meta. Because Meta controls its website markup and authentication flows, DOM-level filters (suggested-post hiding, Story dimming, and Following-tab selection) operate on a best-effort basis and may require updates if Meta changes its web UI.

## Build Locally

Requirements: **Android SDK 35**, **JDK 17**, and **Gradle 8.7**.

```bash
git clone https://github.com/prathyushin/Doomscroll-.git
cd Doomscroll-
gradle testDebugUnitTest assembleDebug
```

The installable APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Credits

Built by **Pranav** and **Prathyush**.

*Instagram, Threads, and Meta are trademarks of their respective owners.*
