package com.takusima.takutune.core.presentation

import android.content.ContentResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.takusima.takutune.core.database.TakuTuneDatabase
import com.takusima.takutune.core.model.Track
import com.takusima.takutune.core.preferences.AppearanceSettings
import com.takusima.takutune.core.preferences.SettingsStore
import com.takusima.takutune.library.LocalLibraryRepository
import com.takusima.takutune.library.LocalMusicScanner
import com.takusima.takutune.playback.PlaybackController
import com.takusima.takutune.playback.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Presentation state owner for TakuTune. @author Takusima */
data class TakuTuneUiState(
    val tracks: List<Track> = emptyList(),
    val favoriteIds: Set<Long> = emptySet(),
    val blockedIds: Set<Long> = emptySet(),
    val playback: PlaybackState = PlaybackState(),
    val appearance: AppearanceSettings = AppearanceSettings()
)

/** Coordinates Room, DataStore and Media3. @author Takusima */
class TakuTuneViewModel(
    private val library: LocalLibraryRepository,
    private val settings: SettingsStore,
    private val playbackController: PlaybackController,
    private val scanner: LocalMusicScanner
) : ViewModel() {

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(500)
                playbackController.refresh()
            }
        }
    }

    val uiState: StateFlow<TakuTuneUiState> =
        combine(
            library.observeTracks(),
            library.observeFavoriteIds(),
            library.observeBlockedIds(),
            playbackController.state,
            settings.appearance
        ) { tracks, favorites, blocked, playback, appearance ->
            TakuTuneUiState(
                tracks = tracks,
                favoriteIds = favorites.toSet(),
                blockedIds = blocked.toSet(),
                playback = playback,
                appearance = appearance
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            TakuTuneUiState()
        )

    fun scan() {
        viewModelScope.launch {
            val found = withContext(Dispatchers.IO) { scanner.scan() }
            withContext(Dispatchers.IO) { library.replaceTracks(found) }
        }
    }

    fun play(track: Track) {
        val tracks = uiState.value.tracks
        val index = tracks.indexOfFirst { it.id == track.id }
        if (index < 0) return
        playbackController.playQueue(tracks, index)
        viewModelScope.launch(Dispatchers.IO) { library.addHistory(track.id) }
    }

    fun toggleFavorite(track: Track) {
        val currentlyFavorite = track.id in uiState.value.favoriteIds
        viewModelScope.launch(Dispatchers.IO) {
            library.toggleFavorite(track.id, currentlyFavorite)
        }
    }

    fun toggleBlocked(track: Track) {
        val currentlyBlocked = track.id in uiState.value.blockedIds
        viewModelScope.launch(Dispatchers.IO) {
            library.toggleBlocked(track.id, currentlyBlocked)
        }
    }

    fun togglePlayback() = playbackController.toggle()
    fun next() = playbackController.next()
    fun previous() = playbackController.previous()
    fun seekTo(positionMs: Long) = playbackController.seekTo(positionMs)
    fun setShuffle(enabled: Boolean) = playbackController.setShuffle(enabled)
    fun setRepeat(mode: Int) = playbackController.setRepeat(mode)

    fun setTheme(value: String) {
        viewModelScope.launch { settings.setTheme(value) }
    }

    fun setAmoled(enabled: Boolean) {
        viewModelScope.launch { settings.setAmoled(enabled) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settings.setDynamicColor(enabled) }
    }

    fun setAccent(value: String) {
        viewModelScope.launch { settings.setAccent(value) }
    }

    fun setAnimationScale(value: Float) {
        viewModelScope.launch { settings.setAnimationScale(value) }
    }

    override fun onCleared() {
        playbackController.release()
        super.onCleared()
    }

    class Factory(
        private val contentResolver: ContentResolver,
        private val database: TakuTuneDatabase,
        private val settings: SettingsStore,
        private val playbackController: PlaybackController
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TakuTuneViewModel::class.java))
            return TakuTuneViewModel(
                library = LocalLibraryRepository(database.dao()),
                settings = settings,
                playbackController = playbackController,
                scanner = LocalMusicScanner(contentResolver)
            ) as T
        }
    }
}
