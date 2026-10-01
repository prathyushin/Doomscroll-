# Doom Scroll

**Doom Scroll v1.0.0 — device-level digital-wellbeing intervention**

Doom Scroll is an Android digital-wellbeing intervention system designed to make compulsive social-app sessions interruptible without becoming another social-media client.

## Architecture decision

The project has deliberately migrated away from the earlier Instagram/Threads client concept. V1 does **not** ingest, mirror, scrape, or proxy social-media feeds.

Instead, Doom Scroll operates locally on the device:

```
Android AccessibilityService
        ↓
Local behavior signals
        ↓
PolicyEngine
        ↓
Pause overlay
        ↓
Intentional continuation or exit
```

The product objective is to preserve access to the user's existing social apps while introducing a deliberate pause when sustained behavioral signals cross user-configured thresholds.

## V1.0.0 scope

### Included
- Android 9+ / API 26+
- Local AccessibilityService
- User-selected protected apps
- Session-duration detection
- Scroll-event accumulation
- Reopen-frequency signal
- Debounced policy evaluation
- Local intervention overlay
- Explicit intentional continuation
- Exit/back action
- Local-only preferences
- Calm warm-ivory editorial UI
- Unit tests for the policy engine
- GitHub Actions debug build and test

### Explicitly excluded
- Instagram/Threads feed aggregation
- Scraping or reverse-engineering private APIs
- Password collection
- Provider access tokens
- Cloud behavioral telemetry
- Message-content inspection
- Automated taps, swipes, ad skipping, or security bypasses
- Recommendation feed
- Infinite-scroll replacement UI
- Gamified streaks or engagement loops

## Privacy boundary

V1 stores policy configuration locally. The AccessibilityService consumes only the minimum event information needed for intervention decisions. It is configured without window-content retrieval and without gesture automation.

No server is required for the V1 intervention loop.

## User flow

1. Open Doom Scroll.
2. Choose which installed apps should be protected.
3. Configure the session limit and cooldown.
4. Enable Doom Scroll from Android Accessibility settings.
5. Use the selected apps normally.
6. When sustained signals cross the configured policy, Doom Scroll presents a pause.
7. The user can intentionally continue or leave.

## Important platform boundary

Android AccessibilityService is a privileged, user-consented capability. Distribution, disclosure, permission wording, and Play policy compliance must be validated against the current Google requirements before public release. This repository does not claim that a Play Store submission is already approved.

## Roadmap

- **V1.0:** local intervention core.
- **V1.1:** stronger heuristics, better battery profiling, false-positive tuning and richer settings.
- **V1.5:** optional local-only reflection/journaling and analytics.
- **V2.0:** evaluate any additional integrations only after platform-policy and privacy review.

## Source of truth

- **Research/specification:** project PDF + architecture audit
- **Notion:** durable product decisions, architecture, risks, roadmap and review notes
- **Figma:** visual system and interaction design
- **GitHub:** implementation, tests, CI and release artifacts

## Review status

This branch is the V1 device-intervention migration. It should be treated as an engineering implementation candidate until the CI build, on-device AccessibilityService behavior, permission flows, battery impact, false-positive behavior, and distribution-policy requirements have been verified.

