# Doom Scroll

**A calmer way to use Instagram and Threads.**

Doom Scroll is an Android app that places the official Instagram and Threads websites in separate WebViews inside one app shell. It aims to make intentional social use easier while reducing default access to selected algorithmic destinations.

> **Repository status:** v1.2.1 is a privacy-minimized device-testing release. Automated tests and Android builds run in GitHub Actions; real-device testing is still required for Meta login, media uploads, voice notes, feed behavior, and device-specific installation.

## Downloads

- [Download Doom Scroll v1.2.1 APK](https://github.com/prathyushin/Doomscroll-/releases/download/v1.2.1/DoomScroll-v1.2.1.apk)
- [Latest release](https://github.com/prathyushin/Doomscroll-/releases/latest)
- [All releases](https://github.com/prathyushin/Doomscroll-/releases)

The public APK is debug-signed for device testing, not production-signed for Google Play. A debug-signed update may not install over a build signed with a different key; uninstalling an older build can erase its local app data, so back up anything important before doing so.

## v1.2.1 scope

- Instagram and Threads in one Android app, each in its own WebView.
- Shared Android WebView CookieManager to support persistent website login sessions.
- Pure-black interface with a floating translucent glass-style app switcher that auto-hides.
- Instagram Story and Reel viewing in the original website viewer.
- Long-press Story tiles to hide/unhide an account for the current app session only. Choices are not saved.
- Fullscreen WebView media support when the website requests it.
- Best-effort Instagram Explore/Reels route restrictions and DM-origin single-Reel handling.
- Best-effort Following-feed selection and recommendation-card suppression.
- Threads For You tab hiding and supported first-party navigation handling.
- Android system picker for website-requested photo/video selection.
- Camera and microphone permission requests only after a trusted Instagram/Threads page initiates a capture request.
- External HTTP(S) links outside supported social domains open in the system browser.

Website controls depend on Meta's current markup and are not guaranteed to survive site changes.

## Privacy by design

### What this app does not collect

- No app analytics or advertising SDKs are included in the declared Gradle dependencies.
- No Doom Scroll account, backend, telemetry endpoint, or upload service is implemented.
- No usage-history tracking or usage analytics are included.
- No notification-listener service is included. Doom Scroll does not request notification access or read notification contents.
- Story mute/hide choices exist only in memory for the current app session; they are not written to preferences.
- Doom Scroll does not request Meta API keys or ask for Instagram/Threads passwords outside the official websites.

### Necessary local session data

Instagram and Threads sign-in sessions are maintained by Android WebView cookies and website storage on the device. This is necessary for login persistence. These are platform session records managed by Android WebView, not a Doom Scroll account database. Clearing app data or website cookies will sign the user out.

The app enables JavaScript and website storage because the official social sites require them. WebView file/content access is restricted, mixed content is disabled, and external destinations are opened outside the embedded social views. No native JavaScript bridge is exposed to website content; the app uses narrow internal prompt messages for its own UI behavior.

### Camera, microphone, and media picker

Camera and microphone permissions remain declared because the social sites support capture features. Android runtime permission prompts are initiated only after a trusted Instagram/Threads page asks to use the corresponding capture resource. Unsupported resources and untrusted origins are denied. The system picker is opened only when a website requests a media upload and the user chooses to proceed.

### Limits of this privacy review

This release removes the app's notification-reading service, usage-history persistence, and persistent Story username preferences. The source and declared dependency list have been reviewed for these features and for obvious analytics/backend integrations. This is not a formal security certification or a guarantee against vulnerabilities in Android, WebView, Meta websites, or third-party platform code.

## Build and validation

Requirements: Android SDK, JDK 17, and Gradle 8.7.

```bash
git clone https://github.com/prathyushin/Doomscroll-.git
cd Doomscroll-
git checkout v1.2.1-privacy-hardening
gradle testDebugUnitTest
gradle assembleDebug
```

The installable debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

A successful CI build confirms compilation and automated tests only. It does not replace physical-device testing for login, media uploads, voice notes, feed behavior, fullscreen, Android Back handling, dock behavior, or OEM installation/security checks.

## Important limitations

Doom Scroll is an independent WebView shell around the official Instagram and Threads websites. It is not affiliated with, endorsed by, or sponsored by Meta. Meta controls authentication, website markup, available features, and compatibility.

## Bug reports

Report reproducible issues using the [GitHub issue tracker](https://github.com/prathyushin/Doomscroll-/issues). Include the app version, Android version/device, and steps to reproduce. Remove private information from logs. Never include passwords, session cookies, access tokens, or other secrets.

## Project status and licensing

This repository is public so anyone can inspect the code, download published builds, and report issues. No open-source license has been added yet. Public visibility does not grant permission to redistribute or create derivative works.

## Acknowledgements

Doom Scroll is an independent community project. Instagram, Threads, and Meta are trademarks of their respective owners.
