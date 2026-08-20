package com.example.ui.discover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReaderApplication
import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.source.BookSource
import com.example.data.source.DiscoverResult
import com.example.data.source.FileType
import com.example.data.source.UnifiedImportResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException

sealed class DiscoverUiState {
    object Idle : DiscoverUiState()
    object Loading : DiscoverUiState()
    data class Success(val books: List<DiscoverResult>, val query: String, val sourceId: String) : DiscoverUiState()
    data class Empty(val query: String, val sourceName: String) : DiscoverUiState()
    data class Error(val message: String, val isNetworkError: Boolean) : DiscoverUiState()
}

sealed class ItemDownloadStatus {
    object Idle : ItemDownloadStatus()
    data class Downloading(val progress: Float) : ItemDownloadStatus()
    object Processing : ItemDownloadStatus()
    data class Failed(val error: String) : ItemDownloadStatus()
    data class Unavailable(val reason: String) : ItemDownloadStatus()
}

data class PendingImportBook(
    val identifier: String,
    val initialTitle: String,
    val initialAuthor: String,
    val initialGenre: String,
    val year: String?,
    val importResult: UnifiedImportResult,
    val coverUrl: String?,
    val sourceId: String,
    val sourceDisplayName: String
)

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val bookSourceManager = (application as ReaderApplication).bookSourceManager
    private val bookRepository = (application as ReaderApplication).bookRepository

    val availableSources: List<BookSource> = bookSourceManager.sources

    private val _selectedSourceId = MutableStateFlow(availableSources.firstOrNull()?.id ?: "gutenberg")
    val selectedSourceId: StateFlow<String> = _selectedSourceId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Idle)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _downloadStatusMap = MutableStateFlow<Map<String, ItemDownloadStatus>>(emptyMap())
    val downloadStatusMap: StateFlow<Map<String, ItemDownloadStatus>> = _downloadStatusMap.asStateFlow()

    private val _pendingImport = MutableStateFlow<PendingImportBook?>(null)
    val pendingImport: StateFlow<PendingImportBook?> = _pendingImport.asStateFlow()

    val availableGenres = bookRepository.allGenres

    private var searchJob: Job? = null

    init {
        // Initial search on default source (Project Gutenberg)
        performSearch("Sherlock Holmes")
    }

    fun selectSource(sourceId: String) {
        if (_selectedSourceId.value == sourceId) return
        _selectedSourceId.value = sourceId
        val currentQuery = _searchQuery.value.ifBlank {
            when (sourceId) {
                "standard_ebooks" -> "Pride and Prejudice"
                "doab" -> "Science"
                "gutenberg" -> "Frankenstein"
                else -> "Sherlock Holmes"
            }
        }
        performSearch(currentQuery, sourceId)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = DiscoverUiState.Idle
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce typing
            performSearch(query, _selectedSourceId.value)
        }
    }

    fun performSearch(query: String, sourceId: String = _selectedSourceId.value) {
        if (query.isBlank()) return

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.value = DiscoverUiState.Loading
            val source = bookSourceManager.getSource(sourceId)
            val result = bookSourceManager.search(sourceId, query.trim())

            result.onSuccess { books ->
                if (books.isEmpty()) {
                    _uiState.value = DiscoverUiState.Empty(query.trim(), source.displayName)
                } else {
                    _uiState.value = DiscoverUiState.Success(
                        books = books,
                        query = query.trim(),
                        sourceId = sourceId
                    )
                }
            }.onFailure { err ->
                val isNetwork = err is UnknownHostException || err is IOException
                val msg = if (isNetwork) {
                    "Unable to reach ${source.displayName}. Please check your network connection."
                } else {
                    err.localizedMessage ?: "Failed to search books. Please try again."
                }
                _uiState.value = DiscoverUiState.Error(message = msg, isNetworkError = isNetwork)
            }
        }
    }

    fun retrySearch() {
        val current = _searchQuery.value.ifBlank {
            if (_selectedSourceId.value == "doab") "Science" else "Classic Literature"
        }
        performSearch(current, _selectedSourceId.value)
    }

    fun downloadAndImport(book: DiscoverResult) {
        val identifier = book.id
        updateDownloadStatus(identifier, ItemDownloadStatus.Downloading(0.05f))

        viewModelScope.launch {
            val result = bookSourceManager.downloadAndProcess(
                result = book,
                onProgress = { progress ->
                    if (progress >= 0.99f) {
                        updateDownloadStatus(identifier, ItemDownloadStatus.Processing)
                    } else {
                        updateDownloadStatus(identifier, ItemDownloadStatus.Downloading(progress))
                    }
                }
            )

            result.onSuccess { importResult ->
                updateDownloadStatus(identifier, ItemDownloadStatus.Idle)
                // Determine default genre
                val guessedGenre = when {
                    book.title.contains("Design", true) -> "Design"
                    book.title.contains("Psychology", true) -> "Psychology"
                    book.title.contains("Science", true) -> "Non-Fiction"
                    book.title.contains("Technology", true) -> "Design"
                    (book.description ?: "").contains("Psychology", true) -> "Psychology"
                    else -> "Novels"
                }

                _pendingImport.value = PendingImportBook(
                    identifier = identifier,
                    initialTitle = book.title.ifBlank { importResult.guessedTitle },
                    initialAuthor = book.author.ifBlank { "Classic Author" },
                    initialGenre = guessedGenre,
                    year = book.year,
                    importResult = importResult,
                    coverUrl = book.coverUrl,
                    sourceId = book.sourceId,
                    sourceDisplayName = book.sourceDisplayName
                )
            }.onFailure { error ->
                val errorMsg = error.localizedMessage ?: "Download failed"
                if (errorMsg.contains("Direct download is not available", ignoreCase = true) ||
                    errorMsg.contains("not available for direct download", ignoreCase = true)) {
                    updateDownloadStatus(identifier, ItemDownloadStatus.Unavailable("Direct file download is not available for this title"))
                } else {
                    updateDownloadStatus(identifier, ItemDownloadStatus.Failed(errorMsg))
                }
            }
        }
    }

    fun dismissPendingImport() {
        _pendingImport.value = null
    }

    fun confirmImportToLibrary(
        title: String,
        author: String,
        genre: String,
        description: String,
        colorHex: String,
        onSuccess: (Long) -> Unit
    ) {
        val pending = _pendingImport.value ?: return

        viewModelScope.launch {
            val unifiedRes = pending.importResult
            val isEpub = unifiedRes.fileType == FileType.EPUB

            val book = Book(
                title = title.ifBlank { pending.initialTitle },
                author = author.ifBlank { pending.initialAuthor },
                genre = genre.ifBlank { "Novels" },
                description = description.ifBlank {
                    if (isEpub) {
                        "Classic EPUB book from ${pending.sourceDisplayName}. ${unifiedRes.pageCount} estimated pages."
                    } else {
                        "PDF document from ${pending.sourceDisplayName} (${pending.year ?: "Public Domain"}). ${unifiedRes.pageCount} pages."
                    }
                },
                rating = 4.9f,
                pageCount = unifiedRes.pageCount.coerceAtLeast(1),
                coverImageUri = unifiedRes.coverImagePath.ifBlank { pending.coverUrl ?: "" },
                pdfFilePath = unifiedRes.filePath,
                isImportedPdf = !isEpub,
                fileType = if (isEpub) "EPUB" else "PDF",
                source = pending.sourceId,
                colorHex = colorHex
            )

            val chapters = if (isEpub && unifiedRes.epubChapters.isNotEmpty()) {
                unifiedRes.epubChapters.map { ep ->
                    Chapter(
                        bookId = 0L,
                        number = ep.number,
                        title = ep.title,
                        subtitle = ep.subtitle,
                        estimatedReadMinutes = ep.estimatedReadMinutes,
                        content = ep.content
                    )
                }
            } else if (isEpub) {
                listOf(
                    Chapter(
                        bookId = 0L,
                        number = 1,
                        title = "Chapter 1",
                        subtitle = "Full Edition",
                        estimatedReadMinutes = 10,
                        content = "Welcome to ${book.title} by ${book.author}."
                    )
                )
            } else {
                // PDF pages
                (1..book.pageCount).map { pageNum ->
                    Chapter(
                        bookId = 0L,
                        number = pageNum,
                        title = "Page $pageNum",
                        subtitle = if (pageNum == 1) "Cover & Title" else "Page $pageNum",
                        estimatedReadMinutes = 2,
                        content = ""
                    )
                }
            }

            val insertedId = bookRepository.insertBookWithChapters(book, chapters)
            val firstChapter = bookRepository.getFirstChapterForBook(insertedId)
            if (firstChapter != null) {
                bookRepository.updateReadingProgress(
                    bookId = insertedId,
                    chapterId = firstChapter.id,
                    percentComplete = 0f
                )
            }

            _pendingImport.value = null
            onSuccess(insertedId)
        }
    }

    private fun updateDownloadStatus(identifier: String, status: ItemDownloadStatus) {
        val current = _downloadStatusMap.value.toMutableMap()
        current[identifier] = status
        _downloadStatusMap.value = current
    }
}
