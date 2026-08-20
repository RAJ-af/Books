package com.example.data.source

enum class FileType {
    PDF,
    EPUB
}

data class DiscoverResult(
    val id: String,
    val title: String,
    val author: String,
    val year: String? = null,
    val coverUrl: String? = null,
    val sourceId: String,
    val sourceDisplayName: String,
    val description: String? = null,
    val directDownloadUrl: String? = null
)

data class DownloadInfo(
    val url: String,
    val fileType: FileType = FileType.PDF
)

interface BookSource {
    val id: String              // e.g. "internet_archive", "doab"
    val displayName: String     // shown in source chip
    val description: String     // brief subtitle/description
    
    suspend fun search(query: String): List<DiscoverResult>
    suspend fun resolveDownload(result: DiscoverResult): DownloadInfo?  // null if file is not directly downloadable
}
