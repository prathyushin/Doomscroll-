# Doom Scroll

**A calmer way to use Instagram and Threads.**

Doom Scroll is an Android app that puts Instagram and Threads in one lightweight WebView shell while reducing access to algorithmic rabbit holes. The goal is simple: keep the social features people intentionally use—friends, Stories, posts, search, profiles and DMs—without making Reels and Explore the default destinations.

> **Status:** Public v1.1.0 device-testing build. This project is actively developed. Platform website changes can affect compatibility.

## Download

- **[Download the latest Android APK](https://github.com/prathyushin/Doomscroll-/releases/latest)**
- [All releases and release notes](https://github.com/prathyushin/Doomscroll-/releases)

Download the APK asset from the latest release, open it on your Android device, and follow Android's installation prompts. Only install builds published under this repository's Releases page.

The first public APK is built with Android's standard debug signing key. It is suitable for evaluation and device testing, but it is **not a production-signed Play Store release**. Android may require you to approve installation of apps from the source you used. Do not disable device security protections to install it.

## What it does

- **Instagram + Threads:** two tabs in one Android app.
- **Persistent sessions:** Android WebView CookieManager stores supported website session cookies.
- **Less Explore and Reels:** Instagram Explore and Reels-feed routes are blocked, and matching navigation links are hidden where the website markup allows it.
- **Intentional shared Reel access:** a singular Reel opened from a DM is treated as a one-item destination; vertical navigation to the next Reel is restricted on that route.
- **Local Story muting:** save usernames locally and hide matching Story tiles/rings where the current Instagram page exposes identifiable Story-tray elements.
- **External links:** supported web links open outside the app in the system browser.

## Important limitations

Doom Scroll is a WebView shell around the official Instagram and Threads websites. It is **not** an independently implemented client and is not affiliated with, endorsed by, or sponsored by Meta.

Meta controls the websites, authentication flows, available features, and page markup. Those can change at any time. Route restrictions and DOM-based hiding are best-effort controls and may need updates when the websites change. Embedded login and session behavior can vary by device and platform policy. Story muting is local to Doom Scroll; it does not change account-level mute settings on Instagram.

Automated builds and unit tests are run in GitHub Actions. Real-device coverage is still important: test sign-in, DMs, Story trays, Reel navigation, external links, Android Back behavior, and both Threads tabs on your device. Report reproducible issues using the [Bug Report template](https://github.com/prathyushin/Doomscroll-/issues/new?template=bug_report.yml).

## Source code and development

- **[Browse the active release source](https://github.com/prathyushin/Doomscroll-/tree/v1-release-hardening)**
- [Browse the Android app source](https://github.com/prathyushin/Doomscroll-/tree/v1-release-hardening/app/src/main)
- [Inspect the route policy and tests](https://github.com/prathyushin/Doomscroll-/tree/v1-release-hardening/app/src/test)
- [View build and release automation](https://github.com/prathyushin/Doomscroll-/tree/v1-release-hardening/.github/workflows)
- [Report a bug or request an improvement](https://github.com/prathyushin/Doomscroll-/issues)

### Build locally

Requirements: Android SDK, JDK 17, and Gradle 8.7.

```bash
git clone https://github.com/prathyushin/Doomscroll-.git
cd Doomscroll-
git checkout v1-release-hardening
gradle testDebugUnitTest
gradle assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Privacy and security boundaries

- No Meta API keys or provider passwords are requested by Doom Scroll.
- Sign-in is performed on the official website inside the WebView.
- Story mute preferences are stored locally on the device.
- No private Meta endpoints, feed scraping, or server-side behavioral telemetry are part of this MVP.
- WebView file/content access is restricted and mixed content is disabled.

## Contributing

Bug reports, reproducible test cases, and focused pull requests are welcome. Please include your Android version/device, the app version, the steps to reproduce, and relevant logs or screenshots with private information removed. Never include passwords, session cookies, access tokens, or other secrets.

Before submitting a pull request, run:

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

## Project status and licensing

This repository is public so anyone can inspect the code, download published builds, and report issues. **No open-source license has been added yet.** Until the project owner selects and adds a license, public visibility does not grant permission to redistribute or create derivative works. Contributions remain subject to GitHub's applicable terms.

## Acknowledgements

Doom Scroll is an independent community project. Instagram, Threads, and Meta are trademarks of their respective owners.
