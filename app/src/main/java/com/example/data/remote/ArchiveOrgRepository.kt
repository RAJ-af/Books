package com.example.data.remote

import android.content.Context
import com.example.util.PdfHelper
import com.example.util.PdfImportResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ArchiveOrgRepository(private val context: Context) {

    private val moshi = Moshi.Builder()
        .add(ArchiveDocJsonAdapter())
        .add(ArchiveMetadataInfoJsonAdapter())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://archive.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService: ArchiveApiService = retrofit.create(ArchiveApiService::class.java)

    suspend fun searchBooks(query: String): Result<List<ArchiveDoc>> = withContext(Dispatchers.IO) {
        try {
            val formattedQuery = "(${query.trim()}) AND mediatype:texts"
            val response = apiService.searchBooks(query = formattedQuery)
            val docs = response.response?.docs ?: emptyList()
            Result.success(docs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findDownloadablePdf(identifier: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val metadata = apiService.getMetadata(identifier)
            val files = metadata.files

            // Look for a PDF file that is not restricted or system auxiliary
            val pdfFiles = files.filter { file ->
                val name = file.name.lowercase()
                name.endsWith(".pdf") &&
                        !name.startsWith(".") &&
                        !name.contains("_encrypted") &&
                        !name.contains("_page_numbers") &&
                        !name.contains("meta.pdf")
            }

            if (pdfFiles.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Not available for direct download (no direct public-domain PDF found).")
                )
            }

            // Prioritize formats: "Text PDF" or "Additional Text PDF" or largest file
            val chosenFile = pdfFiles.find { it.format?.contains("Text PDF", ignoreCase = true) == true }
                ?: pdfFiles.find { it.format?.contains("PDF", ignoreCase = true) == true }
                ?: pdfFiles.first()

            val downloadUrl = "https://archive.org/download/$identifier/${chosenFile.name}"
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadAndProcessPdf(
        identifier: String,
        title: String,
        creator: String,
        onProgress: (Float) -> Unit
    ): Result<PdfImportResult> = withContext(Dispatchers.IO) {
        try {
            val pdfUrlResult = findDownloadablePdf(identifier)
            if (pdfUrlResult.isFailure) {
                return@withContext Result.failure(
                    pdfUrlResult.exceptionOrNull() ?: IllegalStateException("PDF not available for direct download")
                )
            }

            val downloadUrl = pdfUrlResult.getOrThrow()

            val response = apiService.downloadFileStream(downloadUrl)
            val body = response.body() ?: return@withContext Result.failure(IllegalStateException("Failed to download file from archive.org"))

            val totalBytes = body.contentLength()
            val pdfsDir = File(context.filesDir, "imported_pdfs").apply { if (!exists()) mkdirs() }
            val timestamp = System.currentTimeMillis()
            val cleanId = identifier.replace("[^a-zA-Z0-9]".toRegex(), "").take(24)
            val downloadedFile = File(pdfsDir, "ia_${cleanId}_${timestamp}.pdf")

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

            // Now run PdfHelper on the downloaded file to render page count and cover thumbnail
            val processResult = PdfHelper.processLocalPdfFile(
                context = context,
                pdfFile = downloadedFile,
                title = title.ifBlank { "Internet Archive Document" },
                author = creator.ifBlank { "Public Domain" }
            )

            processResult
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
