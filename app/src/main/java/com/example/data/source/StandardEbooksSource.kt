package com.example.data.source

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URLEncoder

class StandardEbooksSource(
    private val okHttpClient: OkHttpClient = GutenbergSource.createDefaultOkHttpClient()
) : BookSource {

    override val id: String = "standard_ebooks"
    override val displayName: String = "Standard Ebooks"
    override val description: String = "Free, carefully formatted public domain ebooks"

    override suspend fun search(query: String): List<DiscoverResult> = withContext(Dispatchers.IO) {
        try {
            val q = query.trim()
            val url = if (q.isBlank() || q.equals("all", ignoreCase = true)) {
                "https://standardebooks.org/feeds/opds/all"
            } else {
                "https://standardebooks.org/feeds/opds/all?query=${URLEncoder.encode(q, "UTF-8")}"
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; ReaderApp/1.0)")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext emptyList()
            }

            val xmlBody = response.body?.string() ?: return@withContext emptyList()
            parseOpdsXml(xmlBody)
        } catch (e: Exception) {
            Log.e("StandardEbooksSource", "Failed to search Standard Ebooks feed", e)
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

    private fun parseOpdsXml(xmlString: String): List<DiscoverResult> {
        val results = mutableListOf<DiscoverResult>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlString))

            var eventType = parser.eventType
            var inEntry = false
            var inAuthor = false

            var currentTitle = ""
            var currentAuthor = ""
            var currentSummary = ""
            var currentContent = ""
            var currentCoverUrl = ""
            var currentEpubUrl = ""
            var currentBookId = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name ?: ""

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (name.equals("entry", ignoreCase = true)) {
                            inEntry = true
                            currentTitle = ""
                            currentAuthor = ""
                            currentSummary = ""
                            currentContent = ""
                            currentCoverUrl = ""
                            currentEpubUrl = ""
                            currentBookId = ""
                        } else if (inEntry) {
                            if (name.equals("author", ignoreCase = true)) {
                                inAuthor = true
                            } else if (name.equals("name", ignoreCase = true) && inAuthor) {
                                currentAuthor = parser.nextText().trim()
                            } else if (name.equals("title", ignoreCase = true) && !inAuthor) {
                                currentTitle = parser.nextText().trim()
                            } else if (name.equals("id", ignoreCase = true)) {
                                currentBookId = parser.nextText().trim()
                            } else if (name.equals("summary", ignoreCase = true)) {
                                currentSummary = parser.nextText().trim()
                            } else if (name.equals("content", ignoreCase = true)) {
                                val text = parser.nextText().trim()
                                currentContent = text.replace(Regex("<[^>]*>"), "").trim()
                            } else if (name.equals("link", ignoreCase = true)) {
                                val rel = parser.getAttributeValue(null, "rel") ?: ""
                                val type = parser.getAttributeValue(null, "type") ?: ""
                                val href = parser.getAttributeValue(null, "href") ?: ""
                                val titleAttr = parser.getAttributeValue(null, "title") ?: ""

                                // Cover Image URL
                                if ((rel.contains("image", ignoreCase = true) || type.contains("image", ignoreCase = true)) && currentCoverUrl.isBlank()) {
                                    currentCoverUrl = href
                                }

                                // EPUB Download URL
                                if (type.contains("epub", ignoreCase = true) && !href.contains(".kepub.", ignoreCase = true)) {
                                    if (currentEpubUrl.isBlank() || titleAttr.contains("Recommended", ignoreCase = true)) {
                                        currentEpubUrl = href
                                    }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        if (name.equals("author", ignoreCase = true)) {
                            inAuthor = false
                        } else if (name.equals("entry", ignoreCase = true)) {
                            inEntry = false

                            if (currentEpubUrl.isNotBlank()) {
                                val finalTitle = currentTitle.ifBlank { "Standard Ebook" }
                                val finalAuthor = currentAuthor.ifBlank { "Public Domain" }
                                val description = currentSummary.ifBlank { currentContent }
                                    .ifBlank { "Carefully produced, high quality public domain ebook from Standard Ebooks." }
                                val itemUniqueId = if (currentBookId.isNotBlank()) {
                                    currentBookId.replace("[^a-zA-Z0-9]".toRegex(), "").takeLast(30)
                                } else {
                                    currentEpubUrl.hashCode().toString()
                                }

                                results.add(
                                    DiscoverResult(
                                        id = itemUniqueId,
                                        title = finalTitle,
                                        author = finalAuthor,
                                        year = null,
                                        coverUrl = currentCoverUrl.ifBlank { null },
                                        sourceId = this@StandardEbooksSource.id,
                                        sourceDisplayName = displayName,
                                        description = description,
                                        directDownloadUrl = currentEpubUrl
                                    )
                                )
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("StandardEbooksSource", "XML parsing error", e)
        }
        return results
    }
}
