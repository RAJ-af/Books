package com.example.data.audio

import android.util.Log
import com.example.data.source.GutenbergSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface LibrivoxApiService {
    @GET("api/feed/audiobooks/")
    suspend fun searchAudiobooks(
        @Query("title") title: String,
        @Query("format") format: String = "json",
        @Query("extended") extended: String = "1"
    ): LibrivoxAudiobooksResponse

    @GET("api/feed/audiotracks/")
    suspend fun getAudioTracks(
        @Query("project_id") projectId: String,
        @Query("format") format: String = "json"
    ): LibrivoxTracksResponse
}

class LibrivoxSource(
    private val okHttpClient: OkHttpClient = GutenbergSource.createDefaultOkHttpClient()
) {
    private val TAG = "LibrivoxSource"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://librivox.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: LibrivoxApiService = retrofit.create(LibrivoxApiService::class.java)

    /**
     * Searches LibriVox for audiobooks matching the given query.
     */
    suspend fun searchAudiobooks(query: String): List<LibrivoxAudiobook> = withContext(Dispatchers.IO) {
        try {
            val cleanQuery = query.trim()
            if (cleanQuery.isBlank()) return@withContext emptyList()

            // LibriVox exact/prefix search using ^
            val searchTitleParam = "^$cleanQuery"
            var response = apiService.searchAudiobooks(title = searchTitleParam)

            // Fallback to plain query if caret prefix yields no results
            if (response.books.isNullOrEmpty()) {
                response = apiService.searchAudiobooks(title = cleanQuery)
            }

            val books = response.books ?: return@withContext emptyList()

            books.map { item ->
                val authorStr = item.authors?.joinToString(", ") { a ->
                    val first = a.firstName?.trim().orEmpty()
                    val last = a.lastName?.trim().orEmpty()
                    if (first.isNotBlank() && last.isNotBlank()) "$first $last"
                    else last.ifBlank { first }.ifBlank { "Unknown Author" }
                } ?: "Unknown Author"

                LibrivoxAudiobook(
                    id = item.id,
                    title = item.title?.trim() ?: "Untitled Audiobook",
                    author = authorStr,
                    language = item.language ?: "English",
                    totalTime = item.totalTime ?: "00:00:00",
                    totalTimeSecs = item.totalTimeSecs ?: 0L,
                    description = item.description?.replace(Regex("<[^>]*>"), "")?.trim() ?: "",
                    numSections = item.numSections?.toIntOrNull() ?: 1
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching LibriVox audiobooks for query: $query", e)
            emptyList()
        }
    }

    /**
     * Fetches audio tracks (chapters) for a given LibriVox project ID.
     */
    suspend fun fetchAudioTracks(projectId: String): List<LibrivoxAudioTrack> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAudioTracks(projectId = projectId)
            val sections = response.sections ?: return@withContext emptyList()

            sections.mapIndexed { index, sec ->
                val trackNum = sec.sectionNumber?.toIntOrNull() ?: (index + 1)
                val trackTitle = sec.title?.trim()
                    ?.ifBlank { "Part ${trackNum.toString().padStart(2, '0')}" }
                    ?: "Part ${trackNum.toString().padStart(2, '0')}"

                val listenUrl = sec.listenUrl?.trim().orEmpty()
                val secs = sec.playtime?.toLongOrNull() ?: 0L

                LibrivoxAudioTrack(
                    id = sec.id.ifBlank { "$projectId-$index" },
                    sectionNumber = trackNum,
                    title = trackTitle,
                    listenUrl = listenUrl,
                    playtimeSecs = secs
                )
            }.filter { it.listenUrl.isNotBlank() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching audio tracks for project: $projectId", e)
            emptyList()
        }
    }
}
