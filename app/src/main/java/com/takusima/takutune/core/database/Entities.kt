package com.takusima.takutune.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Persistent local track metadata. @author Takusima */
@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long
)

/** User favorite relation. @author Takusima */
@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val trackId: Long)

/** Playback history entry. @author Takusima */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: Long,
    val playedAt: Long
)

/** User playlist. @author Takusima */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

/** Playlist-to-track relation. @author Takusima */
@Entity(primaryKeys = ["playlistId", "trackId"], tableName = "playlist_tracks")
data class PlaylistTrackEntity(
    val playlistId: Long,
    val trackId: Long
)

/** User-blocked track relation. @author Takusima */
@Entity(tableName = "blocked_tracks")
data class BlockedTrackEntity(@PrimaryKey val trackId: Long)
