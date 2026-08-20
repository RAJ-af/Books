package com.example.ui.discover

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReaderApplication
import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.remote.ArchiveDoc
import com.example.util.PdfImportResult
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
    data class Success(val books: List<ArchiveDoc>, val query: String) : DiscoverUiState()
    data class Empty(val query: String) : DiscoverUiState()
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
    val importResult: PdfImportResult,
    val coverUrl: String
)

class DiscoverViewModel(application: Application) : AndroidViewModel(application) {

    private val archiveRepository = (application as ReaderApplication).archiveOrgRepository
    private val bookRepository = (application as ReaderApplication).bookRepository

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
        // Initial suggested search for classic literature
        performSearch("Sherlock Holmes")
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
            performSearch(query)
        }
    }

    fun performSearch(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = DiscoverUiState.Loading
            val result = archiveRepository.searchBooks(query.trim())
            result.onSuccess { docs ->
                if (docs.isEmpty()) {
                    _uiState.value = DiscoverUiState.Empty(query.trim())
                } else {
                    _uiState.value = DiscoverUiState.Success(books = docs, query = query.trim())
                }
            }.onFailure { err ->
                val isNetwork = err is UnknownHostException || err is IOException
                val msg = if (isNetwork) {
                    "Unable to reach Internet Archive. Please check your network connection."
                } else {
                    err.localizedMessage ?: "Failed to search books. Please try again."
                }
                _uiState.value = DiscoverUiState.Error(message = msg, isNetworkError = isNetwork)
            }
        }
    }

    fun retrySearch() {
        val current = _searchQuery.value.ifBlank { "Classic Literature" }
        performSearch(current)
    }

    fun downloadAndImport(book: ArchiveDoc) {
        val identifier = book.identifier
        updateDownloadStatus(identifier, ItemDownloadStatus.Downloading(0.05f))

        viewModelScope.launch {
            val result = archiveRepository.downloadAndProcessPdf(
                identifier = identifier,
                title = book.title ?: "Public Domain Book",
                creator = book.creator ?: "Internet Archive",
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
                // Determine a nice default genre
                val guessedGenre = when {
                    (book.title ?: "").contains("Design", true) -> "Design"
                    (book.title ?: "").contains("Psychology", true) -> "Psychology"
                    (book.description ?: "").contains("Psychology", true) -> "Psychology"
                    else -> "Novels"
                }

                _pendingImport.value = PendingImportBook(
                    identifier = identifier,
                    initialTitle = book.title?.trim() ?: importResult.guessedTitle,
                    initialAuthor = book.creator?.trim() ?: "Public Domain Author",
                    initialGenre = guessedGenre,
                    year = book.year,
                    importResult = importResult,
                    coverUrl = book.coverThumbnailUrl
                )
            }.onFailure { error ->
                val errorMsg = error.localizedMessage ?: "Download failed"
                if (errorMsg.contains("Not available for direct download", ignoreCase = true)) {
                    updateDownloadStatus(identifier, ItemDownloadStatus.Unavailable("Not available for direct download (borrow-restricted or non-PDF)"))
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
            val pdfResult = pending.importResult
            val book = Book(
                title = title.ifBlank { pending.initialTitle },
                author = author.ifBlank { pending.initialAuthor },
                genre = genre.ifBlank { "Novels" },
                description = description.ifBlank { "Public Domain book from Internet Archive (${pending.year ?: "Public Domain"}). ${pdfResult.pageCount} pages." },
                rating = 4.8f,
                pageCount = pdfResult.pageCount.coerceAtLeast(1),
                coverImageUri = pdfResult.coverImagePath.ifBlank { pending.coverUrl },
                pdfFilePath = pdfResult.pdfPath,
                isImportedPdf = true,
                source = "internet_archive",
                colorHex = colorHex
            )

            // Generate one chapter per PDF page
            val chapters = (1..book.pageCount).map { pageNum ->
                Chapter(
                    bookId = 0L,
                    number = pageNum,
                    title = "Page $pageNum",
                    subtitle = if (pageNum == 1) "Cover & Title" else "Page $pageNum",
                    estimatedReadMinutes = 2,
                    content = ""
                )
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
