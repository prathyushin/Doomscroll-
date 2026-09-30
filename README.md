# Doom Scroll

**Doom Scroll v1.0.0 — intentional social client foundation**

Doom Scroll is an Android social-media client designed around intentional social use rather than recommendation-driven infinite consumption.

## V1 product rules

- **Home = Following first.** Recommended content is filtered from the primary Home feed.
- **Search is a destination, not Explore.** Discovery happens through an explicit search action.
- **Five primary destinations:** Home, Search, Notifications, Chats, Profile.
- **Floating dock:** macOS-style floating navigation dock with an ivory editorial visual system.
- **Stories are separate.** Story viewing never auto-advances; Previous/Next are explicit user actions.
- **Per-person hiding:** a person can be hidden from the local feed/story surface.
- **No password collection:** Instagram and Threads connection starts an official browser OAuth flow.
- **Token protection:** OAuth access tokens are encrypted with an Android Keystore-backed AES-GCM key and are never rendered in UI/logs.
- **No recommendation injection:** the core policy engine rejects recommended and sponsored feed items.
- **Intentional video boundary:** the product policy prevents automatic continuation into another short video.

## Platform integration boundary

The Android client contains the OAuth authorization-code + PKCE flow and callback routing for Instagram and Threads. Platform access is still governed by the provider's current APIs, scopes, app-review requirements, rate limits and account eligibility.

Client IDs are **configuration, not secrets**. Supply them through Gradle properties:

```properties
INSTAGRAM_CLIENT_ID=your_instagram_app_id
THREADS_CLIENT_ID=your_threads_app_id
```

Do not commit client secrets or access tokens. If a provider requires a confidential client secret for a particular exchange, the exchange must be moved to a trusted server; never embed that secret in the APK.

The registered redirect URIs for this build are:

- `doomscroll://oauth/instagram`
- `doomscroll://oauth/threads`

These must match the redirect configuration of the corresponding provider application.

## Architecture

```
Provider OAuth/API
      ↓
Platform adapter
      ↓
Normalized content model
      ↓
DoomPolicyEngine
      ↓
Local state / secure token store
      ↓
Compose UI
```

Platform-specific behavior is isolated from the product policy so Instagram/Threads API changes do not redefine the Doom Scroll experience.

## Android stack

- Kotlin
- Jetpack Compose + Material 3
- Android 9+ / API 26+
- Target SDK 35
- Java/Kotlin 17
- Android Keystore + AES-GCM token encryption
- GitHub Actions build/test workflow

## Design system

The UI follows the supplied ivory editorial direction:

- warm ivory foundation `#FFF3E6`
- charcoal typography
- deep burgundy structure
- restrained crimson accents
- calm paper-like surfaces
- generous whitespace
- rounded editorial cards
- no recommendation-first Explore surface

## Project source of truth

- **Notion:** product requirements, durable decisions, research and architecture
- **Figma:** UI/UX, components and interaction flows
- **GitHub:** Android source, tests, builds and releases

## Release status

**V1.0.0 Android implementation is in the repository.**

The remaining production deployment work is provider-side configuration: registering the app with the supported Instagram/Threads APIs, configuring exact approved scopes and redirect URIs, completing any required platform review, and supplying the client IDs through build configuration.
