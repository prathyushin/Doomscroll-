package com.prathyushin.doomscroll.model

enum class SocialPlatform(val displayName: String, val homeUrl: String, val hostSuffixes: Set<String>) {
    INSTAGRAM(
        displayName = "Instagram",
        homeUrl = "https://www.instagram.com/",
        hostSuffixes = setOf("instagram.com", "cdninstagram.com")
    ),
    THREADS(
        displayName = "Threads",
        homeUrl = "https://www.threads.net/",
        hostSuffixes = setOf("threads.net", "threads.com")
    )
}

enum class ZenDestination(val label: String) {
    DASHBOARD("Zen"),
    INSTAGRAM("Instagram"),
    THREADS("Threads"),
    FOCUS("Focus Schedule"),
    STORIES("Story Controls"),
    NOTIFICATIONS("Notifications"),
    APPS("App Selection"),
    PRIVACY("Privacy"),
    ABOUT("About")
}

enum class WebConnectionState {
    IDLE,
    LOADING,
    READY,
    OFFLINE,
    ERROR
}
