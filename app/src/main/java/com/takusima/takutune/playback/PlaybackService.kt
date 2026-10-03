package com.takusima.takutune.playback

import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

@UnstableApi
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, ExoPlayer.Builder(this).build()).build()
    }
    override fun onDestroy() {
        mediaSession?.player?.release(); mediaSession?.release(); mediaSession=null
        super.onDestroy()
    }
}
