package com.example.data.source

import com.example.data.remote.ArchiveApiService
import com.example.data.remote.ArchiveDocJsonAdapter
import com.example.data.remote.ArchiveMetadataInfoJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class InternetArchiveSource(
    private val okHttpClient: OkHttpClient = createDefaultOkHttpClient()
) : BookSource {

    override val id: String = "internet_archive"
    override val displayName: String = "Internet Archive"
    override val description: String = "Public domain books, classics & historical archives"

    private val moshi = Moshi.Builder()
        .add(ArchiveDocJsonAdapter())
        .add(ArchiveMetadataInfoJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://archive.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: ArchiveApiService = retrofit.create(ArchiveApiService::class.java)

    override suspend fun search(query: String): List<DiscoverResult> = withContext(Dispatchers.IO) {
        val formattedQuery = "(${query.trim()}) AND mediatype:texts"
        val response = apiService.searchBooks(query = formattedQuery)
        val docs = response.response?.docs ?: emptyList()

        docs.map { doc ->
            DiscoverResult(
                id = doc.identifier,
                title = doc.title?.trim() ?: "Untitled Document",
                author = doc.creator?.trim() ?: "Public Domain",
                year = doc.year,
                coverUrl = doc.coverThumbnailUrl,
                sourceId = id,
                sourceDisplayName = displayName,
                description = doc.description
            )
        }
    }

    override suspend fun resolveDownload(result: DiscoverResult): DownloadInfo? = withContext(Dispatchers.IO) {
        try {
            val metadata = apiService.getMetadata(result.id)
            val files = metadata.files

            val pdfFiles = files.filter { file ->
                val name = file.name.lowercase()
                name.endsWith(".pdf") &&
                        !name.startsWith(".") &&
                        !name.contains("_encrypted") &&
                        !name.contains("_page_numbers") &&
                        !name.contains("meta.pdf")
            }

            if (pdfFiles.isEmpty()) {
                return@withContext null
            }

            val chosenFile = pdfFiles.find { it.format?.contains("Text PDF", ignoreCase = true) == true }
                ?: pdfFiles.find { it.format?.contains("PDF", ignoreCase = true) == true }
                ?: pdfFiles.first()

            val downloadUrl = "https://archive.org/download/${result.id}/${chosenFile.name}"
            DownloadInfo(url = downloadUrl, fileType = FileType.PDF)
        } catch (e: Exception) {
            null
        }
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
