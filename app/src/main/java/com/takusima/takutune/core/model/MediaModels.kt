package com.takusima.takutune.core.model

data class Artist(val id: String, val name: String)
data class Album(val id: String, val title: String, val artist: String, val artworkUri: String? = null)
data class Playlist(val id: String, val name: String, val trackIds: List<String> = emptyList())
data class PlaybackSource(val uri: String, val source: String)
