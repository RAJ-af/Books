package com.example.data.source

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GutendexResponse(
    @Json(name = "count") val count: Int? = null,
    @Json(name = "next") val next: String? = null,
    @Json(name = "results") val results: List<GutendexBook>? = null
)

@JsonClass(generateAdapter = true)
data class GutendexBook(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "authors") val authors: List<GutendexPerson>? = null,
    @Json(name = "formats") val formats: Map<String, String>? = null,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "bookshelves") val bookshelves: List<String>? = null,
    @Json(name = "subjects") val subjects: List<String>? = null,
    @Json(name = "summaries") val summaries: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GutendexPerson(
    @Json(name = "name") val name: String? = null,
    @Json(name = "birth_year") val birthYear: Int? = null,
    @Json(name = "death_year") val deathYear: Int? = null
)

interface GutendexApiService {
    @GET("books/")
    suspend fun searchBooks(
        @Query("search") query: String
    ): GutendexResponse
}

class GutenbergSource(
    private val okHttpClient: OkHttpClient = createDefaultOkHttpClient()
) : BookSource {

    override val id: String = "gutenberg"
    override val displayName: String = "Project Gutenberg"
    override val description: String = "Classic public domain ebooks & literature in EPUB"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://gutendex.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: GutendexApiService = retrofit.create(GutendexApiService::class.java)

    override suspend fun search(query: String): List<DiscoverResult> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchBooks(query = query.trim())
            val books = response.results ?: emptyList()
            val results = mutableListOf<DiscoverResult>()

            for (book in books) {
                val formats = book.formats ?: emptyMap()
                
                // Extract EPUB download link (application/epub+zip)
                val epubUrl = formats["application/epub+zip"]
                    ?: formats.entries.firstOrNull { it.key.contains("epub", ignoreCase = true) }?.value

                // Skip items without direct EPUB download link
                if (epubUrl.isNullOrBlank()) {
                    continue
                }

                val title = book.title?.trim() ?: "Untitled Public Domain Book"

                // Format Author name (e.g., "Shelley, Mary Wollstonecraft" -> "Mary Wollstonecraft Shelley")
                val author = formatAuthors(book.authors)

                // Cover image (image/jpeg)
                val coverUrl = formats["image/jpeg"]
                    ?: formats.entries.firstOrNull { it.key.contains("image", ignoreCase = true) }?.value

                // Summary / description
                val summary = book.summaries?.firstOrNull()
                    ?: book.bookshelves?.take(3)?.joinToString(", ")
                    ?: "Classic public domain ebook provided by Project Gutenberg."

                val bookId = book.id?.toString() ?: epubUrl.hashCode().toString()

                results.add(
                    DiscoverResult(
                        id = bookId,
                        title = title,
                        author = author,
                        year = null,
                        coverUrl = coverUrl,
                        sourceId = id,
                        sourceDisplayName = displayName,
                        description = summary,
                        directDownloadUrl = epubUrl
                    )
                )
            }

            results
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun resolveDownload(result: DiscoverResult): DownloadInfo? = withContext(Dispatchers.IO) {
        val url = result.directDownloadUrl
        if (!url.isNullOrBlank()) {
            DownloadInfo(url = url, fileType = FileType.EPUB)
        } else {
            null
        }
    }

    private fun formatAuthors(authors: List<GutendexPerson>?): String {
        if (authors.isNullOrEmpty()) return "Project Gutenberg Author"
        return authors.mapNotNull { person ->
            val name = person.name?.trim() ?: return@mapNotNull null
            if (name.contains(",")) {
                val parts = name.split(",")
                val lastName = parts[0].trim()
                val firstName = parts.getOrNull(1)?.trim() ?: ""
                if (firstName.isNotBlank()) "$firstName $lastName" else lastName
            } else {
                name
            }
        }.joinToString(", ")
    }

    companion object {
        fun createDefaultOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
        }
    }
}
