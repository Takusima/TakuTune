package com.takusima.takutune.sources

import com.takusima.takutune.core.model.Track

class SourceRegistry(private val sources: List<MusicSource>) {
    fun all(): List<MusicSource> = sources
    suspend fun search(query: String): List<Pair<MusicSource, List<Track>>> = sources.map { it to it.search(query) }
}