# Doom Scroll

Instagram without the Reels rabbit hole.

We built Doom Scroll because deleting Instagram completely isn't always realistic. Friends still text on DMs, share Stories, and send posts. The real problem is opening the app to reply to one message and losing an hour to endless Reels and algorithmic feeds.

Doom Scroll wraps Instagram in a lightweight, dark Android shell that keeps the parts you actually use—DMs, Stories, profiles, posts, and Search—while stripping out the Reels feed and nudging you to pause when a session drags on.

---

### Download

**Latest release: `v2.1.3` (Zen)**

- [**Download Doom Scroll v2.1.3 APK**](https://github.com/prathyushin/Doomscroll-/releases/download/v2.1.3/app-debug.apk)
- [Release notes (`v2.1.3`)](https://github.com/prathyushin/Doomscroll-/releases/tag/v2.1.3)
- [All releases](https://github.com/prathyushin/Doomscroll-/releases)

> **Installing on Android:** Download the APK and open it to install. If Android asks to allow installs from your browser or file manager, grant it for that app. We intentionally keep sensitive system permissions (like Accessibility and Notification Listener) out of direct-install builds so Google Play Protect doesn't block you from installing it.

---

### What it does

- **Opens straight into Instagram:** No extra setup walls or annoying "why are you opening this app?" popups.
- **No Reels feed:** The Reels tab is gone. If someone sends you a Reel in DMs—or you search for a specific video or food spot—you can watch that single video, and it stops you from endlessly swiping into the next one.
- **Search still works:** You can search for people by username, follow or message them, and look up topics without getting pulled into a random Explore grid.
- **Auto-hiding glass dock:** A small translucent handle sits quietly at the bottom edge. Tap it whenever you want to open Settings, and it tucks itself back away after 3 seconds.
- **Gentle session pauses:** Set a comfortable session limit and cooldown in Settings. When you cross it, Doom Scroll offers a calm pause instead of locking you out.
- **Focus schedules:** Set work hours or overnight wind-down windows (including midnight wrap-around) with stricter Reels blocking and shorter limits.
- **Local Story muting:** Long-press a Story tile (or add a username in Story Controls) to dim it locally inside Doom Scroll without touching your actual Instagram account settings.

---

### Privacy

Doom Scroll is 100% local to your phone:

- No Doom Scroll account.
- No analytics, trackers, or ads.
- No servers—your login happens directly on Instagram's official web page inside the WebView.
- We never read or store your DMs or passwords.
- Camera and microphone are only requested by Android when you actually try to take a photo or send a voice note.
- You can check your local footprint and wipe your usage history or muted list anytime inside the **Privacy Dashboard**.

---

### From v1 to v2.1.3

- **`v1.2.0` – `v1.2.1`**: Our first public test builds—rough around the edges, with WebView bugs and missing controls, which showed us what needed to be rebuilt from scratch.
- **`v2.0.0` (Zen)**: Full ground-up rewrite in Kotlin and Jetpack Compose with a state-driven pause engine, Focus Schedules, local Story muting, Privacy Dashboard, and a new minimal charcoal-and-mint icon.
- **`v2.0.1`**: Restored intentional Search/Explore (so you can find accounts and search topics), fixed Play Protect sideload blocking, and simplified the app name to **Doom Scroll**.
- **`v2.1.2` – `v2.1.3`**: Redesigned Settings and the floating dock with frosted liquid-glass surfaces, made the app launch straight into Instagram, added a 3-second auto-hiding 2-slot dock, and temporarily removed Threads to keep the core Instagram experience rock-solid.

---

### Good to know

Doom Scroll is an independent Android WebView app built around Instagram's official mobile website. It is not affiliated with or endorsed by Meta. Because Meta updates its web markup from time to time, feed and DOM filters work on a best-effort basis and get updated when things shift.

---

### Build it yourself

You'll need **JDK 17**, **Android SDK 35**, and **Gradle 8.7**:

```bash
git clone https://github.com/prathyushin/Doomscroll-.git
cd Doomscroll-
gradle testDebugUnitTest assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

Built by **Pranav** and **Prathyush**.
