package com.prathyushin.doomscroll.model

enum class NotificationCategory(
    val key: String,
    val title: String,
    val description: String,
    val defaultEnabled: Boolean
) {
    DIRECT_MESSAGES(
        key = "dm",
        title = "Direct messages",
        description = "Private messages and message requests",
        defaultEnabled = true
    ),
    MENTIONS_REPLIES(
        key = "mentions_replies",
        title = "Mentions and replies",
        description = "Direct @mentions and thread replies",
        defaultEnabled = true
    ),
    COMMENTS(
        key = "comments",
        title = "Comments",
        description = "Comments on your posts and threads",
        defaultEnabled = true
    ),
    DM_SHARED_REELS(
        key = "dm_reels",
        title = "Reels shared through DMs",
        description = "Clips or reels sent directly by friends",
        defaultEnabled = true
    ),
    LIKES(
        key = "likes",
        title = "Likes",
        description = "Post, reply, and story likes",
        defaultEnabled = false
    ),
    FOLLOWING_ACTIVITY(
        key = "following",
        title = "Following activity",
        description = "New followers and follow-back alerts",
        defaultEnabled = false
    ),
    STORIES_NOTES(
        key = "stories_notes",
        title = "Stories and Notes",
        description = "Story mentions, notes, and new story uploads",
        defaultEnabled = false
    )
}

data class NotificationFilterConfig(
    val masterEnabled: Boolean = false,
    val filterInstagramApp: Boolean = false,
    val filterThreadsApp: Boolean = false,
    val enabledCategories: Set<NotificationCategory> = NotificationCategory.entries
        .filter { it.defaultEnabled }
        .toSet()
)

data class NotificationFilterDecision(
    val shouldKeep: Boolean,
    val category: NotificationCategory?,
    val reason: String
)

data class NotificationStats(
    val keptCount: Int = 0,
    val filteredCount: Int = 0
)

data class MutedStoryAccount(
    val username: String,
    val mutedAtEpochMs: Long
)
