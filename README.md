# Doom Scroll Zen v2.0.0

**A lightweight, privacy-first Android environment for intentional Instagram and Threads use.**

Doom Scroll Zen combines Instagram and Threads into a single controlled Android application built with Kotlin and Jetpack Compose. Instead of eliminating social media, it preserves useful social interactions—DMs, Stories, profiles, posts, replies, and comments—while reducing addictive scrolling, algorithmic recommendations, and mindless consumption.

## What's in v2.0.0 Zen

- **Persistent WebView Platform Runtime**: Separate, persistent WebViews for Instagram and Threads with shared cookie/session persistence, instant tab switching without unnecessary reloads, renderer crash recovery, network error handling, and fullscreen media support.
- **Instagram Feed Firewall**: Preserves posts, Stories, profiles, DMs, comments, and likes while restricting Explore, Reels feeds, and suggested posts. Allows a single Reel intentionally opened from DMs while preventing endless next-Reel browsing.
- **Threads Integration**: Stable Threads environment with redirect-loop protection, Following-tab preference, and session controls.
- **Behavioral Intervention Engine**: Centralized state-driven engine (`IDLE → ACTIVE → GENTLE_PAUSE → COOLDOWN`) with configurable session limits, scroll/reopen thresholds, cooldowns, and session recovery.
- **Smart Focus Schedule**: Custom start/end times, weekday rules, overnight wrap-around handling, timezone/system-clock awareness, and strict Reels blocking during focus periods.
- **Local Story Controls**: Mute or unmute individual Story accounts locally inside Doom Scroll (or via long-press) without modifying Instagram account settings.
- **Local Notification Filtering**: Opt-in in-memory notification filter for official Instagram and Threads apps with 7 configurable categories (Direct messages, Mentions & replies, Comments, Reels shared through DMs, Likes, Following activity, Stories & Notes).
- **Privacy Dashboard & Just-in-Time Permissions**: Full transparency over permissions, local usage footprint, and one-tap data deletion. Camera, microphone, notification, and monitoring permissions are requested only when initiated by the user.
- **Zen Dark UI & Floating Glass Dock**: Charcoal (`#101114`), soft panels (`#1B1A1F`), mint accent (`#B8F2D0`), liquid-glass surfaces, auto-hiding dock, reduced-motion & accessibility support, two-color Zen Enso Aperture emblem, and redesigned About screen.

## Build locally

Requirements: Android SDK 35, JDK 17, and Gradle 8.7.

```bash
gradle testDebugUnitTest assembleDebug
```

The installable debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy Philosophy

No Doom Scroll account, no cloud tracking, no advertising profiles, no storage of DM content, and no retention of notification text.

---

Built by Pranav and Prathyush.
