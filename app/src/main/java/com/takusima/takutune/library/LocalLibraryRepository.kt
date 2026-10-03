package com.takusima.takutune.library

import com.takusima.takutune.core.database.FavoriteEntity
import com.takusima.takutune.core.database.HistoryEntity
import com.takusima.takutune.core.database.TakuTuneDao
import com.takusima.takutune.core.database.TrackEntity
import com.takusima.takutune.core.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalLibraryRepository(private val dao: TakuTuneDao) {
    fun observeTracks(): Flow<List<Track>> = dao.observeTracks().map { it.map { t -> Track(t.id,t.uri,t.title,t.artist,t.album,t.durationMs) } }
    suspend fun replaceTracks(tracks: List<Track>) {
        dao.clearTracks()
        dao.upsertTracks(tracks.map { TrackEntity(it.id,it.uri,it.title,it.artist,it.album,it.durationMs) })
    }
    suspend fun addHistory(track: Track) = dao.addHistory(HistoryEntity(trackId = track.id, playedAt = System.currentTimeMillis()))
    suspend fun addFavorite(track: Track) = dao.addFavorite(FavoriteEntity(track.id))
    suspend fun removeFavorite(track: Track) = dao.removeFavorite(track.id)
    suspend fun isFavorite(track: Track) = dao.isFavorite(track.id)
}
