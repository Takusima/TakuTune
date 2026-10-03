package com.takusima.takutune

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.takusima.takutune.core.database.TakuTuneDatabase
import com.takusima.takutune.core.model.Track
import com.takusima.takutune.library.LocalLibraryRepository
import com.takusima.takutune.library.LocalMusicScanner
import com.takusima.takutune.playback.PlaybackController
import com.takusima.takutune.playback.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Background = Color(0xFF09070D)
private val SurfaceColor = Color(0xFF15101C)
private val Elevated = Color(0xFF21182C)
private val Purple = Color(0xFFB36BFF)
private val SecondaryText = Color(0xFFAAA0B4)

class MainActivity : ComponentActivity() {
    private lateinit var playback: PlaybackController
    private lateinit var library: LocalLibraryRepository
    private var tracks by mutableStateOf<List<Track>>(emptyList())

    private val permission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scan()
    }

    private fun scan() {
        lifecycleScope.launch {
            val found = withContext(Dispatchers.IO) { LocalMusicScanner(contentResolver).scan() }
            withContext(Dispatchers.IO) { library.replaceTracks(found) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        library = LocalLibraryRepository(TakuTuneDatabase.get(this).dao())
        playback = PlaybackController(this)

        lifecycleScope.launch {
            library.observeTracks().collectLatest { tracks = it }
        }

        if (Build.VERSION.SDK_INT >= 33) permission.launch(Manifest.permission.READ_MEDIA_AUDIO)
        else permission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        setContent {
            val playbackState by playback.state.collectAsState()
            TakuTuneApp(
                tracks = tracks,
                playback = playbackState,
                onPlay = { track, index ->
                    playback.playQueue(tracks, index)
                    lifecycleScope.launch(Dispatchers.IO) { library.addHistory(track) }
                },
                onToggle = playback::toggle,
                onNext = playback::next,
                onPrevious = playback::previous,
                onSeek = playback::seekTo,
                onRefresh = ::scan
            )
        }
    }

    override fun onDestroy() {
        playback.release()
        super.onDestroy()
    }
}

@Composable
private fun TakuTuneApp(
    tracks: List<Track>,
    playback: PlaybackState,
    onPlay: (Track, Int) -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onRefresh: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var playerOpen by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Purple,
            background = Background,
            surface = SurfaceColor
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Background) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (playerOpen && playback.current != null) {
                        FullPlayer(playback, onToggle, onNext, onPrevious, onSeek) { playerOpen = false }
                    } else {
                        when (tab) {
                            0 -> Home(tracks, playback, onPlay, { tab = 1 }, onRefresh)
                            1 -> Search(tracks, onPlay)
                            2 -> Library(tracks, onPlay)
                            else -> Settings()
                        }
                    }
                }
                if (!playerOpen) {
                    if (playback.current != null) {
                        MiniPlayer(playback) { playerOpen = true }
                    }
                    BottomBar(tab) { tab = it }
                }
            }
        }
    }
}

@Composable
private fun Home(tracks: List<Track>, playback: PlaybackState, onPlay: (Track, Int) -> Unit, search: () -> Unit, refresh: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("TakuTune", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Твоя музыка. Твой интерфейс.", color = SecondaryText)
            Spacer(Modifier.height(14.dp))
            SearchPill(search)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCard("Избранное", Icons.Default.FavoriteBorder, Modifier.weight(1f))
                ActionCard("Плейлисты", Icons.Default.QueueMusic, Modifier.weight(1f))
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Локальная музыка", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(refresh) { Text("Сканировать") }
            }
        }
        if (tracks.isEmpty()) {
            item { EmptyCard("Музыка не найдена", "Разреши доступ к аудио и запусти сканирование.") }
        } else {
            items(tracks.take(100), key = { it.id }) { track ->
                TrackRow(track, tracks.indexOfFirst { it.id == track.id }) { onPlay(track, tracks.indexOf(track)) }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun Search(tracks: List<Track>, onPlay: (Track, Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    val result = remember(query, tracks) { tracks.filter { q -> query.isBlank() || q.title.contains(query, true) || q.artist.contains(query, true) || q.album.contains(query, true) } }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Поиск", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Песня, исполнитель, альбом") }, leadingIcon = { Icon(Icons.Default.Search, null) })
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(result.take(100), key = { it.id }) { track -> TrackRow(track, result.indexOf(track)) { onPlay(track, tracks.indexOf(track)) } }
        }
    }
}

