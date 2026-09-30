package com.prathyushin.doomscroll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prathyushin.doomscroll.auth.OAuthConfig
import com.prathyushin.doomscroll.auth.OAuthManager
import com.prathyushin.doomscroll.auth.Platform
import com.prathyushin.doomscroll.core.model.ContentItem
import com.prathyushin.doomscroll.core.model.ContentType
import com.prathyushin.doomscroll.core.model.NavigationDestination
import com.prathyushin.doomscroll.core.policy.DoomPolicyEngine
import com.prathyushin.doomscroll.data.SampleContentRepository

private val Ivory = Color(0xFFFFF3E6)
private val Paper = Color(0xFFFBF8F4)
private val Charcoal = Color(0xFF111111)
private val Muted = Color(0xFF746D66)
private val Burgundy = Color(0xFF43302E)
private val Crimson = Color(0xFF8B1735)
private val Dock = Color(0xFFE9E3DC)

@Composable
fun DoomScrollApp(oauth: OAuthManager) {
    var destination by remember { mutableStateOf(NavigationDestination.HOME) }
    var query by remember { mutableStateOf("") }
    var storyAuthor by remember { mutableStateOf<String?>(null) }
    var hiddenPeople by remember { mutableStateOf(setOf<String>()) }
    var connected by remember {
        mutableStateOf(
            setOf<Platform>().filter { oauth.isConnected(it) }.toSet()
        )
    }
    val engine = remember { DoomPolicyEngine() }
    val repository = remember { SampleContentRepository() }

    val all = repository.home().filterNot { it.author in hiddenPeople }
    val following = all.filter { engine.evaluate(it).decision.name == "ALLOW" }
    val searchResults = following.filter {
        query.isBlank() || it.author.contains(query, true) || it.body.contains(query, true)
    }
    val stories = following.filter { it.type == ContentType.STORY }

    MaterialTheme(colorScheme = lightColorScheme(
        background = Ivory, surface = Paper, primary = Burgundy,
        secondary = Crimson, onBackground = Charcoal, onSurface = Charcoal
    )) {
        Box(Modifier.fillMaxSize().background(Ivory)) {
            when (destination) {
                NavigationDestination.HOME -> HomeScreen(
                    posts = following, stories = stories,
                    onStory = { storyAuthor = it },
                    onSearch = { destination = NavigationDestination.SEARCH; query = it },
                    onHidePerson = { person -> hiddenPeople = hiddenPeople + person }
                )
                NavigationDestination.SEARCH -> SearchScreen(
                    query = query, onQuery = { query = it }, posts = searchResults,
                    onHidePerson = { person -> hiddenPeople = hiddenPeople + person }
                )
                NavigationDestination.NOTIFICATIONS -> EmptyScreen(
                    "Notifications", "Only direct, useful events. No engagement bait."
                )
                NavigationDestination.CHATS -> EmptyScreen(
                    "Chats", "Conversations first. No recommendation surface."
                )
                NavigationDestination.PROFILE -> ProfileScreen(
                    connected = connected,
                    onConnect = { platform ->
                        val config = when (platform) {
                            Platform.INSTAGRAM -> OAuthConfig.instagram(com.prathyushin.doomscroll.BuildConfig.INSTAGRAM_CLIENT_ID)
                            Platform.THREADS -> OAuthConfig.threads(com.prathyushin.doomscroll.BuildConfig.THREADS_CLIENT_ID)
                        }
                        if (config.clientId.isNotBlank()) oauth.begin(config)
                    },
                    onDisconnect = { platform -> oauth.disconnect(platform); connected = connected - platform }
                )
            }

            FloatingDock(
                selected = destination,
                onSelect = { destination = it },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )

            storyAuthor?.let { author ->
                StoryViewer(
                    author = author,
                    stories = stories,
                    onClose = { storyAuthor = null },
                    onHidePerson = { person ->
                        hiddenPeople = hiddenPeople + person
                        storyAuthor = null
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    posts: List<ContentItem>,
    stories: List<ContentItem>,
    onStory: (String) -> Unit,
    onSearch: (String) -> Unit,
    onHidePerson: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Ivory),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DOOM SCROLL", color = Burgundy, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                    Text("Following first. Nothing recommended.", color = Muted, fontSize = 12.sp)
                }
                Text("1.0.0", color = Crimson, fontSize = 11.sp)
            }
        }
        item {
            SearchBar(onClick = { onSearch("") })
        }
        if (stories.isNotEmpty()) {
            item {
                Column {
                    Text("Stories", color = Burgundy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        stories.forEach { story ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(64.dp).clickable { onStory(story.author) }
                            ) {
                                Box(Modifier.size(58.dp).background(Crimson, CircleShape), contentAlignment = Alignment.Center) {
                                    Text(story.author.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(story.author, fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
        items(posts, key = { it.id }) { post ->
            PostCard(post, onHidePerson)
        }
    }
}

@Composable
private fun SearchScreen(
    query: String, onQuery: (String) -> Unit, posts: List<ContentItem>,
    onHidePerson: (String) -> Unit
) {
    Column(Modifier.fillMaxSize().background(Ivory).padding(16.dp)) {
        Text("Search", color = Burgundy, fontSize = 28.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search people and posts") }, singleLine = true,
            shape = RoundedCornerShape(20.dp)
        )
        Spacer(Modifier.height(14.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            items(posts, key = { it.id }) { PostCard(it, onHidePerson) }
        }
    }
}

@Composable
private fun PostCard(post: ContentItem, onHidePerson: (String) -> Unit) {
    Surface(color = Paper, shape = RoundedCornerShape(22.dp), tonalElevation = 1.dp) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.author, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onHidePerson(post.author) }) {
                    Text("Hide", color = Crimson, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.fillMaxWidth().height(if (post.type == ContentType.VIDEO) 230.dp else 180.dp)
                    .background(Color(0xFFE5DED6), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (post.type == ContentType.VIDEO) "PLAY ONCE" else "EDITORIAL MEDIA", color = Burgundy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(9.dp))
            Text(post.body, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SearchBar(onClick: () -> Unit) {
    Surface(
        color = Color(0xFFEAE5DF), shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Text("⌕  Search intentionally", color = Muted, modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp))
    }
}

@Composable
private fun StoryViewer(
    author: String, stories: List<ContentItem>,
    onClose: () -> Unit, onHidePerson: (String) -> Unit
) {
    var index by remember(author) { mutableStateOf(stories.indexOfFirst { it.author == author }.coerceAtLeast(0)) }
    val current = stories.getOrNull(index)
    Surface(Modifier.fillMaxSize(), color = Color.Black) {
        Box(Modifier.fillMaxSize()) {
            Text("STORY", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart).padding(20.dp))
            Text("×", color = Color.White, fontSize = 28.sp, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).clickable(onClick = onClose))
            Column(Modifier.align(Alignment.Center).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(current?.author ?: author, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(18.dp))
                Text(current?.body ?: "Story unavailable", color = Color.White, fontSize = 18.sp)
                Spacer(Modifier.height(28.dp))
                Text("Auto-next is disabled.", color = Color.LightGray, fontSize = 12.sp)
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (index > 0) OutlinedButton(onClick = { index-- }) { Text("Previous") }
                    if (index < stories.lastIndex) OutlinedButton(onClick = { index++ }) { Text("Next") }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { onHidePerson(current?.author ?: author) }) {
                    Text("Hide this person", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun FloatingDock(
    selected: NavigationDestination, onSelect: (NavigationDestination) -> Unit, modifier: Modifier
) {
    Surface(
        modifier = modifier, shape = RoundedCornerShape(28.dp),
        color = Dock, tonalElevation = 8.dp, shadowElevation = 10.dp
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            NavigationDestination.values().forEach { item ->
                val active = item == selected
                Surface(
                    color = if (active) Burgundy else Color.Transparent,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(width = 58.dp, height = 46.dp).clickable { onSelect(item) }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(item.symbol, color = if (active) Color.White else Burgundy, fontSize = 19.sp)
                        Text(item.title, color = if (active) Color.White else Burgundy, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    connected: Set<Platform>, onConnect: (Platform) -> Unit, onDisconnect: (Platform) -> Unit
) {
    Column(Modifier.fillMaxSize().background(Ivory).padding(20.dp)) {
        Text("Profile", color = Burgundy, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text("Connect through official authorization only. Doom Scroll never asks for a social password.", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        Platform.values().forEach { platform ->
            val isConnected = platform in connected
            Surface(color = Paper, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(platform.label, fontWeight = FontWeight.Bold)
                        Text(if (isConnected) "Connected securely" else "Not connected", color = Muted, fontSize = 12.sp)
                    }
                    TextButton(onClick = { if (isConnected) onDisconnect(platform) else onConnect(platform) }) {
                        Text(if (isConnected) "Disconnect" else "Connect")
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Privacy", color = Burgundy, fontWeight = FontWeight.Bold)
        Text("OAuth access tokens are encrypted with an Android Keystore-backed AES-GCM key and are never displayed or logged.", color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun EmptyScreen(title: String, subtitle: String) {
    Column(Modifier.fillMaxSize().background(Ivory).padding(22.dp), verticalArrangement = Arrangement.Center) {
        Text(title, color = Burgundy, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Charcoal, fontSize = 15.sp)
    }
}
