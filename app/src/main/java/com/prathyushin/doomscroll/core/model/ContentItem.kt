package com.prathyushin.doomscroll.core.model

enum class ContentType { IMAGE, VIDEO, STORY, TEXT }

enum class ContentSource { FOLLOWING, RECOMMENDED, SEARCH, SPONSORED }

data class ContentItem(
    val id: String,
    val author: String,
    val body: String,
    val mediaUrl: String? = null,
    val type: ContentType = ContentType.IMAGE,
    val source: ContentSource = ContentSource.FOLLOWING,
    val isHiddenByUser: Boolean = false
)