@Composable
private fun Library(tracks: List<Track>, onPlay: (Track, Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Медиатека", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("${tracks.size} треков", color = SecondaryText)
            Spacer(Modifier.height(12.dp))
        }
        item { ActionCard("Избранное", Icons.Default.FavoriteBorder, Modifier.fillMaxWidth()) }
        item { ActionCard("Плейлисты", Icons.Default.QueueMusic, Modifier.fillMaxWidth()) }
        item { ActionCard("История", Icons.Default.History, Modifier.fillMaxWidth()) }
        items(tracks.take(100), key = { it.id }) { track -> TrackRow(track, tracks.indexOf(track)) { onPlay(track, tracks.indexOf(track)) } }
    }
}

@Composable
private fun Settings() {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Настройки", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("TakuTune строится вокруг глубокой кастомизации.", color = SecondaryText)
            Spacer(Modifier.height(12.dp))
        }
        item { SettingCard("Оформление", "Темы, AMOLED, акцент, фон, прозрачность и анимации", Icons.Default.Palette) }
        item { SettingCard("Плеер", "Очередь, повтор, перемешивание, жесты и таймер", Icons.Default.Tune) }
        item { SettingCard("Аудио", "Эквалайзер, усиление баса и crossfade", Icons.Default.Equalizer) }
        item { SettingCard("Источники", "Local / YouTube / Spotify / VK", Icons.Default.Cloud) }
        item { SettingCard("Данные", "Резервная копия, история и медиатека", Icons.Default.Storage) }
    }
}

@Composable
private fun FullPlayer(state: PlaybackState, toggle: () -> Unit, next: () -> Unit, previous: () -> Unit, seek: (Long) -> Unit, back: () -> Unit) {
    val track = state.current ?: return
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.Default.KeyboardArrowDown, "Назад", tint = Color.White) }
            Text("Сейчас играет", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(32.dp)).background(Elevated), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MusicNote, null, tint = Purple, modifier = Modifier.size(100.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(track.title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text(track.artist, color = SecondaryText)
        Spacer(Modifier.height(18.dp))
        Slider(value = if (state.durationMs > 0) state.positionMs.toFloat().coerceIn(0f, state.durationMs.toFloat()) else 0f, onValueChange = { seek(it.toLong()) }, valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            IconButton(previous) { Icon(Icons.Default.SkipPrevious, "Предыдущий", tint = Color.White, modifier = Modifier.size(34.dp)) }
            IconButton(toggle) {
                Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Воспроизведение", tint = Purple, modifier = Modifier.size(56.dp))
            }
            IconButton(next) { Icon(Icons.Default.SkipNext, "Следующий", tint = Color.White, modifier = Modifier.size(34.dp)) }
        }
    }
}

@Composable
private fun MiniPlayer(state: PlaybackState, open: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(10.dp).clip(RoundedCornerShape(18.dp)).background(Elevated).clickable(open).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.MusicNote, null, tint = Purple, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(state.current?.title.orEmpty(), color = Color.White, maxLines = 1)
            Text(state.current?.artist.orEmpty(), color = SecondaryText, fontSize = 12.sp, maxLines = 1)
        }
        Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Purple)
    }
}

@Composable
private fun TrackRow(track: Track, index: Int, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceColor).clickable(onClick = onClick).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(Elevated), contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = Purple) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(track.artist, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
        }
        Icon(Icons.Default.PlayArrow, null, tint = Purple)
    }
}

@Composable
private fun ActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Row(modifier.clip(RoundedCornerShape(18.dp)).background(SurfaceColor).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Purple)
        Spacer(Modifier.width(10.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceColor).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Purple, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(14.dp))
        Column { Text(title, color = Color.White, fontWeight = FontWeight.SemiBold); Text(subtitle, color = SecondaryText, fontSize = 12.sp) }
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SurfaceColor).padding(20.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        Text(subtitle, color = SecondaryText, fontSize = 13.sp)
    }
}

@Composable
private fun SearchPill(onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceColor).clickable(onClick).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Search, null, tint = Purple)
        Spacer(Modifier.width(10.dp))
        Text("Песня, исполнитель, альбом", color = SecondaryText)
    }
}

@Composable
private fun BottomBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(Icons.Default.Home to "Главная", Icons.Default.Search to "Поиск", Icons.Default.LibraryMusic to "Медиатека", Icons.Default.Settings to "Настройки")
    Row(Modifier.fillMaxWidth().background(Color(0xFF0D0A12)).navigationBarsPadding().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        items.forEachIndexed { index, item ->
            Column(Modifier.clickable { onSelect(index) }.padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(item.first, item.second, tint = if (selected == index) Purple else SecondaryText, modifier = Modifier.size(22.dp))
                Text(item.second, color = if (selected == index) Purple else SecondaryText, fontSize = 10.sp)
            }
        }
    }
}
