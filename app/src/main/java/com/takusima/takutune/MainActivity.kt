package com.takusima.takutune

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Purple = Color(0xFF9C5CFF)
private val PurpleDark = Color(0xFF6C35B5)
private val Background = Color(0xFF0D0914)
private val SurfaceColor = Color(0xFF17111F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TakuTuneApp() }
    }
}

@Composable
private fun TakuTuneApp() {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Purple,
            secondary = PurpleDark,
            background = Background,
            surface = SurfaceColor
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            TakuTuneScreen()
        }
    }
}

@Composable
private fun TakuTuneScreen() {
    var tab by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val tracks = remember {
        listOf(
            Track("After Dark", "Mr.Kitty", "AD"),
            Track("Resonance", "HOME", "RE"),
            Track("Nightcall", "Kavinsky", "NI"),
            Track("Midnight City", "M83", "MC"),
            Track("The Less I Know The Better", "Tame Impala", "TK")
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 22.dp, bottom = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                when (tab) {
                    0 -> HomeHeader()
                    1 -> SearchHeader(query) { query = it }
                    2 -> LibraryHeader()
                    else -> SettingsHeader()
                }
            }

            if (tab == 0) {
                item { SectionTitle("Recently played") }
                items(tracks.take(3)) { track -> TrackRow(track, playing) { playing = !playing } }
                item { SectionTitle("Made for you") }
                items(tracks.drop(3)) { track -> TrackRow(track, playing) { playing = !playing } }
            } else if (tab == 1) {
                val filtered = tracks.filter {
                    query.isBlank() || it.title.contains(query, true) || it.artist.contains(query, true)
                }
                if (query.isBlank()) {
                    item { EmptyHint("Search for songs, artists and albums") }
                } else {
                    items(filtered) { track -> TrackRow(track, playing) { playing = !playing } }
                    if (filtered.isEmpty()) item { EmptyHint("Nothing found yet") }
                }
            } else if (tab == 2) {
                item { LibraryCard("♥", "Liked songs", "Your favorite tracks") }
                item { LibraryCard("♫", "Playlists", "Create and manage playlists") }
                item { LibraryCard("◷", "History", "Recently played tracks") }
            } else {
                item { LibraryCard("🎨", "Appearance", "Purple dark theme") }
                item { LibraryCard("⏱", "Sleep timer", "Stop playback automatically") }
                item { LibraryCard("⚙", "Playback", "Queue and player settings") }
            }
        }

        if (playing) {
            MiniPlayer(tracks.first()) { playing = false }
        }

        BottomBar(tab) { tab = it }
    }
}

private data class Track(val title: String, val artist: String, val initials: String)

@Composable
private fun HomeHeader() {
    Column {
        Text("TakuTune", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text("Your music. Your vibe.", color = Color(0xFFB8AFC0), fontSize = 15.sp)
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(PurpleDark, Color(0xFF24133A), Color(0xFF120C19))))
                .padding(22.dp)
        ) {
            Column {
                Text("WELCOME TO", color = Color(0xFFDCCBFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("TakuTune", color = Color.White, fontSize = 31.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Discover something you love.", color = Color(0xFFE5DDF0), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SearchHeader(query: String, onQuery: (String) -> Unit) {
    Column {
        Text("Search", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        TextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Songs, artists, albums...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            shape = RoundedCornerShape(18.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun LibraryHeader() {
    Column {
        Text("Library", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Everything you saved", color = Color(0xFFB8AFC0), fontSize = 15.sp)
    }
}

@Composable
private fun SettingsHeader() {
    Column {
        Text("Settings", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Make TakuTune yours", color = Color(0xFFB8AFC0), fontSize = 15.sp)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun TrackRow(track: Track, playing: Boolean, onPlay: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceColor)
            .clickable { onPlay() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(
                Brush.linearGradient(listOf(Purple, PurpleDark))
            ),
            contentAlignment = Alignment.Center
        ) { Text(track.initials, color = Color.White, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, color = Color(0xFFAAA1B3), fontSize = 13.sp, maxLines = 1)
        }
        IconButton(onClick = onPlay) {
            Icon(if (playing) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Purple)
        }
    }
}

@Composable
private fun LibraryCard(icon: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceColor).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(PurpleDark), contentAlignment = Alignment.Center) {
            Text(icon, color = Color.White, fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFFAAA1B3), fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Color(0xFF8F8598), fontSize = 15.sp)
    }
}

@Composable
private fun MiniPlayer(track: Track, onPause: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF21172B)).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(Purple), contentAlignment = Alignment.Center) {
            Text(track.initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(track.artist, color = Color(0xFFB8AFC0), fontSize = 12.sp)
        }
        IconButton(onClick = {}) { Icon(Icons.Default.SkipPrevious, null, tint = Color.White) }
        IconButton(onClick = onPause) { Icon(Icons.Default.Pause, null, tint = Purple) }
        IconButton(onClick = {}) { Icon(Icons.Default.SkipNext, null, tint = Color.White) }
    }
}

@Composable
private fun BottomBar(selected: Int, onSelected: (Int) -> Unit) {
    val items = listOf(
        Icons.Default.Home to "Home",
        Icons.Default.Search to "Search",
        Icons.Default.LibraryMusic to "Library",
        Icons.Default.Settings to "Settings"
    )
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF100B16)).navigationBarsPadding().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, pair ->
            val active = selected == index
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onSelected(index) }.padding(horizontal = 14.dp)) {
                Icon(pair.first, contentDescription = pair.second, tint = if (active) Purple else Color(0xFF77707D), modifier = Modifier.size(23.dp))
                Text(pair.second, color = if (active) Purple else Color(0xFF77707D), fontSize = 11.sp)
            }
        }
    }
}
