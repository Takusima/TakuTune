package com.takusima.takutune.sources

import com.takusima.takutune.core.model.*
interface MusicSource {
    val id: String
    val displayName: String
    suspend fun search(query: String): List<Track>
    suspend fun getTrack(id: String): Track?
    suspend fun getAlbum(id: String): Album?
    suspend fun getArtist(id: String): Artist?
    suspend fun getPlaylist(id: String): Playlist?
    suspend fun resolvePlayback(track: Track): PlaybackSource?
    suspend fun authenticate(): Boolean
}