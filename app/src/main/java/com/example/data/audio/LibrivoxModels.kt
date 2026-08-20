package com.example.data.audio

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LibrivoxAudiobooksResponse(
    @Json(name = "books") val books: List<LibrivoxBookItem>? = null,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class LibrivoxBookItem(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "copyright_year") val copyrightYear: String? = null,
    @Json(name = "num_sections") val numSections: String? = null,
    @Json(name = "totaltime") val totalTime: String? = null,
    @Json(name = "totaltimesecs") val totalTimeSecs: Long? = null,
    @Json(name = "url_zip_file") val urlZipFile: String? = null,
    @Json(name = "url_librivox") val urlLibrivox: String? = null,
    @Json(name = "authors") val authors: List<LibrivoxAuthorItem>? = null
)

@JsonClass(generateAdapter = true)
data class LibrivoxAuthorItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "first_name") val firstName: String? = null,
    @Json(name = "last_name") val lastName: String? = null
)

@JsonClass(generateAdapter = true)
data class LibrivoxTracksResponse(
    @Json(name = "sections") val sections: List<LibrivoxSectionItem>? = null
)

@JsonClass(generateAdapter = true)
data class LibrivoxSectionItem(
    @Json(name = "id") val id: String,
    @Json(name = "section_number") val sectionNumber: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "listen_url") val listenUrl: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "playtime") val playtime: String? = null
)

data class LibrivoxAudiobook(
    val id: String,
    val title: String,
    val author: String,
    val language: String,
    val totalTime: String,
    val totalTimeSecs: Long,
    val description: String,
    val numSections: Int
)

data class LibrivoxAudioTrack(
    val id: String,
    val sectionNumber: Int,
    val title: String,
    val listenUrl: String,
    val playtimeSecs: Long
)
