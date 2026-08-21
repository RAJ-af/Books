package com.example.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ReaderApplication
import com.example.data.local.entity.Book
import com.example.data.local.entity.Chapter
import com.example.data.local.entity.ReadingProgress
import com.example.data.repository.BookWithDetails
import com.example.util.PdfHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategorySection(
    val genre: String,
    val count: Int,
    val books: List<BookWithDetails>,
    val trayTintHex: String = "#E89A5A"
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ReaderApplication).bookRepository

    init {
        viewModelScope.launch {
            repository.checkAndVerifyPdfBooks(getApplication())
        }
    }

    val allBooks = repository.allBooks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allProgress = repository.allProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableGenres: StateFlow<List<String>> = repository.allGenres.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Design", "Psychology", "Novels")
    )

    val categorySections: StateFlow<List<CategorySection>> = combine(
        repository.allBooks,
        repository.allProgress
    ) { books, progressList ->
        val progressMap = progressList.associateBy { it.bookId }
        val booksWithProgress = books.map { book ->
            BookWithDetails(book = book, progress = progressMap[book.id])
        }

        val predefinedGenres = listOf("Design", "Psychology", "Novels")
        val grouped = booksWithProgress.groupBy { it.book.genre }

        // Ordered sections with pre-defined categories first
        val result = mutableListOf<CategorySection>()
        predefinedGenres.forEach { genre ->
            val list = grouped[genre] ?: emptyList()
            if (list.isNotEmpty()) {
                val tint = when (genre) {
                    "Design" -> "#E89A5A"
                    "Psychology" -> "#6C96C8"
                    "Novels" -> "#6DA77E"
                    else -> "#C88A35"
                }
                result.add(CategorySection(genre = genre, count = list.size, books = list, trayTintHex = tint))
            }
        }

        // Add any other dynamic categories added by user
        grouped.forEach { (genre, list) ->
            if (!predefinedGenres.contains(genre) && list.isNotEmpty()) {
                result.add(CategorySection(genre = genre, count = list.size, books = list, trayTintHex = "#C88A35"))
            }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalBookCount: StateFlow<Int> = repository.allBooks.combine(repository.allBooks) { books, _ ->
        books.size
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<BookWithDetails>> = _searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                combine(
                    repository.searchBooks(query),
                    repository.allProgress
                ) { books, progressList ->
                    val progressMap = progressList.associateBy { it.bookId }
                    books.map { book ->
                        BookWithDetails(book = book, progress = progressMap[book.id])
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addNewBook(
        title: String,
        author: String,
        genre: String,
        description: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val book = Book(
                title = title,
                author = author,
                genre = genre,
                description = description.ifBlank { "A curated addition to your personal library." },
                rating = 4.8f,
                pageCount = 180,
                colorHex = colorHex
            )
            val sampleChapters = listOf(
                Chapter(
                    bookId = 0L,
                    number = 1,
                    title = "Introduction & Beginnings",
                    subtitle = "Foundations of Thought",
                    estimatedReadMinutes = 8,
                    content = """
                        Welcome to '$title'. Every great journey of exploration begins with a single page turned, an unspoken curiosity, and a willingness to see the world from a fresh vantage point.

                        As $author outlines in this introductory opening, true understanding is not the accumulation of rigid facts, but the cultivation of active perception. When we engage deeply with written ideas, we participate in an intimate conversation across time and distance.
                    """.trimIndent()
                ),
                Chapter(
                    bookId = 0L,
                    number = 2,
                    title = "The Core Architecture",
                    subtitle = "Principles and Frameworks",
                    estimatedReadMinutes = 12,
                    content = """
                        In this second chapter, the core framework comes into sharp focus. Rather than reacting to surface noise, we examine the underlying systems and rhythms that govern this domain.

                        Pay close attention to how subtle patterns repeat. Whether in design, psychology, or narrative storytelling, mastery is revealed in the quiet details that most observers overlook.
                    """.trimIndent()
                ),
                Chapter(
                    bookId = 0L,
                    number = 3,
                    title = "Synthesis and Application",
                    subtitle = "Translating Insight into Mastery",
                    estimatedReadMinutes = 10,
                    content = """
                        The ultimate measure of any idea is not how elegantly it is discussed, but how reliably it can be applied to real life.

                        As we conclude this volume, reflect on the core signposts shared throughout. Take what resonates, test it against your own lived experience, and make it your own.
                    """.trimIndent()
                )
            )
            repository.insertBookWithChapters(book, sampleChapters)
        }
    }

    fun importPdfBook(
        title: String,
        author: String,
        genre: String,
        description: String,
        pdfPath: String,
        coverPath: String,
        pageCount: Int,
        colorHex: String = "#285698",
        isScanned: Boolean = false,
        pageTexts: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val book = Book(
                title = title.ifBlank { "Imported Document" },
                author = author.ifBlank { "Unknown Author" },
                genre = genre.ifBlank { "Imported" },
                description = description.ifBlank { "Imported PDF document ($pageCount pages)." },
                rating = 4.7f,
                pageCount = pageCount.coerceAtLeast(1),
                coverImageUri = coverPath,
                pdfFilePath = pdfPath,
                isImportedPdf = true,
                isScanned = isScanned,
                source = "local",
                colorHex = colorHex
            )

            // Generate grouped chapters/sections instead of raw pages
            val parsedChapters = PdfHelper.parsePdfToChapters(pageTexts, pageCount)
            val chapters = parsedChapters.map { parsed ->
                Chapter(
                    bookId = 0L,
                    number = parsed.number,
                    title = parsed.title,
                    subtitle = parsed.subtitle,
                    estimatedReadMinutes = (parsed.content.length / 1000).coerceAtLeast(2),
                    content = parsed.content,
                    pdfPageStart = parsed.startPage
                )
            }

            val insertedId = repository.insertBookWithChapters(book, chapters)
            val firstChapter = repository.getFirstChapterForBook(insertedId)
            if (firstChapter != null) {
                repository.updateReadingProgress(
                    bookId = insertedId,
                    chapterId = firstChapter.id,
                    percentComplete = 0f
                )
            }

            // If scanned (image-only / low text per page), start WorkManager background OCR job
            if (isScanned) {
                com.example.util.OcrManager.enqueueOcrJob(getApplication(), insertedId, pdfPath)
            }
        }
    }
}
