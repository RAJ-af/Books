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
            PDFBoxResourceLoader.init(context)
            val doc = PDDocument.load(savedPdfFile)
            val guessedTitleFromPdf = guessPdfTitle(doc, displayName)

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
            Log.d(TAG, "PDFBox extracted $totalChars total chars ($avgChars/page). isScanned=$isScanned, title=$guessedTitleFromPdf")

            val sizeInMb = savedPdfFile.length() / (1024f * 1024f)
            val formattedSize = if (sizeInMb >= 1f) String.format("%.1f MB", sizeInMb) else "${savedPdfFile.length() / 1024} KB"

            Result.success(
                PdfImportResult(
                    pdfPath = savedPdfFile.absolutePath,
                    coverImagePath = savedCoverFile.absolutePath,
                    pageCount = pageCount,
                    guessedTitle = guessedTitleFromPdf,
                    fileSizeFormatted = formattedSize,
                    isScanned = isScanned,
                    pageTexts = pageTexts
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isSuspiciousTitle(title: String?): Boolean {
        if (title.isNullOrBlank()) return true
        val t = title.trim()
        if (t.length <= 2) return true
        val lower = t.lowercase()
        val suspiciousKeywords = listOf(
            "safepdfkit", "ilovepdf", "pdfkit", "pdfbox", "adobe", "acrobat", "microsoft", "word",
            "export", "scanned", "scanner", "camscanner", "phantompdf", "foxit", "nitro", "unknown",
            "untitled", "document", "gutenberg", "page", "untitled document", "print", "sdk", "writer",
            "creator", "distiller", "renderer", "generator", "pdf", "scan"
        )
        if (suspiciousKeywords.any { lower.contains(it) }) return true
        return false
    }

    private fun cleanTitleString(name: String): String {
        return name
            .replace("(?i)\\.pdf$".toRegex(), "")
            .replace("[_-]".toRegex(), " ")
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    private fun guessPdfTitle(doc: PDDocument, displayName: String): String {
        // First check actual PDF metadata
        try {
            val docInfo = doc.documentInformation
            val metadataTitle = docInfo?.title?.trim()
            if (!metadataTitle.isNullOrBlank() && !isSuspiciousTitle(metadataTitle)) {
                return metadataTitle
            }
        } catch (_: Throwable) {}

        // Fallback (a): Check first outline/bookmark heading
        try {
            val outline = doc.documentCatalog.documentOutline
            if (outline != null) {
                val firstItem = outline.firstChild
                val outlineTitle = firstItem?.title?.trim()
                if (!outlineTitle.isNullOrBlank() && !isSuspiciousTitle(outlineTitle)) {
                    return outlineTitle
                }
            }
        } catch (_: Throwable) {}

        // Fallback (b): Clean up display/file name
        val cleanedFilename = cleanTitleString(displayName)
        if (cleanedFilename.isNotBlank() && !isSuspiciousTitle(cleanedFilename)) {
            return cleanedFilename
        }

        // Fallback (c): Generic "Imported Document [date]"
        val dateStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        return "Imported Document $dateStr"
    }

    fun parsePdfToChapters(
        pageTexts: List<String>,
        pageCount: Int
    ): List<ParsedChapter> {
        val chapters = mutableListOf<ParsedChapter>()

        // 1. Try to find clear heading patterns (e.g., "Chapter X", "SECTION X", "Introduction")
        val headingRegex = "^(Chapter\\s+\\d+|Section\\s+\\d+|Introduction|Conclusion|Foreword|Preface)\\b.*".toRegex(RegexOption.IGNORE_CASE)
        val detectedHeadings = mutableListOf<Pair<Int, String>>() // (pageIndex, headingTitle)

        for (i in pageTexts.indices) {
            val text = pageTexts[i]
            val firstLine = text.split("\n").firstOrNull { it.trim().isNotBlank() }?.trim() ?: ""
            if (firstLine.length in 3..60 && headingRegex.matches(firstLine)) {
                detectedHeadings.add(i to firstLine)
            }
        }

        if (detectedHeadings.size >= 2) {
            // We found clear heading patterns! Use them to make chapters.
            for (index in detectedHeadings.indices) {
                val (startPage, title) = detectedHeadings[index]
                val endPage = if (index + 1 < detectedHeadings.size) detectedHeadings[index + 1].first else pageCount
                val contentBuilder = StringBuilder()
                for (p in startPage until endPage) {
                    contentBuilder.append(pageTexts.getOrNull(p) ?: "").append("\n\n")
                }
                chapters.add(
                    ParsedChapter(
                        number = index + 1,
                        title = title,
                        subtitle = "Pages ${startPage + 1} to $endPage",
                        content = contentBuilder.toString().trim(),
                        startPage = startPage,
                        endPage = endPage
                    )
                )
            }
        } else {
            // No clear heading pattern found, fallback to section grouping (~12 pages per section)
            val pagesPerSection = 12
            if (pageCount <= 3) {
                // Short document, single chapter
                val content = pageTexts.joinToString("\n\n")
                chapters.add(
                    ParsedChapter(
                        number = 1,
                        title = "Full Document",
                        subtitle = "Pages 1 to $pageCount",
                        content = content.trim(),
                        startPage = 0,
                        endPage = pageCount
                    )
                )
            } else {
                val numSections = (pageCount + pagesPerSection - 1) / pagesPerSection
                for (s in 0 until numSections) {
                    val startPage = s * pagesPerSection
                    val endPage = ((s + 1) * pagesPerSection).coerceAtMost(pageCount)
                    val contentBuilder = StringBuilder()
                    for (p in startPage until endPage) {
                        contentBuilder.append(pageTexts.getOrNull(p) ?: "").append("\n\n")
                    }
                    chapters.add(
                        ParsedChapter(
                            number = s + 1,
                            title = "Section ${s + 1}",
                            subtitle = "Pages ${startPage + 1} to $endPage",
                            content = contentBuilder.toString().trim(),
                            startPage = startPage,
                            endPage = endPage
                        )
                    )
                }
            }
        }

        return chapters
    }

    data class ParsedChapter(
        val number: Int,
        val title: String,
        val subtitle: String,
        val content: String,
        val startPage: Int,
        val endPage: Int
    )

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
