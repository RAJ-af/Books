package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.regex.Pattern

data class EpubChapterData(
    val number: Int,
    val title: String,
    val subtitle: String,
    val content: String,
    val estimatedReadMinutes: Int = 8
)

data class EpubImportResult(
    val guessedTitle: String,
    val guessedAuthor: String,
    val coverImagePath: String,
    val epubPath: String,
    val pageCount: Int,
    val chapters: List<EpubChapterData>,
    val fileSizeFormatted: String
)

object EpubHelper {

    private const val TAG = "EpubHelper"

    suspend fun processLocalEpubFile(
        context: Context,
        epubFile: File,
        fallbackTitle: String,
        fallbackAuthor: String
    ): Result<EpubImportResult> = withContext(Dispatchers.IO) {
        try {
            if (!epubFile.exists() || epubFile.length() == 0L) {
                return@withContext Result.failure(IllegalArgumentException("EPUB file is missing or empty."))
            }

            val fileSizeMb = epubFile.length().toFloat() / (1024f * 1024f)
            val fileSizeFormatted = if (fileSizeMb < 1f) {
                "${(epubFile.length() / 1024)} KB"
            } else {
                String.format("%.1f MB", fileSizeMb)
            }

            val zipFile = ZipFile(epubFile)
            val entries = zipFile.entries().toList().associateBy { it.name }

            // 1. Find root OPF path from META-INF/container.xml
            val opfPath = findOpfPath(zipFile, entries)
            val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") + "/" else ""

            // 2. Read OPF content
            val opfContent = opfPath.let { path ->
                entries[path]?.let { entry ->
                    zipFile.getInputStream(entry).bufferedReader().use { it.readText() }
                }
            } ?: ""

            // 3. Extract metadata (title, author, cover)
            val extractedTitle = extractTagValue(opfContent, "dc:title")?.trim()
                ?.ifBlank { null } ?: fallbackTitle
            val extractedAuthor = extractTagValue(opfContent, "dc:creator")?.trim()
                ?.ifBlank { null } ?: fallbackAuthor

            // 4. Extract Cover Image
            val coverImagePath = extractAndSaveCoverImage(context, zipFile, entries, opfContent, opfDir, epubFile)

            // 5. Parse Manifest (id -> href) and Spine (ordered idrefs)
            val manifestMap = parseManifest(opfContent)
            val spineIds = parseSpine(opfContent)

            val parsedChapters = mutableListOf<EpubChapterData>()
            var chapterIndex = 1

            for (idref in spineIds) {
                val relativeHref = manifestMap[idref] ?: continue
                val fullHref = if (opfDir.isNotEmpty() && !relativeHref.startsWith(opfDir)) {
                    opfDir + relativeHref
                } else {
                    relativeHref
                }

                val htmlEntry = entries[fullHref] ?: entries[relativeHref]
                    ?: entries.values.find { it.name.endsWith(relativeHref) }

                if (htmlEntry != null) {
                    val rawHtml = zipFile.getInputStream(htmlEntry).bufferedReader().use { it.readText() }
                    val (chapterTitle, cleanText) = parseChapterHtml(rawHtml, chapterIndex)

                    if (cleanText.isNotBlank() && cleanText.length > 50) {
                        val estRead = (cleanText.split("\\s+".toRegex()).size / 200).coerceAtLeast(2)
                        parsedChapters.add(
                            EpubChapterData(
                                number = chapterIndex,
                                title = chapterTitle,
                                subtitle = "Chapter $chapterIndex",
                                content = cleanText,
                                estimatedReadMinutes = estRead
                            )
                        )
                        chapterIndex++
                    }
                }
            }

            zipFile.close()

            // If no chapters parsed, generate a fallback chapter
            if (parsedChapters.isEmpty()) {
                parsedChapters.add(
                    EpubChapterData(
                        number = 1,
                        title = extractedTitle,
                        subtitle = "Full Text",
                        content = "Welcome to $extractedTitle by $extractedAuthor.\n\nThis EPUB edition is ready for reading in your library.",
                        estimatedReadMinutes = 5
                    )
                )
            }

            // Estimate total pages (~1200 chars per page)
            val totalCharCount = parsedChapters.sumOf { it.content.length }
            val estimatedPageCount = (totalCharCount / 1200).coerceAtLeast(parsedChapters.size * 8)

            Result.success(
                EpubImportResult(
                    guessedTitle = extractedTitle,
                    guessedAuthor = extractedAuthor,
                    coverImagePath = coverImagePath,
                    epubPath = epubFile.absolutePath,
                    pageCount = estimatedPageCount,
                    chapters = parsedChapters,
                    fileSizeFormatted = fileSizeFormatted
                )
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse EPUB file: ${epubFile.name}", e)
            Result.failure(e)
        }
    }

    private fun findOpfPath(zipFile: ZipFile, entries: Map<String, ZipEntry>): String {
        val containerEntry = entries["META-INF/container.xml"] ?: return "OEBPS/content.opf"
        val containerXml = zipFile.getInputStream(containerEntry).bufferedReader().use { it.readText() }
        val matcher = Pattern.compile("full-path=\"([^\"]+)\"").matcher(containerXml)
        return if (matcher.find()) matcher.group(1) ?: "OEBPS/content.opf" else "OEBPS/content.opf"
    }

    private fun extractTagValue(xml: String, tagName: String): String? {
        val pattern = Pattern.compile("<$tagName[^>]*>(.*?)</$tagName>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val matcher = pattern.matcher(xml)
        return if (matcher.find()) {
            matcher.group(1)?.replace(Regex("<[^>]*>"), "")?.trim()
        } else null
    }

    private fun parseManifest(opfXml: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val matcher = Pattern.compile("<item\\s+[^>]*>", Pattern.CASE_INSENSITIVE)
        val m = matcher.matcher(opfXml)

        while (m.find()) {
            val itemTag = m.group(0) ?: continue
            val idMatch = Pattern.compile("id=\"([^\"]+)\"").matcher(itemTag)
            val hrefMatch = Pattern.compile("href=\"([^\"]+)\"").matcher(itemTag)

            if (idMatch.find() && hrefMatch.find()) {
                val id = idMatch.group(1)
                val href = hrefMatch.group(1)
                if (id != null && href != null) {
                    map[id] = href
                }
            }
        }
        return map
    }

    private fun parseSpine(opfXml: String): List<String> {
        val list = mutableListOf<String>()
        val matcher = Pattern.compile("<itemref\\s+[^>]*idref=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE)
        val m = matcher.matcher(opfXml)

        while (m.find()) {
            m.group(1)?.let { list.add(it) }
        }
        return list
    }

    private fun parseChapterHtml(rawHtml: String, chapterNumber: Int): Pair<String, String> {
        // Extract title
        val titleFromH1 = extractTagValue(rawHtml, "h1")
        val titleFromH2 = extractTagValue(rawHtml, "h2")
        val titleFromTitle = extractTagValue(rawHtml, "title")

        val chapterTitle = titleFromH1?.takeIf { it.length in 3..80 }
            ?: titleFromH2?.takeIf { it.length in 3..80 }
            ?: titleFromTitle?.takeIf { it.length in 3..80 && !it.contains(".html", true) }
            ?: "Chapter $chapterNumber"

        // Strip script, style, head
        var cleaned = rawHtml
            .replace(Regex("<head.*?>.*?</head>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<style.*?>.*?</style>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<script.*?>.*?</script>", RegexOption.DOT_MATCHES_ALL), "")

        // Convert block elements into line breaks
        cleaned = cleaned
            .replace(Regex("<(p|div|br|h1|h2|h3|h4|h5|h6|li)[^>]*>", RegexOption.IGNORE_CASE), "\n\n")
            .replace(Regex("<[^>]*>"), "")

        // Unescape common HTML entities
        cleaned = unescapeHtml(cleaned)

        // Normalize double spacing
        val paragraphs = cleaned.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n\n")

        return Pair(chapterTitle, paragraphs)
    }

    private fun extractAndSaveCoverImage(
        context: Context,
        zipFile: ZipFile,
        entries: Map<String, ZipEntry>,
        opfXml: String,
        opfDir: String,
        epubFile: File
    ): String {
        try {
            // Check for cover item in manifest
            val coverHref = findCoverHrefInManifest(opfXml)
            if (coverHref != null) {
                val fullHref = if (opfDir.isNotEmpty() && !coverHref.startsWith(opfDir)) {
                    opfDir + coverHref
                } else {
                    coverHref
                }

                val coverEntry = entries[fullHref] ?: entries[coverHref]
                    ?: entries.values.find { it.name.endsWith(coverHref) }

                if (coverEntry != null) {
                    val bitmap = zipFile.getInputStream(coverEntry).use {
                        BitmapFactory.decodeStream(it)
                    }
                    if (bitmap != null) {
                        return saveBitmapToDisk(context, bitmap, epubFile.name)
                    }
                }
            }

            // Fallback: look for any entry ending with cover.jpg, cover.png, etc.
            val fallbackCoverEntry = entries.values.find { entry ->
                val name = entry.name.lowercase()
                (name.contains("cover") && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")))
            }

            if (fallbackCoverEntry != null) {
                val bitmap = zipFile.getInputStream(fallbackCoverEntry).use {
                    BitmapFactory.decodeStream(it)
                }
                if (bitmap != null) {
                    return saveBitmapToDisk(context, bitmap, epubFile.name)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract cover image from EPUB", e)
        }
        return ""
    }

    private fun findCoverHrefInManifest(opfXml: String): String? {
        val matcher = Pattern.compile("<item\\s+[^>]*>", Pattern.CASE_INSENSITIVE)
        val m = matcher.matcher(opfXml)

        while (m.find()) {
            val itemTag = m.group(0) ?: continue
            val isCover = itemTag.contains("properties=\"cover-image\"", true) ||
                    itemTag.contains("id=\"cover\"", true) ||
                    itemTag.contains("id=\"cover-image\"", true)

            if (isCover) {
                val hrefMatch = Pattern.compile("href=\"([^\"]+)\"").matcher(itemTag)
                if (hrefMatch.find()) {
                    return hrefMatch.group(1)
                }
            }
        }
        return null
    }

    private fun saveBitmapToDisk(context: Context, bitmap: Bitmap, fileName: String): String {
        val coversDir = File(context.filesDir, "imported_covers").apply { if (!exists()) mkdirs() }
        val cleanName = fileName.replace("[^a-zA-Z0-9]".toRegex(), "").take(20)
        val coverFile = File(coversDir, "cover_epub_${cleanName}_${System.currentTimeMillis()}.jpg")

        FileOutputStream(coverFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return coverFile.absolutePath
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&mdash;", "—")
            .replace("&ndash;", "–")
            .replace("&hellip;", "…")
    }
}
