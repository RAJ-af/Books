package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class PdfImportResult(
    val pdfPath: String,
    val coverImagePath: String,
    val pageCount: Int,
    val guessedTitle: String,
    val fileSizeFormatted: String,
    val isScanned: Boolean = false,
    val pageTexts: List<String> = emptyList()
)

object PdfHelper {

    private const val TAG = "PdfHelper"

    suspend fun processAndImportPdf(context: Context, uri: Uri): Result<PdfImportResult> = withContext(Dispatchers.IO) {
        try {
            // 1. Resolve Display Name / Guessed Title
            var displayName = "Imported Document"
            var fileSize = 0L
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) {
                            cursor.getString(nameIndex)?.let { displayName = it }
                        }
                        if (sizeIndex != -1) {
                            fileSize = cursor.getLong(sizeIndex)
                        }
                    }
                }
            } catch (_: Exception) {
                uri.lastPathSegment?.let { displayName = it }
            }

            val cleanedTitle = displayName
                .replace("(?i)\\.pdf$".toRegex(), "")
                .replace("[_-]".toRegex(), " ")
                .trim()
                .split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }

            // 2. Prepare Internal Storage Directories
            val pdfsDir = File(context.filesDir, "imported_pdfs").apply { if (!exists()) mkdirs() }
            val coversDir = File(context.filesDir, "imported_covers").apply { if (!exists()) mkdirs() }

            val timestamp = System.currentTimeMillis()
            val savedPdfFile = File(pdfsDir, "doc_${timestamp}.pdf")
            val savedCoverFile = File(coversDir, "cover_${timestamp}.png")

            // 3. Copy file to internal storage
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(savedPdfFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Unable to read PDF from selected file"))

            // 4. Render Page 0 for Cover Thumbnail & get Page Count
            var pageCount = 1
            val pfd = ParcelFileDescriptor.open(savedPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            try {
                pageCount = renderer.pageCount
                if (pageCount > 0) {
                    val firstPage = renderer.openPage(0)
                    val targetWidth = 600
                    val targetHeight = ((targetWidth.toFloat() / firstPage.width.toFloat()) * firstPage.height.toFloat()).toInt().coerceAtLeast(100)
                    val coverBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    coverBitmap.eraseColor(android.graphics.Color.WHITE)

                    firstPage.render(coverBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    firstPage.close()

                    FileOutputStream(savedCoverFile).use { out ->
                        coverBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                    }
                }
            } finally {
                renderer.close()
                pfd.close()
            }

            // 5. PdfBox Text Extraction & Scanned Detection (< 50 chars/page)
            val (isScanned, pageTexts) = extractTextAndCheckScanned(context, savedPdfFile)

            val sizeInMb = savedPdfFile.length() / (1024f * 1024f)
            val formattedSize = if (sizeInMb >= 1f) String.format("%.1f MB", sizeInMb) else "${savedPdfFile.length() / 1024} KB"

            Result.success(
                PdfImportResult(
                    pdfPath = savedPdfFile.absolutePath,
                    coverImagePath = savedCoverFile.absolutePath,
                    pageCount = pageCount,
                    guessedTitle = cleanedTitle.ifBlank { "Untitled PDF" },
                    fileSizeFormatted = formattedSize,
                    isScanned = isScanned,
                    pageTexts = pageTexts
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processLocalPdfFile(
        context: Context,
        pdfFile: File,
        title: String,
        author: String
    ): Result<PdfImportResult> = withContext(Dispatchers.IO) {
        try {
            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                return@withContext Result.failure(IllegalStateException("Downloaded file is empty or missing"))
            }

            val coversDir = File(context.filesDir, "imported_covers").apply { if (!exists()) mkdirs() }
            val timestamp = System.currentTimeMillis()
            val savedCoverFile = File(coversDir, "cover_${timestamp}.png")

            var pageCount = 1
            val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            try {
                pageCount = renderer.pageCount
                if (pageCount > 0) {
                    val firstPage = renderer.openPage(0)
                    val targetWidth = 600
                    val targetHeight = ((targetWidth.toFloat() / firstPage.width.toFloat()) * firstPage.height.toFloat()).toInt().coerceAtLeast(100)
                    val coverBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    coverBitmap.eraseColor(android.graphics.Color.WHITE)

                    firstPage.render(coverBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    firstPage.close()

                    FileOutputStream(savedCoverFile).use { out ->
                        coverBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                    }
                }
            } finally {
                renderer.close()
                pfd.close()
            }

            // PdfBox Text Extraction & Scanned Detection (< 50 chars/page)
            val (isScanned, pageTexts) = extractTextAndCheckScanned(context, pdfFile)

            val sizeInMb = pdfFile.length() / (1024f * 1024f)
            val formattedSize = if (sizeInMb >= 1f) String.format("%.1f MB", sizeInMb) else "${pdfFile.length() / 1024} KB"

            Result.success(
                PdfImportResult(
                    pdfPath = pdfFile.absolutePath,
                    coverImagePath = savedCoverFile.absolutePath,
                    pageCount = pageCount,
                    guessedTitle = title.ifBlank { "Internet Archive Document" },
                    fileSizeFormatted = formattedSize,
                    isScanned = isScanned,
                    pageTexts = pageTexts
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun extractTextAndCheckScanned(context: Context, pdfFile: File): Pair<Boolean, List<String>> {
        return try {
            PDFBoxResourceLoader.init(context)
            val doc = PDDocument.load(pdfFile)
            val stripper = PDFTextStripper()
            val pageTexts = mutableListOf<String>()
            var totalChars = 0

            val numPages = doc.numberOfPages
            for (i in 1..numPages) {
                stripper.startPage = i
                stripper.endPage = i
                val pageText = stripper.getText(doc).trim()
                pageTexts.add(pageText)
                totalChars += pageText.length
            }
            doc.close()

            val avgChars = if (numPages > 0) totalChars / numPages else 0
            val isScanned = avgChars < 50
            Log.d(TAG, "PDFBox extracted $totalChars total chars ($avgChars/page). isScanned=$isScanned")
            Pair(isScanned, pageTexts)
        } catch (e: Exception) {
            Log.w(TAG, "PDFBox text extraction failed: ${e.message}")
            Pair(true, emptyList())
        }
    }

    suspend fun renderPageBitmap(
        pdfPath: String,
        pageIndex: Int,
        targetWidth: Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(pdfPath)
            if (!file.exists()) return@withContext null

            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            try {
                if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

                val page = renderer.openPage(pageIndex)
                val width = targetWidth.coerceAtLeast(400)
                val height = ((width.toFloat() / page.width.toFloat()) * page.height.toFloat()).toInt().coerceAtLeast(400)

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                bitmap
            } finally {
                renderer.close()
                pfd.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
