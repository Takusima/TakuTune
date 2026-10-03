package com.takusima.takutune.library

import android.content.ContentResolver
import android.provider.MediaStore
import com.takusima.takutune.core.model.Track

class LocalMusicScanner(private val resolver: ContentResolver) {
    fun scan(): List<Track> {
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.IS_MUSIC)
        resolver.query(collection, projection, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC")?.use { c ->
            val id=c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID); val title=c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE); val artist=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST); val album=c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM); val duration=c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while(c.moveToNext()){ val trackId=c.getLong(id); tracks += Track(trackId, "$collection/$trackId", c.getString(title).orEmpty(), c.getString(artist).orEmpty().ifBlank{"Неизвестный исполнитель"}, c.getString(album).orEmpty().ifBlank{"Неизвестный альбом"}, c.getLong(duration)) }
        }
        return tracks
    }
}
