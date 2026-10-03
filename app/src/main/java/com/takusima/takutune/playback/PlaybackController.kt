package com.takusima.takutune.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.takusima.takutune.core.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackController(context: Context) {
    private val appContext = context.applicationContext
    private val future: ListenableFuture<MediaController>
    private var controller: MediaController? = null
    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish(player)
    }

    init {
        val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
        future = MediaController.Builder(appContext, token).buildAsync()
        future.addListener({
            controller = runCatching { future.get() }.getOrNull()?.also {
                it.addListener(listener)
                publish(it)
            }
        }, appContext.mainExecutor)
    }

    private fun track(item: MediaItem, duration: Long) = Track(
        id = item.mediaId.hashCode().toLong(),
        uri = item.localConfiguration?.uri?.toString().orEmpty(),
        title = item.mediaMetadata.title?.toString().orEmpty(),
        artist = item.mediaMetadata.artist?.toString().orEmpty(),
        album = item.mediaMetadata.albumTitle?.toString().orEmpty(),
        durationMs = duration.coerceAtLeast(0)
    )

    private fun publish(player: Player) {
        val current = player.currentMediaItem?.let { track(it, player.duration) }
        val queue = (0 until player.mediaItemCount).map { track(player.getMediaItemAt(it), 0) }
        _state.value = PlaybackState(
            current = current,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.coerceAtLeast(0),
            queue = queue,
            index = player.currentMediaItemIndex,
            shuffle = player.shuffleModeEnabled,
            repeatMode = player.repeatMode
        )
    }

    fun play(track: Track) = playQueue(listOf(track))

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        controller?.apply {
            setMediaItems(
                tracks.map {
                    MediaItem.Builder()
                        .setMediaId(it.id.toString())
                        .setUri(it.uri)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(it.title)
                                .setArtist(it.artist)
                                .setAlbumTitle(it.album)
                                .build()
                        )
                        .build()
                },
                startIndex.coerceIn(0, tracks.lastIndex),
                0
            )
            prepare()
            play()
        }
    }

    fun toggle() { controller?.let { if (it.isPlaying) it.pause() else it.play() } }
    fun next() { controller?.seekToNextMediaItem() }
    fun previous() { controller?.seekToPreviousMediaItem() }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    fun setShuffle(enabled: Boolean) { controller?.shuffleModeEnabled = enabled }
    fun setRepeat(mode: Int) { controller?.repeatMode = mode }

    fun release() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }
}