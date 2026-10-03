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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takusima.takutune.core.database.TakuTuneDatabase
import com.takusima.takutune.core.model.Track
import com.takusima.takutune.core.presentation.TakuTuneViewModel
import com.takusima.takutune.core.preferences.AppearanceSettings
import com.takusima.takutune.core.preferences.SettingsStore
import com.takusima.takutune.playback.PlaybackController
import com.takusima.takutune.playback.PlaybackState
import com.takusima.takutune.theme.TakuTuneTheme

private val SurfaceColor = Color(0xFF15101C)
private val Elevated = Color(0xFF21182C)
private val Purple = Color(0xFFB36BFF)
private val SecondaryText = Color(0xFFAAA0B4)

class MainActivity : ComponentActivity() {

    private var activeViewModel: TakuTuneViewModel? = null

    private val permission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) activeViewModel?.scan()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = TakuTuneDatabase.get(this)
        val settings = SettingsStore(this)
        val playback = PlaybackController(this)
        val factory = TakuTuneViewModel.Factory(
            contentResolver = contentResolver,
            database = database,
            settings = settings,
            playbackController = playback
        )

        setContent {
            val viewModel: TakuTuneViewModel = viewModel(factory = factory)
            activeViewModel = viewModel

            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            TakuTuneTheme(uiState.appearance) {
                TakuTuneApp(
                    tracks = uiState.tracks,
                    playback = uiState.playback,
                    appearance = uiState.appearance,
                    favoriteIds = uiState.favoriteIds,
                    onPlay = { track, _ -> viewModel.play(track) },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onToggle = viewModel::togglePlayback,
                    onNext = viewModel::next,
                    onPrevious = viewModel::previous,
                    onSeek = viewModel::seekTo,
                    onShuffle = { viewModel.setShuffle(!uiState.playback.shuffle) },
                    onRepeat = {
                        viewModel.setRepeat(
                            if (uiState.playback.repeatMode == 0) {
                                androidx.media3.common.Player.REPEAT_MODE_ALL
                            } else {
                                androidx.media3.common.Player.REPEAT_MODE_OFF
                            }
                        )
                    },
                    onTheme = viewModel::setTheme,
                    onAmoled = viewModel::setAmoled,
                    onDynamic = viewModel::setDynamicColor,
                    onRefresh = viewModel::scan
                )
            }
        }

        if (Build.VERSION.SDK_INT >= 33) {
            permission.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    override fun onDestroy() {
        activeViewModel = null
        super.onDestroy()
    }
}

@Composable
private fun TakuTuneApp(
    tracks: List<Track>,
    playback: PlaybackState,
    appearance: AppearanceSettings,
    favoriteIds: Set<Long>,
    onPlay: (Track, Int) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onTheme: (String) -> Unit,
    onAmoled: (Boolean) -> Unit,
    onDynamic: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var playerOpen by remember { mutableStateOf(false) }
    MaterialTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (playerOpen && playback.current != null) {
                        FullPlayer(playback, favoriteIds, onToggleFavorite, onToggle, onNext, onPrevious, onSeek, onShuffle, onRepeat) { playerOpen = false }
                    } else {
                        when (tab) {
                            0 -> Home(tracks, playback, favoriteIds, onPlay, onToggleFavorite, { tab = 1 }, onRefresh)
                            1 -> Search(tracks, favoriteIds, onPlay, onToggleFavorite)
                            2 -> Library(tracks, favoriteIds, onPlay, onToggleFavorite)
                            else -> SettingsScreen(appearance, onTheme, onAmoled, onDynamic)
                        }
                    }
                }
                if (!playerOpen) {
                    playback.current?.let { MiniPlayer(playback) { playerOpen = true } }
                    BottomBar(tab) { tab = it }
                }
            }
        }
    }
}

@Composable
private fun Home(tracks: List<Track>, playback: PlaybackState, favoriteIds: Set<Long>, onPlay: (Track, Int) -> Unit, onFavorite: (Track) -> Unit, search: () -> Unit, refresh: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("TakuTune", color = MaterialTheme.colorScheme.onBackground, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Твоя музыка. Твой интерфейс.", color = SecondaryText)
            Spacer(Modifier.height(14.dp))
            SearchPill(search)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionCard("Избранное", Icons.Default.Favorite, Modifier.weight(1f))
                ActionCard("Очередь", Icons.Default.QueueMusic, Modifier.weight(1f))
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Локальная музыка", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                TextButton(refresh) { Text("Сканировать") }
            }
        }
        if (tracks.isEmpty()) item { EmptyCard("Музыка не найдена", "Разреши доступ к аудио и запусти сканирование.") }
        else items(tracks.take(100), key = { it.id }) { track ->
            TrackRow(track, track.id in favoriteIds, { onPlay(track, tracks.indexOf(track)) }, { onFavorite(track) })
        }
    }
}

@Composable
private fun Search(tracks: List<Track>, favoriteIds: Set<Long>, onPlay: (Track, Int) -> Unit, onFavorite: (Track) -> Unit) {
    var query by remember { mutableStateOf("") }
    val result = remember(query, tracks) { tracks.filter { query.isBlank() || it.title.contains(query, true) || it.artist.contains(query, true) || it.album.contains(query, true) } }
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Поиск", color = MaterialTheme.colorScheme.onBackground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Песня, исполнитель, альбом") }, leadingIcon = { Icon(Icons.Default.Search, null) })
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(result.take(100), key = { it.id }) { track ->
                TrackRow(track, track.id in favoriteIds, { onPlay(track, tracks.indexOf(track)) }, { onFavorite(track) })
            }
        }
    }
}

