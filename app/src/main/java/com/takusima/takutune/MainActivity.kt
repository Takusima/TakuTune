package com.takusima.takutune

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
        Surface(Modifier.fillMaxSize(), color = Background) { TakuTuneScreen() }
    }
}

@Composable
private fun TakuTuneScreen() {
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var playerUrl by remember { mutableStateOf<String?>(null) }

    if (playerUrl != null) {
        YouTubeMusicPlayer(playerUrl!!, onBack = { playerUrl = null })
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            when (tab) {
                0 -> HomeScreen(
                    onSearch = { tab = 1 },
                    onOpenMusic = { playerUrl = "https://music.youtube.com/" }
                )
                1 -> SearchScreen(
                    query = query,
                    onQueryChange = { query = it },
                    onPlaySearch = {
                        if (query.isNotBlank()) {
                            val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
                            playerUrl = "https://music.youtube.com/search?q=$encoded"
                        }
                    }
                )
                2 -> LibraryScreen()
                else -> SettingsScreen()
            }
        }
        BottomBar(tab) { tab = it }
    }
}

@Composable
private fun HomeScreen(onSearch: () -> Unit, onOpenMusic: () -> Unit) {
    Column {
        Text("TakuTune", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Музыка из YouTube Music", color = Color(0xFFB8AFC0), fontSize = 15.sp)
        Spacer(Modifier.height(20.dp))

        Box(
            Modifier.fillMaxWidth().height(190.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(PurpleDark, Color(0xFF321B50), Color(0xFF120C19))))
                .clickable { onOpenMusic() }
                .padding(24.dp)
        ) {
            Column {
                Text("YOUTUBE MUSIC", color = Color(0xFFDCCBFF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text("Слушать музыку", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Настоящее воспроизведение через YouTube Music", color = Color(0xFFE5DDF0), fontSize = 14.sp)
                Spacer(Modifier.height(15.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Purple), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Открыть плеер", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Быстрый поиск", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceColor)
                .clickable { onSearch() }.padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Purple)
            Spacer(Modifier.width(12.dp))
            Text("Найти песню, исполнителя или альбом", color = Color(0xFFAAA1B3), fontSize = 14.sp)
        }

        Spacer(Modifier.height(24.dp))
        Text("Каталог и воспроизведение берутся из YouTube Music.", color = Color(0xFF756C7D), fontSize = 12.sp)
    }
}

@Composable
private fun SearchScreen(query: String, onQueryChange: (String) -> Unit, onPlaySearch: () -> Unit) {
    Column {
        Text("Поиск", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("YouTube Music", color = Color(0xFFB8AFC0), fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Например: After Dark") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                IconButton(onClick = onPlaySearch) {
                    Icon(Icons.Default.PlayArrow, "Search", tint = Purple)
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = SurfaceColor,
                focusedBorderColor = Purple,
                unfocusedBorderColor = Color.Transparent
            )
        )
        Spacer(Modifier.height(18.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceColor)
                .clickable { onPlaySearch() }.padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, null, tint = Purple)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Искать и открыть", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Результаты откроются внутри TakuTune", color = Color(0xFFAAA1B3), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen() {
    Column {
        Text("Медиатека", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("Избранное и история — следующий этап", color = Color(0xFFB8AFC0), fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        LibraryCard("♥", "Любимые", "Сохранённые треки")
        Spacer(Modifier.height(10.dp))
        LibraryCard("◷", "История", "Недавно прослушанное")
        Spacer(Modifier.height(10.dp))
        LibraryCard("♫", "Плейлисты", "Твои подборки")
    }
}

@Composable
private fun SettingsScreen() {
    Column {
        Text("Настройки", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text("TakuTune", color = Color(0xFFB8AFC0), fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))
        LibraryCard("🎨", "Оформление", "Фиолетовая тема")
        Spacer(Modifier.height(10.dp))
        LibraryCard("▶", "Воспроизведение", "Источник: YouTube Music")
        Spacer(Modifier.height(10.dp))
        LibraryCard("ℹ", "О приложении", "TakuTune")
    }
}

@Composable
private fun LibraryCard(icon: String, title: String, subtitle: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceColor).padding(16.dp),
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun YouTubeMusicPlayer(url: String, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Color(0xFF100B16)).navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text("TakuTune Player", color = Color.White, fontWeight = FontWeight.Bold)
                Text("YouTube Music", color = Color(0xFFAAA1B3), fontSize = 11.sp)
            }
        }

        AndroidView(
            Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.userAgentString = settings.userAgentString + " TakuTune/1.0"
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = false
                        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                        }
                    }
                    loadUrl(url)
                }
            },
            update = { webView -> if (webView.url != url) webView.loadUrl(url) }
        )
    }
}

@Composable
private fun BottomBar(selected: Int, onSelected: (Int) -> Unit) {
    val items = listOf(
        Icons.Default.Home to "Главная",
        Icons.Default.Search to "Поиск",
        Icons.Default.LibraryMusic to "Медиатека",
        Icons.Default.Settings to "Настройки"
    )
    Row(
        Modifier.fillMaxWidth().background(Color(0xFF100B16)).navigationBarsPadding().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, item ->
            val active = selected == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onSelected(index) }.padding(horizontal = 12.dp)
            ) {
                Icon(item.first, item.second, tint = if (active) Purple else Color(0xFF77707D), modifier = Modifier.size(23.dp))
                Text(item.second, color = if (active) Purple else Color(0xFF77707D), fontSize = 11.sp)
            }
        }
    }
}
