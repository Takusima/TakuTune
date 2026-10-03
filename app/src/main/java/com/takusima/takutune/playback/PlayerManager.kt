package com.takusima.takutune.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer

class PlayerManager(context: Context) {
    val player = ExoPlayer.Builder(context.applicationContext).build()
    fun play(uri: String, title: String, artist: String) {
        player.setMediaItem(MediaItem.Builder().setUri(uri).setMediaMetadata(MediaMetadata.Builder().setTitle(title).setArtist(artist).build()).build())
        player.prepare(); player.play()
    }
    fun release() = player.release()
}
