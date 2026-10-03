package com.takusima.takutune.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TrackEntity::class, FavoriteEntity::class, HistoryEntity::class, PlaylistEntity::class, PlaylistTrackEntity::class, BlockedTrackEntity::class], version = 1, exportSchema = false)
abstract class TakuTuneDatabase : RoomDatabase() {
    abstract fun dao(): TakuTuneDao
    companion object {
        @Volatile private var instance: TakuTuneDatabase? = null
        fun get(context: Context): TakuTuneDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, TakuTuneDatabase::class.java, "takutune.db").build().also { instance = it }
        }
    }
}
