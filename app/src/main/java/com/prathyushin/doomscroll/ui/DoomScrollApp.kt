package com.prathyushin.doomscroll.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prathyushin.doomscroll.core.model.ContentItem
import com.prathyushin.doomscroll.core.policy.DoomPolicyEngine
import com.prathyushin.doomscroll.data.SampleContentRepository

private val Ivory = Color(0xFFFFF3E6)
private val Paper = Color(0xFFF8F5F1)
private val Charcoal = Color(0xFF111111)
private val Burgundy = Color(0xFF43302E)
private val Crimson = Color(0xFF8B1735)

@Composable
fun DoomScrollApp() {
    var selected by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var intentionalVideoOpen by remember { mutableStateOf(false) }

    val engine = remember { DoomPolicyEngine() }
    val repository = remember { SampleContentRepository() }

    val home = repository.home().filter {
        engine.evaluate(it).decision.name == "ALLOW"
    }

    val visiblePosts = if (query.isBlank()) {
        home
    } else {
        home.filter {
            it.author.contains(query, ignoreCase = true) ||
                it.body.contains(query, ignoreCase = true)
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            background = Ivory,
            surface = Paper,
            primary = Burgundy,
            secondary = Crimson,
            onBackground = Charcoal,
            onSurface = Charcoal
        )
    ) {
        Scaffold(
            containerColor = Ivory,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFFF1ECE6)) {
                    listOf("Home", "Alerts", "Chats", "Story", "Profile")
                        .forEachIndexed { index, label ->
                            NavigationBarItem(
                                selected = selected == index,
                                onClick = { selected = index },
                                icon = {
                                    Text(
                                        listOf("⌂", "○", "□", "＋", "●")[index],
                                        fontSize = 18.sp
                                    )
                                },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                }
            }
        ) {
            when (selected) {
                0 -> HomeScreen(
                    posts = visiblePosts,
                    query = query,
                    onQuery = { query = it },
                    onVideoRequested = { intentionalVideoOpen = true }
                )
                1 -> SimpleScreen(
                    "Notifications",
                    "Only useful, actionable notifications."
                )
                2 -> SimpleScreen(
                    "Chats",
                    "Conversations first. No engagement noise."
                )
                3 -> SimpleScreen(
                    "Story",
                    "View or publish stories when supported."
                )
                4 -> SimpleScreen(
                    "Profile",
                    "Your account, controls and content preferences."
                )
            }
        }

        if (intentionalVideoOpen) {
            AlertDialog(
                onDismissRequest = { intentionalVideoOpen = false },
                title = { Text("Intentional video") },
                text = {
                    Text(
                        "You requested this video. Doom Scroll allows the intentional item, " +
                            "but it will not automatically continue to another recommended video."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { intentionalVideoOpen = false }) {
                        Text("Done")
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    posts: List<ContentItem>,
    query: String,
    onQuery: (String) -> Unit,
    onVideoRequested: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "DOOMSCROLL",
                color = Burgundy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.weight(1f)
            )
            Text("v1.0.0", color = Crimson, fontSize = 11.sp)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search intentionally") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFEAE6E1),
                focusedContainerColor = Color(0xFFEAE6E1),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Burgundy
            )
        )

        Spacer(Modifier.height(14.dp))

        Text(
            if (query.isBlank()) "Following" else "Intentional search",
            color = Burgundy,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        if (posts.isEmpty()) {
            Text(
                "Nothing found. Try a deliberate search.",
                color = Color(0xFF6B6661),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 18.dp)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    PostCard(post, onVideoRequested)
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: ContentItem, onVideoRequested: () -> Unit) {
    Surface(
        color = Paper,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.author, fontWeight = FontWeight.Bold, color = Charcoal)
                Spacer(Modifier.weight(1f))
                Text("•••", color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (post.type.name == "VIDEO") 230.dp else 180.dp)
                    .background(Color(0xFFE2DDD7), RoundedCornerShape(16.dp))
                    .then(
                        if (post.type.name == "VIDEO") {
                            Modifier.clickable(onClick = onVideoRequested)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (post.type.name == "VIDEO") "▶" else "PHOTO",
                    color = Burgundy,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(9.dp))
            Text(post.body, color = Charcoal, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SimpleScreen(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ivory)
            .padding(22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, color = Burgundy, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = Charcoal, fontSize = 15.sp)
    }
}
