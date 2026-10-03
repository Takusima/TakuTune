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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0A0710)
private val Surface = Color(0xFF15101C)
private val SurfaceElevated = Color(0xFF1D1626)
private val Purple = Color(0xFFB36BFF)
private val PurpleDark = Color(0xFF6F36A8)
private val TextSecondary = Color(0xFFAAA0B4)

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
            surface = Surface
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Background) {
            TakuTuneShell()
        }
    }
}

@Composable
private fun TakuTuneShell() {
    var tab by remember { mutableIntStateOf(0) }
    var selectedTrack by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                selectedTrack != null -> PlayerScreen(
                    title = selectedTrack!!,
                    onBack = { selectedTrack = null }
                )
                tab == 0 -> HomeScreen(onPlay = { selectedTrack = it }, onSearch = { tab = 1 })
                tab == 1 -> SearchScreen(onPlay = { selectedTrack = it })
                tab == 2 -> LibraryScreen(onPlay = { selectedTrack = it })
                else -> SettingsScreen()
            }
        }

        if (selectedTrack == null) {
            MiniPlayer(
                title = "Ничего не играет",
                onClick = { },
                enabled = false
            )
            BottomBar(tab = tab, onTab = { tab = it })
        }
    }
}

@Composable
private fun HomeScreen(onPlay: (String) -> Unit, onSearch: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(Modifier.height(22.dp))
            Text("TakuTune", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("Твоя музыка. Твой интерфейс.", color = TextSecondary, fontSize = 14.sp)
        }

        item {
            SearchPill(onClick = onSearch)
        }

        item {
            Box(
                Modifier.fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PurpleDark, Color(0xFF3A1C55), Color(0xFF171020))
                        )
                    )
                    .clickable { onPlay("TakuTune demo") }
                    .padding(22.dp)
            ) {
                Column {
                    Text("NOW PLAYING", color = Color(0xFFE4D2FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Музыка без лишнего", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Нативный интерфейс на Jetpack Compose. Плеер и источники подключим следующим этапом.",
                        color = Color(0xFFE1D7E9),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(Purple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Открыть плеер", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            SectionTitle("Быстрый доступ")
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickCard(Icons.Default.FavoriteBorder, "Любимые", Modifier.weight(1f))
                QuickCard(Icons.Default.QueueMusic, "Плейлисты", Modifier.weight(1f))
            }
        }

        item {
            SectionTitle("Недавнее")
        }

        items(listOf("TakuTune demo", "Музыка появится после сканирования")) { title ->
            TrackRow(title = title, subtitle = "Локальная медиатека", onPlay = { onPlay(title) })
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun SearchScreen(onPlay: (String) -> Unit) {
    var query by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(22.dp))
        Text("Поиск", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Единый поиск по подключённым источникам", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(18.dp))

        SearchPill(
            query = query,
            onQueryChange = { query = it },
            onClick = { if (query.isNotBlank()) onPlay(query.trim()) }
        )

        Spacer(Modifier.height(18.dp))
        Text("Источники", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(10.dp))
        SourceRow("Локальная музыка", "На устройстве", true)
        SourceRow("YouTube", "Подключим отдельно", false)
        SourceRow("Spotify", "Подключим через официальный API", false)
        SourceRow("VK", "Провайдер добавим после проверки API", false)
    }
}

@Composable
private fun LibraryScreen(onPlay: (String) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(22.dp))
            Text("Медиатека", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Музыка, история и плейлисты в одном месте", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
        }
        item { LibraryCard(Icons.Default.FavoriteBorder, "Любимые", "0 треков") }
        item { LibraryCard(Icons.Default.Album, "Альбомы", "0 альбомов") }
        item { LibraryCard(Icons.Default.MusicNote, "Исполнители", "0 исполнителей") }
        item { LibraryCard(Icons.Default.QueueMusic, "Плейлисты", "0 плейлистов") }
        item { LibraryCard(Icons.Default.LibraryMusic, "История", "0 прослушиваний") }
        item { TrackRow("Тестовый трек", "Нажми для открытия плеера", onPlay = { onPlay("Тестовый трек") }) }
    }
}

@Composable
private fun SettingsScreen() {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(22.dp))
            Text("Настройки", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Глубокая настройка интерфейса и воспроизведения", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
        }
        item { SettingsCard(Icons.Default.Palette, "Оформление", "Тема, акцент, фон, прозрачность и анимации") }
        item { SettingsCard(Icons.Default.Tune, "Воспроизведение", "Очередь, повтор, перемешивание, скорость и таймер") }
        item { SettingsCard(Icons.Default.MusicNote, "Источники", "Локальная музыка, YouTube, Spotify и VK") }
        item { SettingsCard(Icons.Default.LibraryMusic, "Медиатека", "Сканирование, избранное, история и плейлисты") }
        item { SettingsCard(Icons.Default.Settings, "Система", "Уведомления, Bluetooth, медиа-кнопки и резервная копия") }
    }
}

