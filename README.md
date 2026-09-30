# Doom Scroll

**Doom Scroll v1.0.0 — Android foundation**

Doom Scroll is a social-media experience designed around **intentional social use without infinite short-video consumption**.

The project keeps useful social functions while changing the behavioral model:

- Home prioritizes content from accounts the user follows.
- Algorithmic recommendations are not part of the primary Home feed.
- Search is intentional discovery rather than a recommendation surface.
- A user can deliberately request a short video, but the core policy prevents automatic Reel-to-Reel progression.
- Notifications are curated around useful events rather than routine engagement noise.
- Users can follow a person while choosing to hide that person's posts and/or stories.
- Doom Scroll does not inject its own advertising layer.
- Instagram and Threads capabilities will be integrated only through supported platform capabilities and verified APIs/policies.

## Current build

The repository currently contains the first Android application foundation:

- Kotlin + Jetpack Compose
- Android 9+ (minSdk 26)
- versionName = 1.0.0
- Warm ivory visual system
- Home / Notifications / Chats / Story / Profile navigation
- Integrated intentional search field
- Core DoomPolicyEngine
- Recommendation filtering
- Sponsored-content filtering at the product-policy layer
- Hidden-person filtering
- Single-intent short-video boundary
- Unit tests for the core policy rules
- GitHub Actions Android build and test workflow

## Architecture direction

Platform/API → Platform Adapter → Normalized Content Model → Doom Scroll Policy Engine → Local State/Cache → Doom Scroll UI

Platform-specific integrations are intentionally isolated so changes to Instagram/Threads capabilities do not redefine the product's core rules.

## Design direction

The UI/UX is being developed from the supplied Doom Scroll layout reference:

- warm ivory foundation
- classic/editorial character
- restrained typography
- calm surfaces
- integrated search
- intentional navigation
- no recommendation-first Explore surface

Figma remains the UI/UX source of truth.

## Project source of truth

- **Notion:** product requirements, durable decisions, research and architecture records
- **Figma:** UI/UX, components and interaction flows
- **GitHub:** Android source code, tests, builds and releases

## Status

**Working on it.**

This is an active development repository. Platform-dependent capabilities such as Instagram/Threads content access, Stories, DMs, publishing, notifications and advertising behavior must be verified against current official platform documentation before being treated as production commitments.

## Release target

The first official release target is **Doom Scroll v1.0.0**.
