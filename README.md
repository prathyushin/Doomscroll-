# Doom Scroll

**Doom Scroll v1.1 — native Android digital-wellbeing intervention**

Doom Scroll helps people keep purposeful access to the social apps they already use while making passive, extended consumption interruptible. It is not a social network, a feed mirror, or a replacement browser.

## Product principle

**Make passive consumption interruptible without becoming another engagement surface.**

The app works locally on Android:

```
Android AccessibilityService (user enabled)
        ↓
Minimal app/session + scroll-event signals
        ↓
Local behavior tracker
        ↓
Deterministic PolicyEngine
        ↓
15-second pause overlay
        ↓
Intentional continuation or leave
```

## V1.1 core

- Native Kotlin + Jetpack Compose application.
- Android 9+ / API 26+.
- User-selected protected apps, with launcher-app search.
- Local session-duration measurement.
- Rolling 60-second scroll-event-rate window.
- Sustained high-rate scrolling must persist before it can trigger an intervention.
- Reopen tracking across sessions, with a rolling 10-minute window.
- User-configurable session limit and cooldown.
- 15-second pause before intentional continuation.
- Local intervention history; history writes run off the main thread.
- Local preferences for selected apps and policy settings.
- Prominent Accessibility disclosure and affirmative consent before opening Accessibility settings.
- Unit tests for policy thresholds, sustained scrolling and reopen tracking.
- GitHub Actions build/test workflow.

## Explicitly excluded

- Instagram/Threads feed aggregation or mirroring.
- Meta credentials, social-provider tokens or password collection.
- Scraping or reverse-engineering private APIs.
- Reading screen text, message content or typed content.
- Cloud behavioral telemetry.
- Automated taps, swipes, ad skipping or security bypasses.
- Recommendation feeds, infinite-scroll replacement UI or engagement streaks.

## Privacy and Android boundaries

The AccessibilityService is configured with `canRetrieveWindowContent=false` and `canPerformGestures=false`. It uses selected-app package transitions and scroll-event metadata to estimate usage patterns; it does not measure true pixel-level scroll velocity. The scroll-rate signal is an event-rate proxy, not a clinical or diagnostic measure.

The app picker declares visibility for launcher activities rather than requesting broad package visibility. Behavior signals and preferences remain local. No server is required for the V1 intervention loop.

Accessibility is an optional, user-enabled Android capability. The app explains its use and requires affirmative consent before directing the user to system settings. This does not by itself establish Google Play approval: current Play declarations, privacy policy/Data Safety requirements, device testing and distribution review remain separate release gates.

## User flow

1. Open Doom Scroll.
2. Read the Accessibility disclosure and explicitly consent if protection is desired.
3. Select the apps to protect.
4. Set a session limit and cooldown.
5. Enable Doom Scroll in Android Accessibility settings.
6. Use the selected apps normally.
7. When a sustained policy threshold is reached, a 15-second pause appears.
8. Choose to continue intentionally or leave.

## Verification still required

- Run CI unit tests and Android release compilation.
- Install and test on physical devices, including Android 9+ and recent Android versions.
- Verify service enable/disable, revocation, reboot and OEM background behavior.
- Measure battery use and tune false positives.
- Test overlay lifecycle, rapid app switching and cooldown.
- Complete current Play Console Accessibility declaration, privacy policy and Data Safety work before public Play distribution.

## Source of truth

- **Notion:** product decisions, architecture, risks and roadmap.
- **Figma:** visual and interaction design.
- **GitHub:** implementation, tests, CI and release artifacts.