@Composable
private fun Library(tracks: List<Track>, favoriteIds: Set<Long>, onPlay: (Track, Int) -> Unit, onFavorite: (Track) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Медиатека", color = MaterialTheme.colorScheme.onBackground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("${tracks.size} треков • ${favoriteIds.size} избранных", color = SecondaryText)
            Spacer(Modifier.height(12.dp))
        }
        items(tracks.take(200), key = { it.id }) { track ->
            TrackRow(track, track.id in favoriteIds, { onPlay(track, tracks.indexOf(track)) }, { onFavorite(track) })
        }
    }
}

@Composable
private fun SettingsScreen(settings: AppearanceSettings, onTheme: (String) -> Unit, onAmoled: (Boolean) -> Unit, onDynamic: (Boolean) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Spacer(Modifier.height(24.dp))
            Text("Настройки", color = MaterialTheme.colorScheme.onBackground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Глубокая настройка TakuTune.", color = SecondaryText)
        }
        item {
            SettingCard("Тема", "System / Dark / Light", Icons.Default.Palette)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "System", "dark" to "Dark", "light" to "Light").forEach { (value, label) ->
                    FilterChip(selected = settings.theme == value, onClick = { onTheme(value) }, label = { Text(label) })
                }
            }
        }
        item {
            SettingToggle("AMOLED", "Полностью чёрный фон", settings.amoled, onAmoled, Icons.Default.Brightness4)
        }
        item {
            SettingToggle("Dynamic color", "Поддержка системного динамического цвета", settings.dynamicColor, onDynamic, Icons.Default.ColorLens)
        }
        item { SettingCard("Плеер", "Очередь, повтор, перемешивание, жесты и таймер", Icons.Default.Tune) }
        item { SettingCard("Аудио", "Эквалайзер, усиление баса и crossfade", Icons.Default.Equalizer) }
        item { SettingCard("Источники", "Local / YouTube / Spotify / VK через единую архитектуру", Icons.Default.Cloud) }
        item { SettingCard("Данные", "Room, история, избранное, плейлисты и резервное копирование", Icons.Default.Storage) }
    }
}

@Composable
private fun FullPlayer(state: PlaybackState, favorites: Set<Long>, favorite: (Track) -> Unit, toggle: () -> Unit, next: () -> Unit, previous: () -> Unit, seek: (Long) -> Unit, shuffle: () -> Unit, repeat: () -> Unit, back: () -> Unit) {
    val track = state.current ?: return
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(back) { Icon(Icons.Default.KeyboardArrowDown, "Назад") }
            Text("Сейчас играет", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton({ favorite(track) }) { Icon(if (track.id in favorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Избранное", tint = Purple) }
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(330.dp).clip(RoundedCornerShape(32.dp)).background(Elevated), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MusicNote, null, tint = Purple, modifier = Modifier.size(100.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(track.title, color = MaterialTheme.colorScheme.onBackground, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text(track.artist, color = SecondaryText)
        Spacer(Modifier.height(14.dp))
        Slider(value = if (state.durationMs > 0) state.positionMs.toFloat().coerceIn(0f, state.durationMs.toFloat()) else 0f, onValueChange = { seek(it.toLong()) }, valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            IconButton(shuffle) { Icon(Icons.Default.Shuffle, "Перемешать", tint = if (state.shuffle) Purple else SecondaryText) }
            IconButton(previous) { Icon(Icons.Default.SkipPrevious, "Предыдущий", modifier = Modifier.size(36.dp)) }
            FilledIconButton(toggle, modifier = Modifier.size(68.dp)) { Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Воспроизведение", modifier = Modifier.size(36.dp)) }
            IconButton(next) { Icon(Icons.Default.SkipNext, "Следующий", modifier = Modifier.size(36.dp)) }
            IconButton(repeat) { Icon(Icons.Default.Repeat, "Повтор", tint = if (state.repeatMode != 0) Purple else SecondaryText) }
        }
    }
}

@Composable
private fun MiniPlayer(state: PlaybackState, open: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(8.dp).clip(RoundedCornerShape(18.dp)).background(Elevated).clickable(open).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.MusicNote, null, tint = Purple, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(state.current?.title.orEmpty(), color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
            Text(state.current?.artist.orEmpty(), color = SecondaryText, fontSize = 12.sp, maxLines = 1)
        }
        Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = Purple)
    }
}

@Composable
private fun TrackRow(track: Track, favorite: Boolean, onPlay: () -> Unit, onFavorite: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceColor).padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(13.dp)).background(Elevated), contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = Purple) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).clickable(onClick = onPlay)) {
            Text(track.title, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(track.artist, color = SecondaryText, fontSize = 12.sp, maxLines = 1)
        }
        IconButton(onFavorite) { Icon(if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = if (favorite) Purple else SecondaryText) }
        IconButton(onPlay) { Icon(Icons.Default.PlayArrow, "Воспроизвести", tint = Purple) }
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceColor).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Purple)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) { Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold); Text(subtitle, color = SecondaryText, fontSize = 12.sp) }
        Switch(checked, onCheckedChange = onChecked)
    }
}

@Composable
private fun SettingCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceColor).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Purple, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(14.dp))
        Column { Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold); Text(subtitle, color = SecondaryText, fontSize = 12.sp) }
    }
}

@Composable
private fun ActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Row(modifier.clip(RoundedCornerShape(18.dp)).background(SurfaceColor).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Purple)
        Spacer(Modifier.width(10.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EmptyCard(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(SurfaceColor).padding(20.dp)) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
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