@Composable
private fun PlayerScreen(title: String, onBack: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Text("‹", color = Color.White, fontSize = 34.sp)
            }
            Text("Плеер", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(22.dp))

        Box(
            Modifier.fillMaxWidth().height(330.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(listOf(PurpleDark, Color(0xFF24132F), Surface))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MusicNote, null, tint = Color(0xFFE7D5FF), modifier = Modifier.size(92.dp))
        }

        Spacer(Modifier.height(20.dp))
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("TakuTune", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))

        Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(SurfaceElevated))
        Spacer(Modifier.height(18.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {}) { Text("↶", color = Color.White, fontSize = 25.sp) }
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(Purple),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(34.dp))
            }
            IconButton(onClick = {}) { Text("↷", color = Color.White, fontSize = 25.sp) }
        }
    }
}

@Composable
private fun MiniPlayer(title: String, onClick: () -> Unit, enabled: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceElevated)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(PurpleDark),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.MusicNote, null, tint = Color.White) }
        Spacer(Modifier.width(10.dp))
        Text(title, color = if (enabled) Color.White else TextSecondary, modifier = Modifier.weight(1f), fontSize = 13.sp)
        Icon(Icons.Default.PlayArrow, null, tint = if (enabled) Purple else TextSecondary)
    }
}

@Composable
private fun BottomBar(tab: Int, onTab: (Int) -> Unit) {
    val items = listOf(
        Icons.Default.Home to "Главная",
        Icons.Default.Search to "Поиск",
        Icons.Default.LibraryMusic to "Медиатека",
        Icons.Default.Settings to "Настройки"
    )
    Row(
        Modifier.fillMaxWidth().background(Color(0xFF0E0A14)).navigationBarsPadding().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, item ->
            val active = tab == index
            Column(
                Modifier.clickable { onTab(index) }.padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(item.first, item.second, tint = if (active) Purple else Color(0xFF706876), modifier = Modifier.size(22.dp))
                Text(item.second, color = if (active) Purple else Color(0xFF706876), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun SearchPill(
    query: String = "",
    onQueryChange: ((String) -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface)
            .clickable(enabled = onQueryChange == null, onClick = onClick).padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, null, tint = Purple)
        Spacer(Modifier.width(10.dp))
        if (onQueryChange != null) {
            androidx.compose.material3.OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Песня, исполнитель, альбом", color = TextSecondary) }
            )
            IconButton(onClick = onClick) {
                Icon(Icons.Default.PlayArrow, null, tint = Purple)
            }
        } else {
            Text("Песня, исполнитель, альбом", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun QuickCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Surface).padding(15.dp)
    ) {
        Icon(icon, null, tint = Purple, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TrackRow(title: String, subtitle: String, onPlay: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface)
            .clickable(onClick = onPlay).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(SurfaceElevated), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.MusicNote, null, tint = Purple)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.PlayArrow, null, tint = Purple)
    }
}

@Composable
private fun LibraryCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(PurpleDark), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White)
        }
        Spacer(Modifier.width(13.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingsCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Purple, modifier = Modifier.size(25.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SourceRow(title: String, subtitle: String, enabled: Boolean) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(if (enabled) Purple else Color(0xFF5C5662)))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
    }
}
