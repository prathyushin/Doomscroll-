# Doom Scroll

**A more intentional way to use Instagram and Threads.**

Doom Scroll is an independent Android app that brings the official Instagram and Threads websites into one focused interface. It uses Android WebView rather than unofficial Meta APIs, with a floating, auto-hiding switcher so you can move between the two services without keeping separate app interfaces open.

The project explores a simple idea: make the social services you choose easier to access, while making selected algorithm-driven destinations less prominent. It does not replace Instagram or Threads, and it cannot control every recommendation or feature served by Meta.

> **Project status (15 November 2026):** The latest published public device-testing release is **v1.2.0**. Version **v1.2.1** is under development in [pull request #7](https://github.com/prathyushin/Doomscroll-/pull/7). The v1.2.1 changes described below are the intended candidate scope, not a claim that v1.2.1 has already been released or verified on a physical device.

## Contents

- [What it does](#what-it-does)
- [Screens and interaction](#screens-and-interaction)
- [Download and install](#download-and-install)
- [How sign-in and sessions work](#how-sign-in-and-sessions-work)
- [Privacy and permissions](#privacy-and-permissions)
- [Known limitations](#known-limitations)
- [Build from source](#build-from-source)
- [Testing and reporting issues](#testing-and-reporting-issues)
- [Project status and license](#project-status-and-license)
- [Credits](#credits)

## What it does

Doom Scroll is designed around these features:

- **Instagram and Threads in one app:** Each service is displayed in its own Android WebView using the official website.
- **Platform switcher:** Switch between Instagram and Threads using a floating dock.
- **Auto-hiding glass-style dock:** The translucent dock hides after inactivity to keep the browsing area clear. Reveal it from the bottom edge.
- **Original website experience:** Stories, Reels, posts, and other supported content are rendered by the official websites rather than recreated by Doom Scroll.
- **Session-only Story hiding (v1.2.1 candidate):** Long-press a Story tile to hide or restore that account's Story in the current app session. This choice is temporary and is not saved.
- **Best-effort feed controls:** The app attempts to make selected recommendation-heavy destinations less prominent and to prefer the Following feed where the website exposes a usable control.
- **Fullscreen media:** Supports the WebView fullscreen flow when a website requests it.
- **Media selection:** Website upload requests can open Android's system picker.
- **Capture permissions:** Camera or microphone access is requested only when a supported, trusted Instagram or Threads page initiates the corresponding capture request.
- **External links:** Links outside the supported social websites are opened in the device's browser.

### What Doom Scroll is not

- It is not an Instagram or Threads replacement, nor an official Meta app.
- It is not affiliated with, endorsed by, or sponsored by Meta.
- It does not provide a separate Doom Scroll account or cloud-sync service.
- It does not guarantee a chronological feed or the removal of every recommendation.
- It does not bypass Meta authentication, platform restrictions, or website controls.

## Screens and interaction

1. Open Doom Scroll.
2. Sign in to Instagram and/or Threads through the official website shown inside the app.
3. Use the floating switcher to move between the services.
4. If the dock has hidden, tap the narrow bottom-edge area to reveal it.
5. Expand the dock to access the platform controls and the **About** entry.
6. Open **About** to see the project credit.

The About dialog displays:

> **Built by Pranav and Prathyush**

## Download and install

### Latest published release

The latest published public device-testing build is **Doom Scroll v1.2.0**:

- [Download v1.2.0 APK](https://github.com/prathyushin/Doomscroll-/releases/download/v1.2.0/DoomScroll-v1.2.0.apk)
- [View v1.2.0 release notes and checksum](https://github.com/prathyushin/Doomscroll-/releases/tag/v1.2.0)
- [Browse all releases](https://github.com/prathyushin/Doomscroll-/releases)

**v1.2.1 is not yet a published release.** Its implementation is being reviewed in [PR #7](https://github.com/prathyushin/Doomscroll-/pull/7). Do not use a v1.2.1 download URL until that release and its APK asset are actually published.

### Device requirements

- Android **8.0 (API 26) or later**.
- Internet connection for Instagram and Threads websites.
- Sufficient free storage for the app and website data.
- A compatible Android System WebView / Chrome WebView implementation.

The app's configured minimum Android version is API 26. Actual website compatibility can vary by Android version, WebView version, device manufacturer, and Meta website changes.

### Installation steps

1. Download the APK from the official GitHub release page linked above.
2. Open the downloaded APK on your Android device.
3. If Android asks whether to allow installation from that source, review the prompt and enable the permission only if you trust the source and intend to install this APK.
4. Review Android's app and security warnings before proceeding.
5. Open Doom Scroll and sign in through the official Instagram or Threads website.

If Android or your device manufacturer blocks the installation or displays a security warning, do not bypass that protection. Check that the APK came from this repository's release page and review the exact warning.

### Important signing and update note

The public v1.2.0 APK is a **debug-signed device-testing build**, not a production-signed Google Play release. Android may reject an update if the installed app and downloaded APK have different signing certificates. Uninstalling an existing installation can delete its local app data, including website sessions. Do not uninstall until you understand that consequence and have considered anything you need to preserve.

## How sign-in and sessions work

Doom Scroll loads the official Instagram and Threads websites inside Android WebView. Sign-in is handled by those websites.

- You sign in on the official website, not through a separate Doom Scroll login screen.
- Android WebView stores the website cookies and local website storage needed to keep sessions signed in.
- These records are stored locally in the app's WebView data; they are not a Doom Scroll cloud account.
- Clearing the app's data or website cookies can sign you out.
- Shared WebView cookie handling may help the services recognize an existing session, but Meta controls whether accounts can share authentication. Separate sign-in may still be required.

**Never share your password, one-time verification code, session cookie, or access token in an issue report or with anyone offering installation help.**

## Privacy and permissions

Privacy is a design goal of the v1.2.1 candidate. The following describes the current v1.2.1 branch implementation; it should not be read as a formal security certification.

### Data handling in the v1.2.1 candidate

- No Doom Scroll account or app-operated cloud backend is implemented.
- No app usage-history tracking or usage analytics are included.
- No notification-listener service is included; the app does not request Android notification access or read notification contents.
- Story hide/mute choices are held in memory for the current session only. They are not saved as a username history.
- No Meta API key is required. Sign-in takes place on the official websites.
- The app does not intentionally upload WebView login cookies or passwords to a Doom Scroll server.

The official websites may themselves store data, set cookies, and communicate with Meta as part of their normal operation. Doom Scroll does not control Meta's data practices.

### Why website data is stored

Instagram and Threads require cookies, JavaScript, and website storage for sign-in and normal website functionality. WebView session data remains on the device so that you do not have to sign in every time. Clearing this data will generally end those sessions.

### Camera, microphone, and media picker

The app declares camera and microphone permissions to support capture features requested by the social websites. In the v1.2.1 candidate, Android runtime permission prompts are initiated only after a trusted Instagram or Threads page requests the corresponding resource. Requests from untrusted origins are not intended to receive access.

For uploads, the website can request Android's system media picker. The picker is shown as part of that user-initiated upload flow. Whether a particular upload, camera, or voice feature works depends on the current website implementation and requires device testing.

### Security scope and limitations

The v1.2.1 work removes the previous notification-reading service and persisted usage/Story preference features. The project has reviewed the relevant app code and declared dependencies for these integrations, but this is not an exhaustive independent security audit. No software can guarantee that Android, WebView, Meta websites, or their dependencies are free of vulnerabilities.

## Known limitations

Doom Scroll depends on the official Instagram and Threads websites. Meta can change page structure, navigation, authentication, or media behavior at any time.

- Feed selection and recommendation suppression are **best-effort website controls**. They may stop working when Meta changes its markup and cannot guarantee a fully chronological or recommendation-free feed.
- Some features may behave differently in WebView than in the standalone Instagram or Threads apps.
- Uploads, camera capture, microphone/voice notes, fullscreen playback, and login flows need real-device verification across supported devices.
- Meta may require additional sign-in checks or may restrict some functionality in embedded browsers.
- OEM installation scanners and Android security controls may flag or block an APK. Review the exact warning; do not disable device protections to install it.

## Build from source

### Requirements

- JDK 17
- Android SDK with Android platform/build tools for API 35
- Gradle 8.7
- Internet access to resolve Gradle and Android dependencies

### Build commands

Clone the repository and check out the v1.2.1 development branch:

```bash
git clone https://github.com/prathyushin/Doomscroll-.git
cd Doomscroll-
git checkout v1.2.1-privacy-hardening
gradle testDebugUnitTest
gradle assembleDebug
```

The debug APK is generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

The project is configured with:

- Application ID: `com.prathyushin.doomscroll`
- Version name: `1.2.1` on the development branch
- Minimum Android version: API 26 (Android 8.0)
- Target Android version: API 35
- Java/Kotlin bytecode target: 17

### Release signing

A release build can be assembled with:

```bash
gradle assembleRelease
gradle bundleRelease
```

For a production-signed release, configure a private Android signing keystore and the required signing environment variables in a secure build environment. Never commit keystore files, passwords, or signing keys to the repository. Without the signing configuration, a release output must not be assumed to be production-signed.

### Continuous integration

GitHub Actions runs the configured unit-test and Android build tasks for supported branch and pull-request events. A successful CI run verifies that those configured tasks completed; it does **not** prove that every feature works on every physical device. Manual device testing is still required.

## Testing and reporting issues

When reporting a bug, include:

- Doom Scroll version and where you installed it from.
- Android version, device model, and WebView/Chrome version if known.
- The exact steps that reproduce the issue.
- What you expected to happen and what happened instead.
- A screenshot or redacted log, if it helps explain the problem.

Remove personal information before sharing screenshots or logs. Never attach passwords, verification codes, cookies, tokens, or private messages.

[Open the GitHub issue tracker](https://github.com/prathyushin/Doomscroll-/issues)

## Project status and license

This repository is public for code inspection, issue reporting, and project development. **No open-source license has been added.** Public visibility alone does not grant permission to reuse, redistribute, or create derivative works. Contact the maintainers for permission.

## Credits

**Built by Pranav and Prathyush.**

Doom Scroll is an independent project. Instagram, Threads, and Meta are trademarks of their respective owners. Their names are used only to identify the services this app can display.
