# Doom Scroll

Doom Scroll is a native Android shell for purposeful access to Instagram and Threads, designed to reduce exposure to algorithmic rabbit holes.

## MVP direction

- Two native tabs: Instagram and Threads.
- Persistent WebView sessions using Android's CookieManager.
- Instagram Explore and Reels-feed routes are blocked; their navigation links are hidden where the site DOM exposes them.
- A directly shared singular Reel URL remains accessible as a one-item destination, with vertical swipe/scroll navigation locked on that route.
- Local Story-ring hiding preferences are stored on-device and applied to matching Story-tray elements where the Instagram DOM exposes identifiable horizontal story rails.
- No Meta API credentials, app passwords, private endpoints, or server-side behavioral telemetry are used.

## Technical boundaries

This MVP is a WebView shell around the official websites, not an independently implemented Instagram/Threads client. Site layout, login requirements, available features, and DOM structure are controlled by Meta and can change without notice. The route guard is enforced during WebView navigation and for same-document history changes. Cosmetic hiding and Story-ring targeting remain best-effort and require real-device testing against current website markup.

The app does not collect Instagram passwords. Authentication is performed by the platform website. If the platform refuses embedded authentication, a compliant external authentication fallback may be required; session sharing between browser contexts is not guaranteed.

## Build

GitHub Actions runs unit tests and assembles a debug APK on pushes to `main`, `v1-release-hardening`, and `mvp-webview-social-shell`.

The debug APK is for device testing and demonstration, not a permanent public update channel.
