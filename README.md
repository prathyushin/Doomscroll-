# Doom Scroll

**A calmer way to use Instagram and Threads.**

Doom Scroll is an Android app that places the official Instagram and Threads websites in separate WebViews inside one app shell. It aims to make intentional social use easier while reducing default access to selected algorithmic destinations.

> **Repository status (14 November 2026):** The `v1.2.0-shared-login-media` branch is a draft release candidate. Its Android CI build and unit tests have passed. The public GitHub Releases page still publishes **v1.1.0**; v1.2.0 is not yet a published release. The usage dashboard and important-only notification work are tracked separately and are **not included in this candidate**.

## Downloads

- **[Latest published Android release](https://github.com/prathyushin/Doomscroll-/releases/latest)** — currently v1.1.0.
- [All published releases](https://github.com/prathyushin/Doomscroll-/releases)
- [v1.2.0 draft pull request](https://github.com/prathyushin/Doomscroll-/pull/5)
- [v1.2.0 usage and notification feature specification](https://github.com/prathyushin/Doomscroll-/issues/6)

Only install APKs published under this repository's Releases page. The v1.2.0 CI artifact is a build artifact, not a published release.

## Verified v1.2.0 candidate scope

The current v1.2.0 branch includes:

- **Instagram and Threads in one Android app**, each in its own WebView.
- **Shared WebView CookieManager** to support Meta's shared sign-in behavior for linked accounts where Meta allows it. Separate sign-in remains possible when required.
- **Pure-black dark interface** with a floating translucent glass-style app switcher.
- **Auto-hiding dock** that can be revealed from the bottom edge; it hides during media viewing and fullscreen playback.
- **Instagram Story muting:** long-press a Story tile to mute or unmute that account locally inside Doom Scroll.
- **Story and Reel viewing:** preserve the original website viewer and its navigation.
- **Fullscreen WebView media support** when the website requests a custom fullscreen view.
- **Instagram Explore and Reels-feed restrictions**, including best-effort route and navigation controls.
- **DM-origin single-Reel handling** to restrict onward Reel-feed navigation from that entry point.
- **Threads intent redirect handling** for supported first-party destinations.
- **External links** to non-supported destinations open in the system browser.

These controls operate on the website UI and routes exposed to the embedded WebView. They are not guaranteed to survive changes to Meta's websites.

## Not included in v1.2.0 candidate

The following requested features are specified in [issue #6](https://github.com/prathyushin/Doomscroll-/issues/6), but are not implemented in this branch:

### Usage Activity and usage limits

- No Instagram/Threads per-tab usage timer or combined usage dashboard is currently present.
- No daily/weekly usage history, session count, or average session duration is currently recorded.
- No configurable usage limit is currently implemented.
- The planned usage-limit controls belong only in **Settings** or **Usage Activity**. They must not appear as repeated banners, countdowns, or interruptions during normal browsing.
- When implemented, measurement must count only the visible active tab while Doom Scroll is foregrounded, pause when backgrounded or the screen is off, and store the data locally by default. It must be labelled as usage **inside Doom Scroll**, not usage in the separate official Instagram or Threads apps.

### Important-only notifications

Doom Scroll embeds the Instagram and Threads websites; a WebView does not automatically receive the native push notifications posted by the separate official apps. This candidate does not deliver Instagram or Threads event notifications.

The requested important-event filter (messages, new followers, likes, replies, mentions and shared media) must not be represented as working until a supported delivery source is implemented and tested. Followed-account posts, Stories, Notes and recommendation/promotional alerts are intended to remain suppressed. No private Meta endpoints, undocumented APIs, scraping, credential collection or platform-control bypasses are used.

An Android notification-listener approach, if considered later, would only filter notifications actually posted by separately installed official apps. It would require explicit user-granted notification access, remain optional, and could not guarantee that every event category is delivered by those apps.

## Build and validation status

The v1.2.0 Android workflow on GitHub Actions has completed successfully for the current candidate commit:

- Unit tests: **passed**.
- Debug APK assembly: **passed**.
- Release APK assembly: **passed**.
- Release AAB assembly: **passed**.
- Optional release signing: **skipped** because signing secrets were not available to that run.
- Installable debug APK artifact: uploaded by the workflow.

[View the v1.2.0 Android build workflow](https://github.com/prathyushin/Doomscroll-/actions/workflows/android.yml).

A successful CI build confirms compilation and automated tests only. It does not replace real-device checks for login, CAPTCHA, DMs, Story navigation, Reel fullscreen, dock behavior, Android Back handling, or long-duration stability. No production-signed Play Store package is claimed.

## Important limitations

Doom Scroll is an independent WebView shell around the official Instagram and Threads websites. It is not an independently implemented social client and is not affiliated with, endorsed by, or sponsored by Meta.

Meta controls authentication, website markup, available features, and notification behavior. These can change without notice. Route restrictions, navigation hiding, and Story muting are best-effort website integrations. Story muting is local to Doom Scroll and does not change account-level mute settings on Instagram.

## Build locally

Requirements: Android SDK, JDK 17, and Gradle 8.7.

```bash
git clone https://github.com/prathyushin/Doomscroll-.git
cd Doomscroll-
git checkout v1.2.0-shared-login-media
gradle testDebugUnitTest
gradle assembleDebug
```

The installable debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy and security

- Sign-in takes place on the official Instagram or Threads website inside the WebView.
- Doom Scroll does not request Meta API keys or provider passwords.
- Story mute preferences are stored locally on the device.
- The v1.2.0 candidate does not collect usage analytics or send behavioral telemetry to a server.
- No private Meta endpoints, undocumented notification APIs, or feed scraping are used.
- WebView file/content access is restricted and mixed content is disabled.
- No additional notification-listener or usage-access permission is requested by this candidate.

## Bug reports

Report reproducible issues using the [Bug Report template](https://github.com/prathyushin/Doomscroll-/issues/new?template=bug_report.yml). Include the app version, Android version/device, steps to reproduce, and relevant logs with private information removed. Never include passwords, session cookies, access tokens, or other secrets.

## Project status and licensing

This repository is public so anyone can inspect the code, download published builds, and report issues. **No open-source license has been added yet.** Until the project owner selects and adds a license, public visibility does not grant permission to redistribute or create derivative works. Contributions remain subject to GitHub's applicable terms.

## Acknowledgements

Doom Scroll is an independent community project. Instagram, Threads, and Meta are trademarks of their respective owners.
