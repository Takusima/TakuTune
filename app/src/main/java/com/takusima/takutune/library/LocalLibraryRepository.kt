package com.takusima.takutune.library

import com.takusima.takutune.core.database.FavoriteEntity
import com.takusima.takutune.core.database.HistoryEntity
import com.takusima.takutune.core.database.PlaylistEntity
import com.takusima.takutune.core.database.PlaylistTrackEntity
import com.takusima.takutune.core.database.TakuTuneDao
import com.takusima.takutune.core.database.TrackEntity
import com.takusima.takutune.core.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalLibraryRepository(private val dao: TakuTuneDao) {
    fun observeTracks(): Flow<List<Track>> = dao.observeTracks().map { it.map(::toTrack) }

    fun observeFavoriteIds(): Flow<List<Long>> = dao.observeFavorites()
    fun observeBlockedIds(): Flow<List<Long>> = dao.observeBlocked()
    fun observeHistory(): Flow<List<Long>> = dao.observeHistory().map { it.map(HistoryEntity::trackId) }
    fun observePlaylists(): Flow<List<PlaylistEntity>> = dao.observePlaylists()

    suspend fun replaceTracks(tracks: List<Track>) {
        dao.clearTracks()
        if (tracks.isNotEmpty()) dao.upsertTracks(tracks.map(::toEntity))
    }

    suspend fun toggleFavorite(track: Track, currentlyFavorite: Boolean) {
        if (currentlyFavorite) dao.removeFavorite(track.id)
        else dao.addFavorite(FavoriteEntity(track.id))
    }

    suspend fun toggleBlocked(track: Track, currentlyBlocked: Boolean) {
        if (currentlyBlocked) dao.unblockTrack(track.id)
        else dao.blockTrack(com.takusima.takutune.core.database.BlockedTrackEntity(track.id))
    }

    suspend fun addHistory(track: Track) = dao.addHistory(
        HistoryEntity(trackId = track.id, playedAt = System.currentTimeMillis())
    )

    suspend fun createPlaylist(name: String): Long = dao.createPlaylist(PlaylistEntity(name = name))

    suspend fun addToPlaylist(playlistId: Long, trackId: Long) =
        dao.addToPlaylist(PlaylistTrackEntity(playlistId, trackId))

    private fun toEntity(t: Track) = TrackEntity(t.id, t.uri, t.title, t.artist, t.album, t.durationMs)
    private fun toTrack(t: TrackEntity) = Track(t.id, t.uri, t.title, t.artist, t.album, t.durationMs)
}