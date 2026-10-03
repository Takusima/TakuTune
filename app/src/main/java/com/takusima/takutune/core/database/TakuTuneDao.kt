package com.takusima.takutune.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TakuTuneDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeTracks(): Flow<List<TrackEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<TrackEntity>)
    @Query("DELETE FROM tracks")
    suspend fun clearTracks()
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(item: FavoriteEntity)
    @Query("DELETE FROM favorites WHERE trackId = :trackId")
    suspend fun removeFavorite(trackId: Long)
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE trackId = :trackId)")
    suspend fun isFavorite(trackId: Long): Boolean
    @Insert
    suspend fun addHistory(item: HistoryEntity)
    @Query("SELECT * FROM history ORDER BY playedAt DESC LIMIT 100")
    fun observeHistory(): Flow<List<HistoryEntity>>
    @Insert
    suspend fun createPlaylist(item: PlaylistEntity): Long
    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE")
    fun observePlaylists(): Flow<List<PlaylistEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToPlaylist(item: PlaylistTrackEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun blockTrack(item: BlockedTrackEntity)
    @Query("DELETE FROM blocked_tracks WHERE trackId = :trackId")
    suspend fun unblockTrack(trackId: Long)
    @Query("SELECT trackId FROM blocked_tracks")
    fun observeBlocked(): Flow<List<Long>>
}
