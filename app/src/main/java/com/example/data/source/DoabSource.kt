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
data class DoabItem(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "handle") val handle: String? = null,
    @Json(name = "link") val link: String? = null,
    @Json(name = "metadata") val metadata: List<DoabMetadataEntry>? = null,
    @Json(name = "bitstreams") val bitstreams: List<DoabBitstream>? = null
)

@JsonClass(generateAdapter = true)
data class DoabMetadataEntry(
    @Json(name = "key") val key: String? = null,
    @Json(name = "value") val value: String? = null
)

@JsonClass(generateAdapter = true)
data class DoabBitstream(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "mimeType") val mimeType: String? = null,
    @Json(name = "retrieveLink") val retrieveLink: String? = null,
    @Json(name = "sizeBytes") val sizeBytes: Long? = null
)

interface DoabApiService {
    @GET("rest/search")
    suspend fun search(
        @Query("query") query: String,
        @Query("limit") limit: Int = 30,
        @Query("expand") expand: String = "metadata,bitstreams"
    ): List<DoabItem>
}

class DoabSource(
    private val okHttpClient: OkHttpClient = createDefaultOkHttpClient()
) : BookSource {

    override val id: String = "doab"
    override val displayName: String = "DOAB"
    override val description: String = "Directory of Open Access Books & academic publications"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://directory.doabooks.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: DoabApiService = retrofit.create(DoabApiService::class.java)

    override suspend fun search(query: String): List<DiscoverResult> = withContext(Dispatchers.IO) {
        try {
            val items = apiService.search(query = query.trim(), limit = 35)
            val results = mutableListOf<DiscoverResult>()

            for (item in items) {
                val metadata = item.metadata ?: emptyList()

                // Extract direct PDF link from metadata or bitstreams
                val pdfUrl = extractPdfUrl(item, metadata)

                // Skip items without a direct PDF link
                if (pdfUrl.isNullOrBlank()) {
                    continue
                }

                // Extract title
                val rawTitle = metadata.find { it.key == "dc.title" }?.value
                    ?: item.name
                    ?: "Untitled Open Access Book"
                val title = cleanTitle(rawTitle)

                // Extract authors
                val authorsList = metadata.filter { it.key == "dc.contributor.author" }
                    .mapNotNull { it.value?.trim() }
                    .filter { it.isNotBlank() }
                val author = if (authorsList.isNotEmpty()) {
                    authorsList.joinToString(", ")
                } else {
                    metadata.find { it.key == "dc.creator" }?.value?.trim() ?: "Open Access Author"
                }

                // Extract year
                val issuedDate = metadata.find { it.key == "dc.date.issued" }?.value
                    ?: metadata.find { it.key == "dc.date.created" }?.value
                val year = issuedDate?.take(4)?.filter { it.isDigit() }?.takeIf { it.length == 4 }

                // Extract cover thumbnail
                val coverUrl = metadata.find { it.key == "oapen.identifier.thumbnail" }?.value
                    ?: metadata.find { it.key?.contains("thumbnail", ignoreCase = true) == true }?.value

                // Extract synopsis / abstract
                val description = metadata.find { it.key == "dc.description.abstract" }?.value
                    ?: metadata.find { it.key == "dc.description" }?.value

                val itemId = item.id?.toString() ?: item.handle ?: pdfUrl.hashCode().toString()

                results.add(
                    DiscoverResult(
                        id = itemId,
                        title = title,
                        author = author,
                        year = year,
                        coverUrl = coverUrl,
                        sourceId = id,
                        sourceDisplayName = displayName,
                        description = description,
                        directDownloadUrl = pdfUrl
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
            DownloadInfo(url = url, fileType = FileType.PDF)
        } else {
            null
        }
    }

    private fun extractPdfUrl(item: DoabItem, metadata: List<DoabMetadataEntry>): String? {
        // 1. Direct OAPEN / publisher isAvailableAs link ending in or containing .pdf
        val availableAs = metadata.filter { it.key == "oapen.relation.isAvailableAs" }
            .mapNotNull { it.value?.trim() }
            .firstOrNull { it.contains(".pdf", ignoreCase = true) && it.startsWith("http", ignoreCase = true) }
        if (availableAs != null) {
            return availableAs
        }

        // 2. Check item bitstreams
        val bitstream = item.bitstreams?.firstOrNull {
            it.mimeType.equals("application/pdf", ignoreCase = true) ||
                    it.name?.lowercase()?.endsWith(".pdf") == true
        }
        if (bitstream?.retrieveLink != null) {
            val link = bitstream.retrieveLink
            return if (link.startsWith("http")) link else "https://directory.doabooks.org/rest$link"
        }

        // 3. Other metadata uri ending with .pdf
        val uriPdf = metadata.firstOrNull { entry ->
            val v = entry.value?.trim() ?: ""
            v.startsWith("http", ignoreCase = true) && v.lowercase().endsWith(".pdf")
        }?.value
        if (uriPdf != null) {
            return uriPdf
        }

        return null
    }

    private fun cleanTitle(title: String): String {
        return title.replace(Regex("\\s+"), " ").trim()
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
