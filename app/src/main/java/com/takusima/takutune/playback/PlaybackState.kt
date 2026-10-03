package com.takusima.takutune.playback

import com.takusima.takutune.core.model.Track

data class PlaybackState(
    val current: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val queue: List<Track> = emptyList(),
    val index: Int = -1
)
