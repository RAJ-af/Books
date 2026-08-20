package com.example.data.source

import android.content.Context
import com.example.util.EpubChapterData
import com.example.util.EpubHelper
import com.example.util.PdfHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class UnifiedImportResult(
    val fileType: FileType,
    val guessedTitle: String,
    val guessedAuthor: String,
    val coverImagePath: String,
    val filePath: String,
    val pageCount: Int,
    val fileSizeFormatted: String,
    val epubChapters: List<EpubChapterData> = emptyList()
)

class BookSourceManager(
    private val context: Context,
    private val okHttpClient: OkHttpClient = createSharedClient()
) {

    private val registeredSources: List<BookSource> = listOf(
        GutenbergSource(okHttpClient),
        StandardEbooksSource(okHttpClient),
        InternetArchiveSource(okHttpClient),
        DoabSource(okHttpClient)
    )

    val sources: List<BookSource> get() = registeredSources

    fun getSource(id: String): BookSource {
        return registeredSources.find { it.id == id } ?: registeredSources.first()
    }

    suspend fun search(sourceId: String, query: String): Result<List<DiscoverResult>> = withContext(Dispatchers.IO) {
        try {
            val source = getSource(sourceId)
            val results = source.search(query)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAndProcess(
        result: DiscoverResult,
        onProgress: (Float) -> Unit
    ): Result<UnifiedImportResult> = withContext(Dispatchers.IO) {
        try {
            val source = getSource(result.sourceId)
            val downloadInfo = source.resolveDownload(result)
                ?: return@withContext Result.failure(
                    IllegalStateException("Direct download is not available for this book.")
                )

            val downloadUrl = downloadInfo.url
            val isEpub = downloadInfo.fileType == FileType.EPUB

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; ReaderApp/1.0)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IllegalStateException("Failed to download file (HTTP ${response.code})")
                )
            }

            val body = response.body
                ?: return@withContext Result.failure(IllegalStateException("Empty response body received"))

            val totalBytes = body.contentLength()
            val fileDir = File(context.filesDir, if (isEpub) "imported_epubs" else "imported_pdfs").apply { if (!exists()) mkdirs() }
            val timestamp = System.currentTimeMillis()
            val cleanId = result.id.replace("[^a-zA-Z0-9]".toRegex(), "").take(20)
            val ext = if (isEpub) "epub" else "pdf"
            val prefix = when (result.sourceId) {
                "gutenberg" -> "gut"
                "standard_ebooks" -> "se"
                "doab" -> "doab"
                else -> "ia"
            }
            val downloadedFile = File(fileDir, "${prefix}_${cleanId}_${timestamp}.${ext}")

            body.byteStream().use { input ->
                FileOutputStream(downloadedFile).use { output ->
                    val buffer = ByteArray(16384)
                    var bytesCopied: Long = 0
                    var read = input.read(buffer)
                    while (read >= 0) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                        if (totalBytes > 0) {
                            val fraction = (bytesCopied.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                            onProgress(fraction)
                        }
                        read = input.read(buffer)
                    }
                    output.flush()
                }
            }

            onProgress(1f)

            if (isEpub) {
                // Process EPUB
                val epubRes = EpubHelper.processLocalEpubFile(
                    context = context,
                    epubFile = downloadedFile,
                    fallbackTitle = result.title.ifBlank { "Gutenberg Classic" },
                    fallbackAuthor = result.author.ifBlank { "Project Gutenberg" }
                )

                epubRes.fold(
                    onSuccess = { epubData ->
                        // If cover image path was empty in EPUB, but discover result has cover url, use cover url
                        val finalCover = epubData.coverImagePath.ifBlank { result.coverUrl.orEmpty() }
                        Result.success(
                            UnifiedImportResult(
                                fileType = FileType.EPUB,
                                guessedTitle = epubData.guessedTitle,
                                guessedAuthor = epubData.guessedAuthor,
                                coverImagePath = finalCover,
                                filePath = epubData.epubPath,
                                pageCount = epubData.pageCount,
                                fileSizeFormatted = epubData.fileSizeFormatted,
                                epubChapters = epubData.chapters
                            )
                        )
                    },
                    onFailure = { err ->
                        Result.failure(err)
                    }
                )
            } else {
                // Process PDF
                val pdfRes = PdfHelper.processLocalPdfFile(
                    context = context,
                    pdfFile = downloadedFile,
                    title = result.title.ifBlank { "Imported Document" },
                    author = result.author.ifBlank { "Unknown Author" }
                )

                pdfRes.fold(
                    onSuccess = { pdfData ->
                        Result.success(
                            UnifiedImportResult(
                                fileType = FileType.PDF,
                                guessedTitle = pdfData.guessedTitle,
                                guessedAuthor = result.author,
                                coverImagePath = pdfData.coverImagePath,
                                filePath = pdfData.pdfPath,
                                pageCount = pdfData.pageCount,
                                fileSizeFormatted = pdfData.fileSizeFormatted
                            )
                        )
                    },
                    onFailure = { err ->
                        Result.failure(err)
                    }
                )
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun createSharedClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()
        }
    }
}
