package com.prathyushin.doomscroll.data

import com.prathyushin.doomscroll.core.model.ContentItem
import com.prathyushin.doomscroll.core.model.ContentSource
import com.prathyushin.doomscroll.core.model.ContentType

class SampleContentRepository {
    fun home(): List<ContentItem> = listOf(
        ContentItem("1", "Asha", "A quiet morning by the sea.", type = ContentType.IMAGE),
        ContentItem("2", "Rahul", "Weekend notes and a few photographs.", type = ContentType.IMAGE),
        ContentItem("3", "Maya", "A short clip from today's walk.", type = ContentType.VIDEO),
        ContentItem("4", "Nikhil", "Cooking something simple tonight.", type = ContentType.IMAGE),
        ContentItem(
            "5", "Suggested Account", "This should never appear on Home.",
            source = ContentSource.RECOMMENDED
        )
    )
}
