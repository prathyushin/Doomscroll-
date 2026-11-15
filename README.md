# Doom Scroll

**A calmer way to use Instagram and Threads.**

Doom Scroll is an Android app that places the official Instagram and Threads websites in separate WebViews inside one app shell. It aims to make intentional social use easier while reducing default access to selected algorithmic destinations.

> **Repository status (15 November 2026):** **Doom Scroll v1.2.0 is now published** as a public Android device-testing release. The build and automated tests passed in GitHub Actions. Real-device testing of Meta login, media uploads, voice notes, feed filtering, and notification filtering is still required; this is not a production-signed Play Store package.

## Downloads

- **[Download Doom Scroll v1.2.0 APK](https://github.com/prathyushin/Doomscroll-/releases/download/v1.2.0/DoomScroll-v1.2.0.apk)** — public device-testing build.
- [Latest published release and notes](https://github.com/prathyushin/Doomscroll-/releases/latest) — v1.2.0.
- [All published releases](https://github.com/prathyushin/Doomscroll-/releases)
- [v1.2.0 implementation pull request](https://github.com/prathyushin/Doomscroll-/pull/5)
- [v1.2.0 usage and notification feature specification](https://github.com/prathyushin/Doomscroll-/issues/6)

Install the APK from the v1.2.0 release asset above. This is a debug-signed device-testing build, not a production-signed Play Store package.

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
- **Android media chooser bridge (new, pending verification):** WebView file-chooser requests are forwarded to Android's system document/photo picker, with support for the website's requested selection mode and MIME types.
- **Just-in-time media permissions (new, pending verification):** trusted Instagram/Threads pages can request camera or microphone access through Android runtime permission prompts. These permissions are not requested at app startup; only requested WebView capture resources are eligible for grant.
- **Following-feed preference and recommendation suppression (new, pending verification):** where Instagram/Threads expose a clear “Following” tab, the WebView attempts to select it. Explicitly labelled suggested/recommended post cards are hidden on a best-effort basis.
- **Local Usage Activity (new, pending verification):** records per-platform foreground time, combined daily totals, session counts, average session duration, and a seven-day history. Tracking pauses when the app is not resumed and records only the currently selected platform. Data stays in local app preferences.
- **Optional important-only notification filter (new, pending verification):** an opt-in Android notification-listener service filters notifications from the separately installed official Instagram and Threads apps using a local event-wording allowlist. Master and per-platform toggles default off; the user must explicitly grant Android notification access.

The media bridge, feed controls, and local Usage Activity are code-integrated but have not yet been verified on a physical Android device. Photo/video uploads, Stories, voice notes, and camera capture remain dependent on Meta's current web implementation and require end-to-end testing before being described as fully working. The Following-feed selection and recommendation suppression are heuristic website integrations: they cannot guarantee a strictly chronological feed or remove every recommendation if Meta changes its markup or does not expose the relevant control.

These controls operate on the website UI and routes exposed to the embedded WebView. They are not guaranteed to survive changes to Meta's websites.

## Not included in v1.2.0 candidate

The remaining requested feature not implemented in this branch is configurable usage limits.

### Usage limits

- A configurable usage limit is not currently implemented. The Usage Activity screen is informational and does not interrupt browsing with limit banners or countdowns.
- If limits are added later, their controls belong only in **Settings** or **Usage Activity**—never as repeated in-feed warnings or interruptions.

## Important-only notification limitations

The optional listener filters only notifications posted by the separately installed official Android apps (com.instagram.android and com.instagram.barcelona). It does not receive push events from the embedded WebViews. The filter is off by default, requires the user to grant Android notification access in system settings, and can be enabled independently for Instagram and Threads.

When enabled, the service keeps notifications whose visible text matches a local allowlist for messages, new followers, likes, replies, mentions, tags, shared media, and account-security/verification alerts; other notifications from the selected app are dismissed. This is wording-based and English-oriented: private/hidden notification text, language changes, or Meta wording changes can cause false positives or missed important events. Disable the filter if expected notifications are missing. Notification contents are processed in memory only and are not stored or sent to a server. No private Meta endpoints, undocumented APIs, scraping, credential collection, or platform-control bypasses are used.

## Build and validation status

The latest Android workflow for the candidate's media, usage, feed, and notification changes completed successfully:

- Unit tests: **passed**.
- Debug APK assembly: **passed**.
- Release APK assembly: **passed**.
- Release AAB assembly: **passed**.
- Optional release signing: **skipped** because signing secrets are not configured.
- CI artifact: **DoomScroll-v1.2.0-build** (contains the installable debug APK and release build outputs).

[View successful Android build #43677500679](https://github.com/prathyushin/Doomscroll-/actions/runs/43677500679).

A successful CI build confirms compilation and automated tests only. It does not replace real-device checks for login, photo/video picker, Story publishing, voice notes, feed behavior, notification filtering, usage timing, fullscreen, Android Back handling, or dock behavior. The public device-testing package is debug-signed, not a production-signed Play Store package.

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
- Usage totals are stored locally in app preferences; the candidate does not send behavioral telemetry to a server.
- No private Meta endpoints, undocumented notification APIs, or feed scraping are used.
- WebView file/content access is restricted and mixed content is disabled. Picker-returned content URIs are handled through Android's system chooser rather than enabling broad WebView file/content access.
- Camera and microphone permissions are declared but are requested only in response to a trusted platform page's capture request.
- Notification filtering is opt-in and requires the user to grant Android notification access manually in system settings. The service processes only Instagram/Threads package notifications and does not store or transmit their contents.

## Bug reports

Report reproducible issues using the [Bug Report template](https://github.com/prathyushin/Doomscroll-/issues/new?template=bug_report.yml). Include the app version, Android version/device, steps to reproduce, and relevant logs with private information removed. Never include passwords, session cookies, access tokens, or other secrets.

## Project status and licensing

This repository is public so anyone can inspect the code, download published builds, and report issues. **No open-source license has been added yet.** Until the project owner selects and adds a license, public visibility does not grant permission to redistribute or create derivative works. Contributions remain subject to GitHub's applicable terms.

## Acknowledgements

Doom Scroll is an independent community project. Instagram, Threads, and Meta are trademarks of their respective owners.
