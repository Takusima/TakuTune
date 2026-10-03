package com.takusima.takutune.sources

import com.takusima.takutune.core.model.*

class LocalSource(private val tracksProvider: suspend () -> List<Track>) : MusicSource {
    override val id = "local"
    override val displayName = "Local"
    override suspend fun search(query: String) = tracksProvider().filter { query.isBlank() || it.title.contains(query, true) || it.artist.contains(query, true) || it.album.contains(query, true) }
    override suspend fun getTrack(id: String) = tracksProvider().firstOrNull { it.id.toString() == id }
    override suspend fun getAlbum(id: String): Album? = null
    override suspend fun getArtist(id: String): Artist? = null
    override suspend fun getPlaylist(id: String): Playlist? = null
    override suspend fun resolvePlayback(track: Track) = PlaybackSource(track.uri, id)
    override suspend fun authenticate() = true
}